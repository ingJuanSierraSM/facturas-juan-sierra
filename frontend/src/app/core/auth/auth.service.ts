import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import {
  AuthenticatedUser,
  LoginRequest,
  LoginResponse,
  UserRole,
  parseTokenPayload,
} from './auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenKey = 'global-invoice.token';
  private readonly tokenTypeKey = 'global-invoice.token-type';
  private readonly rememberedUsernameKey = 'global-invoice.remembered-username';

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>('/api/v1/auth/login', request)
      .pipe(tap((response) => this.saveSession(response)));
  }

  getToken(): string | null {
    const token = sessionStorage.getItem(this.tokenKey);

    if (!token) {
      return null;
    }

    if (this.isExpired(parseTokenPayload(token)?.exp)) {
      this.logout();
      return null;
    }

    return token;
  }

  getAuthorizationScheme(): string {
    return sessionStorage.getItem(this.tokenTypeKey) ?? 'Bearer';
  }

  getAuthenticatedUser(): AuthenticatedUser | null {
    const token = this.getToken();

    if (!token) {
      return null;
    }

    const payload = parseTokenPayload(token);

    if (!payload?.sub || !this.isRole(payload.role) || this.isExpired(payload.exp)) {
      return null;
    }

    return { username: payload.sub, role: payload.role };
  }

  rememberUsername(username: string, shouldRemember: boolean): void {
    if (shouldRemember) {
      localStorage.setItem(this.rememberedUsernameKey, username);
      return;
    }

    localStorage.removeItem(this.rememberedUsernameKey);
  }

  getRememberedUsername(): string {
    return localStorage.getItem(this.rememberedUsernameKey) ?? '';
  }

  logout(): void {
    sessionStorage.removeItem(this.tokenKey);
    sessionStorage.removeItem(this.tokenTypeKey);
  }

  private saveSession(response: LoginResponse): void {
    sessionStorage.setItem(this.tokenKey, response.token);
    sessionStorage.setItem(this.tokenTypeKey, response.tokenType);
  }

  private isExpired(expirationTimestamp?: number): boolean {
    return expirationTimestamp !== undefined && expirationTimestamp * 1000 <= Date.now();
  }

  private isRole(role?: string): role is UserRole {
    return role === 'OPERATOR' || role === 'AUDITOR';
  }
}
