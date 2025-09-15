# pxFi - Roadmap to Version 1.0

This document outlines the features and implementation plan for the first fully working version of the pxFi personal finance tracker.

---

### **Completed Features (Pre-v1.0)**

* [X] **Core Engine**: Bank connection, data sync, and persistent storage.
* [X] **Data Integrity**: Robust deduplication and consistent data typing (`ObjectId`).
* [X] **User Authentication**: Full user registration and login system with Spring Security and JWTs.
* [X] **Data Scoping**: All data is correctly scoped to the logged-in user.
* [X] **Transaction Management**: View, categorize, filter by category or date, ignore, and link transactions.
* [X] **Statistics**: Functional monthly and yearly statistics with correct "Asset Transfer" and "Split Bill" logic.
* [X] **Settings**: Full CRUD for categories and a working rules engine.
* [X] **Main Dashboard**: A functional dashboard with key metrics and recent activity.
* [X] **UI/UX Polish**: Global notifications, date filtering, and improved UI feedback.

---

## Tier 1: User Experience Improvement

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

## Tier 3: Security & Finalization

* [X] **Feature: User Authentication & Authorization**
    * [X] **Backend**: Integrate Spring Security and create `User` models and repositories.
    * [X] **Backend**: Build secure registration and JWT-based login endpoints.
    * [X] **Backend**: **Crucially**, update all service methods (`getTransactionsByAccountId`, `getAllCategories`, etc.) to be scoped to the authenticated user.
    * [X] **Frontend**: Create `Login` and `Register` components and pages.
    * [X] **Frontend**: Update `AuthService` to handle JWTs.
    * [X] **Frontend**: Implement an `HttpInterceptor` to automatically attach the JWT to all API requests.
    * [X] **Frontend**: Implement route guards to protect all data-sensitive pages.

## Tier 4: Security Hardening

* [X] **Feature: Application-Level Encryption**
    * [X] **Backend**: Create an `AttributeConverter` class to automatically encrypt and decrypt sensitive fields.
    * [X] **Backend**: Implement a secure key management strategy to store the encryption key outside of the database and source code (e.g., using environment variables or a secrets manager).
    * [X] **Backend**: Apply the encryption converter to the following sensitive fields in the models:
        * `Transaction.transactionId`
        * `Transaction.remittanceInformationUnstructured`
        * `Transaction.transactionAmount.amount`
        * `Account.accountName`
        * `CategorizationRule.valueToMatch`
    * [X] **Task**: Perform a final review of all models for any other potentially sensitive PII that should be encrypted.

## Tier 5: Polish & Final Touches

* [X] **Feature: Harden Registration Process**
    * [X] **Backend**: Add validation to the registration endpoint to enforce strong passwords (e.g., minimum length, uppercase, number, special character).
    * [X] **Backend**: Add validation for username (e.g., minimum length, no special characters) and ensure email is a valid format.
    * [X] **Frontend**: Update the registration form to provide real-time feedback on these new requirements.

* [ ] **Task: General UI/UX Improvements**
    * [X] **Account Naming**: Allow users to edit the name of their connected bank accounts.
    * [X] **Empty States**: Improve the messages and guidance shown on pages with no data (e.g., a new user's transaction page).
    * [ ] **Accessibility Review**: Do a quick pass to ensure all interactive elements have proper labels for screen readers.
    * [ ] **Mobile Responsiveness**: Test and refine the layout on various screen sizes to ensure a good mobile experience.
    * [ ] **Transactions List Improv**: Fix the size of the transactions so we have a consistent container size, only expanding when opening transaction details.

* [ ] **Task: Final Code Cleanup**
    * [ ] **Linting**: Run a linter (like ESLint for frontend, and a static analyzer for Java) across the entire codebase to enforce consistent style.
    * [ ] **Refactoring**: Remove any redundant code, commented-out blocks, and unused imports.
    * [ ] **Logging**: Remove all temporary `console.log` and `System.out.println` statements used for debugging.

---