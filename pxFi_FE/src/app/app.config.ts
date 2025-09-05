import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http'; // Import withInterceptorsFromDi
import { HTTP_INTERCEPTORS } from '@angular/common/http'; // Import HTTP_INTERCEPTORS
import { AuthInterceptor } from './services/auth/auth.interceptor'; // Import the new interceptor

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()), 
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ]
};