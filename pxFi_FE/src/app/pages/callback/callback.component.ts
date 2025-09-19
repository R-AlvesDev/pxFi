import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-callback',
  standalone: true,
  imports: [],
  templateUrl: './callback.component.html',
  styleUrls: ['./callback.component.scss']
})
export class CallbackComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  private router = inject(Router);
  private notificationService = inject(NotificationService);

  message = 'Processing bank connection, please wait...';

  ngOnInit(): void {
    const requisitionId = localStorage.getItem('requisitionId');
    const gocardlessToken = localStorage.getItem('gocardlessToken');

    if (!requisitionId || !gocardlessToken) {
      this.message = 'Error: Missing connection data. Please try connecting again.';
      this.notificationService.show(this.message, 'error');
      this.router.navigate(['/accounts']);
      return;
    }

    this.api.getRequisitionDetails(gocardlessToken, requisitionId).pipe(
      switchMap(details => {
        if (details.status === 'LN' && details.accounts.length > 0) {
          this.message = 'Connection successful! Saving your account...';
          const accountData = {
            gocardlessAccountId: details.accounts[0],
            institutionId: details.institution_id,
            accountName: details.institution_id
          };
          return this.api.saveAccount(accountData);
        } else {
          throw new Error('Bank connection was not completed or no accounts were shared.');
        }
      })
    ).subscribe({
      next: () => {
        this.message = 'Account saved! Redirecting...';
        this.notificationService.show('New account connected successfully!', 'success');
        localStorage.removeItem('requisitionId');
        localStorage.removeItem('gocardlessToken');
        this.router.navigate(['/accounts']);
      },
      error: (err) => {
        console.error('Error processing callback:', err);
        this.message = 'An error occurred while finalizing the connection. Please try again.';
        this.notificationService.show(err.message || this.message, 'error');
        localStorage.removeItem('requisitionId');
        localStorage.removeItem('gocardlessToken');
        this.router.navigate(['/accounts']);
      }
    });
  }
}