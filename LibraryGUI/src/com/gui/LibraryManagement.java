package com.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.List;

public class LibraryManagement extends JFrame {

    private final BookService bookService = new BookService();
    private final MemberService memberService = new MemberService();
    private final TransactionService transactionService = new TransactionService();

    public LibraryManagement() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        createMenuBar();

        JPanel mainPanel = new JPanel(new BorderLayout());

        // 💡 Use vertical Box container for image + welcome text
        Box centerBox = Box.createVerticalBox();
        centerBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        try { 	
            ImageIcon logoIcon = new ImageIcon("logo.png");
            Image image = logoIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(image));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerBox.add(Box.createVerticalStrut(20));
            centerBox.add(logoLabel);
            System.out.println(new File("logo.png").exists());
            System.out.println("Logo added!");
        } catch (Exception e) {
            System.err.println("Logo not found: " + e.getMessage());
        }

        JLabel welcomeLabel = new JLabel("Welcome to Library Management System", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Times New Roman", Font.BOLD, 30));
        welcomeLabel.setForeground(Color.BLUE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerBox.add(Box.createVerticalStrut(20)); // spacing
        centerBox.add(welcomeLabel);

        // 📦 Wrap the box in a panel so BorderLayout.CENTER works nicely
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        centerPanel.add(centerBox);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
    }


    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Quick Stats"));
        
        JButton refreshButton = new JButton("Refresh Stats");
        refreshButton.addActionListener(e -> updateStats());
        panel.add(refreshButton);
        
        return panel;
    }

    private void updateStats() {
        // This could be enhanced to show actual statistics from database
        JOptionPane.showMessageDialog(this, "Statistics feature can be implemented here", 
                                    "Stats", JOptionPane.INFORMATION_MESSAGE);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Books Menu
        JMenu bookMenu = new JMenu("Books");
        JMenuItem viewBooks = new JMenuItem("View Books");
        JMenuItem addBook = new JMenuItem("Add Book");

        viewBooks.addActionListener(e -> bookService.displayBooks());
        addBook.addActionListener(e -> showAddBookDialog());

        bookMenu.add(viewBooks);
        bookMenu.add(addBook);

        // Members Menu
        JMenu memberMenu = new JMenu("Members");
        JMenuItem viewMembers = new JMenuItem("View Members");
        JMenuItem addMember = new JMenuItem("Add Member");

        viewMembers.addActionListener(e -> memberService.displayMembers());
        addMember.addActionListener(e -> showAddMemberDialog());

        memberMenu.add(viewMembers);
        memberMenu.add(addMember);

        // Transactions Menu
        JMenu transactionMenu = new JMenu("Transactions");
        JMenuItem viewTransactions = new JMenuItem("View Transactions");
        JMenuItem overdueBooks = new JMenuItem("Overdue Books"); // Fixed: declare the variable
        JMenuItem issueBook = new JMenuItem("Issue Book");
        JMenuItem returnBook = new JMenuItem("Return Book");

        viewTransactions.addActionListener(e -> transactionService.displayTransactions());
        overdueBooks.addActionListener(e -> showOverdueBooks());
        issueBook.addActionListener(e -> showIssueBookDialog());
        returnBook.addActionListener(e -> showReturnBookDialog());

        transactionMenu.add(viewTransactions);
        transactionMenu.add(overdueBooks);
        transactionMenu.addSeparator();
        transactionMenu.add(issueBook);
        transactionMenu.add(returnBook);

        menuBar.add(bookMenu);
        menuBar.add(memberMenu);
        menuBar.add(transactionMenu);

        setJMenuBar(menuBar);
    }

    private void showAddBookDialog() {
        JTextField titleField = new JTextField(20);
        JTextField authorField = new JTextField(20);
        JTextField qtyField = new JTextField(10);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Book Title:"), gbc);
        gbc.gridx = 1;
        panel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        panel.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        panel.add(qtyField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Book", 
                                                 JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText();
                String author = authorField.getText();
                int qty = Integer.parseInt(qtyField.getText());
                bookService.addBook(title, author, qty);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a number.", 
                                            "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddMemberDialog() {
        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(15);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Member", 
                                                 JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            
            if (name != null && !name.trim().isEmpty() && 
                email != null && !email.trim().isEmpty() && 
                phone != null && !phone.trim().isEmpty()) {
                
                // Validate phone number
                if (!phone.matches("\\d{10}")) {
                    JOptionPane.showMessageDialog(this, "Phone number must be exactly 10 digits.", 
                                                "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Validate email format
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    JOptionPane.showMessageDialog(this, "Invalid email format.", 
                                                "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Add member with all details
                addMemberWithDetails(name, email, phone);
            } else {
                JOptionPane.showMessageDialog(this, "All fields are required.", 
                                            "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addMemberWithDetails(String name, String email, String phone) {
        String sql = "INSERT INTO member (Name, Email, Phone, Reg_Date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name.trim());
            ps.setString(2, email.trim());
            ps.setString(3, phone.trim());
            ps.setDate(4, Date.valueOf(java.time.LocalDate.now()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int memberId = rs.getInt(1);
                JOptionPane.showMessageDialog(null, "Member added successfully.\nMember ID = " + memberId);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showIssueBookDialog() {
        JTextField memberIdField = new JTextField(10);
        JTextField bookIdField = new JTextField(10);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Member ID:"), gbc);
        gbc.gridx = 1;
        panel.add(memberIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Book ID:"), gbc);
        gbc.gridx = 1;
        panel.add(bookIdField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Issue Book", 
                                                 JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int memberId = Integer.parseInt(memberIdField.getText());
                int bookId = Integer.parseInt(bookIdField.getText());
                transactionService.issueBook(memberId, bookId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid ID. Please enter valid numbers.", 
                                            "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showReturnBookDialog() {
        String input = JOptionPane.showInputDialog(this, "Enter Transaction ID:", "Return Book", 
                                                 JOptionPane.PLAIN_MESSAGE);
        if (input != null) {
            try {
                int transactionId = Integer.parseInt(input);
                transactionService.returnBook(transactionId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Transaction ID. Please enter a number.", 
                                            "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showOverdueBooks() {
        List<Object[]> overdueBooks = bookService.getOverdueBooks();
        
        if (overdueBooks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No overdue books found!", "Overdue Books", 
                                        JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] columnNames = {"Transaction ID", "Book Title", "Member Name", "Issue Date", "Due Date", "Days Overdue","Fine"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (Object[] row : overdueBooks) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Overdue Books (" + overdueBooks.size() + " found)", 
                                    JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }

        SwingUtilities.invokeLater(() -> new LibraryManagement().setVisible(true));
    }
}