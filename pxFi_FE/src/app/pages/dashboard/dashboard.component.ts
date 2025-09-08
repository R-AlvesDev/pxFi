import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService, DashboardSummary } from '../../services/api.service';
import { AccountStateService } from '../../services/account-state.service';
import { NotificationService } from '../../services/notification.service';
import { switchMap, of, tap } from 'rxjs';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  summary: DashboardSummary | null = null;
  loading = true;
  currentAccountId: string | null = null;

  constructor(
    private api: ApiService,
    private accountState: AccountStateService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {

    this.accountState.currentAccountId$.pipe(
      tap(() => {
        this.loading = true; 
        this.summary = null;
      }),
      switchMap(accountId => {
        if (accountId) {
          this.currentAccountId = accountId;
          return this.api.getDashboardSummary(accountId);
        }
        return of(null); 
      })
    ).subscribe({
      next: (summaryData) => {
        this.summary = summaryData;
        this.loading = false;
      },
      error: (err) => {
        this.notificationService.show('Failed to load dashboard summary.', 'error');
        this.loading = false;
      }
    });
  }
}