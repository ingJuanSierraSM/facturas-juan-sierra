import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  styleUrl: './login.component.scss',
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal('');
  readonly loginForm = this.formBuilder.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
    rememberSession: [false],
  });

  constructor() {
    const rememberedUsername = this.authService.getRememberedUsername();

    if (rememberedUsername) {
      this.loginForm.patchValue({ username: rememberedUsername, rememberSession: true });
    }
  }

  submitLogin(): void {
    this.errorMessage.set('');

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const { username, password, rememberSession } = this.loginForm.getRawValue();
    const normalizedUsername = username.trim();

    if (!normalizedUsername) {
      this.loginForm.controls.username.setErrors({ required: true });
      this.loginForm.controls.username.markAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    this.authService.login({ username: normalizedUsername, password }).subscribe({
      next: () => {
        this.authService.rememberUsername(normalizedUsername, rememberSession);
        this.isSubmitting.set(false);
        const destination = this.authService.getAuthenticatedUser()?.role === 'AUDITOR'
          ? '/dashboard'
          : '/facturas';
        void this.router.navigateByUrl(destination);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(
          error.status === 401 || error.status === 403
            ? 'Usuario o contraseña inválidos.'
            : 'No fue posible conectarse al servicio. Intenta nuevamente.',
        );
        this.isSubmitting.set(false);
      },
    });
  }
}
