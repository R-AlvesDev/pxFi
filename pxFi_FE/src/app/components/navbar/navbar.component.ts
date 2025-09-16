import { Component, ElementRef, ViewChild } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AsyncPipe } from '@angular/common';
import { AccountStateService } from '../../services/account-state.service';
import { Observable } from 'rxjs';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, AsyncPipe],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {
  // Create an observable for the account ID
  currentAccountId$: Observable<string | null>;
  isLoggedIn$: Observable<boolean>;

  @ViewChild('navbarCollapse') navbarCollapse!: ElementRef;

  constructor(private accountState: AccountStateService, private authService: AuthService) {
    this.currentAccountId$ = this.accountState.currentAccountId$;
    this.isLoggedIn$ = this.authService.loggedIn$;
  }

  logout(): void {
    this.closeMenu(); // Also close menu on logout
    this.authService.logout();
  }

  closeMenu(): void {
    if (this.navbarCollapse.nativeElement.classList.contains('show')) {
      this.navbarCollapse.nativeElement.classList.remove('show');
    }
  }
}