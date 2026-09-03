package com.juansierra.global_invoice_api.repository;

import com.juansierra.global_invoice_api.enums.InvoiceType;
import java.math.BigDecimal;

public interface InvoiceTotalByTypeProjection {

    InvoiceType getType();

    BigDecimal getTotalAmount();
}
