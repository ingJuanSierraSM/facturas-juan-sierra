import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { CreateInvoiceRequest } from '../../../core/invoices/invoice.models';
import { InvoiceService } from '../../../core/invoices/invoice.service';
import { InvoiceCreateComponent } from './invoice-create.component';

describe('InvoiceCreateComponent', () => {
  let component: InvoiceCreateComponent;
  let submittedRequest: CreateInvoiceRequest | undefined;
  let navigatedTo = '';

  beforeEach(() => {
    submittedRequest = undefined;
    navigatedTo = '';

    TestBed.configureTestingModule({
      imports: [InvoiceCreateComponent],
      providers: [
        {
          provide: InvoiceService,
          useValue: {
            create: (request: CreateInvoiceRequest) => {
              submittedRequest = request;
              return of({ id: 24 });
            },
          },
        },
        {
          provide: Router,
          useValue: {
            navigateByUrl: (url: string) => {
              navigatedTo = url;
              return Promise.resolve(true);
            },
          },
        },
      ],
    });

    component = TestBed.runInInjectionContext(() => new InvoiceCreateComponent());
  });

  it('should require a customs code only for export invoices', () => {
    component.invoiceForm.controls.type.setValue('EXPORT');

    expect(component.isExport).toBe(true);
    expect(component.invoiceForm.controls.customsCode.hasError('required')).toBe(true);

    component.invoiceForm.controls.type.setValue('NATIONAL');

    expect(component.isExport).toBe(false);
    expect(component.invoiceForm.controls.customsCode.valid).toBe(true);
  });

  it('should create an export invoice and show its detail', () => {
    component.invoiceForm.setValue({
      invoiceNumber: ' INV-EXPORT-004 ',
      type: 'EXPORT',
      subtotal: 100000,
      customsCode: 'EXP-CO-004',
    });

    component.submit();

    expect(submittedRequest).toEqual({
      invoiceNumber: 'INV-EXPORT-004',
      type: 'EXPORT',
      subtotal: 100000,
      customsCode: 'EXP-CO-004',
    });
    expect(navigatedTo).toBe('/facturas/24');
  });
});
