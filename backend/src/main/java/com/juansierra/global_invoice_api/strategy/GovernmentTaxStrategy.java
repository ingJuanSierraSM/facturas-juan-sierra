package com.juansierra.global_invoice_api.strategy;

import com.juansierra.global_invoice_api.entity.InvoiceTypeConfig;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class GovernmentTaxStrategy implements TaxStrategy {

    @Override
    public InvoiceType getSupportedType() {
        return InvoiceType.GOVERNMENT;
    }

    @Override
    public TaxCalculation calculate(BigDecimal subtotal, InvoiceTypeConfig config) {
        validateInputs(subtotal, config);

        BigDecimal vatRate = config.getVatRate();
        BigDecimal taxAmount = toMoney(subtotal.multiply(vatRate));
        BigDecimal withholdingRate = config.getWithholdingRate();
        BigDecimal withholdingAmount = toMoney(subtotal.multiply(withholdingRate));
        BigDecimal total = toMoney(subtotal.add(taxAmount).subtract(withholdingAmount));

        return new TaxCalculation(vatRate, taxAmount, withholdingRate, withholdingAmount, total);
    }

    private void validateInputs(BigDecimal subtotal, InvoiceTypeConfig config) {
        Objects.requireNonNull(subtotal, "El subtotal es obligatorio");
        Objects.requireNonNull(config, "La configuracion tributaria es obligatoria");
        if (config.getType() != InvoiceType.GOVERNMENT) {
            throw new IllegalArgumentException("La configuracion tributaria no corresponde al tipo GOVERNMENT");
        }
    }

    private BigDecimal toMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
