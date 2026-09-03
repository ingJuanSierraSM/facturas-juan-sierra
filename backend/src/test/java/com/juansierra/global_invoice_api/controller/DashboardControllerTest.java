package com.juansierra.global_invoice_api.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.juansierra.global_invoice_api.dto.response.DashboardResponse;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import com.juansierra.global_invoice_api.security.JwtAuthenticationFilter;
import com.juansierra.global_invoice_api.service.interfaces.DashboardService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = DashboardController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void shouldReturnInvoiceTotalsGroupedByType() throws Exception {
        when(dashboardService.getInvoicesByType()).thenReturn(List.of(
                new DashboardResponse(InvoiceType.NATIONAL, new BigDecimal("500000.00")),
                new DashboardResponse(InvoiceType.EXPORT, new BigDecimal("300000.00"))
        ));

        mockMvc.perform(get("/api/v1/dashboard/invoices-by-type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("NATIONAL"))
                .andExpect(jsonPath("$[0].totalAmount").value(500000))
                .andExpect(jsonPath("$[1].type").value("EXPORT"))
                .andExpect(jsonPath("$[1].totalAmount").value(300000));

        verify(dashboardService).getInvoicesByType();
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoInvoices() throws Exception {
        when(dashboardService.getInvoicesByType()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/dashboard/invoices-by-type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(dashboardService).getInvoicesByType();
    }
}
