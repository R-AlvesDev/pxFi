import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../../services/category.service';
import { RuleService } from '../../services/rule.service';
import { Category, CategorizationRule, RuleField, RuleOperator, TestRuleResponse } from '../../services/api.service';
import { Observable } from 'rxjs';
import { NotificationService } from '../../services/notification.service';
import { AccountStateService } from '../../services/account-state.service';
import { Modal } from 'bootstrap';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
  categoryService = inject(CategoryService);
  ruleService = inject(RuleService);
  private notificationService = inject(NotificationService);
  private accountState = inject(AccountStateService);

  // Properties
  categories$: Observable<Category[]>;
  mainCategories: Category[] = [];
  newCategoryName = '';
  newCategoryParentId: string | null = null;
  expandedCategoryIds = new Set<string>();
  isApplyingRules = false;

  rules$: Observable<CategorizationRule[]>;
  newRule: Partial<CategorizationRule> = {
    fieldToMatch: RuleField.REMITTANCE_INFO,
    operator: RuleOperator.CONTAINS
  };
  ruleFields = Object.values(RuleField);
  textOperators = [RuleOperator.CONTAINS, RuleOperator.EQUALS, RuleOperator.STARTS_WITH, RuleOperator.ENDS_WITH];
  amountOperators = [RuleOperator.AMOUNT_EQUALS, RuleOperator.AMOUNT_GREATER_THAN, RuleOperator.AMOUNT_LESS_THAN];

  testResults: TestRuleResponse | null = null;
  private currentAccountId: string | null = null;
  private testRuleModal: Modal | undefined;



  constructor() {
    this.categories$ = this.categoryService.categories$;
    this.rules$ = this.ruleService.rules$;
  }

  ngOnInit(): void {
    // Refresh the rules and categories when the component loads
    this.ruleService.getAllRules().subscribe();
    this.categoryService.refreshCategories();

    this.categories$.subscribe(() => {
      this.mainCategories = this.categoryService.getMainCategories();
    });
    this.accountState.currentAccountId$.subscribe(id => this.currentAccountId = id);
  }

  get availableOperators(): RuleOperator[] {
    if (this.newRule.fieldToMatch === RuleField.AMOUNT) {
      return this.amountOperators;
    }
    return this.textOperators;
  }

  onFieldToMatchChange(): void {
    this.newRule.operator = this.availableOperators[0];
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

  // --- Rule Methods ---
  createRule(): void {
    if (!this.newRule.valueToMatch || !this.newRule.categoryId) {
      this.notificationService.show('Please fill out all rule fields.', 'error');
      return;
    }
    this.ruleService.createRule(this.newRule).subscribe({
      next: () => {
        this.notificationService.show('Rule created successfully!', 'success');
        this.newRule = {
          fieldToMatch: RuleField.REMITTANCE_INFO,
          operator: RuleOperator.CONTAINS
        };
      },
      error: (err) => this.notificationService.show('Error creating rule: ' + err.message, 'error')
    });
  }

  applyAllRules(): void {
    this.isApplyingRules = true;
    this.notificationService.show('Applying rules to all transactions...', 'info', 10000);
    this.ruleService.applyAllRules().subscribe({
      next: (response) => {
        this.notificationService.show(`Successfully updated ${response.updatedCount} transaction(s)!`, 'success');
        this.isApplyingRules = false;
      },
      error: (err) => {
        this.notificationService.show('Failed to apply rules: ' + err.message, 'error');
        this.isApplyingRules = false;
      }
    });
  }

  getCategoryName(id: string): string {
    return this.categoryService.getCategoryName(id);
  }

  deleteRule(id: string): void {
    if (confirm('Are you sure you want to delete this rule?')) {
      this.ruleService.deleteRule(id).subscribe({
        next: () => this.notificationService.show('Rule deleted.', 'success'),
        error: (err) => this.notificationService.show('Error deleting rule: ' + err.message, 'error')
      });
    }
  }

  testRule(): void {
    if (!this.newRule.valueToMatch || !this.currentAccountId) {
      this.notificationService.show('Please select an account and fill out the rule value.', 'error');
      return;
    }

    this.ruleService.testRule(this.newRule, this.currentAccountId).subscribe({
      next: (response) => {
        this.testResults = response;
        const modalElement = document.getElementById('testRuleModal');
        if (modalElement) {
          this.testRuleModal = new Modal(modalElement);
          this.testRuleModal.show();
        }
      },
      error: (err) => this.notificationService.show('Error testing rule: ' + err.message, 'error')
    });
  }
}