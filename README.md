# pxFi - Personal Finance Intelligence

**pxFi** is a full-stack personal finance application designed to provide a consolidated view of your financial transactions. By leveraging open banking APIs, it connects directly to your bank accounts to fetch transaction data, stores it for long-term analysis, and provides tools to help you understand your spending habits.

---

## Core Concept

The primary goal of pxFi is to help users track personal finances directly from their bank account, overcoming the typical 90-day data limit imposed by most banking APIs. By fetching transactions and persisting them in a dedicated MongoDB database using a stable `internalTransactionId`, the application builds a long-term, comprehensive, and reliable history of your financial life.

This allows for powerful features like year-over-year spending comparisons, long-term savings tracking, and a complete, searchable archive of all your transactions.

---

## Current Status (Stable Pre-v1.0)

The project has a solid and stable foundation. The core functionality is in place and working reliably after significant debugging and refinement of the data handling logic.

**Key Features Implemented:**
* **Full-Stack Architecture**: A decoupled frontend (Angular) and backend (Java Spring Boot).
* **Reliable Open Banking Integration**: Connects to bank accounts via the GoCardless API and correctly deduplicates incoming data.
* **Persistent & Clean Storage**: Successfully saves all retrieved transactions into a MongoDB database, ensuring data integrity.
* **Transaction Management**: A clean UI to view, filter, and manage transactions, including features for manual categorization, ignoring transactions, and linking split bills.
* **Categorization System**: A full CRUD system for custom categories/subcategories.
* **Automation Engine**: A functional rules engine to automate categorization based on user-defined criteria.
* **Statistics Dashboard**: A dedicated page for visualizing monthly and yearly income vs. expenses, with correct handling for "Asset Transfers".

---

## The Vision: Roadmap to Version 1.0

The goal is to evolve pxFi into a polished, secure, and highly intuitive personal finance dashboard. The following roadmap outlines the key steps to reach the first major version.

### 1. Focus on User Experience (UX) and Polishing
The immediate priority is to improve the user feedback loop and make the application feel more seamless and responsive.
* **Global Notifications**: Implement a system to provide clear success and error messages for all background actions (e.g., "Rules applied successfully!").
* **Date Range Filtering**: Add date pickers to the transactions page to allow users to view their financial history within specific periods.
* **Refine UI Feedback**: Enhance loading states and provide clearer confirmation for actions like applying rules or saving changes.

### 2. Build the Main Dashboard
Transform the landing page into a useful, at-a-glance summary of the user's financial health.
* Create a central dashboard component that displays key metrics like current balances, recent spending trends, and top expense categories for the current month.

### 3. Implement Security: User Accounts (Critical Path)
This is the most critical feature required for a v1.0 release, ensuring data privacy and security.
* **Backend**: Integrate Spring Security to add a full user authentication and authorization layer. This includes creating User models and JWT-based login/registration endpoints.
* **Data Scoping**: Modify all data-related services (`TransactionService`, `CategoryService`, etc.) to be user-aware, ensuring a user can only ever access their own financial data.
* **Frontend**: Build Login/Register pages and implement route guards to protect authenticated sections of the application.

---

## Technology Stack

* **Frontend**: Angular, TypeScript, Bootstrap
* **Backend**: Java, Spring Boot
* **Database**: MongoDB
* **Open Banking**: GoCardless API