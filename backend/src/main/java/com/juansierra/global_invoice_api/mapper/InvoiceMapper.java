package com.juansierra.global_invoice_api.mapper;

import com.juansierra.global_invoice_api.dto.request.CreateInvoiceRequest;
import com.juansierra.global_invoice_api.dto.response.InvoiceDetailResponse;
import com.juansierra.global_invoice_api.dto.response.InvoiceResponse;
import com.juansierra.global_invoice_api.entity.Invoice;
import com.juansierra.global_invoice_api.entity.User;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public Invoice toEntity(CreateInvoiceRequest request) {
        return Invoice.builder()
                .invoiceNumber(request.getInvoiceNumber())
                .type(request.getType())
                .subtotal(request.getSubtotal())
                .customsCode(request.getCustomsCode())
                .build();
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getType(),
                invoice.getSubtotal(),
                invoice.getTotal(),
                invoice.getCreatedAt()
        );
    }

    public InvoiceDetailResponse toDetailResponse(Invoice invoice, String totalInWords) {
        return new InvoiceDetailResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getType(),
                invoice.getSubtotal(),
                invoice.getVatRate(),
                invoice.getTaxAmount(),
                invoice.getWithholdingRate(),
                invoice.getWithholdingAmount(),
                invoice.getTotal(),
                invoice.getCustomsCode(),
                invoice.getCreatedAt(),
                getCreatedByUsername(invoice),
                totalInWords
        );
    }

    private String getCreatedByUsername(Invoice invoice) {
        User createdBy = invoice.getCreatedBy();
        return createdBy != null ? createdBy.getUsername() : null;
    }
}
