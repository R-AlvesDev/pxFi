# pxFi - Personal Finance Intelligence

**pxFi** is a full-stack personal finance application designed to provide a consolidated, secure, and long-term view of your financial transactions. By leveraging open banking APIs, it connects directly to your bank accounts to fetch transaction data, encrypts all sensitive information, and provides powerful tools to help you understand your spending habits.

---

## Core Concept

The primary goal of pxFi is to help users track personal finances directly from their bank account, overcoming the typical 90-day data limit imposed by most banking APIs. By fetching transactions and persisting them in a dedicated MongoDB database using a stable `internalTransactionId`, the application builds a long-term, comprehensive, and reliable history of your financial life.

This allows for powerful features like year-over-year spending comparisons, long-term savings tracking, and a complete, searchable, and secure archive of all your transactions.

---

## Current Status (Version 1.0 - Ready for Testing)

The project has reached its version 1.0 milestone and is now feature-complete. All core functionality, from user authentication and end-to-end data encryption to data visualization, has been implemented and is ready for a comprehensive testing phase before public release.

### Key Features Implemented
* **Full-Stack Architecture**: A robust and modern stack featuring an Angular frontend and a Java Spring Boot backend.
* **Secure User Authentication**: Complete user registration and login system using Spring Security and JWTs, ensuring all user data is private and strictly scoped to the logged-in user.
* **Application-Level Encryption**: All sensitive PII and financial data is **encrypted at rest** using a secure key management strategy. This includes transaction details, amounts, account names, and user-defined automation rules.
* **Reliable Open Banking Integration**: Connects securely to bank accounts via the GoCardless API and intelligently deduplicates incoming data to ensure a clean transaction history.
* **Comprehensive Transaction Management**: An intuitive UI to view, filter by category or date, and manage all transactions. Key features include:
    * Manual Categorization (with subcategories)
    * Ignoring irrelevant transactions (e.g., internal transfers)
    * Linking related transactions (e.g., splitting a bill)
* **Powerful Automation Engine**: A functional rules engine to automate categorization based on user-defined criteria, including the ability to test rules against existing transactions before saving.
* **Insightful Dashboard & Statistics**: A dedicated dashboard and statistics page for visualizing monthly and yearly income vs. expenses, with logic that correctly handles "Asset Transfers" and "Split Bills" to provide accurate financial insights.
* **Polished User Experience**: The application is fully responsive for mobile devices and includes global notifications, date range filtering, and improved UI feedback for a seamless and modern user experience.