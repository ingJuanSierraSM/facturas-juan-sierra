package com.juansierra.global_invoice_api.integration;

import java.math.BigDecimal;

public interface NumberConversionClient {

    String convertToWords(BigDecimal amount);
}
