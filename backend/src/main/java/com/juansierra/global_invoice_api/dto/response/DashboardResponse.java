package com.juansierra.global_invoice_api.dto.response;

import com.juansierra.global_invoice_api.enums.InvoiceType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private InvoiceType type;
    private BigDecimal totalAmount;
}
