import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RuleService } from '../../../services/rule.service';
import { CategoryService } from '../../../services/category.service';
import { CategorizationRule, RuleField, RuleOperator, TestRuleResponse, Category } from '../../../services/api.service';
import { Observable } from 'rxjs';
import { NotificationService } from '../../../services/notification.service';
import { AccountStateService } from '../../../services/account-state.service';
import { IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonInput, IonSelect, IonSelectOption, IonButton, IonButtons, IonBackButton, IonSpinner, IonIcon, ModalController } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { trashOutline } from 'ionicons/icons';
import { TestRuleResultModalComponent } from '../../../components/modals/test-rule-result-modal/test-rule-result-modal.component';

@Component({
  selector: 'app-manage-rules',
  standalone: true,
  imports: [CommonModule, FormsModule, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonInput, IonSelect, IonSelectOption, IonButton, IonButtons, IonBackButton, IonSpinner, IonIcon, TestRuleResultModalComponent],
  templateUrl: './manage-rules.component.html',
  styleUrls: ['./manage-rules.component.scss']
})
export class ManageRulesComponent implements OnInit {
  ruleService = inject(RuleService);
  categoryService = inject(CategoryService);
  private notificationService = inject(NotificationService);
  private accountState = inject(AccountStateService);
  private modalCtrl = inject(ModalController);

  isApplyingRules = false;
  rules$: Observable<CategorizationRule[]>;
  newRule: Partial<CategorizationRule> = {
    fieldToMatch: RuleField.REMITTANCE_INFO,
    operator: RuleOperator.CONTAINS
  };
  ruleFields = Object.values(RuleField);
  textOperators = [RuleOperator.CONTAINS, RuleOperator.EQUALS, RuleOperator.STARTS_WITH, RuleOperator.ENDS_WITH];
  amountOperators = [RuleOperator.AMOUNT_EQUALS, RuleOperator.AMOUNT_GREATER_THAN, RuleOperator.AMOUNT_LESS_THAN];
  mainCategories: Category[] = [];
  testResults: TestRuleResponse | null = null;
  private currentAccountId: string | null = null;

  constructor() {
    this.rules$ = this.ruleService.rules$;
    addIcons({ 'trash-outline': trashOutline });
  }

  ngOnInit(): void {
    this.ruleService.getAllRules().subscribe();
    this.categoryService.refreshCategories();
    this.categoryService.categories$.subscribe(() => {
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

  async testRule(): Promise<void> {
    if (!this.newRule.valueToMatch || !this.currentAccountId) {
      this.notificationService.show('Please select an account and fill out the rule value.', 'error');
      return;
    }

    this.ruleService.testRule(this.newRule, this.currentAccountId).subscribe({
      next: async (response) => {
        this.testResults = response;
        const modal = await this.modalCtrl.create({
          component: TestRuleResultModalComponent,
          componentProps: {
            testResults: this.testResults
          }
        });
        await modal.present();
      },
      error: (err) => this.notificationService.show('Error testing rule: ' + err.message, 'error')
    });
  }
}
