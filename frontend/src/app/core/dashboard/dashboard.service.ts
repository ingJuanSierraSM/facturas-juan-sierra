import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DashboardResponse } from './dashboard.models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  getInvoicesByType(): Observable<DashboardResponse[]> {
    return this.http.get<DashboardResponse[]>('/api/v1/dashboard/invoices-by-type');
  }
}
