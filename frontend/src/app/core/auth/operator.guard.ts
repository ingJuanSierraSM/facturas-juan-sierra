import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const operatorGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.getAuthenticatedUser()?.role === 'OPERATOR'
    ? true
    : router.createUrlTree(['/facturas']);
};
