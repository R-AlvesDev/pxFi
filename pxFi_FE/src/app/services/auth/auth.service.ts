import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { ApiService, AuthResponse } from '../api.service';
import { Router } from '@angular/router';
import { AccountStateService } from '../account-state.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private loggedIn = new BehaviorSubject<boolean>(this.hasToken());
  public loggedIn$ = this.loggedIn.asObservable();

  constructor(
    private api: ApiService,
    private router: Router,
    private accountState: AccountStateService
  ) {}

  private hasToken(): boolean {
    return !!localStorage.getItem('accessToken');
  }

  public checkLoginStatus(): void {
    this.loggedIn.next(this.hasToken());
  }
  
  register(registerData: any): Observable<AuthResponse> {
    return this.api.register(registerData).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('accessToken', response.token);
          this.loggedIn.next(true);
        }
      })
    );
  }

  login(loginData: any): Observable<AuthResponse> {
    return this.api.login(loginData).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('accessToken', response.token);
          this.loggedIn.next(true);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    this.loggedIn.next(false);
    this.accountState.clearAll();
    this.router.navigate(['/']);
  }
}