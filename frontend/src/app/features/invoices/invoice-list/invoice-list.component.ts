import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideEye, lucideLoaderCircle, lucideReceiptText } from '@ng-icons/lucide';
import { InvoiceResponse, getInvoiceTypeLabel } from '../../../core/invoices/invoice.models';
import { InvoiceService } from '../../../core/invoices/invoice.service';

@Component({
  selector: 'app-invoice-list',
  imports: [CurrencyPipe, DatePipe, NgIcon, RouterLink],
  providers: [provideIcons({ lucideEye, lucideLoaderCircle, lucideReceiptText })],
  styleUrl: './invoice-list.component.scss',
  templateUrl: './invoice-list.component.html',
})
export class InvoiceListComponent implements OnInit {
  private readonly invoiceService = inject(InvoiceService);

  readonly invoices = signal<InvoiceResponse[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal('');

  ngOnInit(): void {
    this.loadInvoices();
  }

  loadInvoices(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.invoiceService.findAll().subscribe({
      next: (invoices) => {
        this.invoices.set(invoices);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('No fue posible cargar las facturas. Intenta nuevamente.');
        this.isLoading.set(false);
      },
    });
  }

  getInvoiceTypeLabel = getInvoiceTypeLabel;
}
