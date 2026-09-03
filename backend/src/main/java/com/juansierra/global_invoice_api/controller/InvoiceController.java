package com.juansierra.global_invoice_api.controller;

import com.juansierra.global_invoice_api.dto.request.CreateInvoiceRequest;
import com.juansierra.global_invoice_api.dto.response.InvoiceDetailResponse;
import com.juansierra.global_invoice_api.dto.response.InvoiceResponse;
import com.juansierra.global_invoice_api.service.interfaces.InvoiceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> findAll() {
        return ResponseEntity.ok(invoiceService.findAll());
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceDetailResponse> findById(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(invoiceService.findById(invoiceId));
    }
}
