package com.juansierra.global_invoice_api.service.implementation;

import com.juansierra.global_invoice_api.dto.request.CreateInvoiceRequest;
import com.juansierra.global_invoice_api.dto.response.InvoiceDetailResponse;
import com.juansierra.global_invoice_api.dto.response.InvoiceResponse;
import com.juansierra.global_invoice_api.entity.Invoice;
import com.juansierra.global_invoice_api.entity.InvoiceTypeConfig;
import com.juansierra.global_invoice_api.entity.User;
import com.juansierra.global_invoice_api.exception.InvoiceNotFoundException;
import com.juansierra.global_invoice_api.integration.NumberConversionClient;
import com.juansierra.global_invoice_api.mapper.InvoiceMapper;
import com.juansierra.global_invoice_api.repository.InvoiceRepository;
import com.juansierra.global_invoice_api.repository.InvoiceTypeConfigRepository;
import com.juansierra.global_invoice_api.security.AuthenticatedUserService;
import com.juansierra.global_invoice_api.service.interfaces.InvoiceService;
import com.juansierra.global_invoice_api.strategy.TaxCalculation;
import com.juansierra.global_invoice_api.strategy.TaxStrategy;
import com.juansierra.global_invoice_api.strategy.TaxStrategyResolver;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceTypeConfigRepository invoiceTypeConfigRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final InvoiceMapper invoiceMapper;
    private final TaxStrategyResolver taxStrategyResolver;
    private final NumberConversionClient numberConversionClient;

    public InvoiceServiceImpl(
            InvoiceRepository invoiceRepository,
            InvoiceTypeConfigRepository invoiceTypeConfigRepository,
            AuthenticatedUserService authenticatedUserService,
            InvoiceMapper invoiceMapper,
            TaxStrategyResolver taxStrategyResolver,
            NumberConversionClient numberConversionClient
    ) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceTypeConfigRepository = invoiceTypeConfigRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.invoiceMapper = invoiceMapper;
        this.taxStrategyResolver = taxStrategyResolver;
        this.numberConversionClient = numberConversionClient;
    }

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        InvoiceTypeConfig taxConfiguration = invoiceTypeConfigRepository
                .findByTypeAndActiveTrue(request.getType())
                .orElseThrow(() -> new IllegalStateException(
                        "No existe configuracion tributaria activa para " + request.getType()
                ));
        TaxStrategy taxStrategy = taxStrategyResolver.resolve(request.getType());
        TaxCalculation calculation = taxStrategy.calculate(request.getSubtotal(), taxConfiguration);
        User createdBy = authenticatedUserService.getCurrentUser();

        Invoice invoice = invoiceMapper.toEntity(request);
        applyTaxCalculation(invoice, calculation);
        invoice.setCreatedBy(createdBy);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return invoiceMapper.toResponse(savedInvoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> findAll() {
        return invoiceRepository.findAll().stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDetailResponse findById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        String totalInWords = numberConversionClient.convertToWords(invoice.getTotal());
        return invoiceMapper.toDetailResponse(invoice, totalInWords);
    }

    private void applyTaxCalculation(Invoice invoice, TaxCalculation calculation) {
        invoice.setVatRate(calculation.getVatRate());
        invoice.setTaxAmount(calculation.getTaxAmount());
        invoice.setWithholdingRate(calculation.getWithholdingRate());
        invoice.setWithholdingAmount(calculation.getWithholdingAmount());
        invoice.setTotal(calculation.getTotal());
    }
}
