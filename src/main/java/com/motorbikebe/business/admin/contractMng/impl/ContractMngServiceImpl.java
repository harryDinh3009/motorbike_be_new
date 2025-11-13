package com.motorbikebe.business.admin.contractMng.impl;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.motorbikebe.business.admin.contractMng.service.ContractMngService;
import com.motorbikebe.common.ApiStatus;
import com.motorbikebe.common.PageableObject;
import com.motorbikebe.config.cloudinary.CloudinaryUploadImages;
import com.motorbikebe.config.exception.RestApiException;
import com.motorbikebe.constant.enumconstant.ContractStatus;
import com.motorbikebe.dto.business.admin.contractMng.*;
import com.motorbikebe.entity.domain.*;
import com.motorbikebe.repository.business.admin.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Implementation quản lý hợp đồng (đã nâng cấp hoàn toàn)
 */
@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class ContractMngServiceImpl implements ContractMngService {

    private final ContractRepository contractRepository;
    private final ContractCarRepository contractCarRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ContractImageRepository contractImageRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final SurchargeRepository surchargeRepository;
    private final CloudinaryUploadImages cloudinaryUploadImages;
    private final ModelMapper modelMapper;

    @Override
    public PageableObject<ContractDTO> searchContracts(ContractSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(searchDTO.getPage() - 1, searchDTO.getSize());
        Page<ContractDTO> contractPage = contractRepository.searchContracts(pageable, searchDTO);

        // Set status description and load cars for each contract
        contractPage.forEach(contract -> {
            if (contract.getStatus() != null) {
                contract.setStatusNm(contract.getStatus().getDescription());
            }
            
            // Load cars information for each contract
            List<ContractCarDTO> cars = getContractCars(contract.getId());
            contract.setCars(cars);
        });

        return PageableObject.<ContractDTO>builder()
                .data(contractPage.getContent())
                .totalRecords(contractPage.getTotalElements())
                .currentPage(searchDTO.getPage())
                .totalPages(contractPage.getTotalPages())
                .build();
    }

    @Override
    public ContractDTO getContractDetail(String id) {
        Optional<ContractEntity> contractOpt = contractRepository.findById(id);
        if (!contractOpt.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }

        ContractEntity contract = contractOpt.get();
        ContractDTO contractDTO = modelMapper.map(contract, ContractDTO.class);

        // Set status name
        if (contractDTO.getStatus() != null) {
            contractDTO.setStatusNm(contractDTO.getStatus().getDescription());
        }

        // Load customer info
        Optional<CustomerEntity> customer = customerRepository.findById(contract.getCustomerId());
        if (customer.isPresent()) {
            contractDTO.setCustomerName(customer.get().getFullName());
            contractDTO.setPhoneNumber(customer.get().getPhoneNumber());
            contractDTO.setEmail(customer.get().getEmail());
            contractDTO.setCountry(customer.get().getCountry());
            contractDTO.setCitizenId(customer.get().getCitizenId());
            contractDTO.setCreatedDate(new Date(contractOpt.get().getCreatedDate()));

            // Count total contracts
            int totalContracts = contractRepository.findByCustomerId(contract.getCustomerId()).size();
            contractDTO.setTotalContracts(totalContracts);
        }

        // Load branch info
        if (StringUtils.isNotBlank(contract.getPickupBranchId())) {
            branchRepository.findById(contract.getPickupBranchId())
                    .ifPresent(b -> contractDTO.setPickupBranchName(b.getName()));
        }
        if (StringUtils.isNotBlank(contract.getReturnBranchId())) {
            branchRepository.findById(contract.getReturnBranchId())
                    .ifPresent(b -> contractDTO.setReturnBranchName(b.getName()));
        }

        // Load user info
        if (StringUtils.isNotBlank(contract.getDeliveryUserId())) {
            userRepository.findById(contract.getDeliveryUserId())
                    .ifPresent(u -> contractDTO.setDeliveryUserName(u.getFullName()));
        }
        if (StringUtils.isNotBlank(contract.getReturnUserId())) {
            userRepository.findById(contract.getReturnUserId())
                    .ifPresent(u -> contractDTO.setReturnUserName(u.getFullName()));
        }

        // Load cars
        contractDTO.setCars(getContractCars(id));

        // Load surcharges
        contractDTO.setSurcharges(getSurchargesByContractId(id));

        // Load payments
        contractDTO.setPayments(getPaymentHistory(id));

        // Load images
        List<ContractImageEntity> deliveryImages = contractImageRepository.findByContractIdAndImageType(id, "DELIVERY");
        contractDTO.setDeliveryImages(deliveryImages.stream()
                .map(img -> modelMapper.map(img, ContractImageDTO.class))
                .collect(Collectors.toList()));

        List<ContractImageEntity> returnImages = contractImageRepository.findByContractIdAndImageType(id, "RETURN");
        contractDTO.setReturnImages(returnImages.stream()
                .map(img -> modelMapper.map(img, ContractImageDTO.class))
                .collect(Collectors.toList()));

        return contractDTO;
    }

    @Override
    @Transactional
    public Boolean saveContract(@Valid ContractSaveDTO saveDTO) {
        ContractEntity contractEntity;
        boolean isNew = StringUtils.isBlank(saveDTO.getId());

        if (isNew) {
            contractEntity = new ContractEntity();
            
            // Generate contract code
            String contractCode = contractRepository.generateContractCode();
            contractEntity.setContractCode(contractCode != null ? contractCode : "HD000001");
            
            contractEntity.setStatus(saveDTO.getStatus() != null ? saveDTO.getStatus() : ContractStatus.CONFIRMED);
        } else {
            Optional<ContractEntity> existingContract = contractRepository.findById(saveDTO.getId());
            if (!existingContract.isPresent()) {
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }
            contractEntity = existingContract.get();
        }

        // Validate customer exists
        Optional<CustomerEntity> customer = customerRepository.findById(saveDTO.getCustomerId());
        if (!customer.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }

        // Validate cars availability (nếu status CONFIRMED trở lên)
        if (saveDTO.getStatus() != null && 
            (saveDTO.getStatus() == ContractStatus.CONFIRMED || saveDTO.getStatus() == ContractStatus.DELIVERED)) {
            for (ContractCarSaveDTO carDTO : saveDTO.getCars()) {
                List<ContractEntity> overlappingContracts = contractRepository.findOverlappingContracts(
                        carDTO.getCarId(), saveDTO.getStartDate(), saveDTO.getEndDate());
                
                // Loại trừ contract hiện tại nếu đang update
                if (!isNew) {
                    overlappingContracts = overlappingContracts.stream()
                            .filter(c -> !c.getId().equals(saveDTO.getId()))
                            .collect(Collectors.toList());
                }
                
                if (!overlappingContracts.isEmpty()) {
                    throw new RestApiException(ApiStatus.BAD_REQUEST);
                }
            }
        }

        // Set basic info
        contractEntity.setCustomerId(saveDTO.getCustomerId());
        contractEntity.setSource(saveDTO.getSource());
        contractEntity.setStartDate(saveDTO.getStartDate());
        contractEntity.setEndDate(saveDTO.getEndDate());
        contractEntity.setPickupBranchId(saveDTO.getPickupBranchId());
        contractEntity.setReturnBranchId(saveDTO.getReturnBranchId());
        contractEntity.setPickupAddress(saveDTO.getPickupAddress());
        contractEntity.setReturnAddress(saveDTO.getReturnAddress());
        contractEntity.setNeedPickupDelivery(saveDTO.getNeedPickupDelivery());
        contractEntity.setNeedReturnDelivery(saveDTO.getNeedReturnDelivery());
        contractEntity.setNotes(saveDTO.getNotes());

        // Calculate financial info
        BigDecimal totalRentalAmount = saveDTO.getCars().stream()
                .map(ContractCarSaveDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSurcharge = saveDTO.getSurcharges() != null ? 
                saveDTO.getSurcharges().stream()
                        .map(SurchargeSaveDTO::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;

        BigDecimal discountAmount = calculateDiscountAmount(
                totalRentalAmount, 
                saveDTO.getDiscountType(), 
                saveDTO.getDiscountValue());

        BigDecimal finalAmount = totalRentalAmount.add(totalSurcharge).subtract(discountAmount);

        contractEntity.setTotalRentalAmount(totalRentalAmount);
        contractEntity.setTotalSurcharge(totalSurcharge);
        contractEntity.setDiscountType(saveDTO.getDiscountType());
        contractEntity.setDiscountValue(saveDTO.getDiscountValue());
        contractEntity.setDiscountAmount(discountAmount);
        contractEntity.setDepositAmount(saveDTO.getDepositAmount() != null ? saveDTO.getDepositAmount() : BigDecimal.ZERO);
        contractEntity.setFinalAmount(finalAmount);
        
        // Calculate paid and remaining amount
        BigDecimal paidAmount = paymentTransactionRepository.sumAmountByContractId(contractEntity.getId());
        contractEntity.setPaidAmount(paidAmount);
        contractEntity.setRemainingAmount(finalAmount.subtract(paidAmount));

        // Save contract
        ContractEntity savedContract = contractRepository.save(contractEntity);

        // Save cars
        if (isNew) {
            for (ContractCarSaveDTO carDTO : saveDTO.getCars()) {
                ContractCarEntity contractCar = modelMapper.map(carDTO, ContractCarEntity.class);
                contractCar.setContractId(savedContract.getId());
                contractCarRepository.save(contractCar);
            }
        }

        // Save surcharges
        if (isNew && saveDTO.getSurcharges() != null) {
            for (SurchargeSaveDTO surchargeDTO : saveDTO.getSurcharges()) {
                SurchargeEntity surcharge = modelMapper.map(surchargeDTO, SurchargeEntity.class);
                surcharge.setContractId(savedContract.getId());
                surchargeRepository.save(surcharge);
            }
        }

        return true;
    }

    @Override
    @Transactional
    public Boolean deleteContract(String id) {
        Optional<ContractEntity> contractOpt = contractRepository.findById(id);
        if (!contractOpt.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }

        // Delete related data
        contractCarRepository.deleteByContractId(id);
        surchargeRepository.deleteByContractId(id);
        paymentTransactionRepository.deleteByContractId(id);
        contractImageRepository.deleteByContractId(id);

        // Delete contract
        contractRepository.deleteById(id);
        return true;
    }

    // ========== Contract Cars ==========

    @Override
    public List<ContractCarDTO> getContractCars(String contractId) {
        List<ContractCarEntity> contractCars = contractCarRepository.findByContractId(contractId);
        
        return contractCars.stream().map(contractCar -> {
            ContractCarDTO dto = modelMapper.map(contractCar, ContractCarDTO.class);
            
            // Load car info
            carRepository.findById(contractCar.getCarId()).ifPresent(car -> {
                dto.setCarModel(car.getModel());
                dto.setCarType(car.getCarType());
                dto.setLicensePlate(car.getLicensePlate());
                dto.setStatus(car.getStatus().toString());
            });
            
            return dto;
        }).collect(Collectors.toList());
    }

    // ========== Surcharges ==========

    @Override
    @Transactional
    public Boolean addSurcharge(@Valid SurchargeSaveDTO saveDTO) {
        SurchargeEntity surcharge = modelMapper.map(saveDTO, SurchargeEntity.class);
        surchargeRepository.save(surcharge);
        
        // Update contract total
        updateContractTotals(saveDTO.getContractId());
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteSurcharge(String id) {
        Optional<SurchargeEntity> surchargeOpt = surchargeRepository.findById(id);
        if (!surchargeOpt.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        String contractId = surchargeOpt.get().getContractId();
        surchargeRepository.deleteById(id);
        
        // Update contract total
        updateContractTotals(contractId);
        return true;
    }

    @Override
    public List<SurchargeDTO> getSurchargesByContractId(String contractId) {
        return surchargeRepository.findByContractId(contractId);
    }

    // ========== Payments ==========

    @Override
    @Transactional
    public Boolean addPayment(@Valid PaymentTransactionSaveDTO saveDTO) {
        PaymentTransactionEntity payment = modelMapper.map(saveDTO, PaymentTransactionEntity.class);
        
        // Generate transaction code
        String transactionCode = "TT" + System.currentTimeMillis();
        payment.setTransactionCode(transactionCode);
        payment.setStatus("SUCCESS");
        
        paymentTransactionRepository.save(payment);
        
        // Update contract paid amount
        updateContractTotals(saveDTO.getContractId());
        return true;
    }

    @Override
    @Transactional
    public Boolean deletePayment(String id) {
        Optional<PaymentTransactionEntity> paymentOpt = paymentTransactionRepository.findById(id);
        if (!paymentOpt.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }
        
        String contractId = paymentOpt.get().getContractId();
        paymentTransactionRepository.deleteById(id);
        
        // Update contract paid amount
        updateContractTotals(contractId);
        return true;
    }

    @Override
    public List<PaymentTransactionDTO> getPaymentHistory(String contractId) {
        return paymentTransactionRepository.findByContractIdWithEmployee(contractId);
    }

    // ========== Delivery & Return ==========

    @Override
    @Transactional
    public Boolean updateDelivery(@Valid ContractDeliveryDTO deliveryDTO) {
        Optional<ContractEntity> contractOpt = contractRepository.findById(deliveryDTO.getContractId());
        if (!contractOpt.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }

        ContractEntity contract = contractOpt.get();
        
        // Update delivery info
        contract.setDeliveryUserId(deliveryDTO.getDeliveryUserId());
        contract.setDeliveryTime(deliveryDTO.getDeliveryTime());
        contract.setStatus(ContractStatus.DELIVERED);
        
        if (StringUtils.isNotBlank(deliveryDTO.getPickupAddress())) {
            contract.setPickupAddress(deliveryDTO.getPickupAddress());
        }
        
        // Update rental info if needed
        if (Boolean.TRUE.equals(deliveryDTO.getUpdateRentalInfo())) {
            if (deliveryDTO.getNewStartDate() != null) {
                contract.setStartDate(deliveryDTO.getNewStartDate());
            }
            if (deliveryDTO.getNewEndDate() != null) {
                contract.setEndDate(deliveryDTO.getNewEndDate());
            }
            if (deliveryDTO.getNewTotalAmount() != null) {
                contract.setTotalRentalAmount(deliveryDTO.getNewTotalAmount());
            }
        }
        
        contractRepository.save(contract);
        
        // Update cars odometer
        for (ContractCarSaveDTO carDTO : deliveryDTO.getCars()) {
            if (carDTO.getId() != null) {
                contractCarRepository.findById(carDTO.getId()).ifPresent(contractCar -> {
                    contractCar.setStartOdometer(carDTO.getStartOdometer());
                    contractCarRepository.save(contractCar);
                });
            }
        }
        
        // Add surcharges if any
        if (deliveryDTO.getSurcharges() != null) {
            for (SurchargeSaveDTO surchargeDTO : deliveryDTO.getSurcharges()) {
                addSurcharge(surchargeDTO);
            }
        }
        
        return true;
    }

    @Override
    @Transactional
    public List<String> uploadDeliveryImages(String contractId, List<MultipartFile> files) {
        List<String> imageUrls = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            String imageUrl = cloudinaryUploadImages.uploadImage(files.get(i), "contract-delivery-images");
            
            ContractImageEntity image = new ContractImageEntity();
            image.setContractId(contractId);
            image.setImageType("DELIVERY");
            image.setImageUrl(imageUrl);
            image.setDisplayOrder(i + 1);
            contractImageRepository.save(image);
            
            imageUrls.add(imageUrl);
        }
        
        return imageUrls;
    }

    @Override
    @Transactional
    public Boolean updateReturn(@Valid ContractReturnDTO returnDTO) {
        Optional<ContractEntity> contractOpt = contractRepository.findById(returnDTO.getContractId());
        if (!contractOpt.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }

        ContractEntity contract = contractOpt.get();
        
        // Update return info
        contract.setReturnUserId(returnDTO.getReturnUserId());
        contract.setReturnTime(returnDTO.getReturnTime());
        contract.setStatus(ContractStatus.RETURNED);
        
        if (StringUtils.isNotBlank(returnDTO.getReturnAddress())) {
            contract.setReturnAddress(returnDTO.getReturnAddress());
        }
        
        // Update rental info if needed
        if (Boolean.TRUE.equals(returnDTO.getUpdateRentalInfo())) {
            if (returnDTO.getNewStartDate() != null) {
                contract.setStartDate(returnDTO.getNewStartDate());
            }
            if (returnDTO.getNewEndDate() != null) {
                contract.setEndDate(returnDTO.getNewEndDate());
            }
            if (returnDTO.getNewTotalAmount() != null) {
                contract.setTotalRentalAmount(returnDTO.getNewTotalAmount());
            }
        }
        
        contractRepository.save(contract);
        
        // Update cars odometer
        for (ContractCarSaveDTO carDTO : returnDTO.getCars()) {
            if (carDTO.getId() != null) {
                contractCarRepository.findById(carDTO.getId()).ifPresent(contractCar -> {
                    contractCar.setEndOdometer(carDTO.getEndOdometer());
                    contractCarRepository.save(contractCar);
                });
            }
        }
        
        // Add surcharges if any
        if (returnDTO.getSurcharges() != null) {
            for (SurchargeSaveDTO surchargeDTO : returnDTO.getSurcharges()) {
                addSurcharge(surchargeDTO);
            }
        }
        
        return true;
    }

    @Override
    @Transactional
    public List<String> uploadReturnImages(String contractId, List<MultipartFile> files) {
        List<String> imageUrls = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            String imageUrl = cloudinaryUploadImages.uploadImage(files.get(i), "contract-return-images");
            
            ContractImageEntity image = new ContractImageEntity();
            image.setContractId(contractId);
            image.setImageType("RETURN");
            image.setImageUrl(imageUrl);
            image.setDisplayOrder(i + 1);
            contractImageRepository.save(image);
            
            imageUrls.add(imageUrl);
        }
        
        return imageUrls;
    }

    // ========== Complete Contract ==========

    @Override
    @Transactional
    public Boolean completeContract(@Valid ContractCompleteDTO completeDTO) {
        Optional<ContractEntity> contractOpt = contractRepository.findById(completeDTO.getContractId());
        if (!contractOpt.isPresent()) {
            throw new RestApiException(ApiStatus.NOT_FOUND);
        }

        ContractEntity contract = contractOpt.get();
        
        // Add final payment if any
        if (completeDTO.getFinalPaymentAmount() != null && 
            completeDTO.getFinalPaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
            
            PaymentTransactionSaveDTO paymentDTO = new PaymentTransactionSaveDTO();
            paymentDTO.setContractId(completeDTO.getContractId());
            paymentDTO.setPaymentMethod(completeDTO.getPaymentMethod());
            paymentDTO.setAmount(completeDTO.getFinalPaymentAmount());
            paymentDTO.setPaymentDate(completeDTO.getCompletedDate());
            paymentDTO.setNotes(completeDTO.getPaymentNotes());
            
            addPayment(paymentDTO);
        }
        
        // Update contract status
        contract.setStatus(ContractStatus.COMPLETED);
        contract.setCompletedDate(completeDTO.getCompletedDate());
        contractRepository.save(contract);
        
        return true;
    }

    // ========== PDF Generation ==========

    @Override
    public byte[] downloadContractPDF(String id) {
        try {
            // Lấy thông tin hợp đồng
        ContractDTO contract = getContractDetail(id);
        
            if (contract == null) {
                throw new RestApiException(ApiStatus.NOT_FOUND);
            }
            
            // Lấy danh sách xe trong hợp đồng
            List<ContractCarDTO> cars = getContractCars(id);
            
            if (cars == null || cars.isEmpty()) {
                throw new RestApiException(ApiStatus.BAD_REQUEST_VALID);
            }
            
            // Tạo PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(baos);
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.kernel.geom.PageSize pageSize = com.itextpdf.kernel.geom.PageSize.A4;
            Document document = new Document(pdfDoc, pageSize);
            document.setMargins(20, 20, 20, 20);
            
            // Load fonts
            PdfFont font;
            PdfFont fontBold;
            try (InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/times.ttf");
                 InputStream fontBoldStream = getClass().getClassLoader().getResourceAsStream("fonts/timesbd.ttf")) {

                if (fontStream == null || fontBoldStream == null) {
                    throw new FileNotFoundException("Font files not found in resources/fonts/");
                }

               font = PdfFontFactory.createFont(
                        fontStream.readAllBytes(), com.itextpdf.io.font.PdfEncodings.IDENTITY_H);
                fontBold = PdfFontFactory.createFont(
                        fontBoldStream.readAllBytes(), com.itextpdf.io.font.PdfEncodings.IDENTITY_H);
            }

            // ========== HEADER ==========
            Paragraph header1 = new Paragraph("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM")
                    .setFont(fontBold)
                    .setFontSize(13)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setMarginBottom(0);
            document.add(header1);
            
            Paragraph header2 = new Paragraph("Độc lập - Tự do - Hạnh phúc")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setMarginBottom(15);
            document.add(header2);
            
            // Đường kẻ ngang
            com.itextpdf.layout.element.LineSeparator ls = new com.itextpdf.layout.element.LineSeparator(
                    new com.itextpdf.kernel.pdf.canvas.draw.SolidLine());
            document.add(ls);
            document.add(new Paragraph("\n"));
            
            // ========== TIÊU ĐỀ HỢP ĐỒNG ==========
            Paragraph title = new Paragraph("HỢP ĐỒNG CHO THUÊ XE")
                    .setFont(fontBold)
                    .setFontSize(16)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(title);
            
            Paragraph contractCode = new Paragraph("Số: " + (contract.getContractCode() != null ? contract.getContractCode() : ""))
                    .setFont(font)
                    .setFontSize(11)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(contractCode);
            
            // ========== MỞ ĐẦU ==========
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String todayStr = dateFormat.format(new Date());
            
            Paragraph intro = new Paragraph(
                    "Hợp Đồng dịch vụ cho thuê xe (Sau đây gọi là \"Hợp đồng\") được lập " + todayStr + " bởi và giữa các bên:"
            )
                    .setFont(font)
                    .setFontSize(11)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.JUSTIFIED)
                    .setMarginBottom(10);
            document.add(intro);
            
            // ========== BÊN A (CHO THUÊ XE) ==========
            document.add(new Paragraph("BÊN CHO THUÊ XE (Gọi tắt là bên A): Hồi số")
                    .setFont(fontBold)
                    .setFontSize(11)
                    .setMarginBottom(3));
            
            document.add(new Paragraph("Họ tên: Mạnh Hòa")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));
            
            document.add(new Paragraph("Địa chỉ: ")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));
            
            document.add(new Paragraph("ĐT: 0859963203")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(10));
            
            // ========== BÊN B (THUÊ XE) ==========
            document.add(new Paragraph("BÊN THUÊ XE (Gọi tắt là bên B):")
                    .setFont(fontBold)
                    .setFontSize(11)
                    .setMarginBottom(3));
            
            document.add(new Paragraph("Họ tên: " + (contract.getCustomerName() != null ? contract.getCustomerName() : ""))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));
            
            String address = "";
            if (contract.getPickupAddress() != null && !contract.getPickupAddress().isEmpty()) {
                address = contract.getPickupAddress();
            } else if (contract.getReturnAddress() != null && !contract.getReturnAddress().isEmpty()) {
                address = contract.getReturnAddress();
            }
            
            document.add(new Paragraph("Địa chỉ: " + address)
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));
            
            document.add(new Paragraph("ĐT: " + (contract.getPhoneNumber() != null ? contract.getPhoneNumber() : ""))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));
            
            document.add(new Paragraph("Hộ khẩu thường trú tại: Tổ Q Trung Hưng Thị Xã Sơn Tây Hà Nội")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(5));
            
            // Thông tin CMND
            document.add(new Paragraph("Ngày sinh: 18/09/2004")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));
            
            String cmnd = contract.getCitizenId() != null ? contract.getCitizenId() : "001204020429";
            document.add(new Paragraph("CMND CCCD Hộ chiếu số " + cmnd + " do CỤC TRƯỞNG CỤC CẢNH SÁT QUẢN LÝ HÀNH CHÍNH VỀ TRẬT TỪ XÃ HỘI")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));
            
            document.add(new Paragraph("cấp ngày 31/05/2021")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(10));
            
            // ========== NỘI DUNG HỢP ĐỒNG ==========
            document.add(new Paragraph("Hai bên đã thỏa thuận và thống nhất ký kết Hợp đồng thuê xe ô tô với những điều khoản cụ thể như sau:")
                    .setFont(font)
                    .setFontSize(11)
                    .setItalic()
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.JUSTIFIED)
                    .setMarginBottom(10));
            
            // ========== ĐIỀU 1: NỘI DUNG HỢP ĐỒNG ==========
            document.add(new Paragraph("Điều 1. Nội dung hợp đồng")
                    .setFont(fontBold)
                    .setFontSize(11)
                    .setMarginBottom(5));
            
            document.add(new Paragraph("Bên A cho bên B thuê xe :")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(3));
            
            // Thông tin xe - lấy xe đầu tiên
            ContractCarDTO firstCar = cars.get(0);
            
            Table carTable = new Table(new float[]{200f, 200f});
            carTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
            carTable.setMarginBottom(10);
            
            // Hàng 1: Nhãn hiệu, Biển số
            carTable.addCell(createNoBorderCell("Nhãn hiệu : " + (firstCar.getCarModel() != null ? firstCar.getCarModel() : "Honda"), font));
            carTable.addCell(createNoBorderCell("Biển số: " + (firstCar.getLicensePlate() != null ? firstCar.getLicensePlate() : ""), font));
            
            // Hàng 2: Loại xe, Màu sơn
            carTable.addCell(createNoBorderCell("Loại xe : " + (firstCar.getCarType() != null ? firstCar.getCarType() : "CX5"), font));
            carTable.addCell(createNoBorderCell("Màu Sơn: ", font));
            
            // Hàng 3: Số máy, Số khung
            carTable.addCell(createNoBorderCell("Số máy : ", font));
            carTable.addCell(createNoBorderCell("Số khung:", font));
            
            // Hàng 4: Số chỗ ngồi, Đăng ký xe
            carTable.addCell(createNoBorderCell("Số chỗ ngồi : 5 chỗ", font));
            carTable.addCell(createNoBorderCell("Đăng ký xe:", font));
            
            document.add(carTable);
            
            // Thời gian thuê
            String startDateStr = contract.getStartDate() != null ? dateFormat.format(contract.getStartDate()) : "";
            String endDateStr = contract.getEndDate() != null ? dateFormat.format(contract.getEndDate()) : "";
            
            document.add(new Paragraph("Thời gian thuê : 23:00 " + startDateStr + " đến 20:00 " + endDateStr)
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(3));
            
            // Tiền thuê
            java.text.NumberFormat currencyFormat = java.text.NumberFormat.getInstance(new Locale("vi", "VN"));
            String totalAmountStr = contract.getTotalRentalAmount() != null ? 
                    currencyFormat.format(contract.getTotalRentalAmount()) : "0";
            
            document.add(new Paragraph("Tiền thuê : " + totalAmountStr + " (một trăm hai mươi nghìn đồng chẵn)")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(3));
            
            // Cam đoan
            document.add(new Paragraph("- Bên A cam đoan trước khi ký bản Hợp đồng này, xe ô tô nêu trên:")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(2));
            
            document.add(new Paragraph("+ Không có tranh chấp về quyền sở hữu/sử dụng;")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));
            
            document.add(new Paragraph("+ Không bị ràng buộc bởi bất kỳ Hợp đồng thuê xe ô tô nào đang có hiệu lực.")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(10));
            
            // ========== ĐIỀU 2: PHÍ DỊCH VỤ VÀ PHƯƠNG THỨC THANH TOÁN ==========
            document.add(new Paragraph("Điều 2. Phí dịch vụ và phương thức thanh toán")
                    .setFont(fontBold)
                    .setFontSize(11)
                    .setMarginBottom(5));
            
            document.add(new Paragraph("1. Phí dịch vụ")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(3));
            
            // Chi phí
            String finalAmountStr = contract.getFinalAmount() != null ? 
                    currencyFormat.format(contract.getFinalAmount()) : "0";
            
            document.add(new Paragraph("- Phí dịch vụ là " + finalAmountStr + " đồng (hai triệu tám trăm bốn mươi nghìn hai trăm đồng chẵn).")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));
            
            String paidAmountStr = contract.getPaidAmount() != null && contract.getPaidAmount().compareTo(BigDecimal.ZERO) > 0 ? 
                    currencyFormat.format(contract.getPaidAmount()) : "0";
            
            document.add(new Paragraph("- Đã cọc : " + paidAmountStr + " đồng ( đồng chẵn)")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));
            
            String remainingAmountStr = contract.getRemainingAmount() != null ? 
                    currencyFormat.format(contract.getRemainingAmount()) : "0";
            
            document.add(new Paragraph("- Còn lại : " + remainingAmountStr + " đồng (hai triệu tám trăm bốn mươi nghìn hai trăm đồng chẵn)")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));
            
            document.add(new Paragraph("- Khoản phí dịch vụ trên đã bao gồm tiền xăng xe; phí cầu, đường, bến bãi; tiền ăn, của lái xe nhưng chưa bao gồm thuế giá trị gia tăng (VAT).")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(5));
            
            document.add(new Paragraph("2. Phương thức thanh toán")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(3));
            
            document.add(new Paragraph("- Khoản phí dịch vụ trên được thanh toán trực tiếp bằng tiền mặt hoặc chuyển vào tài khoản ngân hàng do Bên B chỉ định túy từng thời điểm khác nhau.")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(10));
            
            // ========== ĐIỀU 3, 4, 5, 6 ==========
            addRemainingClauses(document, font, fontBold);
            
            // ========== CHỮ KÝ ==========
            document.add(new Paragraph("\n\n"));
            
            Table signatureTable = new Table(2);
            signatureTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
            
            com.itextpdf.layout.element.Cell cellA = new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("ĐẠI DIỆN BÊN A")
                            .setFont(fontBold)
                            .setFontSize(12)
                            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))
                    .setBorder(null)
                    .setPaddingTop(30);
            
            com.itextpdf.layout.element.Cell cellB = new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("ĐẠI DIỆN BÊN B")
                            .setFont(fontBold)
                            .setFontSize(12)
                            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))
                    .setBorder(null)
                    .setPaddingTop(30);
            
            signatureTable.addCell(cellA);
            signatureTable.addCell(cellB);
            
            document.add(signatureTable);
            
            // Close document
            document.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating contract PDF", e);
            throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Helper method to create cell without border
    private com.itextpdf.layout.element.Cell createNoBorderCell(String text, com.itextpdf.kernel.font.PdfFont font) {
        return new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(11))
                .setBorder(null)
                .setPadding(2);
    }
    
    // Helper method to add remaining clauses
    private void addRemainingClauses(Document document, com.itextpdf.kernel.font.PdfFont font, com.itextpdf.kernel.font.PdfFont fontBold) {
        // ĐIỀU 3
        document.add(new Paragraph("Điều 3. Trách nhiệm của Bên B")
                .setFont(fontBold)
                .setFontSize(11)
                .setMarginBottom(5));
        
        com.itextpdf.layout.element.List list1 = new com.itextpdf.layout.element.List()
                .setSymbolIndent(10)
                .setListSymbol("\u2022")
                .setFont(font)
                .setFontSize(11);
        
        list1.add(new com.itextpdf.layout.element.ListItem("Đưa, đón người của Bên A đúng thời gian và địa điểm thỏa thuận của Hợp đồng;"));
        list1.add(new com.itextpdf.layout.element.ListItem("Đảm bảo chất lượng xe tốt và lái xe an toàn trong quá trình đưa đón của Bên A;"));
        list1.add(new com.itextpdf.layout.element.ListItem("Có trách nhiệm mua bảo hiểm dân sự cho xe và người được vận tải trên xe;"));
        list1.add(new com.itextpdf.layout.element.ListItem("Bồi thường thiệt hại cho Bên A nếu gây ra thiệt hại trong quá trình thực hiện các công việc trên;"));
        list1.add(new com.itextpdf.layout.element.ListItem("Các nghĩa vụ khác theo quy định của pháp luật hiện hành."));
        
        document.add(list1);
        document.add(new Paragraph("\n"));
        
        // ĐIỀU 4
        document.add(new Paragraph("Điều 4. Trách nhiệm của Bên A")
                .setFont(fontBold)
                .setFontSize(11)
                .setMarginBottom(5));
        
        com.itextpdf.layout.element.List list2 = new com.itextpdf.layout.element.List()
                .setSymbolIndent(10)
                .setListSymbol("\u2022")
                .setFont(font)
                .setFontSize(11);
        
        list2.add(new com.itextpdf.layout.element.ListItem("Thông báo chính xác thời gian và địa điểm đưa, đón cho Bên A trước ít nhất là 2h nếu có sự thay đổi;"));
        list2.add(new com.itextpdf.layout.element.ListItem("Thanh toán đầy đủ và đúng hạn khoản phí dịch vụ theo quy định tại Điều 2 cho Bên B;"));
        list2.add(new com.itextpdf.layout.element.ListItem("Các nghĩa vụ khác theo quy định của pháp luật."));
        
        document.add(list2);
        document.add(new Paragraph("\n"));
        
        // ĐIỀU 5
        document.add(new Paragraph("Điều 5. Xử lý bồi thường bảo hiểm và xử lý vi phạm hợp đồng")
                .setFont(fontBold)
                .setFontSize(11)
                .setMarginBottom(5));
        
        document.add(new Paragraph("1. Trong trường hợp Bên A có sự thay đổi về kế hoạch do yêu cầu của công việc hoặc do yếu tố khách quan khác mà không thể tiến hành theo đúng thời gian tại Điều 1 thì phải thông báo cho Bên A trước ít nhất là 02 ngày trước ngày tiến hành công việc tại Điều 1 đồng thời bảo lưu cho Bên A chính thức được di dời, dịch chuyển thời gian chính xác để đưa đón Bên A. Nếu Bên B vẫn không thể tiến hành theo đúng thời gian tại Điều 1 do trình do trên thì Bên A vẫn không tiền tiến hành theo đúng thời gian tại Điều 1. Trong trường hợp đã lùi lại thời gian mà Bên A vẫn không thể tiến hành theo đúng thời gian thì thuận lũi lại thời gian mà Bên B không phải hoàn trả lại số tiền đã thanh toán trước.")
                .setFont(font)
                .setFontSize(11)
                .setMarginBottom(5));
        
        document.add(new Paragraph("2. Trong trường hợp Bên B không đưa, đón đúng xe theo đúng thời gian thỏa thuận tại Điều 1 thì phải thông báo trước cho Bên A ít nhất là 2 ngày trước ngày tiến hành công việc tại Điều 1 đồng thời bảo lưu cho Bên A thời gian chính xác để đưa đón Bên A. Nếu Bên B vẫn không thể tiến hành theo đúng thời gian tại Điều 1 do thời gian chính xác để đưa đón Bên A không thể trả lại cho Bên A thời gian chính xác đề đưa đón. Nếu Bên B vẫn không đóng thời gian tại Điều 1 do thời gian chính xác để đưa đón Bên A trong bị phát một khoản tiền bằng với số tiền Bên A đã thanh toán trước.")
                .setFont(font)
                .setFontSize(11)
                .setMarginBottom(5));
        
        document.add(new Paragraph("3. Nếu một trong các bên có sự thay đổi về thời gian theo quy định tại Điều 1 mà không báo trước 2 ngày thì phải chịu trách nhiệm về chi phí xử lý như sau:")
                .setFont(font)
                .setFontSize(11)
                .setMarginBottom(3));
        
        document.add(new Paragraph("- Bên B phải trả lại Bên A khoản tiền đã thanh toán trước đồng thời bị phạt một khoản tiền bằng với khoản tiền đã đặt trước.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(2));
        
        document.add(new Paragraph("- Bên A sẽ không được hoàn lại số tiền đã thanh toán trước.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(5));
        
        document.add(new Paragraph("4. Trong trường hợp việc thay đổi thời gian của một bên mà gây thiệt hại cho bên còn lại (kể cả đã thông báo trước 2 ngày) thì bên có lỗi phải bồi thường thiệt hại do sự thay đổi thời gian.")
                .setFont(font)
                .setFontSize(11)
                .setMarginBottom(5));
        
        document.add(new Paragraph("5. Trong trường hợp Bên B không đón Bên A từ ___về ___theo đúng thời hạn quy định tại Điều 1 thì phải chịu các chi phí ăn, nghỉ cho Bên A cho thời gian chậm đến về theo giá thực tế.")
                .setFont(font)
                .setFontSize(11)
                .setMarginBottom(10));
        
        // ĐIỀU 6
        document.add(new Paragraph("Điều 6. Các thỏa thuận khác")
                .setFont(fontBold)
                .setFontSize(11)
                .setMarginBottom(5));
        
        com.itextpdf.layout.element.List list3 = new com.itextpdf.layout.element.List()
                .setSymbolIndent(10)
                .setListSymbol("\u2022")
                .setFont(font)
                .setFontSize(11);
        
        list3.add(new com.itextpdf.layout.element.ListItem("Hai bên cam kết thực hiện đầy đủ các thỏa thuận trong Hợp đồng này."));
        list3.add(new com.itextpdf.layout.element.ListItem("Những nội dung không được thỏa thuận trong Hợp đồng này thì áp dụng các văn bản pháp luật hiện hành có liên quan;"));
        list3.add(new com.itextpdf.layout.element.ListItem("Nếu có tranh chấp phát sinh từ Hợp đồng này thì các bên trước hết phải cùng nhau giải quyết bằng thương lượng, hòa giải. Nếu không đạt được sự thương lượng, hòa giải thì mỗi bên có quyền khởi kiện ra tòa án để giải quyết theo thủ tục chung của pháp luật."));
        list3.add(new com.itextpdf.layout.element.ListItem("Hợp đồng này gồm 2 trang, và được lập thành 02 (hai) bản, mỗi bên giữ 01 bản, có giá trị pháp lý như nhau và có hiệu lực từ ngày ký."));
        
        document.add(list3);
    }

    // ========== Helper Methods ==========

    private BigDecimal calculateDiscountAmount(BigDecimal totalAmount, String discountType, BigDecimal discountValue) {
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        if ("PERCENTAGE".equals(discountType)) {
            return totalAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
        } else if ("AMOUNT".equals(discountType)) {
            return discountValue;
        }
        
        return BigDecimal.ZERO;
    }

    private void updateContractTotals(String contractId) {
        Optional<ContractEntity> contractOpt = contractRepository.findById(contractId);
        if (!contractOpt.isPresent()) {
            return;
        }

        ContractEntity contract = contractOpt.get();
        
        // Recalculate surcharge total
        BigDecimal totalSurcharge = surchargeRepository.findByContractId(contractId).stream()
                .map(s -> s.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Recalculate final amount
        BigDecimal finalAmount = contract.getTotalRentalAmount()
                .add(totalSurcharge)
                .subtract(contract.getDiscountAmount() != null ? contract.getDiscountAmount() : BigDecimal.ZERO);
        
        // Recalculate paid amount
        BigDecimal paidAmount = paymentTransactionRepository.sumAmountByContractId(contractId);
        
        contract.setTotalSurcharge(totalSurcharge);
        contract.setFinalAmount(finalAmount);
        contract.setPaidAmount(paidAmount);
        contract.setRemainingAmount(finalAmount.subtract(paidAmount));
        
        contractRepository.save(contract);
    }
}
