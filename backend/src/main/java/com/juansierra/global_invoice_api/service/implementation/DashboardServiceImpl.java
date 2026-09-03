package com.juansierra.global_invoice_api.service.implementation;

import com.juansierra.global_invoice_api.dto.response.DashboardResponse;
import com.juansierra.global_invoice_api.repository.InvoiceRepository;
import com.juansierra.global_invoice_api.service.interfaces.DashboardService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final InvoiceRepository invoiceRepository;

    public DashboardServiceImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public List<DashboardResponse> getInvoicesByType() {
        return invoiceRepository.findTotalAmountsByType().stream()
                .map(totalByType -> new DashboardResponse(
                        totalByType.getType(),
                        totalByType.getTotalAmount()
                ))
                .toList();
    }
}
