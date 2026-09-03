package com.juansierra.global_invoice_api.service.interfaces;

import com.juansierra.global_invoice_api.dto.response.DashboardResponse;
import java.util.List;

public interface DashboardService {

    List<DashboardResponse> getInvoicesByType();
}
