package com.juansierra.global_invoice_api.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.juansierra.global_invoice_api.enums.InvoiceType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CreateInvoiceRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldPassValidationWhenExportHasCustomsCode() {
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                "INV-001",
                InvoiceType.EXPORT,
                new BigDecimal("100.00"),
                "EXP-CO-001"
        );

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenExportHasNoCustomsCode() {
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                "INV-001",
                InvoiceType.EXPORT,
                new BigDecimal("100.00"),
                null
        );

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(violation -> "customsCodeValid".contentEquals(violation.getPropertyPath().toString()));
    }

    @Test
    void shouldFailValidationWhenExportHasBlankCustomsCode() {
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                "INV-001",
                InvoiceType.EXPORT,
                new BigDecimal("100.00"),
                "   "
        );

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(violation -> "customsCodeValid".contentEquals(violation.getPropertyPath().toString()));
    }

    @Test
    void shouldFailValidationWhenNationalHasCustomsCode() {
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                "INV-001",
                InvoiceType.NATIONAL,
                new BigDecimal("100.00"),
                "EXP-CO-001"
        );

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(violation -> "customsCodeValid".contentEquals(violation.getPropertyPath().toString()));
    }

    @Test
    void shouldFailValidationWhenGovernmentHasCustomsCode() {
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                "INV-001",
                InvoiceType.GOVERNMENT,
                new BigDecimal("100.00"),
                "EXP-CO-001"
        );

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(violation -> "customsCodeValid".contentEquals(violation.getPropertyPath().toString()));
    }

    @Test
    void shouldFailValidationWhenRequiredFieldsAreMissing() {
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                "",
                null,
                null,
                null
        );

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("invoiceNumber", "type", "subtotal");
    }

    @Test
    void shouldFailValidationWhenSubtotalIsNotPositive() {
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                "INV-001",
                InvoiceType.NATIONAL,
                BigDecimal.ZERO,
                null
        );

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("subtotal");
    }
}
