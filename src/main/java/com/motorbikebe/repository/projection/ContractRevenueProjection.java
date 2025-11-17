package com.motorbikebe.repository.projection;

import java.math.BigDecimal;

public interface ContractRevenueProjection {
    BigDecimal getContractAmount();
    BigDecimal getRentalAmount();
    BigDecimal getSurchargeAmount();
}

