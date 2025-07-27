# pxFi - Personal Finance Intelligence

**pxFi** is a full-stack personal finance application designed to provide a consolidated view of your financial transactions. By leveraging open banking APIs, it connects directly to your bank accounts to fetch transaction data, stores it for long-term analysis, and provides tools to help you understand your spending habits.

---

## Core Concept

The primary goal of pxFi is to overcome the typical 90-day data limit imposed by most banking APIs. By fetching transactions and persisting them in a dedicated MongoDB database, the application builds a long-term, comprehensive history of your financial life.

This allows for powerful features like year-over-year spending comparisons, long-term savings tracking, and a complete, searchable archive of all your transactions.

---

## Current Status (v0.3)

The project is currently in its initial development phase. The foundational structure is in place, and the core functionality of connecting to a bank and retrieving transactions is working.

**Key Features Implemented:**
* **Full-Stack Architecture**: A decoupled frontend and backend.
    * **Frontend (`pxFi_FE`)**: An Angular application that provides the user interface for connecting to banks and viewing transactions.
    * **Backend (`pxFi_BE`)**: A Java Spring Boot application that handles business logic, securely communicates with the open banking API, and manages the database.
* **Open Banking Integration**: Connects to bank accounts via the GoCardless API to securely fetch account and transaction data.
* **Persistent Storage**: Successfully saves all retrieved transactions into a MongoDB database, ensuring data is not lost after the 90-day API window closes.
* **Transaction View**: A clean user interface that displays a list of transactions. Each transaction can be expanded to show more detailed information, keeping the main view uncluttered.
* **Categorization**: Each transaction can be categorized, as well as having subcategories for better detailing. If detected, the user will be prompted for auto-completion of similar transactions.
* **On-Demand Refresh**: Users can load their transaction history instantly from the local database and manually trigger a refresh from the bank's API to fetch new transactions, respecting API rate limits.
* **Custom Categories & Rule Creation**: Users can create their own Categories and Subcategories as well custom Rules to automate the categorization of transactions.

---

## The Vision (Roadmap to v1.0)

The goal is to evolve pxFi into a smart, intuitive, and highly automated personal finance dashboard. The following user stories outline the roadmap to the first major version.

### Feature: Advanced Categorization
* Provide a default set of common financial categories and subcategories.
* Allow users to create, edit, and delete their own custom categories (e.g., "Food") and subcategories (e.g., "Groceries", "Restaurants").
* When a user categorizes one transaction, the app will find similar uncategorized transactions and offer to categorize them all in one batch.

### Feature: Automated Categorization
* Implement a keyword-based system to automatically assign categories to new transactions (e.g., a description containing "UBER" is automatically assigned to "Transportation").
* Develop a custom rules engine where users can define their own logic for how transactions should be categorized.

### Feature: Statistics & Reporting
* Create a dedicated dashboard page for financial insights.
* Allow users to filter their financial data by a specific month or an entire year.
* Display a high-level summary of total income vs. total expenses for the selected period.
* Provide a visual breakdown (charts and graphs) of spending by category to help users understand where their money is going.

---

## Technology Stack

* **Frontend**: Angular, TypeScript, Bootstrap
* **Backend**: Java, Spring Boot
* **Database**: MongoDB
* **Open Banking**: GoCardless API