package com.juansierra.global_invoice_api.strategy;

import com.juansierra.global_invoice_api.entity.InvoiceTypeConfig;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class ExportTaxStrategy implements TaxStrategy {

    @Override
    public InvoiceType getSupportedType() {
        return InvoiceType.EXPORT;
    }

    @Override
    public TaxCalculation calculate(BigDecimal subtotal, InvoiceTypeConfig config) {
        validateInputs(subtotal, config);

        BigDecimal vatRate = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal withholdingRate = BigDecimal.ZERO;
        BigDecimal withholdingAmount = BigDecimal.ZERO;
        BigDecimal total = subtotal.setScale(2, RoundingMode.HALF_UP);

        return new TaxCalculation(vatRate, taxAmount, withholdingRate, withholdingAmount, total);
    }

    private void validateInputs(BigDecimal subtotal, InvoiceTypeConfig config) {
        Objects.requireNonNull(subtotal, "El subtotal es obligatorio");
        Objects.requireNonNull(config, "La configuracion tributaria es obligatoria");
        if (config.getType() != InvoiceType.EXPORT) {
            throw new IllegalArgumentException("La configuracion tributaria no corresponde al tipo EXPORT");
        }
    }
}
