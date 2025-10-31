package com.translateai.business.admin.contractMng.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.translateai.business.admin.contractMng.service.ContractMngService;
import com.translateai.common.ApiStatus;
import com.translateai.common.PageableObject;
import com.translateai.config.exception.RestApiException;
import com.translateai.constant.enumconstant.CarStatus;
import com.translateai.constant.enumconstant.ContractStatus;
import com.translateai.dto.business.admin.contractMng.*;
import com.translateai.entity.domain.CarEntity;
import com.translateai.entity.domain.ContractEntity;
import com.translateai.entity.domain.CustomerEntity;
import com.translateai.entity.domain.SurchargeEntity;
import com.translateai.repository.business.admin.CarRepository;
import com.translateai.repository.business.admin.ContractRepository;
import com.translateai.repository.business.admin.CustomerRepository;
import com.translateai.repository.business.admin.SurchargeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class ContractMngServiceImpl implements ContractMngService {

    private final ContractRepository contractRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final SurchargeRepository surchargeRepository;
    private final ModelMapper modelMapper;

    @Override
    public PageableObject<ContractDTO> searchContracts(ContractSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
        Page<ContractDTO> contractPage = contractRepository.searchContracts(pageable, searchDTO);
        
        // Map status description và load surcharges
        contractPage.forEach(contract -> {
            if (contract.getStatus() != null) {
                contract.setStatusNm(contract.getStatus().getDescription());
            }
            // Load surcharges cho mỗi contract
            List<SurchargeDTO> surcharges = surchargeRepository.findByContractId(contract.getId());
            contract.setSurcharges(surcharges);
        });
        
        return new PageableObject<>(contractPage);
    }

    @Override
    public ContractDTO getContractDetail(String id) {
        Optional<ContractEntity> contractEntity = contractRepository.findById(id);
        if (!contractEntity.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        ContractEntity contract = contractEntity.get();
        ContractDTO contractDTO = new ContractDTO();
        
        // Lấy thông tin xe
        Optional<CarEntity> carEntity = carRepository.findById(contract.getCarId());
        if (!carEntity.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        // Lấy thông tin khách hàng
        Optional<CustomerEntity> customerEntity = customerRepository.findById(contract.getCustomerId());
        if (!customerEntity.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        contractDTO.setId(contract.getId());
        contractDTO.setCarId(contract.getCarId());
        contractDTO.setCarName(carEntity.get().getModel());
        contractDTO.setLicensePlate(carEntity.get().getLicensePlate());
        contractDTO.setCustomerId(contract.getCustomerId());
        contractDTO.setCustomerName(customerEntity.get().getFullName());
        contractDTO.setPhoneNumber(customerEntity.get().getPhoneNumber());
        contractDTO.setStartDate(contract.getStartDate());
        contractDTO.setEndDate(contract.getEndDate());
        contractDTO.setRentalDays(contract.getRentalDays());
        contractDTO.setDailyPrice(contract.getDailyPrice());
        contractDTO.setTotalAmount(contract.getTotalAmount());
        contractDTO.setSurchargeAmount(contract.getSurchargeAmount());
        contractDTO.setFinalAmount(contract.getFinalAmount());
        contractDTO.setStatus(contract.getStatus());
        contractDTO.setStatusNm(contract.getStatus().getDescription());
        contractDTO.setNotes(contract.getNotes());
        contractDTO.setActualEndDate(contract.getActualEndDate());
        
        // Load surcharges
        List<SurchargeDTO> surcharges = surchargeRepository.findByContractId(id);
        contractDTO.setSurcharges(surcharges);
        
        return contractDTO;
    }

    @Override
    @Transactional
    public Boolean saveContract(@Valid ContractSaveDTO saveDTO) {
        ContractEntity contractEntity;
        boolean isNew = StringUtils.isBlank(saveDTO.getId());

        if (isNew) {
            contractEntity = new ContractEntity();
            
            // Kiểm tra xe có tồn tại không
            Optional<CarEntity> carEntity = carRepository.findById(saveDTO.getCarId());
            if (!carEntity.isPresent()) {
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }
            
            // Kiểm tra khách hàng có tồn tại không
            Optional<CustomerEntity> customerEntity = customerRepository.findById(saveDTO.getCustomerId());
            if (!customerEntity.isPresent()) {
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }
            
            // Kiểm tra xe có đang được thuê không
            List<ContractEntity> activeContracts = contractRepository.findActiveContractsByCarId(
                    saveDTO.getCarId(), 
                    ContractStatus.RENTING, 
                    saveDTO.getStartDate()
            );
            if (!activeContracts.isEmpty()) {
                throw new RestApiException(ApiStatus.BAD_REQUEST);
            }
            
            contractEntity.setCarId(carEntity.get().getId());
            contractEntity.setCustomerId(customerEntity.get().getId());
            contractEntity.setStatus(ContractStatus.NEW);
            
            // Tính toán số ngày thuê
            long daysDiff = (saveDTO.getEndDate() - saveDTO.getStartDate()) / (1000 * 60 * 60 * 24);
            int rentalDays = (int) daysDiff + 1; // +1 để tính cả ngày bắt đầu
            
            contractEntity.setRentalDays(rentalDays);
            contractEntity.setDailyPrice(carEntity.get().getDailyPrice());
            
            // Tính tổng tiền: số ngày * giá thuê theo ngày
            BigDecimal totalAmount = carEntity.get().getDailyPrice().multiply(new BigDecimal(rentalDays));
            contractEntity.setTotalAmount(totalAmount);
            contractEntity.setSurchargeAmount(BigDecimal.ZERO);
            contractEntity.setFinalAmount(totalAmount);
            
        } else {
            Optional<ContractEntity> contractEntityFind = contractRepository.findById(saveDTO.getId());
            if (!contractEntityFind.isPresent()) {
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }
            contractEntity = contractEntityFind.get();
            
            // Không cho phép sửa hợp đồng đã ký kết (trạng thái khác NEW)
            if (!contractEntity.getStatus().equals(ContractStatus.NEW)) {
                throw new RestApiException(ApiStatus.BAD_REQUEST);
            }
        }

        contractEntity.setStartDate(saveDTO.getStartDate());
        contractEntity.setEndDate(saveDTO.getEndDate());
        contractEntity.setNotes(saveDTO.getNotes());

        contractRepository.save(contractEntity);
        return true;
    }

    @Override
    @Transactional
    public Boolean updateContractStatus(@Valid ContractUpdateStatusDTO updateStatusDTO) {
        Optional<ContractEntity> contractEntityFind = contractRepository.findById(updateStatusDTO.getId());
        if (!contractEntityFind.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        ContractEntity contractEntity = contractEntityFind.get();
        
        // Lấy thông tin xe
        Optional<CarEntity> carEntityOpt = carRepository.findById(contractEntity.getCarId());
        if (!carEntityOpt.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        CarEntity carEntity = carEntityOpt.get();
        
        ContractStatus oldStatus = contractEntity.getStatus();
        ContractStatus newStatus = updateStatusDTO.getStatus();
        
        // Giao xe: NEW -> RENTING
        if (oldStatus.equals(ContractStatus.NEW) && newStatus.equals(ContractStatus.RENTING)) {
            contractEntity.setStatus(ContractStatus.RENTING);
            carEntity.setStatus(CarStatus.RENTED);
        }
        // Nhận xe / Trả xe: RENTING -> COMPLETED
        else if (oldStatus.equals(ContractStatus.RENTING) && newStatus.equals(ContractStatus.COMPLETED)) {
            contractEntity.setStatus(ContractStatus.COMPLETED);
            contractEntity.setActualEndDate(updateStatusDTO.getActualEndDate());
            carEntity.setStatus(CarStatus.AVAILABLE);
        }
        // Hủy hợp đồng: NEW -> CANCELLED
        else if (oldStatus.equals(ContractStatus.NEW) && newStatus.equals(ContractStatus.CANCELLED)) {
            contractEntity.setStatus(ContractStatus.CANCELLED);
        }
        else {
            throw new RestApiException(ApiStatus.BAD_REQUEST);
        }
        
        if (StringUtils.isNotBlank(updateStatusDTO.getNotes())) {
            contractEntity.setNotes(updateStatusDTO.getNotes());
        }
        
        contractRepository.save(contractEntity);
        carRepository.save(carEntity);
        
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteContract(String id) {
        Optional<ContractEntity> contractEntity = contractRepository.findById(id);
        if (!contractEntity.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        // Chỉ cho phép xóa hợp đồng mới tạo hoặc đã hủy
        if (!contractEntity.get().getStatus().equals(ContractStatus.NEW) && 
            !contractEntity.get().getStatus().equals(ContractStatus.CANCELLED)) {
            throw new RestApiException(ApiStatus.BAD_REQUEST);
        }
        
        // Xóa các phụ phí liên quan
        surchargeRepository.deleteByContractId(id);
        
        contractRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public byte[] downloadContractPDF(String id) {
        Optional<ContractEntity> contractEntityFind = contractRepository.findById(id);
        if (!contractEntityFind.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        ContractEntity contract = contractEntityFind.get();
        
        try {
            // Tạo PDF trong memory (ByteArrayOutputStream)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Tiêu đề
            Paragraph title = new Paragraph("HỢP ĐỒNG THUÊ XE")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            
            document.add(new Paragraph("\n"));
            
            // Thông tin hợp đồng
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            
            document.add(new Paragraph("Mã hợp đồng: " + contract.getId()));
            document.add(new Paragraph("Ngày tạo: " + sdf.format(new Date(contract.getCreatedDate()))));
            document.add(new Paragraph("\n"));
            
            // Lấy thông tin khách hàng
            Optional<CustomerEntity> customerEntity = customerRepository.findById(contract.getCustomerId());
            if (!customerEntity.isPresent()) {
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }
            CustomerEntity customer = customerEntity.get();
            
            // Lấy thông tin xe
            Optional<CarEntity> carEntity = carRepository.findById(contract.getCarId());
            if (!carEntity.isPresent()) {
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }
            CarEntity car = carEntity.get();
            
            // Thông tin khách hàng
            document.add(new Paragraph("THÔNG TIN KHÁCH HÀNG").setBold());
            document.add(new Paragraph("Họ tên: " + customer.getFullName()));
            document.add(new Paragraph("Số điện thoại: " + customer.getPhoneNumber()));
            if (StringUtils.isNotBlank(customer.getEmail())) {
                document.add(new Paragraph("Email: " + customer.getEmail()));
            }
            if (StringUtils.isNotBlank(customer.getCitizenId())) {
                document.add(new Paragraph("CCCD/CMND: " + customer.getCitizenId()));
            }
            if (StringUtils.isNotBlank(customer.getAddress())) {
                document.add(new Paragraph("Địa chỉ: " + customer.getAddress()));
            }
            document.add(new Paragraph("\n"));
            
            // Thông tin xe
            document.add(new Paragraph("THÔNG TIN XE THUÊ").setBold());
            document.add(new Paragraph("Mẫu xe: " + car.getModel()));
            document.add(new Paragraph("Biển số: " + car.getLicensePlate()));
            if (StringUtils.isNotBlank(car.getCarType())) {
                document.add(new Paragraph("Loại xe: " + car.getCarType()));
            }
            document.add(new Paragraph("\n"));
            
            // Thời gian thuê
            document.add(new Paragraph("THỜI GIAN THUÊ").setBold());
            document.add(new Paragraph("Ngày thuê: " + sdf.format(new Date(contract.getStartDate()))));
            document.add(new Paragraph("Ngày trả: " + sdf.format(new Date(contract.getEndDate()))));
            document.add(new Paragraph("Số ngày thuê: " + contract.getRentalDays() + " ngày"));
            document.add(new Paragraph("\n"));
            
            // Bảng chi phí
            document.add(new Paragraph("CHI TIẾT CHI PHÍ").setBold());
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2}));
            table.setWidth(UnitValue.createPercentValue(100));
            
            table.addCell("Giá thuê theo ngày:");
            table.addCell(currencyFormat.format(contract.getDailyPrice()));
            
            table.addCell("Số ngày thuê:");
            table.addCell(contract.getRentalDays() + " ngày");
            
            table.addCell("Tổng tiền thuê:");
            table.addCell(currencyFormat.format(contract.getTotalAmount()));
            
            // Thêm phụ phí nếu có
            List<SurchargeDTO> surcharges = surchargeRepository.findByContractId(id);
            if (!surcharges.isEmpty()) {
                for (SurchargeDTO surcharge : surcharges) {
                    table.addCell("Phụ phí: " + surcharge.getDescription());
                    table.addCell(currencyFormat.format(surcharge.getAmount()));
                }
                table.addCell("Tổng phụ phí:");
                table.addCell(currencyFormat.format(contract.getSurchargeAmount()));
            }
            
            table.addCell("TỔNG THANH TOÁN:");
            table.addCell(currencyFormat.format(contract.getFinalAmount()));
            
            document.add(table);
            document.add(new Paragraph("\n"));
            
            // Ghi chú
            if (StringUtils.isNotBlank(contract.getNotes())) {
                document.add(new Paragraph("GHI CHÚ").setBold());
                document.add(new Paragraph(contract.getNotes()));
                document.add(new Paragraph("\n"));
            }
            
            // Chữ ký
            document.add(new Paragraph("\n\n"));
            Table signTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            signTable.setWidth(UnitValue.createPercentValue(100));
            
            signTable.addCell(new Paragraph("BÊN CHO THUÊ\n(Ký và ghi rõ họ tên)")
                    .setTextAlignment(TextAlignment.CENTER));
            signTable.addCell(new Paragraph("BÊN THUÊ\n(Ký và ghi rõ họ tên)")
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.add(signTable);
            
            document.close();
            
            // Trả về byte array
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public Boolean addSurcharge(@Valid SurchargeSaveDTO saveDTO) {
        // Kiểm tra hợp đồng có tồn tại không
        Optional<ContractEntity> contractEntityFind = contractRepository.findById(saveDTO.getContractId());
        if (!contractEntityFind.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        ContractEntity contractEntity = contractEntityFind.get();
        
        SurchargeEntity surchargeEntity;
        boolean isNew = StringUtils.isBlank(saveDTO.getId());
        
        if (isNew) {
            surchargeEntity = new SurchargeEntity();
            surchargeEntity.setContractId(contractEntity.getId());
        } else {
            Optional<SurchargeEntity> surchargeEntityFind = surchargeRepository.findById(saveDTO.getId());
            if (!surchargeEntityFind.isPresent()) {
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }
            surchargeEntity = surchargeEntityFind.get();
        }
        
        surchargeEntity.setDescription(saveDTO.getDescription());
        surchargeEntity.setAmount(saveDTO.getAmount());
        surchargeEntity.setNotes(saveDTO.getNotes());
        
        surchargeRepository.save(surchargeEntity);
        
        // Cập nhật tổng phụ phí và tổng tiền cuối cùng
        List<SurchargeDTO> allSurcharges = surchargeRepository.findByContractId(saveDTO.getContractId());
        BigDecimal totalSurcharge = allSurcharges.stream()
                .map(SurchargeDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        contractEntity.setSurchargeAmount(totalSurcharge);
        contractEntity.setFinalAmount(contractEntity.getTotalAmount().add(totalSurcharge));
        
        contractRepository.save(contractEntity);
        
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteSurcharge(String id) {
        Optional<SurchargeEntity> surchargeEntity = surchargeRepository.findById(id);
        if (!surchargeEntity.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        String contractId = surchargeEntity.get().getContractId();
        
        surchargeRepository.deleteById(id);
        
        // Cập nhật lại tổng phụ phí
        Optional<ContractEntity> contractEntityFind = contractRepository.findById(contractId);
        if (contractEntityFind.isPresent()) {
            ContractEntity contractEntity = contractEntityFind.get();
            
            List<SurchargeDTO> remainingSurcharges = surchargeRepository.findByContractId(contractId);
            BigDecimal totalSurcharge = remainingSurcharges.stream()
                    .map(SurchargeDTO::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            contractEntity.setSurchargeAmount(totalSurcharge);
            contractEntity.setFinalAmount(contractEntity.getTotalAmount().add(totalSurcharge));
            
            contractRepository.save(contractEntity);
        }
        
        return true;
    }

    @Override
    public List<SurchargeDTO> getSurchargesByContractId(String contractId) {
        return surchargeRepository.findByContractId(contractId);
    }
}

