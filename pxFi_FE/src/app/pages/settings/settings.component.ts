import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../../services/category.service';
import { RuleService } from '../../services/rule.service';
import { Category, CategorizationRule, RuleField, RuleOperator } from '../../services/api.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
  // Category Properties
  categories$: Observable<Category[]>;
  mainCategories: Category[] = [];
  newCategoryName: string = '';
  newCategoryParentId: string | null = null;
  expandedCategoryIds = new Set<string>();
  isApplyingRules = false;

  // Rule Properties
  rules$: Observable<CategorizationRule[]>;
  newRule: Partial<CategorizationRule> = {
    fieldToMatch: RuleField.REMITTANCE_INFO,
    operator: RuleOperator.CONTAINS
  };
  ruleFields = Object.values(RuleField);
  ruleOperators = Object.values(RuleOperator);

  constructor(
    public categoryService: CategoryService,
    public ruleService: RuleService
  ) {
    this.categories$ = this.categoryService.categories$;
    this.rules$ = this.ruleService.rules$;
  }

  ngOnInit(): void {
    this.categories$.subscribe(() => {
      this.mainCategories = this.categoryService.getMainCategories();
    });
  }

  // --- Category Methods ---
  toggleCategory(id: string): void {
    if (this.expandedCategoryIds.has(id)) {
      this.expandedCategoryIds.delete(id);
    } else {
      this.expandedCategoryIds.add(id);
    }
  }

  addCategory(): void {
    if (!this.newCategoryName.trim()) return;
    this.categoryService.createCategory(this.newCategoryName, this.newCategoryParentId).subscribe(() => {
      this.newCategoryName = '';
      this.newCategoryParentId = null;
    });
  }

  deleteCategory(id: string): void {
    if (confirm('Are you sure you want to delete this category?')) {
      this.categoryService.deleteCategory(id).subscribe();
    }
  }

  // --- Rule Methods ---
  createRule(): void {
    if (!this.newRule.valueToMatch || !this.newRule.categoryId) {
      alert('Please fill out all rule fields.');
      return;
    }
    this.ruleService.createRule(this.newRule).subscribe(() => {
      this.newRule = {
        fieldToMatch: RuleField.REMITTANCE_INFO,
        operator: RuleOperator.CONTAINS
      };
    });
  }

  applyAllRules(): void {
    this.isApplyingRules = true;
    this.ruleService.applyAllRules().subscribe({
      next: (response) => {
        alert(`${response.updatedCount} transaction(s) were updated by your rules.`);
        this.isApplyingRules = false;
      },
      error: (err) => {
        console.error('Failed to apply rules', err);
        alert('An error occurred while applying rules.');
        this.isApplyingRules = false;
      }
    });
  }

  getCategoryName(id: string): string {
    return this.categoryService.getCategoryName(id); 
  }

  deleteRule(id: string): void {
    if (confirm('Are you sure you want to delete this rule?')) {
      this.ruleService.deleteRule(id).subscribe();
    }
  }

  onAssetTransferChange(category: Category): void {
    this.categoryService.updateCategory(category).subscribe({
      next: () => console.log(`Category ${category.name} updated.`),
      error: err => console.error('Failed to update category', err)
    });
  }
  
}