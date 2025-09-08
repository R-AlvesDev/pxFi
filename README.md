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
* **Secure User Authentication**: A complete user registration and login system using Spring Security and JWTs ensures all user data is private and scoped to the logged-in user.
* **Reliable Open Banking Integration**: Connects to bank accounts via the GoCardless API and correctly deduplicates incoming data.
* **Persistent & Clean Storage**: Successfully saves all retrieved transactions into a MongoDB database with consistent and correct data typing.
* **Transaction Management**: A clean UI to view, filter by category or date, and manage transactions, including features for manual categorization, ignoring transactions, and linking split bills.
* **Categorization System**: A full CRUD system for custom categories/subcategories.
* **Automation Engine**: A functional rules engine to automate categorization based on user-defined criteria, including the ability to test rules before saving.
* **Main Dashboard & Statistics**: A dedicated dashboard and statistics page for visualizing monthly and yearly income vs. expenses, with correct handling for "Asset Transfers" and "Split Bills".
* **Polished User Experience**: Global notifications, date range filtering, and improved UI feedback create a more seamless user experience.

---

## The Vision: Roadmap to Version 1.0

The goal is to evolve pxFi into a polished and highly secure personal finance dashboard. The following roadmap outlines the key steps to reach the first major version.

### 1. Security Hardening (Essential for v1.0)
The highest priority is to implement robust, application-level encryption for all sensitive user data, ensuring compliance with standards like GDPR.
* **Backend**: Implement an automatic encryption/decryption layer for sensitive fields in the database.
* **Security**: Securely manage the encryption key outside of the codebase.
* **Data Protection**: Encrypt all personally identifiable financial data, including transaction details, amounts, and user-created rules and account names.

### 2. Final Polish & Touches
The final step before a public release is to refine the user experience and add final quality-of-life improvements.
* **Harden Registration**: Add frontend and backend validation for strong passwords and valid usernames/emails.
* **Improve UX**: Allow users to edit account names, improve "empty state" messages, and ensure the application is fully responsive on mobile devices.

---

## Technology Stack

* **Frontend**: Angular, TypeScript, Bootstrap
* **Backend**: Java, Spring Boot, Spring Security
* **Database**: MongoDB
* **Open Banking**: GoCardless API