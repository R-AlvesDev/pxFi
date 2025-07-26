import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService, Transaction as ApiTransaction, Category } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AccountStateService } from '../../services/account-state.service';
import { CategoryService } from '../../services/category.service';

interface Transaction extends ApiTransaction {
  expanded?: boolean;
  categoryDirty?: boolean;
}

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.scss']
})
export class TransactionsComponent implements OnInit {
  selectedAccountId: string | null = null;
  transactions: Transaction[] = [];
  error: string | null = null;
  loading = false;
  accessToken: string | null = null;
  mainCategories: Category[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: ApiService,
    private accountState: AccountStateService,
    public categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.accessToken = localStorage.getItem('accessToken');
    if (!this.accessToken) {
      this.error = 'Access token missing. Please reconnect your bank.';
      return;
    }

    this.categoryService.categories$.subscribe(() => {
      this.mainCategories = this.categoryService.getMainCategories();
    });

    this.route.paramMap.subscribe(params => {
      const accountId = params.get('accountId');
      if (accountId) {
        this.selectedAccountId = accountId;
        this.accountState.setCurrentAccountId(accountId);
        this.loadCachedTransactions();
      } else {
        this.error = 'No account selected.';
        this.accountState.setCurrentAccountId(null);
      }
    });
  }

  loadCachedTransactions(): void {
    if (!this.selectedAccountId || !this.accessToken) return;
    this.loading = true;
    this.error = null;
    this.api.getAccountTransactions(this.accessToken, this.selectedAccountId).subscribe({
      next: (res) => {
        this.transactions = res.map(tx => ({ ...tx, expanded: false, categoryDirty: false }));
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load cached transactions: ' + err.message;
        this.loading = false;
      }
    });
  }

  refreshTransactions(): void {
    if (!this.selectedAccountId || !this.accessToken) return;
    this.loading = true;
    this.error = null;
    this.api.refreshTransactions(this.accessToken, this.selectedAccountId).subscribe({
      next: (res) => {
        this.transactions = res.map(tx => ({ ...tx, expanded: false, categoryDirty: false }));
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to refresh transactions: ' + err.message;
        this.loading = false;
      }
    });
  }

  onCategorySelectionChange(transaction: Transaction): void {
    transaction.subCategoryId = null; // Reset subcategory
    transaction.categoryDirty = true;
  }

  onSubCategorySelectionChange(transaction: Transaction): void {
    transaction.categoryDirty = true;
  }

  saveCategory(transaction: Transaction): void {
    if (!transaction.categoryId) {
      console.error("Cannot save without a main category.");
      return;
    }

    // Find the original transaction state to check its previous category status
    const originalTransaction = this.transactions.find(t => t.id === transaction.id);
    const wasAlreadyCategorized = !!originalTransaction?.categoryId;

    this.api.updateTransactionCategory(transaction.id, transaction.categoryId, transaction.subCategoryId || null).subscribe({
      next: updatedTx => {
        const index = this.transactions.findIndex(t => t.id === updatedTx.id);
        if (index !== -1) {
          const wasExpanded = this.transactions[index].expanded;
          this.transactions[index] = { ...updatedTx, expanded: wasExpanded, categoryDirty: false };
        }
        this.promptToCategorizeSimilar(updatedTx, wasAlreadyCategorized);
      },
      error: err => console.error('Failed to update category', err)
    });
  }

  private promptToCategorizeSimilar(updatedTx: Transaction, wasAlreadyCategorized: boolean): void {
    const remittanceInfo = updatedTx.remittanceInformationUnstructured;
    if (!remittanceInfo) return;

    let similarCount = 0;
    let isSubcategoryUpdate = wasAlreadyCategorized && !!updatedTx.subCategoryId;

    if (isSubcategoryUpdate) {
      // Find transactions with same main category but missing a subcategory
      similarCount = this.transactions.filter(t =>
        t.id !== updatedTx.id &&
        t.remittanceInformationUnstructured === remittanceInfo &&
        t.categoryId === updatedTx.categoryId &&
        !t.subCategoryId
      ).length;
    } else {
      // Find completely uncategorized transactions
      similarCount = this.transactions.filter(t =>
        t.id !== updatedTx.id &&
        t.remittanceInformationUnstructured === remittanceInfo &&
        !t.categoryId
      ).length;
    }

    if (similarCount > 0) {
      const message = isSubcategoryUpdate
        ? `Found ${similarCount} other transaction(s) with the category "${updatedTx.categoryName}" but no subcategory. Apply the "${updatedTx.subCategoryName}" subcategory to them all?`
        : `Found ${similarCount} other uncategorized transaction(s) with the same description. Apply this category to them all?`;

      const userConfirmed = confirm(message);

      if (userConfirmed) {
        this.api.categorizeSimilarTransactions(remittanceInfo, updatedTx.categoryId!, updatedTx.subCategoryId || null).subscribe({
          next: () => this.loadCachedTransactions(),
          error: (err) => console.error('Failed to categorize similar transactions', err)
        });
      }
    }
  }
}