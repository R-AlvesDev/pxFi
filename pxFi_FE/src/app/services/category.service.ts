import { Injectable } from '@angular/core';
import { ApiService, Category } from './api.service';
import { BehaviorSubject, Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private categories = new BehaviorSubject<Category[]>([]);
  public categories$ = this.categories.asObservable();

  constructor(private api: ApiService) {
  }

  getMainCategories(): Category[] {
    return this.categories.getValue().filter(c => c.parentId === null);
  }

  getSubCategories(parentId: string): Category[] {
    return this.categories.getValue().filter(c => c.parentId === parentId);
  }

  refreshCategories(accessToken: string) {
    this.api.getAllCategories(accessToken).pipe(
      tap(categories => this.categories.next(categories))
    ).subscribe();
  }

  createCategory(accessToken: string, name: string, parentId: string | null): Observable<Category> {
    return this.api.createCategory(accessToken, { name, parentId }).pipe(
      tap(() => this.refreshCategories(accessToken))
    );
  }

  updateCategory(accessToken: string, category: Category): Observable<Category> {
    return this.api.updateCategory(accessToken, category.id, category).pipe(
      tap(() => this.refreshCategories(accessToken))
    );
  }

  deleteCategory(accessToken: string, id: string): Observable<void> {
    return this.api.deleteCategory(accessToken, id).pipe(
      tap(() => this.refreshCategories(accessToken))
    );
  }

  getCategoryName(id: string): string {
    const category = this.categories.getValue().find(c => c.id === id);
    return category ? category.name : 'N/A';
  }
}