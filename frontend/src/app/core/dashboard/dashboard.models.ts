import { InvoiceType, getInvoiceTypeLabel } from '../invoices/invoice.models';

export interface DashboardResponse {
  type: InvoiceType;
  totalAmount: number;
}

export interface DashboardSummary extends DashboardResponse {
  color: string;
  label: string;
  percentage: number;
}

const dashboardTypes: Array<{ type: InvoiceType; color: string }> = [
  { type: 'NATIONAL', color: '#d6332f' },
  { type: 'EXPORT', color: '#287b99' },
  { type: 'GOVERNMENT', color: '#a46c14' },
];

export function createDashboardSummaries(response: DashboardResponse[]): DashboardSummary[] {
  const amountsByType = new Map(response.map((item) => [item.type, Number(item.totalAmount) || 0]));
  const total = dashboardTypes.reduce((sum, item) => sum + (amountsByType.get(item.type) ?? 0), 0);

  return dashboardTypes.map(({ type, color }) => {
    const totalAmount = amountsByType.get(type) ?? 0;

    return {
      type,
      color,
      label: getInvoiceTypeLabel(type),
      totalAmount,
      percentage: total > 0 ? (totalAmount / total) * 100 : 0,
    };
  });
}
