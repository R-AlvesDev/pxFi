import { Component, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { ApiService, RequisitionResponse } from '../../services/api.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-bank-connection',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
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

  constructor(private api: ApiService, private router: Router, private authService: AuthService) {}

  ngOnInit() {
    this.api.getAccessToken().subscribe({
      next: (tokenObj) => {
        this.accessToken = tokenObj.accessToken;
        localStorage.setItem('accessToken', this.accessToken); // <-- Save token here

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
      this.error = 'Please select a bank.';
      return;
    }
    if (!this.accessToken) {
      this.error = 'Access token is missing.';
      return;
    }

    this.loadingConnection = true;

    this.api.createEndUserAgreement(this.accessToken, this.bankControl.value).subscribe({
      next: (agreementResponse) => {
        this.api.createRequisition(this.accessToken!, this.bankControl.value!, agreementResponse.id).subscribe({
          next: (requisitionResponse) => {
            this.loadingConnection = false;
            if (requisitionResponse.id && requisitionResponse.link) {
              // Save requisition ID and accessToken in localStorage for use in callback and transactions
              localStorage.setItem('requisitionId', requisitionResponse.id);
              localStorage.setItem('accessToken', this.accessToken!);

              this.authService.updateLoginState();
              
              // Redirect to bank authentication link
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
