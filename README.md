# CPS510 – A9 POS Database Manager

## 1. Overview

This project is a Java Swing application that manages a fully-normalized (3NF / BCNF) POS (Point-of-Sale) relational database using Oracle XE.

The tool provides:

### **Database Administration**
- Drop all objects
- Create schema
- Populate sample data

### **Data Entry Forms**
- Add Suppliers
- Add Customers
- Add Products

### **Query Tables Tab**
- View all rows from any base table
- Find a row by primary key

### **Reports Tab**
- Daily Sales (`V_DAILY_SALES`)
- Customer Purchase History (`V_CUSTOMER_PURCHASE_HISTORY`)
- Product Inventory Status (`V_PRODUCT_INVENTORY_STATUS`)
- Product Revenue (computed query)

All SQL is run through JDBC (`oracle.jdbc.OracleDriver`).

---

## 2. Requirements

### Software Needed
- Java 17+
- Oracle XE 18c or 21c
- Oracle JDBC Driver (`ojdbc11.jar`)

### Database Credentials
Update inside `DbManager.java`:

```java
private static final String URL = "jdbc:oracle:thin:@//localhost:1521/xepdb1";
private static final String USER = "your_username";
private static final String PASSWORD = "your_password";
```

---

## 3. How to Compile

### Windows:
```sh
javac -cp .;ojdbc11.jar DbManager.java Main.java
```

### macOS/Linux:
```sh
javac -cp .:ojdbc11.jar DbManager.java Main.java
```

---

## 4. How to Run

### Windows:
```sh
java -cp .;ojdbc11.jar Main
```

### macOS/Linux:
```sh
java -cp .:ojdbc11.jar Main
```

GUI Tabs:
1. Database Admin
2. Insert Data
3. Query Tables
4. Reports

---

## 5. Typical Usage Flow

1. Open the program
2. Go to **Database Admin**
3. Click **DROP ALL OBJECTS**
4. Click **CREATE SCHEMA**
5. Click **POPULATE DATA**

Then you can:
- Insert records
- Query tables
- Run reports

---

## 6. Troubleshooting

### No suitable driver
Ensure `ojdbc11.jar` is in the classpath.

### Cannot connect to database
- Oracle XE service is running
- Correct credentials
- Listener active at `localhost:1521`
- PDB `xepdb1` is open (`SHOW PDBS;`)

---

## 7. Notes

This project satisfies the A9 requirements for:
- DB administration
- Data insertion
- Table querying
- Analytical SQL reports  
