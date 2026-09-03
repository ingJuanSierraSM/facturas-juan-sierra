package com.juansierra.global_invoice_api.repository;

import com.juansierra.global_invoice_api.entity.InvoiceTypeConfig;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceTypeConfigRepository extends JpaRepository<InvoiceTypeConfig, Long> {

    Optional<InvoiceTypeConfig> findByType(InvoiceType type);

    Optional<InvoiceTypeConfig> findByTypeAndActiveTrue(InvoiceType type);

    boolean existsByType(InvoiceType type);
}
