package com.juansierra.global_invoice_api.repository;

import com.juansierra.global_invoice_api.entity.Invoice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    @Query("""
            SELECT invoice.type AS type, SUM(invoice.total) AS totalAmount
            FROM Invoice invoice
            GROUP BY invoice.type
            """)
    java.util.List<InvoiceTotalByTypeProjection> findTotalAmountsByType();
}
