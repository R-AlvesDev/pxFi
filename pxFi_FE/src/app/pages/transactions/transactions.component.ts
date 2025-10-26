import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService, Transaction as ApiTransaction, Category } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AccountStateService } from '../../services/account-state.service';
import { CategoryService } from '../../services/category.service';
import { NotificationService } from '../../services/notification.service';
import { Subscription } from 'rxjs';
import { IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonFab, IonFabButton, IonIcon, ModalController, IonSpinner, IonSelect, IonSelectOption, IonButton, IonInfiniteScroll, IonInfiniteScrollContent, IonFabList } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { link, refresh, funnel, add, cashOutline, briefcaseOutline, trendingUpOutline, cartOutline, homeOutline, waterOutline, carSportOutline, restaurantOutline, filmOutline, medkitOutline, pricetagsOutline, airplaneOutline } from 'ionicons/icons';
import { FilterModalComponent } from '../../components/modals/filter-modal/filter-modal.component';
import { IconService } from '../../services/icon.service';

addIcons({
  link,
  refresh,
  funnel,
  add,
  'cash-outline': cashOutline,
  'briefcase-outline': briefcaseOutline,
  'trending-up-outline': trendingUpOutline,
  'cart-outline': cartOutline,
  'home-outline': homeOutline,
  'water-outline': waterOutline,
  'car-sport-outline': carSportOutline,
  'restaurant-outline': restaurantOutline,
  'film-outline': filmOutline,
  'medkit-outline': medkitOutline,
  'pricetags-outline': pricetagsOutline,
  'airplane-outline': airplaneOutline,
});

interface Transaction extends ApiTransaction {
  expanded?: boolean;
  categoryDirty?: boolean;
}

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonFab, IonFabButton, IonIcon, FilterModalComponent, IonSpinner, IonSelect, IonSelectOption, IonButton, IonInfiniteScroll, IonInfiniteScrollContent, IonFabList],
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.scss']
})
export class TransactionsComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  private accountState = inject(AccountStateService);
  categoryService = inject(CategoryService);
  private notificationService = inject(NotificationService);
  private modalCtrl = inject(ModalController);
  iconService = inject(IconService);

  selectedAccountId: string | null = null;
  transactions: Transaction[] = [];
  originalTransactions: Transaction[] = [];
  filteredTransactions: Transaction[] = [];
  displayedTransactions: Transaction[] = [];
  error: string | null = null;
  loading = false;
  mainCategories: Category[] = [];
  selectedCategoryFilter = 'all';
  isLinkingMode = false;
  selectedExpenseId: string | null = null;
  selectedIncomeId: string | null = null;
  private scrollPage = 0;
  private readonly transactionsPerPage = 25;
  startDate = '';
  endDate = '';
  searchTerm: string = '';

  private accountSub: Subscription | undefined;

  ngOnInit(): void {
    this.categoryService.categories$.subscribe(() => {
      this.mainCategories = this.categoryService.getMainCategories();
    });

    this.accountSub = this.accountState.currentAccountId$.subscribe(accountId => {
      if (accountId && accountId !== this.selectedAccountId) {
        this.selectedAccountId = accountId;
        this.loadCachedTransactions();
      } else if (!accountId) {
        this.error = 'No account selected.';
      }
    });
  }

  ngOnDestroy(): void {
    this.accountSub?.unsubscribe();
  }

  async openFilterModal() {
    const modal = await this.modalCtrl.create({
      component: FilterModalComponent,
      componentProps: {
        selectedCategoryFilter: this.selectedCategoryFilter,
        mainCategories: this.mainCategories,
        startDate: this.startDate,
        endDate: this.endDate,
        searchTerm: this.searchTerm
      }
    });
    await modal.present();

    const { data, role } = await modal.onWillDismiss();

    if (role === 'confirm') {
      this.selectedCategoryFilter = data.selectedCategoryFilter;
      this.startDate = data.startDate;
      this.endDate = data.endDate;
      this.searchTerm = data.searchTerm;
      this.applyFilters();
      if (this.startDate && this.endDate) {
        this.loadCachedTransactions();
      }
    }
  }

  private setTransactions(data: Transaction[]): void {
    this.transactions = data.sort((a, b) => new Date(b.bookingDate).getTime() - new Date(a.bookingDate).getTime())
      .map(tx => ({ ...tx, expanded: false, categoryDirty: false }));
    this.originalTransactions = JSON.parse(JSON.stringify(this.transactions));
    this.applyFilters();
    this.loading = false;
  }

  loadCachedTransactions(): void {
    if (!this.selectedAccountId) return;
    this.loading = true;
    this.error = null;

    this.api.getAccountTransactions(this.selectedAccountId, this.startDate, this.endDate).subscribe({
      next: (res) => this.setTransactions(res),
      error: (err) => {
        this.error = 'Failed to load transactions: ' + err.message;
        this.loading = false;
        this.notificationService.show('Failed to load transactions.', 'error');
      }
    });
  }

  refreshTransactions(): void {
    if (!this.selectedAccountId) return;
    this.loading = true;
    this.error = null;
    this.notificationService.show('Refreshing transactions from the bank...', 'info');

    this.api.refreshTransactions(this.selectedAccountId).subscribe({
      next: (refreshedTransactions) => {
        this.setTransactions(refreshedTransactions);
        this.notificationService.show('Transactions refreshed successfully!', 'success');
      },
      error: (err) => {
        this.error = 'Failed to refresh transactions: ' + err.message;
        this.loading = false;
        this.notificationService.show('Failed to refresh transactions.', 'error');
      }
    });
  }

  applyFilters(): void {
    let tempTransactions = [...this.transactions];

    if (this.selectedCategoryFilter === 'uncategorized') {
      tempTransactions = tempTransactions.filter(tx => !tx.categoryId);
    } else if (this.selectedCategoryFilter !== 'all') {
      tempTransactions = tempTransactions.filter(tx => tx.categoryId === this.selectedCategoryFilter);
    }

    if (this.searchTerm && this.searchTerm.trim() !== '') {
      const lowerCaseSearch = this.searchTerm.toLowerCase();
      tempTransactions = tempTransactions.filter(tx =>
        tx.remittanceInformationUnstructured &&
        tx.remittanceInformationUnstructured.toLowerCase().includes(lowerCaseSearch)
      );
    }

    this.filteredTransactions = tempTransactions;
    this.resetAndLoadDisplayedTransactions();
  }

  private resetAndLoadDisplayedTransactions(): void {
    this.scrollPage = 0;
    this.displayedTransactions = [];
    this.loadMoreTransactions();
  }

  loadMore(event: any): void {
    this.loadMoreTransactions(() => {
      event.target.complete();
      if (this.displayedTransactions.length >= this.filteredTransactions.length) {
        event.target.disabled = true;
      }
    });
  }

  private loadMoreTransactions(onComplete?: () => void): void {
    const startIndex = this.scrollPage * this.transactionsPerPage;
    const endIndex = startIndex + this.transactionsPerPage;

    // Using a timeout to allow the UI to update before adding more items, which can feel smoother.
    setTimeout(() => {
      this.displayedTransactions.push(...this.filteredTransactions.slice(startIndex, endIndex));
      this.scrollPage++;
      onComplete?.();
    }, 100);
  }
  
  clearSearch(): void {
    if (this.searchTerm) {
      this.searchTerm = '';
      this.applyFilters();
    }
  }

  onCategorySelectionChange(transaction: Transaction): void {
    transaction.subCategoryId = null;
    transaction.categoryDirty = true;
  }

  onSubCategorySelectionChange(transaction: Transaction): void {
    transaction.categoryDirty = true;
  }

  saveCategory(transaction: Transaction): void {
    if (!transaction.categoryId) {
      this.notificationService.show('Please select a main category first.', 'error');
      return;
    }

    const originalTransactionBeforeAnyChange = this.originalTransactions.find(t => t.id === transaction.id);
    const wasAlreadyCategorized = !!originalTransactionBeforeAnyChange?.categoryId;

    this.api.updateTransactionCategory(transaction.id, transaction.categoryId, transaction.subCategoryId || null).subscribe({
      next: updatedTx => {
        const index = this.transactions.findIndex(t => t.id === updatedTx.id);
        if (index !== -1) {
          const wasExpanded = this.transactions[index].expanded;
          this.transactions[index] = { ...updatedTx, expanded: wasExpanded, categoryDirty: false };
          this.originalTransactions[index] = { ...this.transactions[index] };
          this.applyFilters();
        }
        this.notificationService.show('Category saved!', 'success');
        this.promptToCategorizeSimilar(updatedTx, wasAlreadyCategorized);
      },
      error: err => {
        console.error('Failed to update category', err);
        this.notificationService.show('Failed to save category.', 'error');
      }
    });
  }

  private promptToCategorizeSimilar(updatedTx: Transaction, wasAlreadyCategorized: boolean): void {
    const remittanceInfo = updatedTx.remittanceInformationUnstructured;
    if (!remittanceInfo) return;
    const normalizedRemittanceInfo = remittanceInfo.trim().replace(/\s+/g, ' ');
    let similarCount = 0;
    const isNewCategorization = !wasAlreadyCategorized && !!updatedTx.categoryId;
    if (isNewCategorization) {
      similarCount = this.transactions.filter(t => {
        const currentRemittance = t.remittanceInformationUnstructured?.trim().replace(/\s+/g, ' ') || '';
        return t.id !== updatedTx.id &&
          currentRemittance === normalizedRemittanceInfo &&
          !t.categoryId;
      }).length;
    }
    if (similarCount > 0) {
      const message = `Found ${similarCount} other uncategorized transaction(s) with the same description. Apply this category to them all?`;
      if (confirm(message)) {
        this.api.categorizeSimilarTransactions(remittanceInfo, updatedTx.categoryId!, updatedTx.subCategoryId || null, false).subscribe({
          next: () => {
            this.notificationService.show(`Applied category to ${similarCount} similar transaction(s).`, 'success');
            this.loadCachedTransactions();
          },
          error: (err) => {
            console.error('Failed to categorize similar transactions', err);
            this.notificationService.show('Failed to categorize similar transactions.', 'error');
          }
        });
      }
    }
  }

  toggleIgnore(transaction: Transaction): void {
    this.api.toggleTransactionIgnore(transaction.id).subscribe({
      next: updatedTx => {
        const index = this.transactions.findIndex(t => t.id === updatedTx.id);
        if (index !== -1) {
          const wasExpanded = this.transactions[index].expanded;
          this.transactions[index] = { ...updatedTx, expanded: wasExpanded };
          this.applyFilters();
        }
        this.notificationService.show(`Transaction ignore status updated.`, 'success');
      },
      error: err => {
        console.error('Failed to toggle ignore status', err);
        this.notificationService.show('Failed to update ignore status.', 'error');
      }
    });
  }

  toggleLinkingMode(): void {
    this.isLinkingMode = !this.isLinkingMode;
    this.selectedExpenseId = null;
    this.selectedIncomeId = null;
    if (!this.isLinkingMode) {
      this.notificationService.show('Split bill mode cancelled.', 'info');
    } else {
      this.notificationService.show('Split bill mode activated. Select one income and one expense.', 'info');
    }
  }

  selectTransactionForLink(transaction: Transaction): void {
    if (!this.isLinkingMode) return;
    const amount = +transaction.transactionAmount.amount;
    if (amount < 0) {
      this.selectedExpenseId = this.selectedExpenseId === transaction.id ? null : transaction.id;
    } else if (amount > 0) {
      this.selectedIncomeId = this.selectedIncomeId === transaction.id ? null : transaction.id;
    }
  }

  linkSelectedTransactions(): void {
    if (!this.selectedExpenseId || !this.selectedIncomeId) {
      this.notificationService.show('You must select one income and one expense to link.', 'error');
      return;
    }
    this.api.linkTransactions(this.selectedExpenseId, this.selectedIncomeId).subscribe({
      next: () => {
        this.isLinkingMode = false;
        this.selectedExpenseId = null;
        this.selectedIncomeId = null;
        this.notificationService.show('Transactions linked successfully!', 'success');
        this.loadCachedTransactions();
      },
      error: (err) => {
        console.error('Failed to link transactions', err);
        this.notificationService.show('An error occurred while linking.', 'error');
      }
    });
  }

  applyDateFilter(): void {
    if (this.startDate && this.endDate && this.startDate > this.endDate) {
      this.notificationService.show('Start date cannot be after end date.', 'error');
      return;
    }
    if (this.startDate && this.endDate) {
      this.loadCachedTransactions();
    }
  }

  clearDateFilter(): void {
    if (this.startDate || this.endDate) {
      this.startDate = '';
      this.endDate = '';
      this.loadCachedTransactions();
      this.notificationService.show('Date filter cleared.', 'info');
    }
  }
}