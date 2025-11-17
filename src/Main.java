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

        // Layout
        frame.setLayout(new BorderLayout());
        frame.add(tabs, BorderLayout.CENTER);
        frame.add(logScroll, BorderLayout.SOUTH);
        logScroll.setPreferredSize(new Dimension(frame.getWidth(), 200));

        frame.setVisible(true);
    }

    // ---------------------------------------------------------------------
    // ADMIN TAB (Drop / Create / Populate)
    // ---------------------------------------------------------------------
    private static JPanel buildAdminPanel(JTextArea logArea) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnDrop     = new JButton("DROP ALL OBJECTS");
        JButton btnCreate   = new JButton("CREATE SCHEMA");
        JButton btnPopulate = new JButton("POPULATE DATA");

        panel.add(btnDrop);
        panel.add(btnCreate);
        panel.add(btnPopulate);

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

        return panel;
    }

    // ---------------------------------------------------------------------
    // INSERT TAB (forms for Supplier, Customer, Product)
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

    // =========== SUPPLIER FORM ===========================================
    private static JPanel buildSupplierForm(JTextArea logArea) {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTextField tfId      = new JTextField();
        JTextField tfName    = new JTextField();
        JTextField tfContact = new JTextField();
        JTextField tfAddress = new JTextField();
        JTextField tfEmail   = new JTextField();
        JTextField tfPhone   = new JTextField();

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
                append(logArea, "Inserted Supplier ID " + id);
            } catch (NumberFormatException ex) {
                append(logArea, "ERROR: SupplierID must be a number.\n");
            } catch (SQLException ex) {
                append(logArea, "ERROR inserting supplier: " + ex.getMessage() + "\n");
            }
        });

        return form;
    }

    // =========== CUSTOMER FORM ===========================================
    private static JPanel buildCustomerForm(JTextArea logArea) {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTextField tfId      = new JTextField();
        JTextField tfName    = new JTextField();
        JTextField tfPhone   = new JTextField();
        JTextField tfEmail   = new JTextField();
        JTextField tfAddress = new JTextField();
        JTextField tfPoints  = new JTextField("0");

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
                append(logArea, "Inserted Customer ID " + id);
            } catch (NumberFormatException ex) {
                append(logArea, "ERROR: ID and Points must be numbers.\n");
            } catch (SQLException ex) {
                append(logArea, "ERROR inserting customer: " + ex.getMessage() + "\n");
            }
        });

        return form;
    }

    // =========== PRODUCT FORM ============================================
    private static JPanel buildProductForm(JTextArea logArea) {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTextField tfId        = new JTextField();
        JTextField tfSku       = new JTextField();
        JTextField tfName      = new JTextField();
        JTextField tfCategory  = new JTextField();
        JTextField tfPrice     = new JTextField();
        JTextField tfCost      = new JTextField();
        JTextField tfSupplier  = new JTextField(); // optional
        JTextField tfStock     = new JTextField("0");

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
                append(logArea, "Inserted Product ID " + id);
            } catch (NumberFormatException ex) {
                append(logArea, "ERROR: Numeric fields must be valid numbers.\n");
            } catch (SQLException ex) {
                append(logArea, "ERROR inserting product: " + ex.getMessage() + "\n");
            }
        });

        return form;
    }

    // ---------------------------------------------------------------------
    private static void append(JTextArea area, String text) {
        area.append(text + "\n");
        area.setCaretPosition(area.getDocument().getLength());
    }
}
