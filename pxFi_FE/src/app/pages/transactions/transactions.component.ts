import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService, Transaction as ApiTransaction, Category } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AccountStateService } from '../../services/account-state.service';
import { CategoryService } from '../../services/category.service';

// Extend the Transaction interface for UI state
interface Transaction extends ApiTransaction {
  expanded?: boolean;
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

    this.categoryService.categories$.subscribe(categories => {
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

  // This function loads what's already in our database
  loadCachedTransactions(): void {
    if (!this.selectedAccountId || !this.accessToken) return;

    this.loading = true;
    this.error = null;
    this.api.getAccountTransactions(this.accessToken, this.selectedAccountId).subscribe({
      next: (res) => {
        this.transactions = res.map(tx => ({ ...tx, expanded: false }));
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load cached transactions: ' + err.message;
        this.loading = false;
      }
    });
  }

  // This function calls the API to get new data and update the database
  refreshTransactions(): void {
    if (!this.selectedAccountId || !this.accessToken) return;

    this.loading = true;
    this.error = null;
    // Use the dedicated 'refresh' endpoint in the API service
    this.api.refreshTransactions(this.accessToken, this.selectedAccountId).subscribe({
      next: (res) => {
        this.transactions = res.map(tx => ({ ...tx, expanded: false }));
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to refresh transactions: ' + err.message;
        this.loading = false;
      }
    });
  }

  onCategoryChange(transaction: Transaction, newCategoryId: string): void {
    // When a main category is selected, clear the subcategory
    this.updateCategory(transaction, newCategoryId, null);
  }

  onSubCategoryChange(transaction: Transaction, newSubCategoryId: string): void {
    this.updateCategory(transaction, transaction.categoryId!, newSubCategoryId);
  }

  private updateCategory(transaction: Transaction, categoryId: string, subCategoryId: string | null): void {
    this.api.updateTransactionCategory(transaction.id, categoryId, subCategoryId).subscribe({
      next: updatedTx => {
        // Update the transaction in the local array to reflect the change immediately
        const index = this.transactions.findIndex(t => t.id === updatedTx.id);
        if (index !== -1) {
          this.transactions[index] = { ...this.transactions[index], ...updatedTx };
        }
      },
      error: err => console.error('Failed to update category', err)
    });
  }
  
}