import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { InvoiceService } from './invoice.service';

describe('InvoiceService', () => {
  let invoiceService: InvoiceService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    invoiceService = TestBed.inject(InvoiceService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should request the shared invoice list endpoint', () => {
    invoiceService.findAll().subscribe();

    const request = httpTesting.expectOne('/api/v1/invoices');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('should request an invoice detail by its identifier', () => {
    invoiceService.findById(34).subscribe();

    const request = httpTesting.expectOne('/api/v1/invoices/34');
    expect(request.request.method).toBe('GET');
    request.flush({ id: 34 });
  });
});
