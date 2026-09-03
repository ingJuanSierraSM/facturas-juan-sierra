package com.juansierra.global_invoice_api.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.juansierra.global_invoice_api.entity.InvoiceTypeConfig;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class GovernmentTaxStrategyTest {

    private final GovernmentTaxStrategy strategy = new GovernmentTaxStrategy();

    @Test
    void shouldCalculateGovernmentTax() {
        TaxCalculation calculation = strategy.calculate(new BigDecimal("100.00"), config());

        assertThat(strategy.getSupportedType()).isEqualTo(InvoiceType.GOVERNMENT);
        assertThat(calculation.getVatRate()).isEqualByComparingTo("0.19");
        assertThat(calculation.getTaxAmount()).isEqualByComparingTo("19.00");
        assertThat(calculation.getWithholdingRate()).isEqualByComparingTo("0.05");
        assertThat(calculation.getWithholdingAmount()).isEqualByComparingTo("5.00");
        assertThat(calculation.getTotal()).isEqualByComparingTo("114.00");
    }

    private InvoiceTypeConfig config() {
        return InvoiceTypeConfig.builder()
                .type(InvoiceType.GOVERNMENT)
                .vatRate(new BigDecimal("0.19"))
                .withholdingRate(new BigDecimal("0.05"))
                .active(true)
                .build();
    }
}
