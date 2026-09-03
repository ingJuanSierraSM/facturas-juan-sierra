import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from './core/auth/auth.service';

@Component({
  selector: 'app-root',
  imports: [ReactiveFormsModule],
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
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
    this.successMessage.set('');

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
        this.successMessage.set('Inicio de sesión correcto.');
        this.isSubmitting.set(false);
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
