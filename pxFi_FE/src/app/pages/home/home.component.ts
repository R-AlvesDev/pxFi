import { Component } from '@angular/core';
import { AsyncPipe, NgIf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Observable, take, tap } from 'rxjs';
import { AuthService } from '../../services/auth/auth.service';
import { BankConnectionComponent } from '../bank-connection/bank-connection.component';
import { DashboardComponent } from '../dashboard/dashboard.component'; // Import DashboardComponent

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [NgIf, AsyncPipe, RouterLink], 
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {
  isLoggedIn$: Observable<boolean>;

  constructor(private authService: AuthService, private router: Router) {
    this.isLoggedIn$ = this.authService.loggedIn$;
  }

  ngOnInit(): void {
    this.isLoggedIn$.pipe(
      take(1), // Take the first value to prevent multiple redirects
      tap(isLoggedIn => {
        if (isLoggedIn) {
          // If the user is logged in, redirect them immediately to their accounts page
          this.router.navigate(['/accounts']);
        }
      })
    ).subscribe();
  }
}