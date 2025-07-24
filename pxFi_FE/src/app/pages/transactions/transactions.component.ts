import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService, Transaction as ApiTransaction } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: ApiService
  ) {}

  ngOnInit(): void {
    this.accessToken = localStorage.getItem('accessToken');
    if (!this.accessToken) {
      this.error = 'Access token missing. Please reconnect your bank.';
      return;
    }

    this.route.paramMap.subscribe(params => {
      const accountId = params.get('accountId');
      if (accountId) {
        this.selectedAccountId = accountId;
        this.loadCachedTransactions(); // Load from Mongo on init
      } else {
        this.error = 'No account selected.';
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

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('requisitionId');
    this.router.navigate(['/connect']);
  }
}