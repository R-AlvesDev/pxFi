import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { AccountStateService } from '../account-state.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // Use a BehaviorSubject to broadcast the login state
  private readonly loggedIn = new BehaviorSubject<boolean>(this.hasToken());
  public readonly loggedIn$ = this.loggedIn.asObservable();

  constructor(
    private router: Router,
    private accountState: AccountStateService
  ) {}

  /**
   * Checks if a token exists in localStorage.
   */
  private hasToken(): boolean {
    return !!localStorage.getItem('accessToken');
  }

  /**
   * Call this to update the login state, e.g., after a successful connection.
   */
  updateLoginState(): void {
    this.loggedIn.next(this.hasToken());
  }

  /**
   * Logs the user out by clearing stored data and redirecting.
   */
  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('requisitionId');
    this.accountState.setCurrentAccountId(null); // Clear the active account
    this.loggedIn.next(false);
    this.router.navigate(['/']);
  }
}