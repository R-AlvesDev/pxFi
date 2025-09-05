import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  registerData = {
    username: '',
    email: '',
    password: ''
  };

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  onRegister(): void {
    this.authService.register(this.registerData).subscribe({
      next: () => {
        this.router.navigate(['/']);
        this.notificationService.show('Registration successful! Welcome!', 'success');
      },
      error: (err) => {
        this.notificationService.show('Registration failed. Please try again.', 'error');
      }
    });
  }
}