import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { ApiService, Account } from '../../services/api.service';
import { AccountStateService } from '../../services/account-state.service';
import { AuthService } from '../../services/auth/auth.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './accounts.component.html',
  styleUrls: ['./accounts.component.scss']
})
export class AccountsComponent implements OnInit {
  accounts$: Observable<Account[]> | undefined;
  loading = true;

  constructor(
    private api: ApiService,
    private authService: AuthService,
    private accountState: AccountStateService,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.accounts$ = this.api.getAccounts();
    this.accounts$.subscribe({
      next: () => this.loading = false,
      error: (err) => {
        this.notificationService.show('Failed to load accounts.', 'error');
        this.loading = false;
      }
    });
  }

  selectAccount(account: Account): void {
    this.accountState.setCurrentAccountId(account.gocardlessAccountId);
    // FIX: Pass the account's gocardlessAccountId as a parameter in the route
    this.router.navigate(['/dashboard', account.gocardlessAccountId]);
  }
}