import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { AuthenticatedUser } from './auth.models';
import { AuthService } from './auth.service';
import { auditorGuard } from './auditor.guard';

describe('auditorGuard', () => {
  let currentUser: AuthenticatedUser | null;
  const redirectTree = {} as UrlTree;

  beforeEach(() => {
    currentUser = null;

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: { getAuthenticatedUser: () => currentUser } },
        { provide: Router, useValue: { createUrlTree: () => redirectTree } },
      ],
    });
  });

  it('should allow an auditor to access the dashboard', () => {
    currentUser = { username: 'auditor', role: 'AUDITOR' };

    const result = TestBed.runInInjectionContext(() => auditorGuard({} as never, {} as never));

    expect(result).toBe(true);
  });

  it('should redirect an operator to the invoice list', () => {
    currentUser = { username: 'operator', role: 'OPERATOR' };

    const result = TestBed.runInInjectionContext(() => auditorGuard({} as never, {} as never));

    expect(result).toBe(redirectTree);
  });
});
