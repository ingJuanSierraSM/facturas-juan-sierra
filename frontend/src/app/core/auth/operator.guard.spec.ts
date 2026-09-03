import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { AuthenticatedUser } from './auth.models';
import { AuthService } from './auth.service';
import { operatorGuard } from './operator.guard';

describe('operatorGuard', () => {
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

  it('should allow an operator to create invoices', () => {
    currentUser = { username: 'operator', role: 'OPERATOR' };

    const result = TestBed.runInInjectionContext(() => operatorGuard({} as never, {} as never));

    expect(result).toBe(true);
  });

  it('should redirect an auditor to the invoice list', () => {
    currentUser = { username: 'auditor', role: 'AUDITOR' };

    const result = TestBed.runInInjectionContext(() => operatorGuard({} as never, {} as never));

    expect(result).toBe(redirectTree);
  });
});
