package com.juansierra.global_invoice_api.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.juansierra.global_invoice_api.entity.InvoiceTypeConfig;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class NationalTaxStrategyTest {

    private final NationalTaxStrategy strategy = new NationalTaxStrategy();

    @Test
    void shouldCalculateNationalTax() {
        TaxCalculation calculation = strategy.calculate(new BigDecimal("100.00"), config());

        assertThat(strategy.getSupportedType()).isEqualTo(InvoiceType.NATIONAL);
        assertThat(calculation.getVatRate()).isEqualByComparingTo("0.19");
        assertThat(calculation.getTaxAmount()).isEqualByComparingTo("19.00");
        assertThat(calculation.getWithholdingRate()).isEqualByComparingTo("0.00");
        assertThat(calculation.getWithholdingAmount()).isEqualByComparingTo("0.00");
        assertThat(calculation.getTotal()).isEqualByComparingTo("119.00");
    }

    @Test
    void shouldRejectTaxConfigFromAnotherInvoiceType() {
        InvoiceTypeConfig exportConfig = InvoiceTypeConfig.builder()
                .type(InvoiceType.EXPORT)
                .vatRate(new BigDecimal("0.00"))
                .withholdingRate(new BigDecimal("0.00"))
                .active(true)
                .build();

        assertThatThrownBy(() -> strategy.calculate(new BigDecimal("100.00"), exportConfig))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NATIONAL");
    }

    @Test
    void shouldNotApplyWithholdingForNationalInvoices() {
        InvoiceTypeConfig configWithWithholding = InvoiceTypeConfig.builder()
                .type(InvoiceType.NATIONAL)
                .vatRate(new BigDecimal("0.19"))
                .withholdingRate(new BigDecimal("0.05"))
                .active(true)
                .build();

        TaxCalculation calculation = strategy.calculate(new BigDecimal("100.00"), configWithWithholding);

        assertThat(calculation.getWithholdingRate()).isEqualByComparingTo("0.00");
        assertThat(calculation.getWithholdingAmount()).isEqualByComparingTo("0.00");
        assertThat(calculation.getTotal()).isEqualByComparingTo("119.00");
    }

    private InvoiceTypeConfig config() {
        return InvoiceTypeConfig.builder()
                .type(InvoiceType.NATIONAL)
                .vatRate(new BigDecimal("0.19"))
                .withholdingRate(new BigDecimal("0.00"))
                .active(true)
                .build();
    }
}
