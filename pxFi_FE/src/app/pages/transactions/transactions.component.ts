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

  isLinkingMode = false;
  selectedExpenseId: string | null = null;
  selectedIncomeId: string | null = null;

  currentPage: number = 1;
  itemsPerPage: number = 10;
  itemsPerPageOptions: number[] = [10, 25, 50, 100];

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
    transaction.subCategoryId = null;
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
      similarCount = this.transactions.filter(t =>
        t.id !== updatedTx.id &&
        t.remittanceInformationUnstructured === remittanceInfo &&
        t.categoryId === updatedTx.categoryId &&
        !t.subCategoryId
      ).length;
    } else {
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

  toggleIgnore(transaction: Transaction): void {
    this.api.toggleTransactionIgnore(transaction.id).subscribe({
      next: updatedTx => {
        const index = this.transactions.findIndex(t => t.id === updatedTx.id);
        if (index !== -1) {
          const wasExpanded = this.transactions[index].expanded;
          this.transactions[index] = { ...updatedTx, expanded: wasExpanded };
        }
      },
      error: err => console.error('Failed to toggle ignore status', err)
    });
  }

  toggleLinkingMode(): void {
    this.isLinkingMode = !this.isLinkingMode;
    this.selectedExpenseId = null;
    this.selectedIncomeId = null;
  }

  selectTransactionForLink(transaction: Transaction): void {
    if (!this.isLinkingMode) return;
    const amount = +transaction.transactionAmount.amount;
    if (amount < 0) {
      this.selectedExpenseId = this.selectedExpenseId === transaction.id ? null : transaction.id;
    } else if (amount > 0) {
      this.selectedIncomeId = this.selectedIncomeId === transaction.id ? null : transaction.id;
    }
  }

  linkSelectedTransactions(): void {
    if (!this.selectedExpenseId || !this.selectedIncomeId) return;
    this.api.linkTransactions(this.selectedExpenseId, this.selectedIncomeId).subscribe({
      next: () => {
        this.isLinkingMode = false;
        this.selectedExpenseId = null;
        this.selectedIncomeId = null;
        this.loadCachedTransactions();
      },
      error: (err) => {
        console.error('Failed to link transactions', err);
        alert('An error occurred while linking transactions.');
      }
    });
  }

  get paginatedTransactions(): Transaction[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return this.transactions.slice(startIndex, endIndex);
  }

  get totalPages(): number {
    return Math.ceil(this.transactions.length / this.itemsPerPage);
  }

  get pages(): (number | string)[] {
    const total = this.totalPages;
    const current = this.currentPage;
    const delta = 2;
    const range = [];
    range.push(1);
    if (current > delta + 2) {
      range.push('...');
    }
    for (let i = Math.max(2, current - delta); i <= Math.min(total - 1, current + delta); i++) {
      range.push(i);
    }
    if (current < total - delta - 1) {
      range.push('...');
    }
    if (total > 1) {
      range.push(total);
    }
    return range;
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  onItemsPerPageChange(): void {
    this.currentPage = 1;
  }

  /**
   * A type guard to check if a value is a number.
   * This is used in the template to satisfy the TypeScript compiler.
   */
  isNumber(value: any): value is number {
    return typeof value === 'number';
  }
}