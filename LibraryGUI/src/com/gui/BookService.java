package com.gui;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class BookService {

    public void displayBooks() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT * FROM books");
            JTable table = new JTable(buildTableModel(rs));
            JOptionPane.showMessageDialog(null, new JScrollPane(table), "Books List", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            showError(e);
        }
    }

    public void addBook(String title, String author, int quantity) {
        if (title == null || title.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Book title cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (author == null || author.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Author name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (quantity <= 0) {
            JOptionPane.showMessageDialog(null, "Quantity must be greater than 0.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO books (Title, Author, Available, Total) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, title.trim());
            ps.setString(2, author.trim());
            ps.setInt(3, quantity);
            ps.setInt(4, quantity);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int bookId = rs.getInt(1);
                JOptionPane.showMessageDialog(null, "Book added successfully.\nBook ID = " + bookId);
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    public boolean isBookAvailable(int bookId) {
        String sql = "SELECT Available FROM books WHERE BookID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("Available") > 0;
            }
            return false;

        } catch (Exception e) {
            showError(e);
            return false;
        }
    }

    public boolean bookExists(int bookId) {
        String sql = "SELECT COUNT(*) FROM books WHERE BookID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
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

    public void updateBookAvailability(int bookId, int change) {
        String sql = "UPDATE books SET Available = Available + ? WHERE BookID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, change);
            ps.setInt(2, bookId);
            ps.executeUpdate();

        } catch (Exception e) {
            showError(e);
        }
    }

    public List<Object[]> getOverdueBooks() {
        List<Object[]> overdueBooks = new ArrayList<>();
        int finePerDay = 5; // Fine rate per overdue day

        String sql = """
            SELECT t.TransactionID, b.Title, m.Name, t.IssueDate, t.DueDate,
                   DATEDIFF(CURDATE(), t.DueDate) as DaysOverdue,t.Fine
            FROM transactions t
            JOIN books b ON t.BookID = b.BookID
            JOIN member m ON t.MemberID = m.MemberID
            WHERE t.Status = 'ISSUED' AND t.DueDate < CURDATE()
            ORDER BY t.DueDate ASC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int daysOverdue = rs.getInt("DaysOverdue");
                int fine = daysOverdue * finePerDay;

                Object[] row = {
                    rs.getInt("TransactionID"),
                    rs.getString("Title"),
                    rs.getString("Name"),
                    rs.getDate("IssueDate"),
                    rs.getDate("DueDate"),
                    daysOverdue + " days",
                    "₹" + fine
                };
                overdueBooks.add(row);
            }

        } catch (Exception e) {
            showError(e);
        }

        return overdueBooks;
    }
    
    public List<Object[]> getFineTable() {
        List<Object[]> fineList = new ArrayList<>();
        int finePerDay = 5;

        String sql = """
            SELECT t.TransactionID, m.Name AS MemberName, b.Title AS BookTitle,
                   DATEDIFF(CURDATE(), t.DueDate) AS DaysOverdue
            FROM transactions t
            JOIN books b ON t.BookID = b.BookID
            JOIN member m ON t.MemberID = m.MemberID
            WHERE t.Status = 'ISSUED' AND t.DueDate < CURDATE()
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int days = rs.getInt("DaysOverdue");
                int fine = days * finePerDay;
                
                Object[] row = {
                    rs.getInt("TransactionID"),
                    rs.getString("MemberName"),
                    rs.getString("BookTitle"),
                    days,
                    "₹" + fine
                };
                fineList.add(row);
            }

        } catch (Exception e) {
            showError(e);
        }

        return fineList;
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
                row[i - 1] = rs.getObject(i);
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
