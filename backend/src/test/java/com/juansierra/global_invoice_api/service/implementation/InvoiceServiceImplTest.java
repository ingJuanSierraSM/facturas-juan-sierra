package com.juansierra.global_invoice_api.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.juansierra.global_invoice_api.dto.request.CreateInvoiceRequest;
import com.juansierra.global_invoice_api.dto.response.InvoiceDetailResponse;
import com.juansierra.global_invoice_api.dto.response.InvoiceResponse;
import com.juansierra.global_invoice_api.entity.Invoice;
import com.juansierra.global_invoice_api.entity.InvoiceTypeConfig;
import com.juansierra.global_invoice_api.entity.User;
import com.juansierra.global_invoice_api.enums.InvoiceType;
import com.juansierra.global_invoice_api.enums.UserRole;
import com.juansierra.global_invoice_api.exception.DuplicateInvoiceException;
import com.juansierra.global_invoice_api.exception.InvoiceNotFoundException;
import com.juansierra.global_invoice_api.integration.LegacyServiceException;
import com.juansierra.global_invoice_api.integration.NumberConversionClient;
import com.juansierra.global_invoice_api.mapper.InvoiceMapper;
import com.juansierra.global_invoice_api.repository.InvoiceRepository;
import com.juansierra.global_invoice_api.repository.InvoiceTypeConfigRepository;
import com.juansierra.global_invoice_api.security.AuthenticatedUserService;
import com.juansierra.global_invoice_api.strategy.TaxCalculation;
import com.juansierra.global_invoice_api.strategy.TaxStrategy;
import com.juansierra.global_invoice_api.strategy.TaxStrategyResolver;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceTypeConfigRepository invoiceTypeConfigRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private TaxStrategyResolver taxStrategyResolver;

    @Mock
    private TaxStrategy taxStrategy;

    @Mock
    private NumberConversionClient numberConversionClient;

    private InvoiceServiceImpl invoiceService;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceServiceImpl(
                invoiceRepository,
                invoiceTypeConfigRepository,
                authenticatedUserService,
                invoiceMapper,
                taxStrategyResolver,
                numberConversionClient
        );
    }

    @Test
    void shouldCreateInvoiceWithCalculatedAmounts() {
        CreateInvoiceRequest request = createRequest();
        InvoiceTypeConfig taxConfiguration = governmentConfiguration();
        TaxCalculation calculation = governmentCalculation();
        User operator = operator();
        Invoice invoice = Invoice.builder().invoiceNumber("INV-001").build();
        Invoice savedInvoice = Invoice.builder().id(1L).invoiceNumber("INV-001").build();
        InvoiceResponse expectedResponse = new InvoiceResponse(
                1L,
                "INV-001",
                InvoiceType.GOVERNMENT,
                new BigDecimal("100.00"),
                new BigDecimal("114.00"),
                LocalDateTime.of(2026, 9, 2, 20, 0)
        );

        when(invoiceTypeConfigRepository.findByTypeAndActiveTrue(InvoiceType.GOVERNMENT))
                .thenReturn(Optional.of(taxConfiguration));
        when(taxStrategyResolver.resolve(InvoiceType.GOVERNMENT)).thenReturn(taxStrategy);
        when(taxStrategy.calculate(request.getSubtotal(), taxConfiguration)).thenReturn(calculation);
        when(authenticatedUserService.getCurrentUser()).thenReturn(operator);
        when(invoiceMapper.toEntity(request)).thenReturn(invoice);
        when(invoiceRepository.save(invoice)).thenReturn(savedInvoice);
        when(invoiceMapper.toResponse(savedInvoice)).thenReturn(expectedResponse);

        InvoiceResponse result = invoiceService.createInvoice(request);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice invoiceToPersist = invoiceCaptor.getValue();

        assertThat(result).isSameAs(expectedResponse);
        assertThat(invoiceToPersist.getVatRate()).isEqualByComparingTo("0.19");
        assertThat(invoiceToPersist.getTaxAmount()).isEqualByComparingTo("19.00");
        assertThat(invoiceToPersist.getWithholdingRate()).isEqualByComparingTo("0.05");
        assertThat(invoiceToPersist.getWithholdingAmount()).isEqualByComparingTo("5.00");
        assertThat(invoiceToPersist.getTotal()).isEqualByComparingTo("114.00");
        assertThat(invoiceToPersist.getCreatedBy()).isSameAs(operator);
        verify(authenticatedUserService).getCurrentUser();
    }

    @Test
    void shouldRejectInvoiceCreationWhenTaxConfigurationIsInactiveOrMissing() {
        CreateInvoiceRequest request = createRequest();

        when(invoiceTypeConfigRepository.findByTypeAndActiveTrue(InvoiceType.GOVERNMENT))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.createInvoice(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("configuracion tributaria activa");

        verify(taxStrategyResolver, never()).resolve(any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void shouldRejectInvoiceCreationWhenInvoiceNumberAlreadyExists() {
        CreateInvoiceRequest request = createRequest();

        when(invoiceRepository.existsByInvoiceNumber("INV-001")).thenReturn(true);

        assertThatThrownBy(() -> invoiceService.createInvoice(request))
                .isInstanceOf(DuplicateInvoiceException.class)
                .hasMessage("Ya existe una factura con el numero INV-001");

        verify(invoiceRepository).existsByInvoiceNumber("INV-001");
        verify(invoiceRepository, never()).save(any());
        verifyNoInteractions(
                invoiceTypeConfigRepository,
                authenticatedUserService,
                invoiceMapper,
                taxStrategyResolver,
                taxStrategy,
                numberConversionClient
        );
    }

    @Test
    void shouldListInvoices() {
        Invoice firstInvoice = Invoice.builder().id(1L).invoiceNumber("INV-001").build();
        Invoice secondInvoice = Invoice.builder().id(2L).invoiceNumber("INV-002").build();
        InvoiceResponse firstResponse = new InvoiceResponse();
        InvoiceResponse secondResponse = new InvoiceResponse();

        when(invoiceRepository.findAll()).thenReturn(List.of(firstInvoice, secondInvoice));
        when(invoiceMapper.toResponse(firstInvoice)).thenReturn(firstResponse);
        when(invoiceMapper.toResponse(secondInvoice)).thenReturn(secondResponse);

        List<InvoiceResponse> result = invoiceService.findAll();

        assertThat(result).containsExactly(firstResponse, secondResponse);
        verify(invoiceMapper).toResponse(firstInvoice);
        verify(invoiceMapper).toResponse(secondInvoice);
        verifyNoInteractions(numberConversionClient);
    }

    @Test
    void shouldFindInvoiceDetailById() {
        Invoice invoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .total(new BigDecimal("114.00"))
                .build();
        InvoiceDetailResponse expectedResponse = new InvoiceDetailResponse();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(numberConversionClient.convertToWords(new BigDecimal("114.00")))
                .thenReturn("one hundred fourteen and zero cents");
        when(invoiceMapper.toDetailResponse(invoice, "one hundred fourteen and zero cents"))
                .thenReturn(expectedResponse);

        InvoiceDetailResponse result = invoiceService.findById(1L);

        assertThat(result).isSameAs(expectedResponse);
        verify(numberConversionClient).convertToWords(new BigDecimal("114.00"));
        verify(invoiceMapper).toDetailResponse(invoice, "one hundred fourteen and zero cents");
    }

    @Test
    void shouldPropagateLegacyServiceExceptionWhenConvertingInvoiceTotal() {
        Invoice invoice = Invoice.builder().id(1L).total(new BigDecimal("114.00")).build();
        LegacyServiceException exception = new LegacyServiceException("Proveedor no disponible");

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(numberConversionClient.convertToWords(new BigDecimal("114.00"))).thenThrow(exception);

        assertThatThrownBy(() -> invoiceService.findById(1L))
                .isSameAs(exception);

        verify(invoiceMapper, never()).toDetailResponse(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenInvoiceDoesNotExist() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.findById(99L))
                .isInstanceOf(InvoiceNotFoundException.class)
                .hasMessageContaining("99");
    }

    private CreateInvoiceRequest createRequest() {
        return new CreateInvoiceRequest(
                "INV-001",
                InvoiceType.GOVERNMENT,
                new BigDecimal("100.00"),
                null
        );
    }

    private InvoiceTypeConfig governmentConfiguration() {
        return InvoiceTypeConfig.builder()
                .type(InvoiceType.GOVERNMENT)
                .vatRate(new BigDecimal("0.19"))
                .withholdingRate(new BigDecimal("0.05"))
                .active(true)
                .build();
    }

    private TaxCalculation governmentCalculation() {
        return new TaxCalculation(
                new BigDecimal("0.19"),
                new BigDecimal("19.00"),
                new BigDecimal("0.05"),
                new BigDecimal("5.00"),
                new BigDecimal("114.00")
        );
    }

    private User operator() {
        return User.builder()
                .id(1L)
                .username("operator")
                .passwordHash("<BCRYPT_HASH_OPERATOR>")
                .role(UserRole.OPERATOR)
                .enabled(true)
                .build();
    }
}
