import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-callback',
  templateUrl: './callback.component.html',
  styleUrls: ['./callback.component.scss']
})
export class CallbackComponent implements OnInit {
  message = 'Processing callback...';

  constructor(
    private route: ActivatedRoute,
    private api: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const ref = params['ref'];
      if (ref) {
        const requisitionId = localStorage.getItem('requisitionId');
        const accessToken = localStorage.getItem('accessToken');

        if (requisitionId && accessToken) {
          this.api.getRequisitionDetails(accessToken, requisitionId).subscribe({
            next: (res) => {
              this.message = 'Callback processed successfully!';

              // Navigate to transactions passing account ID in route param, no need for state now
              if (res.accounts && res.accounts.length > 0) {
                this.router.navigate(['/transactions', res.accounts[0]]);
              } else {
                this.message = 'No accounts found for this requisition.';
              }
            },
            error: (err) => {
              console.error('Error processing callback:', err);
              this.message = 'Error processing callback.';
            }
          });
        } else {
          this.message = 'Missing requisition or access token in localStorage.';
        }
      } else {
        this.message = 'No ref found in callback.';
      }
    });
  }
}
