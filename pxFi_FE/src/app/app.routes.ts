import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { CallbackComponent } from './pages/callback/callback.component';
import { TransactionsComponent } from './pages/transactions/transactions.component';
import { StatisticsComponent } from './pages/statistics/statistics.component';
import { SettingsComponent } from './pages/settings/settings.component';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { AuthGuard } from './services/auth/auth.guard'; // Import the guard
import { AccountsComponent } from './pages/accounts/accounts.component'; // Import the Accounts component
import { BankConnectionComponent } from './pages/bank-connection/bank-connection.component';

export const routes: Routes = [
  // Public routes
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  // Protected routes (everything inside MainLayout)
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard], // This guard protects all child routes
    children: [
      { path: '', redirectTo: 'accounts', pathMatch: 'full' }, // Logged-in users default to the accounts page
      { path: 'dashboard', component: HomeComponent },         // The dashboard is now at /dashboard
      { path: 'accounts', component: AccountsComponent },       // The new accounts page
      { path: 'connect', component: BankConnectionComponent },
      { path: 'callback', component: CallbackComponent },
      { path: 'transactions/:accountId', component: TransactionsComponent },
      { path: 'statistics', component: StatisticsComponent },
      { path: 'settings', component: SettingsComponent },
    ]
  }
];