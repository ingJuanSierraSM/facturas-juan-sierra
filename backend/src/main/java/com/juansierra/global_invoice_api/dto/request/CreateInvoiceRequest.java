package com.juansierra.global_invoice_api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateInvoiceRequest(
        @NotBlank(message = "El numero de factura es obligatorio")
        String invoiceNumber,

        @NotNull(message = "El tipo de factura es obligatorio")
        InvoiceType type,

        @NotNull(message = "El subtotal es obligatorio")
        @Positive(message = "El subtotal debe ser mayor que cero")
        BigDecimal subtotal,

        String customsCode
) {

    @JsonIgnore
    @AssertTrue(message = "El codigo de aduana es obligatorio solo para facturas de exportacion")
    public boolean isCustomsCodeValid() {
        if (type == null) {
            return true;
        }

        boolean hasCustomsCode = customsCode != null && !customsCode.isBlank();

        if (type == InvoiceType.EXPORT) {
            return hasCustomsCode;
        }

        return !hasCustomsCode;
    }
}
