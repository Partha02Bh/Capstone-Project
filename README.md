# 🏦 SecureMoneyTransfer – Full-Stack Banking System

A secure money transfer application built with **Spring Boot** (Backend) and **Angular 16** (Frontend), using **MySQL** as the database.

---

## 🛠️ Prerequisites

Make sure you have these installed before running:

| Tool           | Version    | Download Link                          |
|----------------|------------|----------------------------------------|
| **Java JDK**   | 17+        | https://adoptium.net/                  |
| **Maven**      | 3.6+       | https://maven.apache.org/download.cgi  |
| **Node.js**    | 18+        | https://nodejs.org/                    |
| **Angular CLI**| 16.x       | `npm install -g @angular/cli@16`       |
| **MySQL**      | 8.0+       | https://dev.mysql.com/downloads/       |

---

## 📁 Project Structure

```
SecureMoneyTransfer/
├── Backend-usingMaven/     # Spring Boot Backend (Java 17, Maven)
├── frontend/               # Angular 16 Frontend
├── database/
│   ├── schema.sql          # Table definitions
│   └── seed-data.sql       # Initial test data
└── README.md
```

---

## 🚀 How to Run (Step-by-Step)

### Step 1: Clone the Repository

```bash
git clone https://github.com/Partha02Bh/Final-MTS.git
cd Final-MTS
```

### Step 2: Set Up MySQL Database

1. Open **MySQL Workbench** (or terminal).
2. Create the database:

```sql
CREATE DATABASE IF NOT EXISTS banking_system_db;
```

3. Run the schema file to create tables:

```sql
SOURCE /path/to/Final-MTS/database/schema.sql;
```

4. *(Optional)* Run the seed data to add test users:

```sql
SOURCE /path/to/Final-MTS/database/seed-data.sql;
```

> **Note:** The app also has a `DataSeeder.java` that auto-creates users on startup, so seed-data.sql is optional.

### Step 3: Configure Database Credentials

Open `Backend-usingMaven/src/main/resources/application.properties` and update with **your** MySQL credentials:

```properties
spring.datasource.url = jdbc:mysql://localhost:3306/banking_system_db
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

> If your MySQL has no password, leave it blank: `spring.datasource.password=`

### Step 4: Start the Backend

```bash
cd Backend-usingMaven
mvn spring-boot:run
```

Wait until you see: `Started DemoApplication on port 8080`

### Step 5: Start the Frontend

Open a **new terminal** window:

```bash
cd frontend
npm install
ng serve
```

> If `ng` is not found, run: `npx ng serve`

Wait until you see: `Compiled successfully`

### Step 6: Open the App

Open your browser and go to:

```
http://localhost:4200
```

---

## 👤 Test Accounts

| Username  | Password    | Role  |
|-----------|-------------|-------|
| admin     | admin123    | ADMIN |
| Partha    | Partha123   | USER  |
| Prakhar   | Prakhar123  | USER  |
| Nidhi     | Nidhi123    | USER  |
| Krishna   | Krishna123  | USER  |

- **USER** login → User Dashboard (Deposit, Withdraw, Transfer)
- **ADMIN** login → Admin Dashboard (View all transactions)

---

## 🔧 Tech Stack

| Layer     | Technology                          |
|-----------|-------------------------------------|
| Frontend  | Angular 16, TypeScript, CSS         |
| Backend   | Spring Boot 3.4, Spring Security, JPA |
| Auth      | JWT (JSON Web Tokens)               |
| Database  | MySQL 8.0                           |
| Build     | Maven (Backend), npm (Frontend)     |

---

## ❓ Troubleshooting

| Issue                         | Fix                                                    |
|-------------------------------|--------------------------------------------------------|
| `Access Denied` on MySQL      | Check username/password in `application.properties`    |
| `Port 8080 already in use`    | Kill the process: `lsof -i :8080` then `kill -9 <PID>` |
| `ng: command not found`       | Use `npx ng serve` or install Angular CLI globally     |
| `npm install` errors          | Delete `node_modules` folder, then run `npm install`   |
