package com.juansierra.global_invoice_api.config;

import com.juansierra.global_invoice_api.entity.InvoiceTypeConfig;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import com.juansierra.global_invoice_api.repository.InvoiceTypeConfigRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitialDataConfig {

    @Bean
    CommandLineRunner seedInvoiceTypeConfigs(InvoiceTypeConfigRepository repository) {
        return args -> {
            List<InvoiceTypeSeed> seeds = List.of(
                    new InvoiceTypeSeed(InvoiceType.NATIONAL, new BigDecimal("0.19"), BigDecimal.ZERO),
                    new InvoiceTypeSeed(InvoiceType.EXPORT, BigDecimal.ZERO, BigDecimal.ZERO),
                    new InvoiceTypeSeed(InvoiceType.GOVERNMENT, new BigDecimal("0.19"), new BigDecimal("0.05"))
            );

            for (InvoiceTypeSeed seed : seeds) {
                if (!repository.existsByType(seed.type())) {
                    repository.save(InvoiceTypeConfig.builder()
                            .type(seed.type())
                            .vatRate(seed.vatRate())
                            .withholdingRate(seed.withholdingRate())
                            .active(true)
                            .build());
                }
            }
        };
    }

    private record InvoiceTypeSeed(InvoiceType type, BigDecimal vatRate, BigDecimal withholdingRate) {
    }
}
