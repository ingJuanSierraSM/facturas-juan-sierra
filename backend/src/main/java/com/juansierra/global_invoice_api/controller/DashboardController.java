package com.juansierra.global_invoice_api.controller;

import com.juansierra.global_invoice_api.dto.response.DashboardResponse;
import com.juansierra.global_invoice_api.service.interfaces.DashboardService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/invoices-by-type")
    public ResponseEntity<List<DashboardResponse>> getInvoicesByType() {
        return ResponseEntity.ok(dashboardService.getInvoicesByType());
    }
}
