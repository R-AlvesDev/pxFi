import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { BankConnectionComponent } from './pages/bank-connection/bank-connection.component';
import { CallbackComponent } from './pages/callback/callback.component';
import { TransactionsComponent } from './pages/transactions/transactions.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'connect', component: BankConnectionComponent },
  { path: 'callback', component: CallbackComponent },
  { path: 'transactions/:accountId', component: TransactionsComponent },
  { path: '**', redirectTo: '' }
];

