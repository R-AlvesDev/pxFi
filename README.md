# pxFi - Personal Finance Intelligence

**pxFi** is a full-stack personal finance application designed to provide a consolidated, secure, and long-term view of your financial transactions. By leveraging open banking APIs, it connects directly to your bank accounts to fetch transaction data, encrypts all sensitive information, and provides powerful tools to help you understand your spending habits.

---

## Core Concept

The primary goal of pxFi is to help users track personal finances directly from their bank account, overcoming the typical 90-day data limit imposed by most banking APIs. By fetching transactions and persisting them in a dedicated MongoDB database using a stable `internalTransactionId`, the application builds a long-term, comprehensive, and reliable history of your financial life.

This allows for powerful features like year-over-year spending comparisons, long-term savings tracking, and a complete, searchable, and secure archive of all your transactions.

---

## Current Status (Version 1.0 Feature Complete)

The project is now feature-complete, with a robust and stable foundation. All core functionality, from user authentication to data encryption, is in place and working reliably.

**Key Features Implemented:**
* **Full-Stack Architecture**: A decoupled frontend (Angular) and backend (Java Spring Boot).
* **Secure User Authentication**: A complete user registration and login system using Spring Security and JWTs ensures all user data is private and scoped to the logged-in user.
* **Application-Level Encryption**: All sensitive PII and financial data is **encrypted at rest** using a secure key management strategy, including transaction details, amounts, account names, and user-defined rules.
* **Reliable Open Banking Integration**: Connects to bank accounts via the GoCardless API and correctly deduplicates incoming data.
* **Transaction Management**: A clean UI to view, filter by category or date, and manage transactions, including features for manual categorization, ignoring transactions, and linking split bills.
* **Categorization System**: A full CRUD system for custom categories/subcategories.
* **Automation Engine**: A functional rules engine to automate categorization based on user-defined criteria, including the ability to test rules before saving.
* **Main Dashboard & Statistics**: A dedicated dashboard and statistics page for visualizing monthly and yearly income vs. expenses, with correct handling for "Asset Transfers" and "Split Bills".
* **Polished User Experience**: Global notifications, date range filtering, and improved UI feedback create a seamless user experience.

---

## The Vision: Roadmap to Public Release

With all major features implemented, the final step is to refine the application for a public release. The following roadmap outlines the key polishing and UX improvements.

### Tier 5: Polish & Final Touches
* **Harden Registration Process**: Add frontend and backend validation for strong passwords and valid usernames/emails.
* **Improve UX**: Allow users to edit account names, improve "empty state" messages for new users, and refine the layout for a better mobile experience.
* **Improve Transactions List**: Fix the size of the transactions so we have a consistent container size, only expanding when opening transaction details.
* **Final Code Cleanup**: Run linters and static analyzers to ensure consistent code style and remove any remaining debug logs or commented-out code.
* **Accessibility Review**: Ensure all interactive elements have proper labels for screen readers.