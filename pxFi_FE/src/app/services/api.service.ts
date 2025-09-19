import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Institution {
  id: string;
  name: string;
  logo?: string;
}

export interface AgreementResponse {
  id: string;
  created: string;
  max_historical_days: number;
  access_valid_for_days: number;
  access_scope: string[];
  accepted: string;
  institution_id: string;
}

export interface RequisitionResponse {
  id: string;
  created: string;
  redirect: string;
  status: string;
  institution_id: string;
  agreement: string;
  reference: string;
  accounts: string[];
  user_language: string;
  link: string;
  ssn?: string;
  account_selection: boolean;
  redirect_immediate: boolean;
}

export interface Account {
  id: string;
  userId: string;
  gocardlessAccountId: string;
  accountName: string;
  institutionId: string;
  iban: string;
}

export interface DebtorAccount {
  iban?: string;
  bban?: string;
  pan?: string;
  maskedPan?: string;
  msisdn?: string;
  currency?: string;
}

export interface TransactionAmount {
  amount: string;
  currency: string;
}

export interface Transaction {
  id: string;
  transactionId: string;
  debtorName: string;
  internalTransactionId: string;
  debtorAccount: DebtorAccount;
  transactionAmount: TransactionAmount;
  bookingDate: string;
  valueDate: string;
  remittanceInformationUnstructured: string;
  bankTransactionCode: string;
  categoryId?: string;
  subCategoryId?: string | null;
  categoryName?: string;
  subCategoryName?: string;
  ignored: boolean;
  linkedTransactionId?: string | null;
}

export interface TransactionsResponse {
  transactions: {
    booked: Transaction[];
    pending: Transaction[];
  };
}

export interface Category {
  id: string;
  name: string;
  parentId: string | null;
  isAssetTransfer: boolean;
}

export enum RuleField {
  REMITTANCE_INFO = 'REMITTANCE_INFO',
  AMOUNT = 'AMOUNT'
}

export enum RuleOperator {
  CONTAINS = 'CONTAINS',
  EQUALS = 'EQUALS',
  STARTS_WITH = 'STARTS_WITH',
  ENDS_WITH = 'ENDS_WITH',
  // Amount operators
  AMOUNT_EQUALS = 'AMOUNT_EQUALS',
  AMOUNT_GREATER_THAN = 'AMOUNT_GREATER_THAN',
  AMOUNT_LESS_THAN = 'AMOUNT_LESS_THAN'
}

export interface AuthResponse {
  token: string;
}


export interface CategorizationRule {
  id: string;
  fieldToMatch: RuleField;
  operator: RuleOperator;
  valueToMatch: string;
  categoryId: string;
  subCategoryId: string | null;
}

export interface CategorySpending {
  categoryName: string;
  total: number;
}

export interface StatisticsResponse {
  totalIncome: number;
  totalExpenses: number;
  expensesByCategory: CategorySpending[];
}

export interface MonthlyBreakdown {
  month: number;
  income: number;
  expenses: number;
}

export interface YearlyStatisticsResponse {
  totalIncome: number;
  totalExpenses: number;
  averageMonthlyIncome: number;
  averageMonthlyExpenses: number;
  monthlyBreakdowns: MonthlyBreakdown[];
}

export interface TestRuleResponse {
  matchedTransactions: Transaction[];
  matchCount: number;
}

export interface DashboardSummary {
  currentMonthIncome: number;
  currentMonthExpenses: number;
  netBalance: number;
  topSpendingCategories: CategorySpending[];
  recentTransactions: Transaction[];
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private http = inject(HttpClient);

  private baseUrl = environment.apiUrl;

  // --- Auth & Public GoCardless ---

  register(registerData: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/register`, registerData);
  }

  login(loginData: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, loginData);
  }

  getAccessToken(): Observable<{ accessToken: string }> {
    return this.http.get<{ accessToken: string }>(`${this.baseUrl}/access-token`);
  }

  getInstitutions(accessToken: string, countryCode: string): Observable<Institution[]> {
    return this.http.get<Institution[]>(`${this.baseUrl}/institutions`, {
      params: { accessToken, countryCode }
    });
  }

  // --- Bank Connection Flow (User JWT is added by interceptor) ---

  createEndUserAgreement(gocardlessToken: string, institutionId: string): Observable<AgreementResponse> {
    const payload = { institution_id: institutionId };
    return this.http.post<AgreementResponse>(
      `${this.baseUrl}/agreements/enduser`,
      payload,
      { params: { gocardlessToken } }
    );
  }

  createRequisition(gocardlessToken: string, institutionId: string, agreementId: string): Observable<RequisitionResponse> {
    const body = { institutionId, agreementId };
    return this.http.post<RequisitionResponse>(
      `${this.baseUrl}/requisitions/create`,
      body,
      { params: { gocardlessToken } }
    );
  }

  getRequisitionDetails(gocardlessToken: string, requisitionId: string): Observable<RequisitionResponse> {
    return this.http.get<RequisitionResponse>(`${this.baseUrl}/requisitions/details`, {
      params: { gocardlessToken, requisitionId }
    });
  }

  // --- Account & Transaction Management (User JWT is added by interceptor) ---

  saveAccount(accountData: any): Observable<Account> {
    return this.http.post<Account>(`${this.baseUrl}/accounts`, accountData);
  }

  getAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.baseUrl}/accounts`);
  }

  updateAccountName(accountId: string, newName: string): Observable<Account> {
    const payload = { name: newName };
    return this.http.put<Account>(`${this.baseUrl}/accounts/${accountId}`, payload);
  }

  deleteAccount(accountId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/accounts/${accountId}`);
  }

  getAccountTransactions(accountId: string, startDate?: string, endDate?: string): Observable<Transaction[]> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<Transaction[]>(`${this.baseUrl}/accounts/${accountId}/transactions`, { params });
  }

  refreshTransactions(accountId: string): Observable<Transaction[]> {
    return this.http.post<Transaction[]>(`${this.baseUrl}/accounts/${accountId}/transactions/refresh`, {});
  }

  toggleTransactionIgnore(transactionId: string): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.baseUrl}/transactions/${transactionId}/toggle-ignore`, {});
  }

  updateTransactionCategory(transactionId: string, categoryId: string, subCategoryId: string | null): Observable<Transaction> {
    const payload = { categoryId, subCategoryId };
    return this.http.post<Transaction>(`${this.baseUrl}/transactions/${transactionId}/category`, payload);
  }

  categorizeSimilarTransactions(remittanceInfo: string, categoryId: string, subCategoryId: string | null, isAddingSubcategory: boolean): Observable<Transaction[]> {
    const payload = { remittanceInfo, categoryId, subCategoryId, isAddingSubcategory };
    return this.http.post<Transaction[]>(`${this.baseUrl}/transactions/categorize-similar`, payload);
  }

  linkTransactions(expenseId: string, incomeId: string): Observable<void> {
    const payload = { expenseId, incomeId };
    return this.http.post<void>(`${this.baseUrl}/transactions/link`, payload);
  }

  // --- Category & Rule Methods (User JWT is added by interceptor) ---

  getAllCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/categories`);
  }

  createCategory(category: { name: string, parentId: string | null }): Observable<Category> {
    return this.http.post<Category>(`${this.baseUrl}/categories`, category);
  }

  updateCategory(id: string, category: Partial<Category>): Observable<Category> {
    return this.http.put<Category>(`${this.baseUrl}/categories/${id}`, category);
  }

  deleteCategory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/categories/${id}`);
  }

  getAllRules(): Observable<CategorizationRule[]> {
    return this.http.get<CategorizationRule[]>(`${this.baseUrl}/rules`);
  }

  createRule(rule: Partial<CategorizationRule>): Observable<CategorizationRule> {
    return this.http.post<CategorizationRule>(`${this.baseUrl}/rules`, rule);
  }

  applyAllRules(): Observable<{ updatedCount: number }> {
    return this.http.post<{ updatedCount: number }>(`${this.baseUrl}/rules/apply-all`, {});
  }

  deleteRule(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/rules/${id}`);
  }

  testRule(rule: Partial<CategorizationRule>, accountId: string): Observable<TestRuleResponse> {
    const payload = { rule, accountId };
    return this.http.post<TestRuleResponse>(`${this.baseUrl}/rules/test`, payload);
  }

  // --- Statistics & Dashboard Methods (User JWT is added by interceptor) ---

  getMonthlyStatistics(accountId: string, year: number, month: number): Observable<StatisticsResponse> {
    const params = new HttpParams().set('year', year.toString()).set('month', month.toString());
    return this.http.get<StatisticsResponse>(`${this.baseUrl}/statistics/monthly/${accountId}`, { params });
  }

  getYearlyStatistics(accountId: string, year: number): Observable<YearlyStatisticsResponse> {
    const params = new HttpParams().set('year', year.toString());
    return this.http.get<YearlyStatisticsResponse>(`${this.baseUrl}/statistics/yearly/${accountId}`, { params });
  }

  getDashboardSummary(accountId: string): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.baseUrl}/dashboard/summary/${accountId}`);
  }
}