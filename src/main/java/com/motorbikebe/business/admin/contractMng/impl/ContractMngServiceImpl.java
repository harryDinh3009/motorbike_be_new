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
            CustomerEntity customerEntity = customer.get();
            contractDTO.setCustomerName(customerEntity.getFullName());
            contractDTO.setPhoneNumber(customerEntity.getPhoneNumber());
            contractDTO.setEmail(customerEntity.getEmail());
            contractDTO.setCountry(customerEntity.getCountry());
            contractDTO.setCitizenId(customerEntity.getCitizenId());
            contractDTO.setCustomerAddress(customerEntity.getAddress());
            contractDTO.setCustomerDateOfBirth(customerEntity.getDateOfBirth());
            // Hiện tại hệ thống chưa lưu riêng ngày cấp CCCD, tạm thời dùng ngày tạo hồ sơ khách
            if (customerEntity.getCreatedDate() != null) {
                contractDTO.setCitizenIdIssuedDate(new Date(customerEntity.getCreatedDate()));
            }
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
    public ContractEntity saveContract(@Valid ContractSaveDTO saveDTO) {
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

        return contractEntity;
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

        return contractCars.stream()
                .map(this::buildContractCarDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContractCarDTO addContractCar(@Valid ContractCarCreateDTO createDTO) {
        ContractEntity contract = contractRepository.findById(createDTO.getContractId())
                .orElseThrow(() -> new RestApiException(ApiStatus.NOT_FOUND));

        carRepository.findById(createDTO.getCarId())
                .orElseThrow(() -> new RestApiException(ApiStatus.NOT_FOUND));

        if (contractCarRepository.existsByContractIdAndCarId(createDTO.getContractId(), createDTO.getCarId())) {
            throw new RestApiException(ApiStatus.BAD_REQUEST);
        }

        validateCarAvailabilityForContract(contract, createDTO.getCarId());

        ContractCarEntity contractCar = ContractCarEntity.builder()
                .contractId(createDTO.getContractId())
                .carId(createDTO.getCarId())
                .dailyPrice(createDTO.getDailyPrice())
                .hourlyPrice(createDTO.getHourlyPrice())
                .totalAmount(createDTO.getTotalAmount())
                .startOdometer(createDTO.getStartOdometer())
                .endOdometer(createDTO.getEndOdometer())
                .notes(createDTO.getNotes())
                .build();

        ContractCarEntity saved = contractCarRepository.save(contractCar);

        updateContractTotals(contract.getId());

        return buildContractCarDTO(saved);
    }

    @Override
    @Transactional
    public ContractCarDTO updateContractCar(String contractCarId, @Valid ContractCarUpdateDTO updateDTO) {
        ContractCarEntity contractCar = contractCarRepository.findById(contractCarId)
                .orElseThrow(() -> new RestApiException(ApiStatus.NOT_FOUND));

        ContractEntity contract = contractRepository.findById(contractCar.getContractId())
                .orElseThrow(() -> new RestApiException(ApiStatus.NOT_FOUND));

        if (StringUtils.isNotBlank(updateDTO.getCarId()) &&
                !updateDTO.getCarId().equals(contractCar.getCarId())) {
            carRepository.findById(updateDTO.getCarId())
                    .orElseThrow(() -> new RestApiException(ApiStatus.NOT_FOUND));

            if (contractCarRepository.existsByContractIdAndCarId(contractCar.getContractId(), updateDTO.getCarId())) {
                throw new RestApiException(ApiStatus.BAD_REQUEST);
            }

            validateCarAvailabilityForContract(contract, updateDTO.getCarId());
            contractCar.setCarId(updateDTO.getCarId());
        }

        if (updateDTO.getDailyPrice() != null) {
            contractCar.setDailyPrice(updateDTO.getDailyPrice());
        }
        if (updateDTO.getHourlyPrice() != null) {
            contractCar.setHourlyPrice(updateDTO.getHourlyPrice());
        }
        if (updateDTO.getTotalAmount() != null) {
            contractCar.setTotalAmount(updateDTO.getTotalAmount());
        }
        if (updateDTO.getStartOdometer() != null) {
            contractCar.setStartOdometer(updateDTO.getStartOdometer());
        }
        if (updateDTO.getEndOdometer() != null) {
            contractCar.setEndOdometer(updateDTO.getEndOdometer());
        }
        if (updateDTO.getNotes() != null) {
            contractCar.setNotes(updateDTO.getNotes());
        }

        ContractCarEntity saved = contractCarRepository.save(contractCar);

        updateContractTotals(contract.getId());

        return buildContractCarDTO(saved);
    }

    @Override
    @Transactional
    public Boolean deleteContractCar(String contractCarId) {
        ContractCarEntity contractCar = contractCarRepository.findById(contractCarId)
                .orElseThrow(() -> new RestApiException(ApiStatus.NOT_FOUND));

        contractCarRepository.deleteById(contractCarId);

        updateContractTotals(contractCar.getContractId());
        return true;
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
    public Boolean updateSurcharge(String id, @Valid SurchargeSaveDTO saveDTO) {
        SurchargeEntity surcharge = surchargeRepository.findById(id)
                .orElseThrow(() -> new RestApiException(ApiStatus.NOT_FOUND));

        if (StringUtils.isNotBlank(saveDTO.getDescription())) {
            surcharge.setDescription(saveDTO.getDescription());
        }
        if (saveDTO.getAmount() != null) {
            surcharge.setAmount(saveDTO.getAmount());
        }
        if (saveDTO.getNotes() != null) {
            surcharge.setNotes(saveDTO.getNotes());
        }

        surchargeRepository.save(surcharge);

        updateContractTotals(surcharge.getContractId());
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
                    if (carDTO.getStartOdometer() != null)
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

                carRepository.findById(carDTO.getId()).ifPresent(car -> {
                   car.setCurrentOdometer(carDTO.getEndOdometer());
                   car.setStatus(carDTO.getStatus());
                   carRepository.save(car);
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
            String contractDateStr = contract.getCreatedDate() != null ?
                    dateFormat.format(contract.getCreatedDate()) : todayStr;

            Paragraph intro = new Paragraph(
                    "Hợp đồng dịch vụ cho thuê xe (sau đây gọi tắt là \"Hợp đồng\") được lập ngày "
                            + contractDateStr + " bởi và giữa các bên:"
            )
                    .setFont(font)
                    .setFontSize(11)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.JUSTIFIED)
                    .setMarginBottom(10);
            document.add(intro);

            // ========== BÊN A (CHO THUÊ XE) ==========
            document.add(new Paragraph("BÊN CHO THUÊ XE (Gọi tắt là bên A):")
                    .setFont(fontBold)
                    .setFontSize(11)
                    .setMarginBottom(3));

            document.add(new Paragraph("Họ tên người đại diện: Đinh Hòa")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));

            document.add(new Paragraph("Địa chỉ: Tổ 1, Thôn Cầu Mè, Phương Thiện, Hà Giang, Việt Nam")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));

            document.add(new Paragraph("ĐT: 0859963204")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(10));

            // ========== BÊN B (THUÊ XE) ==========
            document.add(new Paragraph("BÊN THUÊ XE (Gọi tắt là bên B):")
                    .setFont(fontBold)
                    .setFontSize(11)
                    .setMarginBottom(3));

            document.add(new Paragraph("Họ tên: " + valueOrPlaceholder(contract.getCustomerName(), "[Tên khách hàng]"))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));

            String address = firstNonEmpty(
                    contract.getCustomerAddress(),
                    contract.getPickupAddress(),
                    contract.getReturnAddress());

            document.add(new Paragraph("Địa chỉ: " + valueOrPlaceholder(address, "[Địa chỉ]"))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));

            document.add(new Paragraph("ĐT: " + valueOrPlaceholder(contract.getPhoneNumber(), "[Số điện thoại]"))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));

            document.add(new Paragraph("Ngày sinh: " + formatDateValue(contract.getCustomerDateOfBirth(), "dd/MM/yyyy", "[Ngày sinh]"))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(5));

            document.add(new Paragraph("CMND/CCCD: " + valueOrPlaceholder(contract.getCitizenId(), "[Căn cước công dân]"))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(2));
            document.add(new Paragraph("Ngày cấp: " + formatDateValue(contract.getCitizenIdIssuedDate(), "dd/MM/yyyy", "[Ngày cấp]"))
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

            document.add(new Paragraph("Bên A cho bên B thuê xe.")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(3));

            String startDateStr = formatDateValue(contract.getStartDate(), "dd/MM/yyyy HH:mm", "[Ngày thuê]");
            String endDateStr = formatDateValue(contract.getEndDate(), "dd/MM/yyyy HH:mm", "[Ngày trả]");

            document.add(new Paragraph("Thời gian thuê: " + startDateStr + " đến " + endDateStr)
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(3));

            java.text.NumberFormat currencyFormat = java.text.NumberFormat.getInstance(new Locale("vi", "VN"));
            String totalAmountStr = currencyFormat.format(
                    contract.getFinalAmount() != null ? contract.getFinalAmount() : BigDecimal.ZERO);

            document.add(new Paragraph("Tiền thuê: " + totalAmountStr + " đồng")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(3));

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

            // ========== ĐIỀU 2: PHƯƠNG THỨC THANH TOÁN ==========
            document.add(new Paragraph("Điều 2. Phương thức thanh toán")
                    .setFont(fontBold)
                    .setFontSize(11)
                    .setMarginBottom(5));

            document.add(new Paragraph("- Khoản phí dịch vụ trên được thanh toán trực tiếp bằng tiền mặt hoặc chuyển vào tài khoản ngân hàng do Bên B chỉ định tùy từng thời điểm khác nhau.")
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

    private String valueOrPlaceholder(String value, String placeholder) {
        return (value != null && !value.trim().isEmpty()) ? value : placeholder;
    }

    private String formatDateValue(Date date, String pattern, String placeholder) {
        if (date == null) {
            return placeholder;
        }
        return new SimpleDateFormat(pattern).format(date);
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    // Helper method to add remaining clauses
    private void addRemainingClauses(Document document, com.itextpdf.kernel.font.PdfFont font, com.itextpdf.kernel.font.PdfFont fontBold) {
        document.add(new Paragraph("Điều 3. Trách nhiệm của Bên B")
                .setFont(fontBold)
                .setFontSize(11)
                .setMarginBottom(5));

        document.add(new Paragraph("1. Nhận xe đúng thời gian và địa điểm theo thỏa thuận trong Hợp đồng;")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(2));
        document.add(new Paragraph("2. Đảm bảo sử dụng xe đúng mục đích, đảm bảo chất lượng xe tốt và lái xe an toàn trong suốt quá trình thuê;")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(2));
        document.add(new Paragraph("3. Bồi thường thiệt hại cho Bên A nếu gây ra thiệt hại trong quá trình sử dụng xe;")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(2));
        document.add(new Paragraph("4. Các nghĩa vụ khác theo quy định của pháp luật hiện hành.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(10));

        document.add(new Paragraph("Điều 4. Trách nhiệm của Bên A")
                .setFont(fontBold)
                .setFontSize(11)
                .setMarginBottom(5));

        document.add(new Paragraph("1. Cung cấp xe đúng thời gian và địa điểm thỏa thuận trong Hợp đồng;")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(2));
        document.add(new Paragraph("2. Đảm bảo xe có chất lượng tốt, đầy đủ giấy tờ và bảo hành trong suốt thời gian cho thuê;")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(2));
        document.add(new Paragraph("3. Thông báo cho Bên B về các thay đổi liên quan đến xe (nếu có);")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(2));
        document.add(new Paragraph("4. Các nghĩa vụ khác theo quy định của pháp luật.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(10));

        document.add(new Paragraph("Điều 5. Thông báo và xử lý vi phạm hợp đồng")
                .setFont(fontBold)
                .setFontSize(11)
                .setMarginBottom(5));

        document.add(new Paragraph("1. Trong trường hợp Bên A thay đổi kế hoạch do yêu cầu công việc hoặc yếu tố khách quan, Bên A phải thông báo cho Bên B ít nhất 2 ngày trước ngày giao xe. Nếu Bên A không thể thực hiện theo thời gian mới, Bên B không cần hoàn trả tiền đã thanh toán.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(3));
        document.add(new Paragraph("2. Nếu Bên B không thể nhận xe đúng thời gian thỏa thuận, Bên B phải thông báo trước cho Bên A ít nhất 2 ngày và thỏa thuận lại thời gian nhận xe. Nếu Bên B không thực hiện theo thời gian đã thỏa thuận, Bên B phải hoàn trả số tiền đã thanh toán.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(3));
        document.add(new Paragraph("3. Nếu Bên B không trả xe đúng thời gian thỏa thuận tại Điều 1, Bên B phải thông báo cho Bên A ít nhất 2 ngày trước ngày trả xe và thỏa thuận lại thời gian trả xe. Nếu Bên B không trả xe đúng thời gian đã thỏa thuận, Bên B phải chịu thêm một khoản phụ thu.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(3));
        document.add(new Paragraph("4. Nếu một trong các bên có sự thay đổi về thời gian theo quy định tại Điều 1 mà không báo trước 2 ngày thì phải chịu trách nhiệm xử lý như sau:")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(2));
        document.add(new Paragraph("- Bên B phải trả lại Bên A khoản tiền đã thanh toán trước;")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(35)
                .setMarginBottom(2));
        document.add(new Paragraph("- Bên A sẽ không được hoàn lại số tiền đã thanh toán trước.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(35)
                .setMarginBottom(3));
        document.add(new Paragraph("5. Trong trường hợp việc thay đổi thời gian của một bên mà gây thiệt hại cho bên còn lại (kể cả đã thông báo trước 2 ngày) thì bên có lỗi phải bồi thường thiệt hại do sự thay đổi thời gian.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(3));
        document.add(new Paragraph("6. Nếu một trong các bên không thực hiện đúng thời hạn quy định tại Điều 1 thì phải chịu các chi phí phát sinh (ăn, nghỉ...) theo giá thực tế.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(10));

        document.add(new Paragraph("Điều 6. Các thỏa thuận khác")
                .setFont(fontBold)
                .setFontSize(11)
                .setMarginBottom(5));

        document.add(new Paragraph("1. Hai bên cam kết thực hiện đầy đủ các thỏa thuận trong Hợp đồng này.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(2));
        document.add(new Paragraph("2. Những nội dung không được thỏa thuận trong Hợp đồng này thì áp dụng các văn bản pháp luật hiện hành có liên quan.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(2));
        document.add(new Paragraph("3. Nếu có tranh chấp phát sinh từ Hợp đồng này thì các bên trước hết phải cùng nhau giải quyết bằng thương lượng, hòa giải. Nếu không đạt được sự thương lượng, hòa giải thì mỗi bên có quyền khởi kiện ra tòa án để giải quyết theo thủ tục chung của pháp luật.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(2));
        document.add(new Paragraph("4. Hợp đồng này gồm 2 trang, và được lập thành 02 (hai) bản, mỗi bên giữ 01 bản, có giá trị pháp lý như nhau và có hiệu lực từ ngày ký.")
                .setFont(font)
                .setFontSize(11)
                .setMarginLeft(20)
                .setMarginBottom(10));
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

        BigDecimal totalRentalAmount = contractCarRepository.findByContractId(contractId).stream()
                .map(car -> car.getTotalAmount() != null ? car.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Recalculate surcharge total
        BigDecimal totalSurcharge = surchargeRepository.findByContractId(contractId).stream()
                .map(SurchargeDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = calculateDiscountAmount(
                totalRentalAmount,
                contract.getDiscountType(),
                contract.getDiscountValue());

        // Recalculate final amount
        BigDecimal finalAmount = totalRentalAmount
                .add(totalSurcharge)
                .subtract(discountAmount);

        // Recalculate paid amount
        BigDecimal paidAmount = paymentTransactionRepository.sumAmountByContractId(contractId);
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }

        contract.setTotalRentalAmount(totalRentalAmount);
        contract.setTotalSurcharge(totalSurcharge);
        contract.setDiscountAmount(discountAmount);
        contract.setFinalAmount(finalAmount);
        contract.setPaidAmount(paidAmount);
        contract.setRemainingAmount(finalAmount.subtract(paidAmount));

        contractRepository.save(contract);
    }

    private ContractCarDTO buildContractCarDTO(ContractCarEntity contractCar) {
        ContractCarDTO dto = modelMapper.map(contractCar, ContractCarDTO.class);

        carRepository.findById(contractCar.getCarId()).ifPresent(car -> {
            dto.setCarModel(car.getModel());
            dto.setCarType(car.getCarType());
            dto.setLicensePlate(car.getLicensePlate());
            dto.setStatus(car.getStatus().toString());
        });

        return dto;
    }

    private void validateCarAvailabilityForContract(ContractEntity contract, String carId) {
        ContractStatus status = contract.getStatus();
        if (status == null || (status != ContractStatus.CONFIRMED && status != ContractStatus.DELIVERED)) {
            return;
        }

        List<ContractEntity> overlappingContracts = contractRepository.findOverlappingContracts(
                        carId,
                        contract.getStartDate(),
                        contract.getEndDate())
                .stream()
                .filter(c -> !c.getId().equals(contract.getId()))
                .collect(Collectors.toList());

        if (!overlappingContracts.isEmpty()) {
            throw new RestApiException(ApiStatus.BAD_REQUEST);
        }
    }
}
