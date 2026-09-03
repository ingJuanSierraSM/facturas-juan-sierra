export type InvoiceType = 'NATIONAL' | 'EXPORT' | 'GOVERNMENT';

export interface CreateInvoiceRequest {
  invoiceNumber: string;
  type: InvoiceType;
  subtotal: number;
  customsCode?: string;
}

export interface InvoiceResponse {
  id: number;
  invoiceNumber: string;
  type: InvoiceType;
  subtotal: number;
  total: number;
  createdAt: string;
}

export interface InvoiceDetailResponse extends InvoiceResponse {
  vatRate: number;
  taxAmount: number;
  withholdingRate: number;
  withholdingAmount: number;
  customsCode: string | null;
  createdByUsername: string;
  totalInWords: string;
}

const invoiceTypeLabels: Record<InvoiceType, string> = {
  NATIONAL: 'Nacional',
  EXPORT: 'Exportación',
  GOVERNMENT: 'Gobierno',
};

export function getInvoiceTypeLabel(type: InvoiceType): string {
  return invoiceTypeLabels[type];
}
