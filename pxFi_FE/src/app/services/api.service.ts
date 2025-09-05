import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

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
  id: string; // This is the MongoDB ID
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
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  private createAuthHeaders(token: string) {
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  register(registerData: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/register`, registerData);
  }

  login(loginData: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, loginData);
  }

  getInstitutions(accessToken: string, countryCode: string): Observable<Institution[]> {
    return this.http.get<Institution[]>(`${this.baseUrl}/institutions`, {
      params: { accessToken, countryCode }
    });
  }

  getAccessToken(): Observable<{ accessToken: string }> {
    return this.http.get<{ accessToken: string }>(`${this.baseUrl}/access-token`);
  }

  createEndUserAgreement(accessToken: string, institutionId: string): Observable<AgreementResponse> {
    const payload = {
      institution_id: institutionId,
      max_historical_days: 90,
      access_valid_for_days: 30,
      access_scope: ['balances', 'details', 'transactions']
    };
    return this.http.post<AgreementResponse>(
      `${this.baseUrl}/agreements/enduser`,
      payload,
      { headers: this.createAuthHeaders(accessToken) }
    );
  }

  createRequisition(accessToken: string, institutionId: string, agreementId: string): Observable<RequisitionResponse> {
    const url = `${this.baseUrl}/requisitions/create`;
    const body = { institutionId, agreementId };
    return this.http.post<RequisitionResponse>(url, body, { headers: this.createAuthHeaders(accessToken) });
  }

  getRequisitionDetails(accessToken: string, requisitionId: string): Observable<RequisitionResponse> {
    return this.http.get<RequisitionResponse>(`${this.baseUrl}/requisitions/details`, {
      params: { requisitionId },
      headers: this.createAuthHeaders(accessToken)
    });
  }

  getAccountTransactions(accessToken: string, accountId: string, startDate?: string, endDate?: string): Observable<Transaction[]> {
    let params = new HttpParams();
    if (startDate && endDate) {
      params = params.set('startDate', startDate);
      params = params.set('endDate', endDate);
    }
    return this.http.get<Transaction[]>(`${this.baseUrl}/accounts/${accountId}/transactions`, {
      headers: this.createAuthHeaders(accessToken),
      params: params
    });
  }

  refreshTransactions(accessToken: string, accountId: string): Observable<Transaction[]> {
    return this.http.post<Transaction[]>(`${this.baseUrl}/accounts/${accountId}/transactions/refresh`, {}, {
      headers: this.createAuthHeaders(accessToken)
    });
  }

  toggleTransactionIgnore(accessToken: string, transactionId: string): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.baseUrl}/transactions/${transactionId}/toggle-ignore`, {}, { headers: this.createAuthHeaders(accessToken) });
  }
  
  updateTransactionCategory(accessToken: string, transactionId: string, categoryId: string, subCategoryId: string | null): Observable<Transaction> {
    const payload = { categoryId, subCategoryId };
    return this.http.post<Transaction>(`${this.baseUrl}/transactions/${transactionId}/category`, payload, { headers: this.createAuthHeaders(accessToken) });
  }
  
  categorizeSimilarTransactions(accessToken: string, remittanceInfo: string, categoryId: string, subCategoryId: string | null): Observable<Transaction[]> {
    const payload = { remittanceInfo, categoryId, subCategoryId };
    return this.http.post<Transaction[]>(`${this.baseUrl}/transactions/categorize-similar`, payload, { headers: this.createAuthHeaders(accessToken) });
  }

  linkTransactions(accessToken: string, expenseId: string, incomeId: string): Observable<void> {
    const payload = { expenseId, incomeId };
    return this.http.post<void>(`${this.baseUrl}/transactions/link`, payload, { headers: this.createAuthHeaders(accessToken) });
  }

  // --- Category & Rule Methods (Secured) ---

  getAllCategories(accessToken: string): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/categories`, { headers: this.createAuthHeaders(accessToken) });
  }
  
  createCategory(accessToken: string, category: { name: string, parentId: string | null }): Observable<Category> {
    return this.http.post<Category>(`${this.baseUrl}/categories`, category, { headers: this.createAuthHeaders(accessToken) });
  }

  updateCategory(accessToken: string, id: string, category: Partial<Category>): Observable<Category> {
    return this.http.put<Category>(`${this.baseUrl}/categories/${id}`, category, { headers: this.createAuthHeaders(accessToken) });
  }

  deleteCategory(accessToken: string, id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/categories/${id}`, { headers: this.createAuthHeaders(accessToken) });
  }

  getAllRules(accessToken: string): Observable<CategorizationRule[]> {
    return this.http.get<CategorizationRule[]>(`${this.baseUrl}/rules`, { headers: this.createAuthHeaders(accessToken) });
  }

  createRule(accessToken: string, rule: Partial<CategorizationRule>): Observable<CategorizationRule> {
    return this.http.post<CategorizationRule>(`${this.baseUrl}/rules`, rule, { headers: this.createAuthHeaders(accessToken) });
  }
  
  applyAllRules(accessToken: string): Observable<{ updatedCount: number }> {
    return this.http.post<{ updatedCount: number }>(`${this.baseUrl}/rules/apply-all`, {}, { headers: this.createAuthHeaders(accessToken) });
  }
  
  deleteRule(accessToken: string, id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/rules/${id}`, { headers: this.createAuthHeaders(accessToken) });
  }
  
  testRule(accessToken: string, rule: Partial<CategorizationRule>, accountId: string): Observable<TestRuleResponse> {
    const payload = { rule, accountId };
    return this.http.post<TestRuleResponse>(`${this.baseUrl}/rules/test`, payload, { headers: this.createAuthHeaders(accessToken) });
  }

  // --- Statistics & Dashboard Methods (Secured) ---

  getMonthlyStatistics(accessToken: string, accountId: string, year: number, month: number): Observable<StatisticsResponse> {
    let params = new HttpParams().set('year', year.toString()).set('month', month.toString());
    return this.http.get<StatisticsResponse>(`${this.baseUrl}/statistics/monthly/${accountId}`, {
      headers: this.createAuthHeaders(accessToken),
      params: params
    });
  }

  getYearlyStatistics(accessToken: string, accountId: string, year: number): Observable<YearlyStatisticsResponse> {
    let params = new HttpParams().set('year', year.toString());
    return this.http.get<YearlyStatisticsResponse>(`${this.baseUrl}/statistics/yearly/${accountId}`, {
      headers: this.createAuthHeaders(accessToken),
      params: params
    });
  }

  getDashboardSummary(accessToken: string, accountId: string): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.baseUrl}/dashboard/summary/${accountId}`, {
      headers: this.createAuthHeaders(accessToken)
    });
  }
  
  // --- Account Management (Secured) ---

  getAccounts(accessToken: string): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.baseUrl}/accounts`, {
      headers: this.createAuthHeaders(accessToken)
    });
  }

  saveAccount(accessToken: string, accountData: any): Observable<Account> {
    return this.http.post<Account>(`${this.baseUrl}/accounts`, accountData, {
      headers: this.createAuthHeaders(accessToken)
    });
  }
}
