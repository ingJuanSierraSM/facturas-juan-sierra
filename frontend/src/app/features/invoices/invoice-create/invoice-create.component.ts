import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CreateInvoiceRequest, InvoiceType } from '../../../core/invoices/invoice.models';
import { InvoiceService } from '../../../core/invoices/invoice.service';

type InvoiceFormField = 'invoiceNumber' | 'subtotal' | 'customsCode';

interface ApiErrorResponse {
  errors?: Record<string, string>;
  message?: string;
}

@Component({
  selector: 'app-invoice-create',
  imports: [ReactiveFormsModule, RouterLink],
  styleUrl: './invoice-create.component.scss',
  templateUrl: './invoice-create.component.html',
})
export class InvoiceCreateComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly invoiceService = inject(InvoiceService);
  private readonly router = inject(Router);

  readonly isSubmitting = signal(false);
  readonly isSubmitted = signal(false);
  readonly submitError = signal('');
  readonly serverFieldErrors = signal<Partial<Record<InvoiceFormField, string>>>({});
  readonly invoiceForm = this.formBuilder.group({
    invoiceNumber: this.formBuilder.nonNullable.control('', [Validators.required]),
    type: this.formBuilder.nonNullable.control<InvoiceType>('NATIONAL', [Validators.required]),
    subtotal: this.formBuilder.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    customsCode: this.formBuilder.nonNullable.control(''),
  });

  constructor() {
    this.invoiceForm.controls.type.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe((type) => this.configureCustomsCode(type));
  }

  get isExport(): boolean {
    return this.invoiceForm.controls.type.value === 'EXPORT';
  }

  getFieldError(field: InvoiceFormField): string {
    const serverError = this.serverFieldErrors()[field];

    if (serverError) {
      return serverError;
    }

    const control = this.invoiceForm.controls[field];

    if (!this.isSubmitted() && !control.touched) {
      return '';
    }

    if (control.hasError('required')) {
      return 'Este campo es obligatorio.';
    }

    if (control.hasError('min')) {
      return 'Ingresa un subtotal mayor que cero.';
    }

    return '';
  }

  clearServerFieldError(field: InvoiceFormField): void {
    const errors = { ...this.serverFieldErrors() };
    delete errors[field];
    this.serverFieldErrors.set(errors);
  }

  submit(): void {
    this.isSubmitted.set(true);
    this.submitError.set('');
    this.serverFieldErrors.set({});
    this.trimInvoiceNumber();

    if (this.invoiceForm.invalid) {
      this.invoiceForm.markAllAsTouched();
      return;
    }

    const { invoiceNumber, type, subtotal, customsCode } = this.invoiceForm.getRawValue();
    const request: CreateInvoiceRequest = {
      invoiceNumber,
      type,
      subtotal: subtotal as number,
      ...(type === 'EXPORT' ? { customsCode: customsCode.trim() } : {}),
    };

    this.isSubmitting.set(true);

    this.invoiceService.create(request).subscribe({
      next: (invoice) => {
        void this.router.navigateByUrl(`/facturas/${invoice.id}`);
      },
      error: (error: HttpErrorResponse) => {
        this.applyServerErrors(error);
        this.isSubmitting.set(false);
      },
    });
  }

  private configureCustomsCode(type: InvoiceType): void {
    const customsCode = this.invoiceForm.controls.customsCode;

    if (type === 'EXPORT') {
      customsCode.setValidators([Validators.required]);
    } else {
      customsCode.clearValidators();
      customsCode.setValue('');
      this.clearServerFieldError('customsCode');
    }

    customsCode.updateValueAndValidity({ emitEvent: false });
  }

  private trimInvoiceNumber(): void {
    const control = this.invoiceForm.controls.invoiceNumber;
    control.setValue(control.value.trim());
    control.updateValueAndValidity({ emitEvent: false });
  }

  private applyServerErrors(error: HttpErrorResponse): void {
    const apiError = error.error as ApiErrorResponse | null;

    if (error.status === 400 && apiError?.errors) {
      this.serverFieldErrors.set({
        invoiceNumber: apiError.errors['invoiceNumber'],
        subtotal: apiError.errors['subtotal'],
        customsCode: apiError.errors['customsCodeValid'],
      });
      this.submitError.set('Revisa los campos indicados.');
      return;
    }

    this.submitError.set(apiError?.message ?? 'No fue posible crear la factura. Intenta nuevamente.');
  }
}
