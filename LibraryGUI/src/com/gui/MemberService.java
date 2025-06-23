package com.gui;

import java.sql.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MemberService {

    public void displayMembers() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT * FROM member");
            JTable table = new JTable(buildTableModel(rs));
            JOptionPane.showMessageDialog(null, new JScrollPane(table), "Members List", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            showError(e);
        }
    }

    public void addMember(String name) {
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Member name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get email and phone from user
        String email = JOptionPane.showInputDialog(null, "Enter Email Address:", "Member Details", JOptionPane.PLAIN_MESSAGE);
        if (email == null || email.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Email address is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String phone = JOptionPane.showInputDialog(null, "Enter Phone Number (10 digits):", "Member Details", JOptionPane.PLAIN_MESSAGE);
        if (phone == null || phone.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Phone number is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate phone number
        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(null, "Phone number must be exactly 10 digits.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(null, "Invalid email format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if member already exists
        if (memberEmailExists(email.trim())) {
            JOptionPane.showMessageDialog(null, "Member with this email already exists.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO member (Name, Email, Phone, Reg_Date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name.trim());
            ps.setString(2, email.trim());
            ps.setString(3, phone.trim());
            ps.setDate(4, Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int memberId = rs.getInt(1);
                JOptionPane.showMessageDialog(null, "Member added successfully.\nMember ID = " + memberId);
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    public boolean memberExists(int memberId) {
        String sql = "SELECT COUNT(*) FROM member WHERE MemberID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, memberId);
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

    private boolean memberEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM member WHERE LOWER(Email) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
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