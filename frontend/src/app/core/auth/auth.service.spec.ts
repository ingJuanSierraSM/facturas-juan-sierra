import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let authService: AuthService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    authService = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should authenticate and store the token in the current session', () => {
    const token =
      'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhdWRpdG9yIiwicm9sZSI6IkFVRElUT1IifQ.signature';

    authService.login({ username: 'auditor', password: 'secret' }).subscribe();

    const request = httpTesting.expectOne('/api/v1/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ username: 'auditor', password: 'secret' });
    request.flush({ token, tokenType: 'Bearer', expiresIn: 3600 });

    expect(authService.getToken()).toBe(token);
    expect(authService.getAuthenticatedUser()).toEqual({ username: 'auditor', role: 'AUDITOR' });
  });

  it('should discard an expired token', () => {
    const expiredToken =
      'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhdWRpdG9yIiwicm9sZSI6IkFVRElUT1IiLCJleHAiOjF9.signature';
    sessionStorage.setItem('global-invoice.token', expiredToken);
    sessionStorage.setItem('global-invoice.token-type', 'Bearer');

    expect(authService.getToken()).toBeNull();
    expect(sessionStorage.getItem('global-invoice.token-type')).toBeNull();
  });
});
