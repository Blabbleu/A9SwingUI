import java.sql.*;

public class DbManager {
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/xepdb1";
    private static final String USER = "ninh_dg";
    private static final String PASSWORD = "22112005";

    static {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void dropAllObjects() throws SQLException {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {

            String[] blocks = {

                    // Drop views if they exist
                    """
                BEGIN
                    EXECUTE IMMEDIATE 'DROP VIEW V_DAILY_SALES';
                EXCEPTION WHEN OTHERS THEN NULL;
                END;
                """,
                    """
                BEGIN
                    EXECUTE IMMEDIATE 'DROP VIEW V_CUSTOMER_PURCHASE_HISTORY';
                EXCEPTION WHEN OTHERS THEN NULL;
                END;
                """,
                    """
                BEGIN
                    EXECUTE IMMEDIATE 'DROP VIEW V_PRODUCT_INVENTORY_STATUS';
                EXCEPTION WHEN OTHERS THEN NULL;
                END;
                """,

                    // Drop indexes
                    "BEGIN EXECUTE IMMEDIATE 'DROP INDEX ix_inv_emp';       EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP INDEX ix_inv_prod';      EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP INDEX ix_pay_txn';       EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP INDEX ix_txd_product';   EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP INDEX ix_txd_txn';       EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP INDEX ix_products_supplier'; EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP INDEX uq_discountproduct';   EXCEPTION WHEN OTHERS THEN NULL; END;",

                    // Drop tables
                    "BEGIN EXECUTE IMMEDIATE 'DROP TABLE DiscountProduct CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP TABLE Discounts CASCADE CONSTRAINTS';       EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP TABLE InventoryTransactions CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP TABLE TransactionDetails CASCADE CONSTRAINTS';    EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP TABLE Payments CASCADE CONSTRAINTS';         EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP TABLE Transactions CASCADE CONSTRAINTS';     EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP TABLE Products CASCADE CONSTRAINTS';         EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP TABLE Suppliers CASCADE CONSTRAINTS';        EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP TABLE Customers CASCADE CONSTRAINTS';        EXCEPTION WHEN OTHERS THEN NULL; END;",
                    "BEGIN EXECUTE IMMEDIATE 'DROP TABLE Employees CASCADE CONSTRAINTS';        EXCEPTION WHEN OTHERS THEN NULL; END;",
                    // Purge recyclebin
                    "PURGE RECYCLEBIN"
            };

            for (String block : blocks) {
                st.execute(block);
            }
        }
    }

    public static void createSchema() throws SQLException {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {

            // SUPPLIERS
            st.execute("""
                CREATE TABLE Suppliers (
                    SupplierID      NUMBER            PRIMARY KEY,
                    Name            VARCHAR2(100)     NOT NULL,
                    ContactInfo     VARCHAR2(100),
                    Address         VARCHAR2(200),
                    Email           VARCHAR2(120),
                    Phone           VARCHAR2(30)
                )
                """);

            // PRODUCTS
            st.execute("""
                CREATE TABLE Products (
                    ProductID       NUMBER            PRIMARY KEY,
                    SKU             VARCHAR2(40)      NOT NULL UNIQUE,
                    Name            VARCHAR2(120)     NOT NULL,
                    Category        VARCHAR2(60),
                    Price           NUMBER(10,2)      NOT NULL,
                    Cost            NUMBER(10,2)      NOT NULL,
                    SupplierID      NUMBER,
                    StockQuantity   NUMBER            DEFAULT 0 NOT NULL,
                    CONSTRAINT fk_products_supplier
                        FOREIGN KEY (SupplierID) REFERENCES Suppliers(SupplierID) ON DELETE CASCADE,
                    CONSTRAINT ck_product_price
                        CHECK (Price >= 0),
                    CONSTRAINT ck_product_cost
                        CHECK (Cost  >= 0),
                    CONSTRAINT ck_product_quantity
                        CHECK (StockQuantity >= 0)
                )
                """);

            // CUSTOMERS
            st.execute("""
                CREATE TABLE Customers (
                    CustomerID      NUMBER            PRIMARY KEY,
                    Name            VARCHAR2(120)     NOT NULL,
                    Phone           VARCHAR2(30),
                    Email           VARCHAR2(120),
                    Address         VARCHAR2(200),
                    LoyaltyPoints   NUMBER            DEFAULT 0 NOT NULL,
                    CONSTRAINT ck_customer_points
                        CHECK (LoyaltyPoints >= 0)
                )
                """);

            // EMPLOYEES
            st.execute("""
                CREATE TABLE Employees (
                    EmployeeID      NUMBER            PRIMARY KEY,
                    Name            VARCHAR2(120)     NOT NULL,
                    Role            VARCHAR2(60),
                    Username        VARCHAR2(60)      UNIQUE,
                    PasswordHash    VARCHAR2(200),
                    ContactInfo     VARCHAR2(100)
                )
                """);

            // TRANSACTIONS
            st.execute("""
                CREATE TABLE Transactions (
                    TransactionID   NUMBER            PRIMARY KEY,
                    DateTime        TIMESTAMP(0)      DEFAULT SYSTIMESTAMP NOT NULL,
                    CustomerID      NUMBER            NULL,
                    EmployeeID      NUMBER            NOT NULL,
                    TotalAmount     NUMBER(12,2)      NOT NULL,
                    Status          VARCHAR2(20)      NOT NULL,
                    CONSTRAINT fk_txn_customer
                        FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID) ON DELETE CASCADE,
                    CONSTRAINT fk_txn_employee
                        FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID) ON DELETE CASCADE,
                    CONSTRAINT ck_txn_total
                        CHECK (TotalAmount >= 0),
                    CONSTRAINT ck_txn_status
                        CHECK (Status IN ('Completed','Cancelled','Pending','Refunded'))
                )
                """);

            // PAYMENTS
            st.execute("""
                CREATE TABLE Payments (
                    PaymentID       NUMBER            PRIMARY KEY,
                    TransactionID   NUMBER            NOT NULL,
                    PaymentType     VARCHAR2(20)      NOT NULL,
                    AmountPaid      NUMBER(12,2)      NOT NULL,
                    PaymentStatus   VARCHAR2(20)      NOT NULL,
                    CONSTRAINT fk_pay_txn
                        FOREIGN KEY (TransactionID) REFERENCES Transactions(TransactionID) ON DELETE CASCADE,
                    CONSTRAINT ck_pay_type
                        CHECK (PaymentType IN ('Cash','Credit Card','Debit','Wallet')),
                    CONSTRAINT ck_pay_amount_nonneg
                        CHECK (AmountPaid >= 0),
                    CONSTRAINT ck_pay_status
                        CHECK (PaymentStatus IN ('Pending','Completed','Failed','Refunded'))
                )
                """);

            // TRANSACTION DETAILS (no Subtotal stored)
            st.execute("""
                CREATE TABLE TransactionDetails (
                    TransactionDetailID NUMBER        PRIMARY KEY,
                    TransactionID       NUMBER        NOT NULL,
                    ProductID           NUMBER        NOT NULL,
                    Quantity            NUMBER        NOT NULL CHECK (Quantity > 0),
                    SalePrice           NUMBER(10,2)  NOT NULL CHECK (SalePrice >= 0),
                    Discount            NUMBER(10,2)  DEFAULT 0 NOT NULL CHECK (Discount >= 0),
                    CONSTRAINT fk_details_transaction
                        FOREIGN KEY (TransactionID) REFERENCES Transactions(TransactionID) ON DELETE CASCADE,
                    CONSTRAINT fk_details_product
                        FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE CASCADE,
                    CONSTRAINT ck_txd_discount_le_sale
                        CHECK (Discount <= SalePrice)
                )
                """);

            // INVENTORY TRANSACTIONS
            st.execute("""
                CREATE TABLE InventoryTransactions (
                    InventoryTransactionID NUMBER       PRIMARY KEY,
                    ProductID              NUMBER       NOT NULL,
                    ChangeQty              NUMBER       NOT NULL,
                    Reason                 VARCHAR2(60) NOT NULL,
                    DateTime               TIMESTAMP(0) DEFAULT SYSTIMESTAMP NOT NULL,
                    EmployeeID             NUMBER,
                    CONSTRAINT fk_inventory_product
                        FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE CASCADE,
                    CONSTRAINT fk_inventory_employee
                        FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID) ON DELETE CASCADE
                )
                """);

            // DISCOUNTS
            st.execute("""
                CREATE TABLE Discounts (
                    DiscountID      NUMBER          PRIMARY KEY,
                    Name            VARCHAR2(120)   NOT NULL,
                    Description     VARCHAR2(400),
                    DiscountType    VARCHAR2(20)    NOT NULL CHECK (DiscountType IN ('Percentage','Amount')),
                    Value           NUMBER(10,2)    NOT NULL CHECK (Value >= 0),
                    StartDate       DATE            NOT NULL,
                    EndDate         DATE            NOT NULL,
                    CONSTRAINT ck_discounts_value
                        CHECK (
                            (DiscountType = 'Percentage' AND Value BETWEEN 0 AND 100)
                            OR
                            (DiscountType = 'Amount'     AND Value >= 0)
                        ),
                    CONSTRAINT ck_discounts_date CHECK (EndDate >= StartDate)
                )
                """);

            // DISCOUNT PRODUCT
            st.execute("""
                CREATE TABLE DiscountProduct (
                    DiscountProductID NUMBER PRIMARY KEY,
                    DiscountID        NUMBER NOT NULL,
                    ProductID         NUMBER NOT NULL,
                    CONSTRAINT fk_dp_discount
                        FOREIGN KEY (DiscountID) REFERENCES Discounts(DiscountID) ON DELETE CASCADE,
                    CONSTRAINT fk_dp_product
                        FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE CASCADE
                )
                """);

            // Indexes
            st.execute("CREATE UNIQUE INDEX uq_discountproduct ON DiscountProduct (DiscountID, ProductID)");
            st.execute("CREATE INDEX ix_products_supplier ON Products(SupplierID)");
            st.execute("CREATE INDEX ix_txd_txn      ON TransactionDetails(TransactionID)");
            st.execute("CREATE INDEX ix_txd_product  ON TransactionDetails(ProductID)");
            st.execute("CREATE INDEX ix_pay_txn      ON Payments(TransactionID)");
            st.execute("CREATE INDEX ix_inv_prod     ON InventoryTransactions(ProductID)");
            st.execute("CREATE INDEX ix_inv_emp      ON InventoryTransactions(EmployeeID)");
        }
    }

    public static void populateSampleData() throws SQLException {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {

            conn.setAutoCommit(false);
            try {
                // SUPPLIERS
                st.execute("""
                    INSERT INTO Suppliers (SupplierID, Name, ContactInfo, Address, Email, Phone) VALUES
                    (1, 'Lee''s Veggies Co', '416-111-2222', '123 Dundas St E', 'leeveg@gmail.com', '416-111-2222')
                    """);
                st.execute("""
                    INSERT INTO Suppliers (SupplierID, Name, ContactInfo, Address, Email, Phone) VALUES
                    (2, 'Gadget Supply Ltd', '999-999-9998', '2 West Ave', 'gadget@supplier.com', '999-999-9998')
                    """);
                st.execute("""
                    INSERT INTO Suppliers (SupplierID, Name, ContactInfo, Address, Email, Phone) VALUES
                    (3, 'Mark Butchery', '912-969-9998', '98 Main St', 'mark@butcher.com', '912-969-9998')
                    """);

                // PRODUCTS
                st.execute("""
                    INSERT INTO Products (ProductID, SKU, Name, Category, Price, Cost, SupplierID, StockQuantity) VALUES
                    (1, 'VEG01', 'Carrot', 'Vegetable', 1.99, 1.00, 1, 100)
                    """);
                st.execute("""
                    INSERT INTO Products (ProductID, SKU, Name, Category, Price, Cost, SupplierID, StockQuantity) VALUES
                    (2, 'ELE01', 'Buds Pro 2', 'Electronics', 149.99, 99.99, 2, 20)
                    """);
                st.execute("""
                    INSERT INTO Products (ProductID, SKU, Name, Category, Price, Cost, SupplierID, StockQuantity) VALUES
                    (3, 'MEAT01', 'Beef Sirloin', 'Meat', 15.50, 9.00, 3, 50)
                    """);

                // CUSTOMERS
                st.execute("""
                    INSERT INTO Customers (CustomerID, Name, Phone, Email, Address, LoyaltyPoints) VALUES
                    (1, 'John Smith', '696-696-9696', 'js@gmail.com', '1 Main St', 123)
                    """);
                st.execute("""
                    INSERT INTO Customers (CustomerID, Name, Phone, Email, Address, LoyaltyPoints) VALUES
                    (2, 'Mary Jane', '123-234-4567', 'mj@gmail.com', '154 Chester St', 60)
                    """);
                st.execute("""
                    INSERT INTO Customers (CustomerID, Name, Phone, Email, Address, LoyaltyPoints) VALUES
                    (3, 'Alice Doe', '647-888-9999', 'alice@example.com', '77 Queen St', 0)
                    """);

                // EMPLOYEES
                st.execute("""
                    INSERT INTO Employees (EmployeeID, Name, Role, Username, PasswordHash, ContactInfo) VALUES
                    (1, 'John Cart', 'Cashier', 'jcart', 'pass123hash', '416-555-8323')
                    """);
                st.execute("""
                    INSERT INTO Employees (EmployeeID, Name, Role, Username, PasswordHash, ContactInfo) VALUES
                    (2, 'Emily Dave', 'Cashier', 'edave', 'Pass345h', '416-343-2232')
                    """);
                st.execute("""
                    INSERT INTO Employees (EmployeeID, Name, Role, Username, PasswordHash, ContactInfo) VALUES
                    (3, 'Jackie Jackson', 'Manager', 'jjack', 'P@ssword', '416-756-1235')
                    """);

                // TRANSACTIONS
                st.execute("""
                    INSERT INTO Transactions (TransactionID, DateTime, CustomerID, EmployeeID, TotalAmount, Status) VALUES
                    (1, SYSTIMESTAMP, 1, 1, 3.98, 'Completed')
                    """);
                st.execute("""
                    INSERT INTO Transactions (TransactionID, DateTime, CustomerID, EmployeeID, TotalAmount, Status) VALUES
                    (2, SYSTIMESTAMP, NULL, 2, 149.99, 'Completed')
                    """);
                st.execute("""
                    INSERT INTO Transactions (TransactionID, DateTime, CustomerID, EmployeeID, TotalAmount, Status) VALUES
                    (3, SYSTIMESTAMP, 2, 1, 31.00, 'Completed')
                    """);

                // TRANSACTION DETAILS (no Subtotal)
                st.execute("""
                    INSERT INTO TransactionDetails
                        (TransactionDetailID, TransactionID, ProductID, Quantity, SalePrice, Discount)
                    VALUES
                        (1, 1, 1, 2, 1.99, 0)
                    """);
                st.execute("""
                    INSERT INTO TransactionDetails
                        (TransactionDetailID, TransactionID, ProductID, Quantity, SalePrice, Discount)
                    VALUES
                        (2, 2, 2, 1, 149.99, 0)
                    """);
                st.execute("""
                    INSERT INTO TransactionDetails
                        (TransactionDetailID, TransactionID, ProductID, Quantity, SalePrice, Discount)
                    VALUES
                        (3, 3, 3, 2, 15.50, 0)
                    """);

                // PAYMENTS
                st.execute("""
                    INSERT INTO Payments (PaymentID, TransactionID, PaymentType, AmountPaid, PaymentStatus) VALUES
                    (1, 1, 'Debit', 3.98, 'Completed')
                    """);
                st.execute("""
                    INSERT INTO Payments (PaymentID, TransactionID, PaymentType, AmountPaid, PaymentStatus) VALUES
                    (2, 2, 'Cash', 149.99, 'Completed')
                    """);
                st.execute("""
                    INSERT INTO Payments (PaymentID, TransactionID, PaymentType, AmountPaid, PaymentStatus) VALUES
                    (3, 3, 'Credit Card', 31.00, 'Completed')
                    """);

                // INVENTORY TRANSACTIONS
                st.execute("""
                    INSERT INTO InventoryTransactions
                        (InventoryTransactionID, ProductID, ChangeQty, Reason, DateTime, EmployeeID) VALUES
                        (1, 1, 10, 'Restock', SYSTIMESTAMP, 1)
                    """);
                st.execute("""
                    INSERT INTO InventoryTransactions
                        (InventoryTransactionID, ProductID, ChangeQty, Reason, DateTime, EmployeeID) VALUES
                        (2, 1, -2, 'Sale', SYSTIMESTAMP, 1)
                    """);
                st.execute("""
                    INSERT INTO InventoryTransactions
                        (InventoryTransactionID, ProductID, ChangeQty, Reason, DateTime, EmployeeID) VALUES
                        (3, 3, -2, 'Sale', SYSTIMESTAMP, 1)
                    """);

                // DISCOUNTS
                st.execute("""
                    INSERT INTO Discounts (DiscountID, Name, Description, DiscountType, Value, StartDate, EndDate) VALUES
                    (1, 'Electro Fest', '10% off on electronics', 'Percentage', 10, DATE '2025-09-05', DATE '2025-11-05')
                    """);
                st.execute("""
                    INSERT INTO Discounts (DiscountID, Name, Description, DiscountType, Value, StartDate, EndDate) VALUES
                    (2, 'Meat Week', '5 dollar off per beef unit', 'Amount', 5, DATE '2025-09-10', DATE '2025-12-31')
                    """);

                // DISCOUNT PRODUCT LINK
                st.execute("INSERT INTO DiscountProduct (DiscountProductID, DiscountID, ProductID) VALUES (1, 1, 2)");
                st.execute("INSERT INTO DiscountProduct (DiscountProductID, DiscountID, ProductID) VALUES (2, 2, 3)");

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    // ---------------------------------------------------------------------
// INSERT HELPERS FOR FORMS
// ---------------------------------------------------------------------

    public static void insertSupplier(
            int supplierId,
            String name,
            String contactInfo,
            String address,
            String email,
            String phone
    ) throws SQLException {
        String sql = """
        INSERT INTO Suppliers
            (SupplierID, Name, ContactInfo, Address, Email, Phone)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supplierId);
            ps.setString(2, name);
            ps.setString(3, contactInfo);
            ps.setString(4, address);
            ps.setString(5, email);
            ps.setString(6, phone);

            ps.executeUpdate();
        }
    }

    public static void insertCustomer(
            int customerId,
            String name,
            String phone,
            String email,
            String address,
            int loyaltyPoints
    ) throws SQLException {
        String sql = """
        INSERT INTO Customers
            (CustomerID, Name, Phone, Email, Address, LoyaltyPoints)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setString(2, name);
            ps.setString(3, phone);
            ps.setString(4, email);
            ps.setString(5, address);
            ps.setInt(6, loyaltyPoints);

            ps.executeUpdate();
        }
    }

    public static void insertProduct(
            int productId,
            String sku,
            String name,
            String category,
            double price,
            double cost,
            Integer supplierId,   // nullable
            int stockQty
    ) throws SQLException {
        String sql = """
        INSERT INTO Products
            (ProductID, SKU, Name, Category, Price, Cost, SupplierID, StockQuantity)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ps.setString(2, sku);
            ps.setString(3, name);
            ps.setString(4, category);
            ps.setDouble(5, price);
            ps.setDouble(6, cost);

            if (supplierId == null) {
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(7, supplierId);
            }

            ps.setInt(8, stockQty);

            ps.executeUpdate();
        }
    }


    // Simple manual test
    public static void main(String[] args) {
        try {
            dropAllObjects();
            System.out.println("Dropped existing objects.");
            createSchema();
            System.out.println("Created schema.");
            populateSampleData();
            System.out.println("Populated sample data.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}