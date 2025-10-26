import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ApiService, Account } from '../../services/api.service';
import { AccountStateService } from '../../services/account-state.service';
import { AuthService } from '../../services/auth/auth.service';
import { NotificationService } from '../../services/notification.service';
import { FormsModule } from '@angular/forms';
import { IonHeader, IonToolbar, IonTitle, IonContent, IonSpinner, IonButton, IonIcon } from '@ionic/angular/standalone';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, IonHeader, IonToolbar, IonTitle, IonContent, IonSpinner, IonButton, IonIcon],
  templateUrl: './accounts.component.html',
  styleUrls: ['./accounts.component.scss']
})
export class AccountsComponent implements OnInit {
  private api = inject(ApiService);
  private authService = inject(AuthService);
  private accountState = inject(AccountStateService);
  private router = inject(Router);
  private notificationService = inject(NotificationService);

  accounts: Account[] = [];
  loading = true;

  editingAccountId: string | null = null;
  originalAccountName = '';

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.loading = true;
    this.api.getAccounts().subscribe({
      next: (data) => {
        this.accounts = data;
        this.loading = false;
      },
      error: () => {
        this.notificationService.show('Failed to load accounts.', 'error');
        this.loading = false;
      }
    });
  }

  selectAccount(account: Account): void {
    if (this.editingAccountId !== account.id) {
      this.accountState.setCurrentAccountId(account.gocardlessAccountId);
      this.router.navigate(['/dashboard', account.gocardlessAccountId]);
    }
  }

  startEditing(account: Account, event: MouseEvent): void {
    event.stopPropagation();
    this.editingAccountId = account.id;
    this.originalAccountName = account.accountName;
  }

  cancelEditing(account: Account, event: Event): void {
    event.stopPropagation();
    account.accountName = this.originalAccountName;
    this.editingAccountId = null;
  }

  saveAccountName(account: Account, event: Event): void {
    event.stopPropagation();
    if (!account.accountName || account.accountName.trim() === '') {
      this.notificationService.show('Account name cannot be empty.', 'error');
      account.accountName = this.originalAccountName;
      return;
    }

    this.api.updateAccountName(account.id, account.accountName).subscribe({
      next: (updatedAccount) => {
        const index = this.accounts.findIndex(a => a.id === updatedAccount.id);
        if (index !== -1) {
          this.accounts[index] = updatedAccount;
        }
        this.notificationService.show('Account name updated!', 'success');
        this.editingAccountId = null;
      },
      error: () => {
        this.notificationService.show('Failed to update name.', 'error');
        account.accountName = this.originalAccountName;
      }
    });
  }

  deleteAccount(accountId: string, event: Event): void {
    event.stopPropagation();

    if (confirm('Are you sure you want to delete this account? This will also delete all of its transactions.')) {
      this.api.deleteAccount(accountId).subscribe({
        next: () => {
          this.notificationService.show('Account deleted successfully!', 'success');
          this.loadAccounts();
        },
        error: () => {
          this.notificationService.show('Failed to delete account.', 'error');
        }
      });
    }
  }
}