import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, effect, inject, signal, viewChild } from '@angular/core';
import { Chart, ChartConfiguration, DoughnutController, ArcElement, Tooltip } from 'chart.js';
import ChartDataLabels from 'chartjs-plugin-datalabels';
import { DashboardSummary, createDashboardSummaries } from '../../core/dashboard/dashboard.models';
import { DashboardService } from '../../core/dashboard/dashboard.service';

Chart.register(DoughnutController, ArcElement, Tooltip, ChartDataLabels);

@Component({
  selector: 'app-dashboard',
  imports: [CurrencyPipe, DecimalPipe],
  styleUrl: './dashboard.component.scss',
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit, OnDestroy {
  private readonly dashboardService = inject(DashboardService);
  private chart: Chart<'doughnut'> | undefined;

  readonly chartCanvas = viewChild<ElementRef<HTMLCanvasElement>>('chartCanvas');
  readonly summaries = signal<DashboardSummary[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal('');

  constructor() {
    effect(() => {
      const canvas = this.chartCanvas()?.nativeElement;
      const summaries = this.summaries();

      if (!canvas || !this.hasDashboardData()) {
        this.destroyChart();
        return;
      }

      this.renderChart(canvas, summaries);
    });
  }

  ngOnInit(): void {
    this.loadDashboard();
  }

  ngOnDestroy(): void {
    this.destroyChart();
  }

  get totalAmount(): number {
    return this.summaries().reduce((total, summary) => total + summary.totalAmount, 0);
  }

  hasDashboardData(): boolean {
    return this.totalAmount > 0;
  }

  loadDashboard(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.dashboardService.getInvoicesByType().subscribe({
      next: (response) => {
        this.summaries.set(createDashboardSummaries(response));
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('No fue posible cargar el resumen de facturación.');
        this.isLoading.set(false);
      },
    });
  }

  private renderChart(canvas: HTMLCanvasElement, summaries: DashboardSummary[]): void {
    this.destroyChart();

    const configuration: ChartConfiguration<'doughnut'> = {
      type: 'doughnut',
      data: {
        labels: summaries.map((summary) => summary.label),
        datasets: [
          {
            backgroundColor: summaries.map((summary) => summary.color),
            borderColor: '#ffffff',
            borderWidth: 4,
            data: summaries.map((summary) => summary.totalAmount),
            hoverOffset: 7,
          },
        ],
      },
      options: {
        cutout: '62%',
        maintainAspectRatio: true,
        plugins: {
          datalabels: {
            color: '#ffffff',
            display: (context) => Number(context.dataset.data[context.dataIndex]) > 0,
            font: { size: 14, weight: 'bold' },
            formatter: (value: number) => `${Math.round((value / this.totalAmount) * 100)}%`,
          },
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (context) => {
                const summary = summaries[context.dataIndex];
                return `${summary.label}: ${this.formatCurrency(summary.totalAmount)} (${summary.percentage.toFixed(1)}%)`;
              },
            },
          },
        },
      },
    };

    this.chart = new Chart(canvas, configuration);
  }

  private destroyChart(): void {
    this.chart?.destroy();
    this.chart = undefined;
  }

  private formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-CO', {
      currency: 'COP',
      currencyDisplay: 'narrowSymbol',
      maximumFractionDigits: 0,
      style: 'currency',
    }).format(value);
  }
}
