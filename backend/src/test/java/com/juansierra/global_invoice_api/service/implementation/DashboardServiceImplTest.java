package com.juansierra.global_invoice_api.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.juansierra.global_invoice_api.dto.response.DashboardResponse;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import com.juansierra.global_invoice_api.repository.InvoiceRepository;
import com.juansierra.global_invoice_api.repository.InvoiceTotalByTypeProjection;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Test
    void shouldReturnTotalAmountsByInvoiceType() {
        DashboardServiceImpl dashboardService = new DashboardServiceImpl(invoiceRepository);
        when(invoiceRepository.findTotalAmountsByType()).thenReturn(List.of(
                totalByType(InvoiceType.NATIONAL, "500000.00"),
                totalByType(InvoiceType.EXPORT, "300000.00")
        ));

        List<DashboardResponse> result = dashboardService.getInvoicesByType();

        assertThat(result)
                .extracting(DashboardResponse::getType, DashboardResponse::getTotalAmount)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(InvoiceType.NATIONAL, new BigDecimal("500000.00")),
                        org.assertj.core.groups.Tuple.tuple(InvoiceType.EXPORT, new BigDecimal("300000.00"))
                );
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoInvoices() {
        DashboardServiceImpl dashboardService = new DashboardServiceImpl(invoiceRepository);
        when(invoiceRepository.findTotalAmountsByType()).thenReturn(List.of());

        assertThat(dashboardService.getInvoicesByType()).isEmpty();
    }

    private InvoiceTotalByTypeProjection totalByType(InvoiceType type, String totalAmount) {
        return new InvoiceTotalByTypeProjection() {
            @Override
            public InvoiceType getType() {
                return type;
            }

            @Override
            public BigDecimal getTotalAmount() {
                return new BigDecimal(totalAmount);
            }
        };
    }
}
