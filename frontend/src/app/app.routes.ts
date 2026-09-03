import { Routes } from '@angular/router';
import { auditorGuard } from './core/auth/auditor.guard';
import { authGuard } from './core/auth/auth.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { InvoiceDetailComponent } from './features/invoices/invoice-detail/invoice-detail.component';
import { InvoiceListComponent } from './features/invoices/invoice-list/invoice-list.component';
import { ApplicationShellComponent } from './shared/application-shell/application-shell.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: ApplicationShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        canActivate: [auditorGuard],
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((module) => module.DashboardComponent),
      },
      { path: 'facturas', component: InvoiceListComponent },
      { path: 'facturas/:invoiceId', component: InvoiceDetailComponent },
    ],
  },
  { path: '**', redirectTo: 'facturas' },
];
