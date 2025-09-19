import { Component, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private notificationService = inject(NotificationService);

  credentials = {
    username: '',
    password: ''
  };

  onLogin(): void {
    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.router.navigate(['/accounts']);
        this.notificationService.show('Login successful!', 'success');
      },
      error: () => {
        this.notificationService.show('Login failed. Please check your credentials.', 'error');
      }
    });
  }
}