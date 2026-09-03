package com.juansierra.global_invoice_api.service.interfaces;

import com.juansierra.global_invoice_api.dto.request.CreateInvoiceRequest;
import com.juansierra.global_invoice_api.dto.response.InvoiceDetailResponse;
import com.juansierra.global_invoice_api.dto.response.InvoiceResponse;
import java.util.List;

public interface InvoiceService {

    InvoiceResponse createInvoice(CreateInvoiceRequest request);

    List<InvoiceResponse> findAll();

    InvoiceDetailResponse findById(Long invoiceId);
}
