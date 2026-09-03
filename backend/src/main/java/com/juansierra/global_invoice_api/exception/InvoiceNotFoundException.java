package com.juansierra.global_invoice_api.exception;

public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(Long invoiceId) {
        super("No existe una factura con el id " + invoiceId);
    }
}
