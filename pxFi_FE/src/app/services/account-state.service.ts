import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AccountStateService {
  private currentAccountId = new BehaviorSubject<string | null>(null);
  public currentAccountId$ = this.currentAccountId.asObservable();

  constructor() {
    const savedAccountId = localStorage.getItem('selectedAccountId');
    if (savedAccountId) {
      this.currentAccountId.next(savedAccountId);
    }
  }

  setCurrentAccountId(accountId: string | null): void {
    if (accountId) {
      localStorage.setItem('selectedAccountId', accountId);
    } else {
      localStorage.removeItem('selectedAccountId');
    }
    this.currentAccountId.next(accountId);
  }

  clearAll(): void {
    localStorage.removeItem('selectedAccountId');
    this.currentAccountId.next(null);
  }
}