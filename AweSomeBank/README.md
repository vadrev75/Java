AwesomeGIC Bank System
A simple Java console application that simulates a banking system with account management, transaction processing, and interest calculation capabilities.
Table of Contents

Features
Project Structure
Architecture Benefits
Running the Application
Testing

Features

Bank Account Management: Create and manage bank accounts.
Transaction Processing: Handle deposits and withdrawals with validation.
Interest Rules: Define and manage interest rate rules with effective dates.
Interest Calculation: Automatically calculate interest based on daily balances and applicable rules.
Monthly Statements: Generate account statements for a specific month, including transaction history and applied interest.

Project Structure
com.awesome.gic
├── interfaces/
│   ├── AccountService.java
│   ├── InterestService.java
│   └── TransactionService.java
├── models/
│   ├── Account.java
│   ├── InterestRule.java
│   ├── Statement.java
│   └── Transaction.java
├── services/
│   ├── AccountServiceImpl.java 
│   ├── InterestRuleServiceImpl.java 
│   └── TransactionServiceImpl.java
├── main/
│   ├── BankSystem.java
└── test/java
│   ├── AccountServiceTest.java
│   ├── InterestServiceTest.java
│   └── TransactionServiceTest.java

Architecture Benefits
Interface-based Design
The project uses interface-based design with separate interfaces and implementations for services. This approach offers several advantages:

Loose Coupling: Components depend on interfaces, not concrete implementations, making the system more flexible.
Testability: Interfaces can be easily mocked for unit testing.
Extensibility: New implementations can be added without changing client code.
Maintainability: Clear separation of concerns makes code easier to understand and maintain.
Parallel Development: Teams can work on different implementations simultaneously.

Service Layer Implementation
The service layer pattern provides these benefits:

Business Logic Encapsulation: Business rules stay independent of UI or data access logic.
Single Responsibility: Each service focuses on a specific domain (accounts, transactions, interest).
Reusability: Services can be reused across different parts of the application.
Centralized Logic: Business rules are implemented in one place, avoiding duplication.
Simplified Testing: Service-focused tests can verify business logic in isolation.

Running the Application

Clone the repository
Compile the Java files
Run the BankSystem main class

The application provides a console interface with the following options:

T: Input transactions
I: Define interest rules
P: Print statement
Q: Quit

Testing
The project includes comprehensive JUnit tests for all service classes and utilities. Each service method is tested to ensure edge cases are handled correctly.
To run tests:
mvn test
