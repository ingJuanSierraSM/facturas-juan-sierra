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
public class InvoiceDetailResponse {

    private Long id;
    private String invoiceNumber;
    private InvoiceType type;
    private BigDecimal subtotal;
    private BigDecimal vatRate;
    private BigDecimal taxAmount;
    private BigDecimal withholdingRate;
    private BigDecimal withholdingAmount;
    private BigDecimal total;
    private String customsCode;
    private LocalDateTime createdAt;
    private String createdByUsername;
    private String totalInWords;
}
