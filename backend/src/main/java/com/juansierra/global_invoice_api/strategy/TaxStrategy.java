package com.juansierra.global_invoice_api.strategy;

import com.juansierra.global_invoice_api.entity.InvoiceTypeConfig;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import java.math.BigDecimal;

public interface TaxStrategy {

    InvoiceType getSupportedType();

    TaxCalculation calculate(BigDecimal subtotal, InvoiceTypeConfig config);
}
