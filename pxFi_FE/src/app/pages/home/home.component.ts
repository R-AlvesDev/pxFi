import { Component } from '@angular/core';
import { AsyncPipe, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../services/auth/auth.service';
import { BankConnectionComponent } from '../bank-connection/bank-connection.component';
import { DashboardComponent } from '../dashboard/dashboard.component'; // Import DashboardComponent

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [NgIf, AsyncPipe, RouterLink, BankConnectionComponent, DashboardComponent], // Add DashboardComponent
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {
  isLoggedIn$: Observable<boolean>;

  constructor(private authService: AuthService) {
    this.isLoggedIn$ = this.authService.loggedIn$;
  }
}