import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService, Transaction } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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
        // 🚫 Do not auto-load transactions
      } else {
        this.error = 'No account selected.';
      }
    });
  }

  refreshTransactions(): void {
    if (!this.selectedAccountId) {
      this.error = 'No account selected.';
      return;
    }

    if (!this.accessToken) {
      this.error = 'Missing access token.';
      return;
    }

    this.loading = true;
    this.transactions = [];
    this.error = null;

    this.api.getAccountTransactions(this.accessToken, this.selectedAccountId).subscribe({
      next: (res) => {
        this.transactions = res;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load transactions: ' + err.message;
        this.loading = false;
      }
    });
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('requisitionId');
    this.router.navigate(['/connect']); // or wherever your login page is
  }
}
