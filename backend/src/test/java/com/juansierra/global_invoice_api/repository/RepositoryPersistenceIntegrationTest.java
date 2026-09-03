package com.juansierra.global_invoice_api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.juansierra.global_invoice_api.entity.Invoice;
import com.juansierra.global_invoice_api.entity.InvoiceTypeConfig;
import com.juansierra.global_invoice_api.entity.User;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import com.juansierra.global_invoice_api.enums.UserRole;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RepositoryPersistenceIntegrationTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceTypeConfigRepository invoiceTypeConfigRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndRecoverInvoiceWithAppliedAmounts() {
        User operator = findOperator();
        String invoiceNumber = nextInvoiceNumber();
        Invoice savedInvoice = invoiceRepository.saveAndFlush(buildGovernmentInvoice(invoiceNumber, operator));

        entityManager.clear();

        Invoice recoveredInvoice = invoiceRepository.findById(savedInvoice.getId()).orElseThrow();

        assertThat(recoveredInvoice.getInvoiceNumber()).isEqualTo(invoiceNumber);
        assertThat(recoveredInvoice.getType()).isEqualTo(InvoiceType.GOVERNMENT);
        assertThat(recoveredInvoice.getSubtotal()).isEqualByComparingTo("100.00");
        assertThat(recoveredInvoice.getVatRate()).isEqualByComparingTo("0.19");
        assertThat(recoveredInvoice.getTaxAmount()).isEqualByComparingTo("19.00");
        assertThat(recoveredInvoice.getWithholdingRate()).isEqualByComparingTo("0.05");
        assertThat(recoveredInvoice.getWithholdingAmount()).isEqualByComparingTo("5.00");
        assertThat(recoveredInvoice.getTotal()).isEqualByComparingTo("114.00");
        assertThat(recoveredInvoice.getCreatedAt()).isNotNull();
        assertThat(invoiceRepository.existsByInvoiceNumber(invoiceNumber)).isTrue();
        assertThat(invoiceRepository.findByInvoiceNumber(invoiceNumber).orElseThrow().getId())
                .isEqualTo(savedInvoice.getId());
    }

    @Test
    void shouldListPersistedInvoices() {
        User operator = findOperator();
        String firstInvoiceNumber = nextInvoiceNumber();
        String secondInvoiceNumber = nextInvoiceNumber();

        invoiceRepository.saveAndFlush(buildGovernmentInvoice(firstInvoiceNumber, operator));
        invoiceRepository.saveAndFlush(buildGovernmentInvoice(secondInvoiceNumber, operator));

        assertThat(invoiceRepository.findAll())
                .extracting(Invoice::getInvoiceNumber)
                .contains(firstInvoiceNumber, secondInvoiceNumber);
    }

    @Test
    void shouldEnforceUniqueInvoiceNumber() {
        User operator = findOperator();
        String invoiceNumber = nextInvoiceNumber();
        invoiceRepository.saveAndFlush(buildGovernmentInvoice(invoiceNumber, operator));

        assertThatThrownBy(() -> invoiceRepository.saveAndFlush(buildInvoice(
                invoiceNumber,
                InvoiceType.EXPORT,
                new BigDecimal("100.00"),
                operator
        )))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindActiveTaxConfiguration() {
        InvoiceTypeConfig configuration = invoiceTypeConfigRepository
                .findByTypeAndActiveTrue(InvoiceType.GOVERNMENT)
                .orElseThrow();

        assertThat(configuration.getVatRate()).isEqualByComparingTo("0.19");
        assertThat(configuration.getWithholdingRate()).isEqualByComparingTo("0.05");
    }

    @Test
    void shouldFindSeededUserByUsername() {
        User operator = findOperator();

        assertThat(operator.getRole()).isEqualTo(UserRole.OPERATOR);
        assertThat(operator.isEnabled()).isTrue();
    }

    @Test
    void shouldAggregateInvoiceTotalsByType() {
        User operator = findOperator();
        Map<InvoiceType, BigDecimal> totalsBefore = totalAmountsByType();

        invoiceRepository.saveAndFlush(buildInvoice(
                nextInvoiceNumber(),
                InvoiceType.NATIONAL,
                new BigDecimal("119.00"),
                operator
        ));
        invoiceRepository.saveAndFlush(buildInvoice(
                nextInvoiceNumber(),
                InvoiceType.NATIONAL,
                new BigDecimal("238.00"),
                operator
        ));
        invoiceRepository.saveAndFlush(buildInvoice(
                nextInvoiceNumber(),
                InvoiceType.EXPORT,
                new BigDecimal("100.00"),
                operator
        ));

        entityManager.clear();

        Map<InvoiceType, BigDecimal> totalsAfter = totalAmountsByType();

        assertThat(totalsAfter.get(InvoiceType.NATIONAL))
                .isEqualByComparingTo(totalsBefore.getOrDefault(InvoiceType.NATIONAL, BigDecimal.ZERO).add(new BigDecimal("357.00")));
        assertThat(totalsAfter.get(InvoiceType.EXPORT))
                .isEqualByComparingTo(totalsBefore.getOrDefault(InvoiceType.EXPORT, BigDecimal.ZERO).add(new BigDecimal("100.00")));
    }

    private User findOperator() {
        return userRepository.findByUsername("operator").orElseThrow();
    }

    private Invoice buildGovernmentInvoice(String invoiceNumber, User createdBy) {
        return Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .type(InvoiceType.GOVERNMENT)
                .subtotal(new BigDecimal("100.00"))
                .vatRate(new BigDecimal("0.19"))
                .taxAmount(new BigDecimal("19.00"))
                .withholdingRate(new BigDecimal("0.05"))
                .withholdingAmount(new BigDecimal("5.00"))
                .total(new BigDecimal("114.00"))
                .createdBy(createdBy)
                .build();
    }

    private Invoice buildInvoice(String invoiceNumber, InvoiceType type, BigDecimal total, User createdBy) {
        return Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .type(type)
                .subtotal(new BigDecimal("100.00"))
                .vatRate(new BigDecimal("0.19"))
                .taxAmount(new BigDecimal("19.00"))
                .withholdingRate(BigDecimal.ZERO)
                .withholdingAmount(BigDecimal.ZERO)
                .total(total)
                .createdBy(createdBy)
                .build();
    }

    private Map<InvoiceType, BigDecimal> totalAmountsByType() {
        return invoiceRepository.findTotalAmountsByType().stream()
                .collect(Collectors.toMap(
                        InvoiceTotalByTypeProjection::getType,
                        InvoiceTotalByTypeProjection::getTotalAmount
                ));
    }

    private String nextInvoiceNumber() {
        return "TEST-" + UUID.randomUUID();
    }
}
