import { Injectable, inject } from '@angular/core';
import { ApiService, Category } from './api.service';
import { BehaviorSubject, Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private api = inject(ApiService);

  private categories = new BehaviorSubject<Category[]>([]);
  public categories$ = this.categories.asObservable();


  getMainCategories(): Category[] {
    return this.categories.getValue().filter(c => c.parentId === null);
  }

  getSubCategories(parentId: string): Category[] {
    return this.categories.getValue().filter(c => c.parentId === parentId);
  }

  refreshCategories() {
    this.api.getAllCategories().pipe(
      tap(categories => this.categories.next(categories))
    ).subscribe();
  }

  createCategory(name: string, parentId: string | null): Observable<Category> {
    return this.api.createCategory({ name, parentId }).pipe(
      tap(() => this.refreshCategories())
    );
  }

  updateCategory(category: Category): Observable<Category> {
    return this.api.updateCategory(category.id, category).pipe(
      tap(() => this.refreshCategories())
    );
  }

  deleteCategory(id: string): Observable<void> {
    return this.api.deleteCategory(id).pipe(
      tap(() => this.refreshCategories())
    );
  }

  getCategoryName(id: string): string {
    const category = this.categories.getValue().find(c => c.id === id);
    return category ? category.name : 'N/A';
  }
}