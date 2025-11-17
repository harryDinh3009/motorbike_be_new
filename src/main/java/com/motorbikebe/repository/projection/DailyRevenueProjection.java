package com.motorbikebe.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyRevenueProjection {
    LocalDate getRevenueDate();
    BigDecimal getContractAmount();
    BigDecimal getRentalAmount();
    BigDecimal getSurchargeAmount();
}

