import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { ApiService, CategorizationRule, TestRuleResponse } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class RuleService {
  private api = inject(ApiService);

  private rules = new BehaviorSubject<CategorizationRule[]>([]);
  public rules$ = this.rules.asObservable();


  getAllRules(): Observable<CategorizationRule[]> {
    return this.api.getAllRules().pipe(
      tap(rules => this.rules.next(rules))
    );
  }

  createRule(rule: Partial<CategorizationRule>): Observable<CategorizationRule> {
    return this.api.createRule(rule).pipe(
      tap(() => this.getAllRules().subscribe())
    );
  }

  applyAllRules(): Observable<{ updatedCount: number }> {
    return this.api.applyAllRules();
  }

  deleteRule(id: string): Observable<void> {
    return this.api.deleteRule(id).pipe(
      tap(() => this.getAllRules().subscribe())
    );
  }

  testRule(rule: Partial<CategorizationRule>, accountId: string): Observable<TestRuleResponse> {
    return this.api.testRule(rule, accountId);
  }
}