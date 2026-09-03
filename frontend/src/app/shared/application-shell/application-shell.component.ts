import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  lucideChartPie,
  lucideCircleUserRound,
  lucideFilePlus2,
  lucideLogOut,
  lucideReceiptText,
} from '@ng-icons/lucide';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-application-shell',
  imports: [NgIcon, RouterLink, RouterLinkActive, RouterOutlet],
  providers: [
    provideIcons({ lucideChartPie, lucideCircleUserRound, lucideFilePlus2, lucideLogOut, lucideReceiptText }),
  ],
  styleUrl: './application-shell.component.scss',
  templateUrl: './application-shell.component.html',
})
export class ApplicationShellComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly currentUser = computed(() => this.authService.getAuthenticatedUser());
  readonly isAuditor = computed(() => this.currentUser()?.role === 'AUDITOR');
  readonly isOperator = computed(() => this.currentUser()?.role === 'OPERATOR');

  logout(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login');
  }
}
