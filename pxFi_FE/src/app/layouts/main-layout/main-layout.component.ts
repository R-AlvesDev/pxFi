import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterModule } from '@angular/router';
import { NotificationComponent } from '../../components/notification/notification.component';
import { IonContent, IonFooter, IonToolbar, IonIcon, IonLabel } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { home, list, pieChart, settings, logOutOutline } from 'ionicons/icons';
import { AccountStateService } from '../../services/account-state.service';
import { AsyncPipe } from '@angular/common';
import { Observable } from 'rxjs';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    RouterModule,
    NotificationComponent,
    IonFooter,
    IonToolbar,
    IonIcon,
    IonLabel,
    AsyncPipe
  ],
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss'],
  host: {
    '[class.main-layout-host]': 'true'
  }
})
export class MainLayoutComponent {
  currentAccountId$: Observable<string | null>;
  private authService = inject(AuthService);

  constructor(private accountStateService: AccountStateService) {
    this.currentAccountId$ = this.accountStateService.currentAccountId$;
    addIcons({ home, list, pieChart, settings, 'log-out-outline': logOutOutline });
  }

  logout(): void {
    this.authService.logout();
  }
}