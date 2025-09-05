import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  credentials = {
    username: '',
    password: ''
  };

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  onLogin(): void {
    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.router.navigate(['/accounts']); 
        this.notificationService.show('Login successful!', 'success');
      },
      error: (err) => {
        this.notificationService.show('Login failed. Please check your credentials.', 'error');
      }
    });
  }
}