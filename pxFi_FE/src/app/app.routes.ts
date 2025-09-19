import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { CallbackComponent } from './pages/callback/callback.component';
import { TransactionsComponent } from './pages/transactions/transactions.component';
import { StatisticsComponent } from './pages/statistics/statistics.component';
import { SettingsComponent } from './pages/settings/settings.component';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { AuthGuard } from './services/auth/auth.guard';
import { AccountsComponent } from './pages/accounts/accounts.component';
import { BankConnectionComponent } from './pages/bank-connection/bank-connection.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';

export const routes: Routes = [
  // Public routes
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: '', component: HomeComponent, pathMatch: 'full' },

  // Protected routes (everything inside MainLayout)
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'accounts', component: AccountsComponent },
      { path: 'dashboard/:accountId', component: DashboardComponent },
      { path: 'connect', component: BankConnectionComponent },
      { path: 'callback', component: CallbackComponent },
      { path: 'transactions/:accountId', component: TransactionsComponent },
      { path: 'statistics', component: StatisticsComponent },
      { path: 'settings', component: SettingsComponent },
    ]
  }
];