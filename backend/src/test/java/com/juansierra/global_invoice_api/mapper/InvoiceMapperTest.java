package com.juansierra.global_invoice_api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.juansierra.global_invoice_api.dto.request.CreateInvoiceRequest;
import com.juansierra.global_invoice_api.dto.response.InvoiceDetailResponse;
import com.juansierra.global_invoice_api.dto.response.InvoiceResponse;
import com.juansierra.global_invoice_api.entity.Invoice;
import com.juansierra.global_invoice_api.entity.User;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import com.juansierra.global_invoice_api.enums.UserRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class InvoiceMapperTest {

    private final InvoiceMapper mapper = new InvoiceMapper();

    @Test
    void shouldMapCreateRequestToEntityWithoutTaxCalculations() {
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                "INV-001",
                InvoiceType.EXPORT,
                new BigDecimal("100.00"),
                "EXP-CO-001"
        );

        Invoice invoice = mapper.toEntity(request);

        assertThat(invoice.getInvoiceNumber()).isEqualTo("INV-001");
        assertThat(invoice.getType()).isEqualTo(InvoiceType.EXPORT);
        assertThat(invoice.getSubtotal()).isEqualByComparingTo("100.00");
        assertThat(invoice.getCustomsCode()).isEqualTo("EXP-CO-001");
        assertThat(invoice.getVatRate()).isNull();
        assertThat(invoice.getTaxAmount()).isNull();
        assertThat(invoice.getWithholdingRate()).isNull();
        assertThat(invoice.getWithholdingAmount()).isNull();
        assertThat(invoice.getTotal()).isNull();
    }

    @Test
    void shouldMapEntityToResponse() {
        Invoice invoice = buildInvoice();

        InvoiceResponse response = mapper.toResponse(invoice);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getInvoiceNumber()).isEqualTo("INV-001");
        assertThat(response.getType()).isEqualTo(InvoiceType.NATIONAL);
        assertThat(response.getSubtotal()).isEqualByComparingTo("100.00");
        assertThat(response.getTotal()).isEqualByComparingTo("119.00");
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 9, 2, 20, 0));
        assertThat(Arrays.stream(InvoiceResponse.class.getDeclaredFields())
                .map(field -> field.getName()))
                .containsExactly("id", "invoiceNumber", "type", "subtotal", "total", "createdAt");
    }

    @Test
    void shouldMapEntityToDetailResponseWithTotalInWords() {
        Invoice invoice = buildInvoice();

        InvoiceDetailResponse response = mapper.toDetailResponse(invoice, "ciento diecinueve pesos");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getInvoiceNumber()).isEqualTo("INV-001");
        assertThat(response.getTotal()).isEqualByComparingTo("119.00");
        assertThat(response.getTotalInWords()).isEqualTo("ciento diecinueve pesos");
        assertThat(response.getCreatedByUsername()).isEqualTo("operator");
    }

    private Invoice buildInvoice() {
        User operator = User.builder()
                .id(1L)
                .username("operator")
                .passwordHash("<BCRYPT_HASH_OPERATOR>")
                .role(UserRole.OPERATOR)
                .enabled(true)
                .createdAt(LocalDateTime.of(2026, 9, 2, 19, 0))
                .build();

        return Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .type(InvoiceType.NATIONAL)
                .subtotal(new BigDecimal("100.00"))
                .vatRate(new BigDecimal("0.19"))
                .taxAmount(new BigDecimal("19.00"))
                .withholdingRate(new BigDecimal("0.00"))
                .withholdingAmount(new BigDecimal("0.00"))
                .total(new BigDecimal("119.00"))
                .customsCode(null)
                .createdAt(LocalDateTime.of(2026, 9, 2, 20, 0))
                .createdBy(operator)
                .build();
    }
}
