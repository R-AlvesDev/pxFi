import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AccountStateService {
  // BehaviorSubject will hold the current value and notify subscribers of changes
  public readonly currentAccountId$ = new BehaviorSubject<string | null>(null);

  constructor() { }

  /**
   * Sets the currently active account ID.
   * @param accountId The ID of the account to set.
   */
  setCurrentAccountId(accountId: string | null): void {
    this.currentAccountId$.next(accountId);
  }
}