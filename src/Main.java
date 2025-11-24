import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowUI);
    }

    private static void createAndShowUI() {
        JFrame frame = new JFrame("CPS510 - A9 POS Database Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane logScroll = new JScrollPane(logArea);

        // ===== TABS ==========================================================
        JTabbedPane tabs = new JTabbedPane();

        // Tab 1: Admin
        JPanel adminPanel = buildAdminPanel(logArea);
        tabs.addTab("Database Admin", adminPanel);

        // Tab 2: Insert Data
        JPanel insertPanel = buildInsertPanel(logArea);
        tabs.addTab("Insert Data", insertPanel);

        // Tab 3: Reports (Views + analytics)
        JPanel reportsPanel = buildReportsPanel(logArea);
        tabs.addTab("Reports", reportsPanel);

        // Tab 4: Query Tables
        JPanel queryPanel = buildQueryPanel(logArea);
        tabs.addTab("Query Tables", queryPanel);

        // Layout
        frame.setLayout(new BorderLayout());
        frame.add(tabs, BorderLayout.CENTER);
        frame.add(logScroll, BorderLayout.SOUTH);
        logScroll.setPreferredSize(new Dimension(frame.getWidth(), 220));

        frame.setVisible(true);
    }

    // ---------------------------------------------------------------------
    // ADMIN TAB (Drop / Create / Populate)
    // ---------------------------------------------------------------------
    private static JPanel buildAdminPanel(JTextArea logArea) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnDrop = new JButton("DROP ALL OBJECTS");
        JButton btnCreate = new JButton("CREATE SCHEMA");
        JButton btnPopulate = new JButton("POPULATE DATA");
        JButton btnExit = new JButton("EXIT");

        panel.add(btnDrop);
        panel.add(btnCreate);
        panel.add(btnPopulate);
        panel.add(btnExit);

        btnDrop.addActionListener(e -> {
            append(logArea, "Dropping all objects...");
            try {
                DbManager.dropAllObjects();
                append(logArea, "SUCCESS: All objects dropped.\n");
            } catch (SQLException ex) {
                append(logArea, "ERROR dropping objects: " + ex.getMessage() + "\n");
            }
        });

        btnCreate.addActionListener(e -> {
            append(logArea, "Creating schema...");
            try {
                DbManager.createSchema();
                append(logArea, "SUCCESS: Schema created.\n");
            } catch (SQLException ex) {
                append(logArea, "ERROR creating schema: " + ex.getMessage() + "\n");
            }
        });

        btnPopulate.addActionListener(e -> {
            append(logArea, "Populating sample data...");
            try {
                DbManager.populateSampleData();
                append(logArea, "SUCCESS: Sample data inserted.\n");
            } catch (SQLException ex) {
                append(logArea, "ERROR inserting data: " + ex.getMessage() + "\n");
            }
        });

        btnExit.addActionListener(e -> System.exit(0));

        return panel;
    }

    // ---------------------------------------------------------------------
    // Forms Tabs - Manually Insert Data
    // ---------------------------------------------------------------------
    private static JPanel buildInsertPanel(JTextArea logArea) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JTabbedPane innerTabs = new JTabbedPane();
        innerTabs.addTab("Supplier", buildSupplierForm(logArea));
        innerTabs.addTab("Customer", buildCustomerForm(logArea));
        innerTabs.addTab("Product", buildProductForm(logArea));

        panel.add(innerTabs, BorderLayout.CENTER);
        return panel;
    }

    // Supplier
    private static JPanel buildSupplierForm(JTextArea logArea) {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTextField tfId = new JTextField();
        JTextField tfName = new JTextField();
        JTextField tfContact = new JTextField();
        JTextField tfAddress = new JTextField();
        JTextField tfEmail = new JTextField();
        JTextField tfPhone = new JTextField();

        form.add(new JLabel("SupplierID (number):"));
        form.add(tfId);
        form.add(new JLabel("Name:"));
        form.add(tfName);
        form.add(new JLabel("Contact Info:"));
        form.add(tfContact);
        form.add(new JLabel("Address:"));
        form.add(tfAddress);
        form.add(new JLabel("Email:"));
        form.add(tfEmail);
        form.add(new JLabel("Phone:"));
        form.add(tfPhone);

        JButton btnAdd = new JButton("Add Supplier");
        form.add(new JLabel()); // spacer
        form.add(btnAdd);

        btnAdd.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfId.getText().trim());
                String name = tfName.getText().trim();
                String contact = tfContact.getText().trim();
                String address = tfAddress.getText().trim();
                String email = tfEmail.getText().trim();
                String phone = tfPhone.getText().trim();

                DbManager.insertSupplier(id, name, contact, address, email, phone);
                append(logArea, "Inserted Supplier ID " + id + "\n");
            } catch (NumberFormatException ex) {
                append(logArea, "ERROR: SupplierID must be a number.\n");
            } catch (SQLException ex) {
                append(logArea, "ERROR inserting supplier: " + ex.getMessage() + "\n");
            }
        });

        return form;
    }

    // Customer
    private static JPanel buildCustomerForm(JTextArea logArea) {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTextField tfId = new JTextField();
        JTextField tfName = new JTextField();
        JTextField tfPhone = new JTextField();
        JTextField tfEmail = new JTextField();
        JTextField tfAddress = new JTextField();
        JTextField tfPoints = new JTextField("0");

        form.add(new JLabel("CustomerID (number):"));
        form.add(tfId);
        form.add(new JLabel("Name:"));
        form.add(tfName);
        form.add(new JLabel("Phone:"));
        form.add(tfPhone);
        form.add(new JLabel("Email:"));
        form.add(tfEmail);
        form.add(new JLabel("Address:"));
        form.add(tfAddress);
        form.add(new JLabel("Loyalty Points:"));
        form.add(tfPoints);

        JButton btnAdd = new JButton("Add Customer");
        form.add(new JLabel()); // spacer
        form.add(btnAdd);

        btnAdd.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfId.getText().trim());
                String name = tfName.getText().trim();
                String phone = tfPhone.getText().trim();
                String email = tfEmail.getText().trim();
                String address = tfAddress.getText().trim();
                int points = Integer.parseInt(tfPoints.getText().trim());

                DbManager.insertCustomer(id, name, phone, email, address, points);
                append(logArea, "Inserted Customer ID " + id + "\n");
            } catch (NumberFormatException ex) {
                append(logArea, "ERROR: ID and Points must be numbers.\n");
            } catch (SQLException ex) {
                append(logArea, "ERROR inserting customer: " + ex.getMessage() + "\n");
            }
        });

        return form;
    }

    // Product
    private static JPanel buildProductForm(JTextArea logArea) {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTextField tfId = new JTextField();
        JTextField tfSku = new JTextField();
        JTextField tfName = new JTextField();
        JTextField tfCategory = new JTextField();
        JTextField tfPrice = new JTextField();
        JTextField tfCost = new JTextField();
        JTextField tfSupplier = new JTextField(); // optional
        JTextField tfStock = new JTextField("0");

        form.add(new JLabel("ProductID (number):"));
        form.add(tfId);
        form.add(new JLabel("SKU:"));
        form.add(tfSku);
        form.add(new JLabel("Name:"));
        form.add(tfName);
        form.add(new JLabel("Category:"));
        form.add(tfCategory);
        form.add(new JLabel("Price:"));
        form.add(tfPrice);
        form.add(new JLabel("Cost:"));
        form.add(tfCost);
        form.add(new JLabel("SupplierID (optional):"));
        form.add(tfSupplier);
        form.add(new JLabel("Stock Quantity:"));
        form.add(tfStock);

        JButton btnAdd = new JButton("Add Product");
        form.add(new JLabel()); // spacer
        form.add(btnAdd);

        btnAdd.addActionListener(e -> {
            try {
                int id = Integer.parseInt(tfId.getText().trim());
                String sku = tfSku.getText().trim();
                String name = tfName.getText().trim();
                String category = tfCategory.getText().trim();
                double price = Double.parseDouble(tfPrice.getText().trim());
                double cost = Double.parseDouble(tfCost.getText().trim());
                String supplierText = tfSupplier.getText().trim();
                Integer supplierId = supplierText.isEmpty() ? null : Integer.valueOf(supplierText);
                int stock = Integer.parseInt(tfStock.getText().trim());

                DbManager.insertProduct(id, sku, name, category, price, cost, supplierId, stock);
                append(logArea, "Inserted Product ID " + id + "\n");
            } catch (NumberFormatException ex) {
                append(logArea, "ERROR: Numeric fields must be valid numbers.\n");
            } catch (SQLException ex) {
                append(logArea, "ERROR inserting product: " + ex.getMessage() + "\n");
            }
        });

        return form;
    }

    // ---------------------------------------------------------------------
    // REPORTS TAB
    // ---------------------------------------------------------------------
    private static JPanel buildReportsPanel(JTextArea logArea) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JButton btnDailySales = new JButton("Daily Sales (V_DAILY_SALES)");
        JButton btnCustomerHistory = new JButton("Customer Purchase History (V_CUSTOMER_PURCHASE_HISTORY)");
        JButton btnInventoryStatus = new JButton("Product Inventory Status (V_PRODUCT_INVENTORY_STATUS)");
        JButton btnRevenueByProduct = new JButton("Product Revenue (Query)");

        panel.add(new JLabel("Run analytical reports:"));
        panel.add(btnDailySales);
        panel.add(btnCustomerHistory);
        panel.add(btnInventoryStatus);
        panel.add(btnRevenueByProduct);

        btnDailySales.addActionListener(e -> {
            String sql = """
                    SELECT
                        SalesDate,
                        TransactionCount,
                        TotalItemsSold,
                        TotalRevenue,
                        AvgTransactionValue
                    FROM V_DAILY_SALES
                    ORDER BY SalesDate
                    """;
            runReport(logArea, "Daily Sales (V_DAILY_SALES)", sql);
        });

        btnCustomerHistory.addActionListener(e -> {
            String sql = """
                    SELECT
                        CustomerID,
                        CustomerName,
                        TotalTransactions,
                        TotalSpent,
                        LastPurchaseDate
                    FROM V_CUSTOMER_PURCHASE_HISTORY
                    ORDER BY TotalSpent DESC
                    """;
            runReport(logArea, "Customer Purchase History (V_CUSTOMER_PURCHASE_HISTORY)", sql);
        });

        btnInventoryStatus.addActionListener(e -> {
            String sql = """
                    SELECT
                        ProductID,
                        SKU,
                        ProductName,
                        Category,
                        CurrentStock,
                        TotalRestocked,
                        TotalSoldOrRemoved,
                        NetChange,
                        PerUnitProfitMargin,
                        StockStatus
                    FROM V_PRODUCT_INVENTORY_STATUS
                    ORDER BY ProductID
                    """;
            runReport(logArea, "Product Inventory Status (V_PRODUCT_INVENTORY_STATUS)", sql);
        });

        btnRevenueByProduct.addActionListener(e -> {
            // Adapted from A4 Q10, but using computed subtotal
            String sql = """
                    SELECT
                        p.SKU,
                        p.Name,
                        SUM( (td.SalePrice - td.Discount) * td.Quantity ) AS Revenue
                    FROM TransactionDetails td
                    JOIN Transactions t
                        ON t.TransactionID = td.TransactionID
                    JOIN Products p
                        ON p.ProductID = td.ProductID
                    WHERE t.Status = 'Completed'
                    GROUP BY p.SKU, p.Name
                    ORDER BY Revenue DESC
                    """;
            runReport(logArea, "Product Revenue (Query)", sql);
        });

        return panel;
    }

    private static JPanel buildQueryPanel(JTextArea logArea) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] tables = {
                "Suppliers",
                "Products",
                "Customers",
                "Employees",
                "Transactions",
                "TransactionDetails",
                "Payments",
                "InventoryTransactions",
                "Discounts",
                "DiscountProduct"
        };

        JLabel lblTable = new JLabel("Table:");
        JComboBox<String> cbTable = new JComboBox<>(tables);

        JLabel lblId = new JLabel("Find by PK (ID) (optional):");
        JTextField tfId = new JTextField();

        JButton btnViewAll = new JButton("View All");
        JButton btnFindById = new JButton("Find by ID");

        // Row 0: Table label + combo
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblTable, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(cbTable, gbc);

        // Row 1: ID label + field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblId, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(tfId, gbc);

        // Row 2: Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnViewAll);
        btnPanel.add(btnFindById);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        // Actions
        btnViewAll.addActionListener(e -> {
            String table = (String) cbTable.getSelectedItem();
            if (table == null) return;
            String sql = "SELECT * FROM " + table;
            runReport(logArea, "View All from " + table, sql);
        });

        btnFindById.addActionListener(e -> {
            String table = (String) cbTable.getSelectedItem();
            if (table == null) return;
            String idText = tfId.getText().trim();
            if (idText.isEmpty()) {
                append(logArea, "Please enter an ID value to search.\n");
                return;
            }

            String pkCol = getPkColumnName(table);
            if (pkCol == null) {
                append(logArea, "No PK mapping defined for table: " + table + "\n");
                return;
            }

            // All PKs in this schema are numeric, so try parse as integer
            int idVal;
            try {
                idVal = Integer.parseInt(idText);
            } catch (NumberFormatException ex) {
                append(logArea, "ERROR: ID must be a numeric value.\n");
                return;
            }

            String sql = "SELECT * FROM " + table + " WHERE " + pkCol + " = " + idVal;
            runReport(logArea, "Find by ID in " + table + " (" + pkCol + " = " + idVal + ")", sql);
        });

        return panel;
    }

    // Helper: PK column name per table
    private static String getPkColumnName(String table) {
        return switch (table) {
            case "Suppliers" -> "SupplierID";
            case "Products" -> "ProductID";
            case "Customers" -> "CustomerID";
            case "Employees" -> "EmployeeID";
            case "Transactions" -> "TransactionID";
            case "TransactionDetails" -> "TransactionDetailID";
            case "Payments" -> "PaymentID";
            case "InventoryTransactions" -> "InventoryTransactionID";
            case "Discounts" -> "DiscountID";
            case "DiscountProduct" -> "DiscountProductID";
            default -> null;
        };
    }

    private static void runReport(JTextArea logArea, String title, String sql) {
        append(logArea, "\n=== REPORT: " + title + " ===");
        try {
            String result = DbManager.runQuery(sql);
            append(logArea, result + "\n");
        } catch (SQLException ex) {
            append(logArea, "ERROR running report: " + ex.getMessage() + "\n");
        }
    }

    // ---------------------------------------------------------------------
    private static void append(JTextArea area, String text) {
        area.append(text + "\n");
        area.setCaretPosition(area.getDocument().getLength());
    }
}
