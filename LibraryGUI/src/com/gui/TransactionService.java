package com.gui;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TransactionService {

    private static final int FINE_PER_DAY = 5;
    private final BookService bookService = new BookService();
    private final MemberService memberService = new MemberService();

    public void displayTransactions() {
    	updateFine();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = """
                SELECT t.TransactionID, m.Name as MemberName, b.Title as BookTitle, 
                       t.IssueDate, t.DueDate, t.ReturnDate, t.Status, t.Fine, t.DaysOverdue
                FROM transactions t
                JOIN member m ON t.MemberID = m.MemberID
                JOIN books b ON t.BookID = b.BookID
                ORDER BY t.TransactionID ASC
            """;
            
            ResultSet rs = stmt.executeQuery(sql);
            JTable table = new JTable(buildTableModel(rs));
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new java.awt.Dimension(800, 400));
            
            JOptionPane.showMessageDialog(null, scrollPane, "Transactions List", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            showError(e);
        }
    }
    
    public void updateFine() {
        String sql = "SELECT TransactionID, DueDate FROM transactions WHERE ReturnDate IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int finePerDay = 5;

            while (rs.next()) {
                int id = rs.getInt("TransactionID");
                Date due = rs.getDate("DueDate");
                LocalDate dueDate = due.toLocalDate();
                LocalDate today = LocalDate.now();

                if (dueDate.isBefore(today)) {
                    int daysOverdue = (int) ChronoUnit.DAYS.between(dueDate, today);
                    int fine = daysOverdue * finePerDay;

                    String updateQuery = "UPDATE transactions SET Fine = ?, DaysOverdue = ? WHERE TransactionID = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                        ps.setInt(1, fine);
                        ps.setInt(2, daysOverdue);
                        ps.setInt(3, id);
                        ps.executeUpdate();
                    }
                } else {
                    // Optional: Clear fine and daysOverdue if no longer overdue
                    String clearQuery = "UPDATE transactions SET Fine = 0, DaysOverdue = 0 WHERE TransactionID = ?";
                    try (PreparedStatement ps = conn.prepareStatement(clearQuery)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error updating fines: " + e.getMessage(), 
                                          "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    public void issueBook(int memberId, int bookId) {
        // Validate member exists
        if (!memberService.memberExists(memberId)) {
            JOptionPane.showMessageDialog(null, "Member ID " + memberId + " does not exist.", 
                                        "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate book exists
        if (!bookService.bookExists(bookId)) {
            JOptionPane.showMessageDialog(null, "Book ID " + bookId + " does not exist.", 
                                        "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if book is available
        if (!bookService.isBookAvailable(bookId)) {
            JOptionPane.showMessageDialog(null, "Book is not available for issue.", 
                                        "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if member already has this book issued
        if (hasPendingTransaction(memberId, bookId)) {
            JOptionPane.showMessageDialog(null, "Member already has this book issued.", 
                                        "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO transactions (MemberID, BookID, IssueDate, DueDate, Status) VALUES (?, ?, ?, ?, 'ISSUED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {

            LocalDate today = LocalDate.now();
            LocalDate dueDate = today.plusDays(14); // 2 weeks

            ps.setInt(1, memberId);
            ps.setInt(2, bookId);
            ps.setDate(3, Date.valueOf(today));
            ps.setDate(4, Date.valueOf(dueDate));
            ps.executeUpdate();

            // Update book availability
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int transactionId = rs.getInt(1);
                bookService.updateBookAvailability(bookId, -1);
                JOptionPane.showMessageDialog(null, "Book issued successfully.\nTransaction ID = " + transactionId + "\nDue Date: " + dueDate);
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    public void returnBook(int transactionId) {
        // First check if transaction exists and book is not already returned
        if (!isValidTransaction(transactionId)) {
            JOptionPane.showMessageDialog(null, "Invalid transaction ID or book already returned.", 
                                        "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String query = "SELECT DueDate, BookID FROM transactions WHERE TransactionID = ? AND Status = 'ISSUED'";
        String update = "UPDATE transactions SET ReturnDate = ?, Fine = ?, DaysOverdue = ?, Status = 'RETURNED' WHERE TransactionID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement select = conn.prepareStatement(query);
             PreparedStatement updateStmt = conn.prepareStatement(update)) {

            select.setInt(1, transactionId);
            ResultSet rs = select.executeQuery();

            if (rs.next()) {
                LocalDate dueDate = rs.getDate("DueDate").toLocalDate();
                int bookId = rs.getInt("BookID");
                LocalDate returnDate = LocalDate.now();
                long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
                int fine = (int) Math.max(daysOverdue, 0) * FINE_PER_DAY;

                updateStmt.setDate(1, Date.valueOf(returnDate));
                updateStmt.setString(2, String.valueOf(fine)); // Fine is VARCHAR in schema
                updateStmt.setString(3, String.valueOf(Math.max(daysOverdue, 0))); // DaysOverdue is VARCHAR
                updateStmt.setInt(4, transactionId);
                updateStmt.executeUpdate();

                // Update book availability
                bookService.updateBookAvailability(bookId, 1);

                String message = "Book returned successfully.";
                if (fine > 0) {
                    message += "\nFine: ₹" + fine + " (" + daysOverdue + " days overdue)";
                } else {
                    message += "\nNo fine (returned on time)";
                }
                
                JOptionPane.showMessageDialog(null, message);
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    private boolean hasPendingTransaction(int memberId, int bookId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE MemberID = ? AND BookID = ? AND Status = 'ISSUED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, memberId);
            ps.setInt(2, bookId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (Exception e) {
            showError(e);
            return false;
        }
    }

    private boolean isValidTransaction(int transactionId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE TransactionID = ? AND Status = 'ISSUED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transactionId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (Exception e) {
            showError(e);
            return false;
        }
    }

    private DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        DefaultTableModel model = new DefaultTableModel();

        for (int i = 1; i <= columnCount; i++) {
            model.addColumn(meta.getColumnName(i));
        }

        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                String columnName = meta.getColumnName(i);

                if ("Fine".equalsIgnoreCase(columnName)) {
                    double fineValue = rs.getDouble(i);
                    row[i - 1] = String.format("$%.2f", fineValue);  // ✅ Add $ symbol
                } else if ("DaysOverdue".equalsIgnoreCase(columnName)) {
                    int days = rs.getInt(i);
                    row[i - 1] = days + (days > 0 ? " days" : "");   // Optional: format
                } else {
                    row[i - 1] = rs.getObject(i); // Default
                }
            }
            model.addRow(row);
        }
        return model;
    }


    private void showError(Exception e) {
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}