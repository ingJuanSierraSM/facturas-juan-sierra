package com.juansierra.global_invoice_api.strategy;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaxCalculation {

    private BigDecimal vatRate;
    private BigDecimal taxAmount;
    private BigDecimal withholdingRate;
    private BigDecimal withholdingAmount;
    private BigDecimal total;
}
