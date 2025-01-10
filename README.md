# mecash-multicurrency-wallet
 mecash-multicurrency-wallet


meCash Multicurrency Wallet API
A RESTful API for a multicurrency wallet application that enables users to manage accounts, deposit, withdraw, transfer funds, and view transaction history. Designed for scalability and robustness using Spring Boot, JPA/Hibernate, and a relational database.

Features
Account Management: Create and log in to an account.
Deposits: Add money to your account in supported currencies.
Withdrawals: Withdraw funds from your account.
Transfers: Transfer money between users.
Balance Inquiry: Check your account balance.
Transaction History: View a list of past transactions.
Technology Stack
Backend Framework: Spring Boot
Programming Language: Java
ORM: JPA/Hibernate
Database: mysql
Testing: JUnit, Mockito
Version Control: Git
Prerequisites
Ensure the following are installed on your system:

Java 17 or higher
Maven 3.8 or higher
mysql
Git
Setup Instructions
1. Clone the Repository
bash:

https://github.com/bornbylove/mecash-multicurrency-wallet
cd mecash-multicurrency-wallet
2. Configure the Database
Create a database in mysql

sql:

CREATE DATABASE mecash_wallet;
Update the application.properties file with your database credentials:

properties:

spring.datasource.url=jdbc:postgresql://localhost:5432/mecash_wallet
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
3. Build the Project
Use Maven to build the project:

bash:

mvn clean install
4. Run the Application
Start the application using Maven:

bash:

mvn spring-boot:run
The server will run on http://localhost:8080 by default.

API Endpoints
Endpoint	HTTP Method	Description
/api/accounts	POST	Create a new account
/api/login	POST	Log in to an account
/api/accounts/deposit	POST	Deposit funds into the account
/api/accounts/withdraw	POST	Withdraw funds from the account
/api/accounts/transfer	POST	Transfer funds to another account
/api/accounts/balance	GET	View account balance
/api/accounts/transactions	GET	View transaction history