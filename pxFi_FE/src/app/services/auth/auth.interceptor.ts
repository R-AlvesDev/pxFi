import { Injectable } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('accessToken');
    let authReq = req; // Start with the original request

    if (token) {
      authReq = req.clone({
        headers: req.headers.set('Authorization', `Bearer ${token}`)
      });
    }

    // We pass the (potentially cloned) request to the next handler
    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Token is invalid or expired, clear local storage
          console.error('Unauthorized request - redirecting to login page');
          localStorage.removeItem('accessToken');
          localStorage.removeItem('requisitionId');
          localStorage.removeItem('selectedAccountId');
          
          // Redirect to the login page instead of the connect page
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
  }
}