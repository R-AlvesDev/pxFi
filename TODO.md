# pxFi - Project TO-DO & Roadmap (v1.0)

This document outlines the features and implementation plan for the first fully working version of the pxFi personal finance tracker.

---

## Suggested Order of Implementation

1.  **Build the Category Management System**: Create the backend models and API endpoints for managing categories and subcategories, then build the corresponding UI.
2.  **Enhance Transaction View for Manual Categorization**: Update the transaction table to allow assigning categories to individual transactions.
3.  **Implement "Categorize Similar" Feature**: Add the logic to find and categorize similar transactions in a batch.
4.  **Build the Statistics Page**: Develop the backend endpoints and frontend components for financial reporting and visualization.
5.  **Implement Full Automation (Rules Engine)**: Create the system for defining and applying custom, automatic categorization rules.

---

## User Stories

### Feature: Categories & Subcategories
* [X] **As a user, I want to** see a default list of common financial categories and subcategories so I can start organizing my transactions immediately.
* [X] **As a user, I want to** create, edit, and delete my own custom categories and subcategories to perfectly match my spending habits.
* [X] **As a user, I want to** manually assign a category and subcategory to any transaction from the transactions list.
* [X] **As a user, when I categorize a transaction, I want** the app to find other uncategorized transactions with a similar description and ask if I want to apply the same category to all of them.

### Feature: Automated Categorization
* [ ] **As a user, I want** the system to automatically categorize new transactions based on a predefined set of keywords (e.g., "UBER" is always "Transportation").
* [ ] **As a user, I want to** create my own custom rules (e.g., "if the description contains 'Pingo Doce', categorize it as 'Food > Groceries'") to automate my personal tracking.

### Feature: Statistics & Reporting
* [ ] **As a user, I want to** navigate to a dedicated "Statistics" page.
* [ ] **As a user, on the Statistics page, I want to** be able to filter the data by a specific month or an entire year.
* [ ] **As a user, for any selected period, I want to** see a clear, high-level summary of my total income versus my total expenses.
* [ ] **As a user, for any selected period, I want to** see a visual breakdown of my expenses by category (e.g., in a chart or list) to understand where my money goes.