export type UserRole = 'OPERATOR' | 'AUDITOR';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
}

interface TokenPayload {
  exp?: number;
  role?: UserRole;
  sub?: string;
}

export interface AuthenticatedUser {
  role: UserRole;
  username: string;
}

export function parseTokenPayload(token: string): TokenPayload | null {
  try {
    const payload = token.split('.')[1];

    if (!payload) {
      return null;
    }

    const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/');
    const decodedPayload = atob(normalizedPayload);
    const jsonPayload = decodeURIComponent(
      Array.from(decodedPayload)
        .map((character) => `%${character.charCodeAt(0).toString(16).padStart(2, '0')}`)
        .join(''),
    );

    return JSON.parse(jsonPayload) as TokenPayload;
  } catch {
    return null;
  }
}
