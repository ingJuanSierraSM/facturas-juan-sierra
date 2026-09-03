package com.juansierra.global_invoice_api.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.juansierra.global_invoice_api.enums.InvoiceType;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaxStrategyResolverTest {

    @Test
    void shouldResolveStrategyByInvoiceType() {
        TaxStrategyResolver resolver = new TaxStrategyResolver(List.of(
                new NationalTaxStrategy(),
                new ExportTaxStrategy(),
                new GovernmentTaxStrategy()
        ));

        assertThat(resolver.resolve(InvoiceType.NATIONAL)).isInstanceOf(NationalTaxStrategy.class);
        assertThat(resolver.resolve(InvoiceType.EXPORT)).isInstanceOf(ExportTaxStrategy.class);
        assertThat(resolver.resolve(InvoiceType.GOVERNMENT)).isInstanceOf(GovernmentTaxStrategy.class);
    }

    @Test
    void shouldRejectDuplicatedStrategiesForSameInvoiceType() {
        assertThatThrownBy(() -> new TaxStrategyResolver(List.of(
                new NationalTaxStrategy(),
                new NationalTaxStrategy()
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estrategia tributaria duplicada");
    }
}
