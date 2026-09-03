package com.juansierra.global_invoice_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.juansierra.global_invoice_api.dto.request.CreateInvoiceRequest;
import com.juansierra.global_invoice_api.dto.response.InvoiceDetailResponse;
import com.juansierra.global_invoice_api.dto.response.InvoiceResponse;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import com.juansierra.global_invoice_api.exception.InvoiceNotFoundException;
import com.juansierra.global_invoice_api.integration.LegacyServiceException;
import com.juansierra.global_invoice_api.security.JwtAuthenticationFilter;
import com.juansierra.global_invoice_api.service.interfaces.InvoiceService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = InvoiceController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvoiceService invoiceService;

    @Test
    void shouldCreateInvoice() throws Exception {
        InvoiceResponse response = invoiceResponse(1L, "INV-001", new BigDecimal("119000.00"));
        when(invoiceService.createInvoice(any(CreateInvoiceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invoiceNumber": "INV-001",
                                  "type": "NATIONAL",
                                  "subtotal": 100000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.invoiceNumber").value("INV-001"))
                .andExpect(jsonPath("$.type").value("NATIONAL"))
                .andExpect(jsonPath("$.subtotal").value(100000))
                .andExpect(jsonPath("$.total").value(119000));

        verify(invoiceService).createInvoice(any(CreateInvoiceRequest.class));
    }

    @Test
    void shouldListInvoices() throws Exception {
        when(invoiceService.findAll()).thenReturn(List.of(
                invoiceResponse(1L, "INV-001", new BigDecimal("119000.00")),
                invoiceResponse(2L, "INV-002", new BigDecimal("100000.00"))
        ));

        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoiceNumber").value("INV-001"))
                .andExpect(jsonPath("$[1].invoiceNumber").value("INV-002"));

        verify(invoiceService).findAll();
    }

    @Test
    void shouldFindInvoiceById() throws Exception {
        InvoiceDetailResponse response = new InvoiceDetailResponse(
                1L,
                "INV-001",
                InvoiceType.GOVERNMENT,
                new BigDecimal("100000.00"),
                new BigDecimal("0.19"),
                new BigDecimal("19000.00"),
                new BigDecimal("0.05"),
                new BigDecimal("5000.00"),
                new BigDecimal("114000.00"),
                null,
                LocalDateTime.of(2026, 9, 2, 20, 0),
                "operator",
                null
        );
        when(invoiceService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/invoices/{invoiceId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("GOVERNMENT"))
                .andExpect(jsonPath("$.total").value(114000))
                .andExpect(jsonPath("$.createdByUsername").value("operator"));

        verify(invoiceService).findById(1L);
    }

    @Test
    void shouldReturnBadRequestForInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invoiceNumber": "",
                                  "type": "EXPORT",
                                  "subtotal": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("La solicitud contiene datos invalidos"))
                .andExpect(jsonPath("$.errors.invoiceNumber").exists())
                .andExpect(jsonPath("$.errors.subtotal").exists());

        verifyNoInteractions(invoiceService);
    }

    @Test
    void shouldReturnBadRequestWhenExportInvoiceDoesNotIncludeCustomsCode() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invoiceNumber": "INV-EXPORT-001",
                                  "type": "EXPORT",
                                  "subtotal": 100000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.customsCodeValid").exists());

        verifyNoInteractions(invoiceService);
    }

    @Test
    void shouldReturnNotFoundWhenInvoiceDoesNotExist() throws Exception {
        when(invoiceService.findById(99L)).thenThrow(new InvoiceNotFoundException(99L));

        mockMvc.perform(get("/api/v1/invoices/{invoiceId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No existe una factura con el id 99"));
    }

    @Test
    void shouldReturnBadGatewayWhenNumberConversionServiceFails() throws Exception {
        when(invoiceService.findById(1L))
                .thenThrow(new LegacyServiceException("No fue posible convertir el total de la factura a letras"));

        mockMvc.perform(get("/api/v1/invoices/{invoiceId}", 1L))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("No fue posible convertir el total de la factura a letras"));
    }

    private InvoiceResponse invoiceResponse(Long id, String invoiceNumber, BigDecimal total) {
        return new InvoiceResponse(
                id,
                invoiceNumber,
                InvoiceType.NATIONAL,
                new BigDecimal("100000.00"),
                total,
                LocalDateTime.of(2026, 9, 2, 20, 0)
        );
    }
}
