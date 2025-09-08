import { Component, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { ApiService, RequisitionResponse } from '../../services/api.service';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth/auth.service';
import { NotificationService } from '../../services/notification.service'; // Import NotificationService

@Component({
  selector: 'app-bank-connection',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './bank-connection.component.html',
  styleUrls: ['./bank-connection.component.scss']
})
export class BankConnectionComponent implements OnInit {
  countries = [
    { code: 'pt', name: 'Portugal' },
    { code: 'gb', name: 'United Kingdom' },
    { code: 'fr', name: 'France' },
    { code: 'de', name: 'Germany' },
    { code: 'es', name: 'Spain' },
  ];

  banks: { id: string; name: string }[] = [];

  countryControl = new FormControl('');
  bankControl = new FormControl('');

  error: string | null = null;
  loadingBanks = false;
  loadingConnection = false;

  accessToken: string | null = null;

  constructor(
    private api: ApiService, 
    private router: Router, 
    private authService: AuthService,
    private notificationService: NotificationService // Inject NotificationService
  ) {}

  ngOnInit() {
    // This component is for logged-in users, so we can get a temporary token.
    this.api.getAccessToken().subscribe({
      next: (tokenObj) => {
        this.accessToken = tokenObj.accessToken;
        
        this.countryControl.valueChanges.subscribe((countryCode: string | null) => {
          this.banks = [];
          this.bankControl.reset('');
          this.error = null;
          if (countryCode && this.accessToken) {
            this.loadingBanks = true;
            this.api.getInstitutions(this.accessToken, countryCode).subscribe({
              next: (banks) => {
                this.banks = banks;
                this.loadingBanks = false;
              },
              error: (err) => {
                this.error = 'Failed to load banks: ' + err.message;
                this.loadingBanks = false;
              }
            });
          }
        });
      },
      error: (err) => {
        this.error = 'Failed to get access token: ' + err.message;
      }
    });
  }

  connect() {
    if (!this.bankControl.value) {
      this.notificationService.show('Please select a bank.', 'error');
      return;
    }
    if (!this.accessToken) {
        this.notificationService.show('Access token is missing. Please try again.', 'error');
      return;
    }

    this.loadingConnection = true;

    this.api.createEndUserAgreement(this.accessToken, this.bankControl.value).subscribe({
      next: (agreementResponse) => {
        this.api.createRequisition(this.accessToken!, this.bankControl.value!, agreementResponse.id).subscribe({
          next: (requisitionResponse) => {
            this.loadingConnection = false;
            if (requisitionResponse.id && requisitionResponse.link) {
              localStorage.setItem('requisitionId', requisitionResponse.id);
              // Store the gocardless token, not the user's JWT
              localStorage.setItem('gocardlessToken', this.accessToken!);

              window.location.href = requisitionResponse.link;
            } else {
              this.error = 'No connection link returned.';
            }
          },
          error: (err) => {
            this.loadingConnection = false;
            this.error = 'Failed to create requisition: ' + err.message;
          }
        });
      },
      error: (err) => {
        this.loadingConnection = false;
        this.error = 'Failed to create agreement: ' + err.message;
      }
    });
  }
}