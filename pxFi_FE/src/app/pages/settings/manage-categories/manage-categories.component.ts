import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../../../services/category.service';
import { Category } from '../../../services/api.service';
import { Observable } from 'rxjs';
import { NotificationService } from '../../../services/notification.service';
import { IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonInput, IonSelect, IonSelectOption, IonButton, IonItemGroup, IonItemDivider, IonToggle, IonIcon, IonButtons, IonBackButton } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { trashOutline } from 'ionicons/icons';

@Component({
  selector: 'app-manage-categories',
  standalone: true,
  imports: [CommonModule, FormsModule, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonInput, IonSelect, IonSelectOption, IonButton, IonItemGroup, IonItemDivider, IonToggle, IonIcon, IonButtons, IonBackButton],
  templateUrl: './manage-categories.component.html',
  styleUrls: ['./manage-categories.component.scss']
})
export class ManageCategoriesComponent implements OnInit {
  categoryService = inject(CategoryService);
  private notificationService = inject(NotificationService);

  categories$: Observable<Category[]>;
  mainCategories: Category[] = [];
  newCategoryName = '';
  newCategoryParentId: string | null = null;
  expandedCategoryIds = new Set<string>();

  constructor() {
    this.categories$ = this.categoryService.categories$;
    addIcons({ 'trash-outline': trashOutline });
  }

  ngOnInit(): void {
    this.categoryService.refreshCategories();
    this.categories$.subscribe(() => {
      this.mainCategories = this.categoryService.getMainCategories();
    });
  }

  toggleCategory(id: string): void {
    if (this.expandedCategoryIds.has(id)) {
      this.expandedCategoryIds.delete(id);
    } else {
      this.expandedCategoryIds.add(id);
    }
  }

  addCategory(): void {
    if (!this.newCategoryName.trim()) return;
    this.categoryService.createCategory(this.newCategoryName, this.newCategoryParentId).subscribe({
      next: () => {
        this.notificationService.show('Category added successfully!', 'success');
        this.newCategoryName = '';
        this.newCategoryParentId = null;
      },
      error: (err) => this.notificationService.show('Error adding category: ' + err.message, 'error')
    });
  }

  deleteCategory(id: string): void {
    if (confirm('Are you sure you want to delete this category? This cannot be undone.')) {
      this.categoryService.deleteCategory(id).subscribe({
        next: () => this.notificationService.show('Category deleted.', 'success'),
        error: (err) => this.notificationService.show('Error deleting category: ' + err.message, 'error')
      });
    }
  }

  onAssetTransferChange(category: Category): void {
    this.categoryService.updateCategory(category).subscribe({
      next: () => this.notificationService.show(`'${category.name}' asset transfer status updated.`, 'success'),
      error: err => this.notificationService.show('Failed to update category: ' + err.message, 'error')
    });
  }
}
