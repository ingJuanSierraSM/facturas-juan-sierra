import { CurrencyPipe, DatePipe, PercentPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideArrowLeft, lucideLoaderCircle } from '@ng-icons/lucide';
import { InvoiceDetailResponse, getInvoiceTypeLabel } from '../../../core/invoices/invoice.models';
import { InvoiceService } from '../../../core/invoices/invoice.service';

@Component({
  selector: 'app-invoice-detail',
  imports: [CurrencyPipe, DatePipe, PercentPipe, NgIcon, RouterLink],
  providers: [provideIcons({ lucideArrowLeft, lucideLoaderCircle })],
  styleUrl: './invoice-detail.component.scss',
  templateUrl: './invoice-detail.component.html',
})
export class InvoiceDetailComponent implements OnInit {
  private readonly invoiceService = inject(InvoiceService);
  private readonly route = inject(ActivatedRoute);

  readonly invoice = signal<InvoiceDetailResponse | null>(null);
  readonly isLoading = signal(true);
  readonly errorMessage = signal('');

  ngOnInit(): void {
    const invoiceId = Number(this.route.snapshot.paramMap.get('invoiceId'));

    if (!Number.isInteger(invoiceId) || invoiceId <= 0) {
      this.isLoading.set(false);
      this.errorMessage.set('La factura solicitada no es válida.');
      return;
    }

    this.loadInvoice(invoiceId);
  }

  getInvoiceTypeLabel = getInvoiceTypeLabel;

  private loadInvoice(invoiceId: number): void {
    this.invoiceService.findById(invoiceId).subscribe({
      next: (invoice) => {
        this.invoice.set(invoice);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('No fue posible cargar el detalle de la factura.');
        this.isLoading.set(false);
      },
    });
  }
}
