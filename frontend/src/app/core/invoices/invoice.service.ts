import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateInvoiceRequest, InvoiceDetailResponse, InvoiceResponse } from './invoice.models';

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private readonly http = inject(HttpClient);

  create(request: CreateInvoiceRequest): Observable<InvoiceResponse> {
    return this.http.post<InvoiceResponse>('/api/v1/invoices', request);
  }

  findAll(): Observable<InvoiceResponse[]> {
    return this.http.get<InvoiceResponse[]>('/api/v1/invoices');
  }

  findById(invoiceId: number): Observable<InvoiceDetailResponse> {
    return this.http.get<InvoiceDetailResponse>(`/api/v1/invoices/${invoiceId}`);
  }
}
