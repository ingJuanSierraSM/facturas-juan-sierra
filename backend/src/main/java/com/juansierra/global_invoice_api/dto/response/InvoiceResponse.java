package com.juansierra.global_invoice_api.dto.response;

import com.juansierra.global_invoice_api.enums.InvoiceType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private InvoiceType type;
    private BigDecimal subtotal;
    private BigDecimal total;
    private LocalDateTime createdAt;
}
