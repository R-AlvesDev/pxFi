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
  GREATER_THAN = 'GREATER_THAN',
  LESS_THAN = 'LESS_THAN'
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

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  private createAuthHeaders(token: string) {
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
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

  getAccountTransactions(accessToken: string, accountId: string): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.baseUrl}/accounts/${accountId}/transactions`, {
      headers: this.createAuthHeaders(accessToken)
    });
  }

  refreshTransactions(accessToken: string, accountId: string): Observable<Transaction[]> {
    return this.http.post<Transaction[]>(`${this.baseUrl}/accounts/${accountId}/transactions/refresh`, {}, {
      headers: this.createAuthHeaders(accessToken)
    });
  }

  handleCallback(ref: string): Observable<any> {
    const params = new HttpParams().set('ref', ref);
    return this.http.get(`${this.baseUrl}/callback`, { params });
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/categories`);
  }

  updateTransactionCategory(transactionId: string, categoryId: string, subCategoryId: string | null): Observable<Transaction> {
    const payload = { categoryId, subCategoryId };
    return this.http.post<Transaction>(`${this.baseUrl}/transactions/${transactionId}/category`, payload);
  }

  categorizeSimilarTransactions(remittanceInfo: string, categoryId: string, subCategoryId: string | null): Observable<Transaction[]> {
    const payload = { remittanceInfo, categoryId, subCategoryId };
    return this.http.post<Transaction[]>(`${this.baseUrl}/transactions/categorize-similar`, payload);
  }

  createCategory(category: { name: string, parentId: string | null }): Observable<Category> {
    return this.http.post<Category>(`${this.baseUrl}/categories`, category);
  }

  updateCategory(id: string, category: { name: string, parentId: string | null }): Observable<Category> {
    return this.http.put<Category>(`${this.baseUrl}/categories/${id}`, category);
  }

  deleteCategory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/categories/${id}`);
  }

  getRules(): Observable<CategorizationRule[]> {
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

  getMonthlyStatistics(year: number, month: number): Observable<StatisticsResponse> {
    const params = { year: year.toString(), month: month.toString() };
    return this.http.get<StatisticsResponse>(`${this.baseUrl}/statistics/monthly`, { params });
  }

}
