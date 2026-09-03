package com.juansierra.global_invoice_api.dto.response;

import com.juansierra.global_invoice_api.enums.InvoiceType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        InvoiceType type,
        BigDecimal subtotal,
        BigDecimal total,
        LocalDateTime createdAt
) {
}
