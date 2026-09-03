import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();
  });

  it('should render the login title', async () => {
    const fixture = TestBed.createComponent(LoginComponent);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('h1')?.textContent).toContain('Nos alegra que estés aquí');
  });

  it('should render credential fields', async () => {
    const fixture = TestBed.createComponent(LoginComponent);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('#username')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#password')).toBeTruthy();
  });
});
