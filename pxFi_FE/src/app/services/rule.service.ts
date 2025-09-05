import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { ApiService, CategorizationRule, TestRuleResponse } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class RuleService {
  private rules = new BehaviorSubject<CategorizationRule[]>([]);
  public rules$ = this.rules.asObservable();

  constructor(private api: ApiService) {}

  getAllRules(): Observable<CategorizationRule[]> {
    return this.api.getAllRules().pipe(
      tap(rules => this.rules.next(rules))
    );
}

  createRule(accessToken: string, rule: Partial<CategorizationRule>): Observable<CategorizationRule> {
    return this.api.createRule(accessToken, rule).pipe(
      tap(() => this.getAllRules(accessToken).subscribe())
    );
  }

  applyAllRules(accessToken: string): Observable<{ updatedCount: number }> {
    return this.api.applyAllRules(accessToken);
  }

  deleteRule(accessToken: string, id: string): Observable<void> {
    return this.api.deleteRule(accessToken, id).pipe(
      tap(() => this.getAllRules(accessToken).subscribe())
    );
  }

  testRule(accessToken: string, rule: Partial<CategorizationRule>, accountId: string): Observable<TestRuleResponse> {
    return this.api.testRule(accessToken, rule, accountId);
  }
}