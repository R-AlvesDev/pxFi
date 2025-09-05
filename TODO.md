# pxFi - Roadmap to Version 1.0

This document outlines the features and implementation plan for the first fully working and polished version of the pxFi personal finance tracker.

---

## Tier 1: Polish & User Experience (Next Steps)

* [X] **Feature: Global Notifications**
    * [X] Create a simple notification service in Angular (e.g., a toast component).
    * [X] Integrate the service to show success/error messages for major actions (saving categories, applying rules, linking transactions).

* [X] **Feature: Date Range Filtering**
    * [X] Add "Start Date" and "End Date" input fields to the `transactions.component.html`.
    * [X] Update the backend `TransactionService` to accept optional date parameters.
    * [X] Wire up the frontend to refetch and filter transactions based on the selected date range.

* [X] **Task: Refine UI Feedback**
    * [X] When "Apply All Rules" is clicked, show a persistent success message on completion with the number of transactions updated.
    * [X] Add confirmation dialogs for critical delete operations (e.g., deleting a category).

## Tier 2: Core Feature Expansion

* [X] **Feature: Main Dashboard**
    * [X] **Backend**: Create a new API endpoint (`/api/dashboard/summary`).
    * [X] **Backend**: Implement the service logic to calculate key metrics (e.g., net balance this month, top 5 spending categories, recent transactions).
    * [X] **Frontend**: Design and build a new `DashboardComponent` to replace the current home page for logged-in users.
    * [X] **Frontend**: Display summary data using cards and simple charts.

* [X] **Feature: Enhance Rules Engine**
    * [X] Add a rule condition for transaction `amount` (e.g., "if amount is greater than 100").
    * [X] Add a "Test Rule" button that shows a preview of which existing transactions a new rule would apply to before saving it.

## Tier 3: Security & Finalization (Essential for v1.0)

* [ ] **Feature: User Authentication & Authorization**
    * [ ] **Backend**: Integrate Spring Security and create `User` models and repositories.
    * [ ] **Backend**: Build secure registration and JWT-based login endpoints.
    * [ ] **Backend**: **Crucially**, update all service methods (`getTransactionsByAccountId`, `getAllCategories`, etc.) to be scoped to the authenticated user.
    * [ ] **Frontend**: Create `Login` and `Register` components and pages.
    * [ ] **Frontend**: Update `AuthService` to handle JWTs.
    * [ ] **Frontend**: Implement an `HttpInterceptor` to automatically attach the JWT to all API requests.
    * [ ] **Frontend**: Implement route guards to protect all data-sensitive pages.

---
### **Completed Features (Pre-v1.0)**

* [X] **Core Engine**: Bank connection, data sync, and persistent storage.
* [X] **Data Integrity**: Robust deduplication using `internalTransactionId`.
* [X] **Transaction Management**: View, categorize, filter by category, ignore, and link transactions.
* [X] **Statistics**: Functional monthly and yearly statistics with correct "Asset Transfer" logic.
* [X] **Settings**: Full CRUD for categories and a working rules engine.