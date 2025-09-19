import { Component, ElementRef, ViewChild, inject } from '@angular/core';
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
  private accountState = inject(AccountStateService);
  private authService = inject(AuthService);

  currentAccountId$: Observable<string | null>;
  isLoggedIn$: Observable<boolean>;

  @ViewChild('navbarCollapse') navbarCollapse!: ElementRef;



  constructor() {
    this.currentAccountId$ = this.accountState.currentAccountId$;
    this.isLoggedIn$ = this.authService.loggedIn$;
  }

  logout(): void {
    this.closeMenu();
    this.authService.logout();
  }

  toggleMenu(): void {
    this.navbarCollapse.nativeElement.classList.toggle('show');
  }

  closeMenu(): void {
    if (this.navbarCollapse.nativeElement.classList.contains('show')) {
      this.navbarCollapse.nativeElement.classList.remove('show');
    }
  }
}