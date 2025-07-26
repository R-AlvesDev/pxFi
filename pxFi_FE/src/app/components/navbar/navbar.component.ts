import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AsyncPipe, NgIf } from '@angular/common';
import { AccountStateService } from '../../services/account-state.service';
import { Observable } from 'rxjs';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, AsyncPipe, NgIf],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {
  // Create an observable for the account ID
  currentAccountId$: Observable<string | null>;
  isLoggedIn$: Observable<boolean>;

  constructor(private accountState: AccountStateService, private authService: AuthService) {
    this.currentAccountId$ = this.accountState.currentAccountId$;
    this.isLoggedIn$ = this.authService.loggedIn$;
  }

  logout(): void {
    this.authService.logout();
  }
}