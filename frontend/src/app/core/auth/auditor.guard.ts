import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const auditorGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.getAuthenticatedUser()?.role === 'AUDITOR'
    ? true
    : router.createUrlTree(['/facturas']);
};
