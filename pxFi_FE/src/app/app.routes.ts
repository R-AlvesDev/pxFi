import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { BankConnectionComponent } from './pages/bank-connection/bank-connection.component';
import { CallbackComponent } from './pages/callback/callback.component';
import { TransactionsComponent } from './pages/transactions/transactions.component';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component'; // Import new layout
import { SettingsComponent } from './pages/settings/settings.component';

export const routes: Routes = [
  // Routes that will use the main layout with the navbar
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', component: HomeComponent, title: 'pxFi - Home' },
      { path: 'transactions/:accountId', component: TransactionsComponent, title: 'pxFi - Transactions' },
      { path: 'settings', component: SettingsComponent, title: 'pxFi - Settings' } // <-- Add this new route
    ]
  },

  // Standalone routes without the main layout
  { path: 'connect', component: BankConnectionComponent, title: 'pxFi - Connect' },
  { path: 'callback', component: CallbackComponent, title: 'pxFi - Callback' },

  // Wildcard route to redirect to home
  { path: '**', redirectTo: '', pathMatch: 'full' }
];