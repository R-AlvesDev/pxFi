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
    // Fetch categories as soon as the service is created
    this.loadCategories().subscribe();
  }

  private loadCategories(): Observable<Category[]> {
    return this.api.getCategories().pipe(
      tap(categories => this.categories.next(categories))
    );
  }

  getMainCategories(): Category[] {
    return this.categories.getValue().filter(c => c.parentId === null);
  }

  getSubCategories(parentId: string): Category[] {
    return this.categories.getValue().filter(c => c.parentId === parentId);
  }
}