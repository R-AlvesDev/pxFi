import { Injectable } from '@angular/core';
import { ApiService, CategorizationRule } from './api.service';
import { BehaviorSubject, Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RuleService {
  private rules = new BehaviorSubject<CategorizationRule[]>([]);
  public rules$ = this.rules.asObservable();

  constructor(private api: ApiService) {
    this.loadRules().subscribe();
  }

  private loadRules(): Observable<CategorizationRule[]> {
    return this.api.getRules().pipe(
      tap(rules => this.rules.next(rules))
    );
  }

  refreshRules(): void {
    this.loadRules().subscribe();
  }

  createRule(rule: Partial<CategorizationRule>): Observable<CategorizationRule> {
    return this.api.createRule(rule).pipe(
      tap(() => this.refreshRules())
    );
  }

  applyAllRules(): Observable<{ updatedCount: number }> {
    return this.api.applyAllRules();
  }

  deleteRule(id: string): Observable<void> {
    return this.api.deleteRule(id).pipe(
      tap(() => this.refreshRules())
    );
  }
}