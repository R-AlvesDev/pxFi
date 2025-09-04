import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService, Transaction as ApiTransaction, Category } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AccountStateService } from '../../services/account-state.service';
import { CategoryService } from '../../services/category.service';
import { NotificationService } from '../../services/notification.service'; // 1. Import NotificationService

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
  filteredTransactions: Transaction[] = [];
  error: string | null = null;
  loading = false;
  accessToken: string | null = null;
  mainCategories: Category[] = [];
  selectedCategoryFilter: string = 'all';
  isLinkingMode = false;
  selectedExpenseId: string | null = null;
  selectedIncomeId: string | null = null;
  currentPage: number = 1;
  itemsPerPage: number = 10;
  itemsPerPageOptions: number[] = [10, 25, 50, 100];
  startDate: string = '';
  endDate: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: ApiService,
    private accountState: AccountStateService,
    public categoryService: CategoryService,
    private notificationService: NotificationService 
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

    this.api.getAccountTransactions(this.accessToken, this.selectedAccountId, this.startDate, this.endDate).subscribe({
      next: (res) => {
        this.transactions = res.map(tx => ({ ...tx, expanded: false, categoryDirty: false }));
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load transactions: ' + err.message;
        this.loading = false;
        this.notificationService.show('Failed to load transactions.', 'error');
      }
    });
  }

  refreshTransactions(): void {
    if (!this.selectedAccountId || !this.accessToken) return;
    this.loading = true;
    this.error = null;
    this.notificationService.show('Refreshing transactions from the bank...', 'info');
    this.api.refreshTransactions(this.accessToken, this.selectedAccountId).subscribe({
      next: (res) => {
        this.transactions = res.map(tx => ({ ...tx, expanded: false, categoryDirty: false }));
        this.applyFilters();
        this.loading = false;
        this.notificationService.show('Transactions refreshed successfully!', 'success');
      },
      error: (err) => {
        this.error = 'Failed to refresh transactions: ' + err.message;
        this.loading = false;
        this.notificationService.show('Failed to refresh transactions.', 'error');
      }
    });
  }

  applyFilters(): void {
    this.currentPage = 1;
    if (this.selectedCategoryFilter === 'all') {
      this.filteredTransactions = [...this.transactions];
    } else if (this.selectedCategoryFilter === 'uncategorized') {
      this.filteredTransactions = this.transactions.filter(tx => !tx.categoryId);
    } else {
      this.filteredTransactions = this.transactions.filter(tx => tx.categoryId === this.selectedCategoryFilter);
    }
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
      this.notificationService.show('Please select a main category first.', 'error');
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
          this.applyFilters();
        }
        this.notificationService.show('Category saved!', 'success'); // 3. Add success notification
        this.promptToCategorizeSimilar(updatedTx, wasAlreadyCategorized);
      },
      error: err => {
        console.error('Failed to update category', err);
        this.notificationService.show('Failed to save category.', 'error'); // 4. Add error notification
      }
    });
  }

  private promptToCategorizeSimilar(updatedTx: Transaction, wasAlreadyCategorized: boolean): void {
    const remittanceInfo = updatedTx.remittanceInformationUnstructured;
    if (!remittanceInfo) return;

    const normalizedRemittanceInfo = remittanceInfo.trim().replace(/\s+/g, ' ');

    let similarCount = 0;
    const isAddingSubcategory = wasAlreadyCategorized && !!updatedTx.subCategoryId;
    const isNewCategorization = !wasAlreadyCategorized && !!updatedTx.categoryId;

    if (isAddingSubcategory) {
      similarCount = this.transactions.filter(t => {
        const currentRemittance = t.remittanceInformationUnstructured?.trim().replace(/\s+/g, ' ') || '';
        return t.id !== updatedTx.id &&
               currentRemittance === normalizedRemittanceInfo &&
               t.categoryId === updatedTx.categoryId &&
               !t.subCategoryId;
      }).length;
    } else if (isNewCategorization) {
      similarCount = this.transactions.filter(t => {
        const currentRemittance = t.remittanceInformationUnstructured?.trim().replace(/\s+/g, ' ') || '';
        return t.id !== updatedTx.id &&
               currentRemittance === normalizedRemittanceInfo &&
               !t.categoryId;
      }).length;
    }

    if (similarCount > 0) {
      const message = isAddingSubcategory
        ? `Found ${similarCount} other transaction(s) with the category "${updatedTx.categoryName}" but no subcategory. Apply the "${updatedTx.subCategoryName}" subcategory to them all?`
        : `Found ${similarCount} other uncategorized transaction(s) with the same description. Apply this category to them all?`;

      if (confirm(message)) {
        this.api.categorizeSimilarTransactions(remittanceInfo, updatedTx.categoryId!, updatedTx.subCategoryId || null).subscribe({
          next: () => {
            this.notificationService.show(`Applied category to ${similarCount} similar transaction(s).`, 'success');
            this.loadCachedTransactions();
          },
          error: (err) => {
            console.error('Failed to categorize similar transactions', err);
            this.notificationService.show('Failed to categorize similar transactions.', 'error');
          }
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
          this.applyFilters();
        }
        this.notificationService.show(`Transaction ignore status updated.`, 'success');
      },
      error: err => {
        console.error('Failed to toggle ignore status', err);
        this.notificationService.show('Failed to update ignore status.', 'error');
      }
    });
  }

  toggleLinkingMode(): void {
    this.isLinkingMode = !this.isLinkingMode;
    this.selectedExpenseId = null;
    this.selectedIncomeId = null;
    if (!this.isLinkingMode) {
      this.notificationService.show('Split bill mode cancelled.', 'info');
    } else {
      this.notificationService.show('Split bill mode activated. Select one income and one expense.', 'info');
    }
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
    if (!this.selectedExpenseId || !this.selectedIncomeId) {
      this.notificationService.show('You must select one income and one expense to link.', 'error');
      return;
    }
    this.api.linkTransactions(this.selectedExpenseId, this.selectedIncomeId).subscribe({
      next: () => {
        this.isLinkingMode = false;
        this.selectedExpenseId = null;
        this.selectedIncomeId = null;
        this.notificationService.show('Transactions linked successfully!', 'success'); // 5. Add success notification
        this.loadCachedTransactions();
      },
      error: (err) => {
        console.error('Failed to link transactions', err);
        this.notificationService.show('An error occurred while linking.', 'error'); // 6. Add error notification
      }
    });
  }
  
  get paginatedTransactions(): Transaction[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredTransactions.slice(startIndex, startIndex + this.itemsPerPage);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredTransactions.length / this.itemsPerPage);
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
  
  isNumber(value: any): value is number {
    return typeof value === 'number';
  }

  applyDateFilter(): void {
    if (this.startDate && this.endDate && this.startDate > this.endDate) {
      this.notificationService.show('Start date cannot be after end date.', 'error');
      return;
    }
    if (this.startDate && this.endDate) {
      this.loadCachedTransactions();
    }
  }

  clearDateFilter(): void {
    if (this.startDate || this.endDate) {
      this.startDate = '';
      this.endDate = '';
      this.loadCachedTransactions();
      this.notificationService.show('Date filter cleared.', 'info');
    }
  }
}