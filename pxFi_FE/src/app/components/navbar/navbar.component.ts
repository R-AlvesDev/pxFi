import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AsyncPipe, NgIf } from '@angular/common';
import { AccountStateService } from '../../services/account-state.service';
import { Observable } from 'rxjs';

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

  constructor(private accountState: AccountStateService) {
    this.currentAccountId$ = this.accountState.currentAccountId$;
  }
}