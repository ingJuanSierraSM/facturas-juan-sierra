package com.juansierra.global_invoice_api.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.juansierra.global_invoice_api.entity.InvoiceTypeConfig;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ExportTaxStrategyTest {

    private final ExportTaxStrategy strategy = new ExportTaxStrategy();

    @Test
    void shouldCalculateExportTax() {
        TaxCalculation calculation = strategy.calculate(new BigDecimal("100.00"), config());

        assertThat(strategy.getSupportedType()).isEqualTo(InvoiceType.EXPORT);
        assertThat(calculation.getVatRate()).isEqualByComparingTo("0.00");
        assertThat(calculation.getTaxAmount()).isEqualByComparingTo("0.00");
        assertThat(calculation.getWithholdingRate()).isEqualByComparingTo("0.00");
        assertThat(calculation.getWithholdingAmount()).isEqualByComparingTo("0.00");
        assertThat(calculation.getTotal()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldNotApplyConfiguredTaxesForExportInvoices() {
        InvoiceTypeConfig configWithTaxes = InvoiceTypeConfig.builder()
                .type(InvoiceType.EXPORT)
                .vatRate(new BigDecimal("0.19"))
                .withholdingRate(new BigDecimal("0.05"))
                .active(true)
                .build();

        TaxCalculation calculation = strategy.calculate(new BigDecimal("100.00"), configWithTaxes);

        assertThat(calculation.getVatRate()).isEqualByComparingTo("0.00");
        assertThat(calculation.getTaxAmount()).isEqualByComparingTo("0.00");
        assertThat(calculation.getWithholdingRate()).isEqualByComparingTo("0.00");
        assertThat(calculation.getWithholdingAmount()).isEqualByComparingTo("0.00");
        assertThat(calculation.getTotal()).isEqualByComparingTo("100.00");
    }

    private InvoiceTypeConfig config() {
        return InvoiceTypeConfig.builder()
                .type(InvoiceType.EXPORT)
                .vatRate(new BigDecimal("0.00"))
                .withholdingRate(new BigDecimal("0.00"))
                .active(true)
                .build();
    }
}
