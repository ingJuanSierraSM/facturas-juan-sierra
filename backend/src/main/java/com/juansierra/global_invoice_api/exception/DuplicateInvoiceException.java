package com.juansierra.global_invoice_api.exception;

public class DuplicateInvoiceException extends RuntimeException {

    public DuplicateInvoiceException(String invoiceNumber) {
        super("Ya existe una factura con el numero " + invoiceNumber);
    }
}
