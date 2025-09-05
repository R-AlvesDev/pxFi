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
  accessToken: string | null = null;

  constructor(
    private api: ApiService,
    private authService: AuthService,
    private accountState: AccountStateService,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.accessToken = localStorage.getItem('accessToken');
    if (this.accessToken) {
      this.accounts$ = this.api.getAccounts(this.accessToken);
      this.accounts$.subscribe({
        next: () => this.loading = false,
        error: (err) => {
          this.notificationService.show('Failed to load accounts.', 'error');
          this.loading = false;
        }
      });
    }
  }

  selectAccount(account: Account): void {
    this.accountState.setCurrentAccountId(account.gocardlessAccountId);
    this.router.navigate(['/dashboard']); // Navigate to the dashboard (home route)
  }
}