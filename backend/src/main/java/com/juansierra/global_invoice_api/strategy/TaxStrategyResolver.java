package com.juansierra.global_invoice_api.strategy;

import com.juansierra.global_invoice_api.enums.InvoiceType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TaxStrategyResolver {

    private final Map<InvoiceType, TaxStrategy> strategies;

    public TaxStrategyResolver(List<TaxStrategy> strategies) {
        this.strategies = new EnumMap<>(InvoiceType.class);

        for (TaxStrategy strategy : strategies) {
            TaxStrategy previous = this.strategies.put(strategy.getSupportedType(), strategy);
            if (previous != null) {
                throw new IllegalStateException("Estrategia tributaria duplicada para " + strategy.getSupportedType());
            }
        }
    }

    public TaxStrategy resolve(InvoiceType type) {
        return Optional.ofNullable(strategies.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No existe estrategia tributaria para " + type));
    }
}
