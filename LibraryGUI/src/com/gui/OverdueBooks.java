package com.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class OverdueBooks extends JPanel {

    public OverdueBooks() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel overdueTitle = new JLabel("Overdue Books");
        overdueTitle.setAlignmentX(CENTER_ALIGNMENT);
        overdueTitle.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        add(overdueTitle);

        // ========== OVERDUE TABLE ==========
        String[] overdueColumns = {"Transaction ID", "Book Title", "Member Name", "Issue Date", "Due Date", "Days Overdue", "Fine"};
        DefaultTableModel overdueModel = new DefaultTableModel(overdueColumns, 0);
        JTable overdueTable = new JTable(overdueModel);
        JScrollPane overdueScrollPane = new JScrollPane(overdueTable);
        add(overdueScrollPane);

        BookService bookService = new BookService();
        List<Object[]> overdueRows = bookService.getOverdueBooks();
        if (overdueRows.isEmpty()) {
            overdueModel.addRow(new Object[]{"No overdue books found", "", "", "", "", "", ""});
        } else {
            for (Object[] row : overdueRows) {
                overdueModel.addRow(row);
            }
        }

        // ========== FINE TABLE ==========
        add(Box.createVerticalStrut(20));
        JLabel fineTitle = new JLabel("Fine Table");
        fineTitle.setAlignmentX(CENTER_ALIGNMENT);
        fineTitle.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        add(fineTitle);

        String[] fineColumns = {"Transaction ID", "Member Name", "Book Title", "Days Overdue", "Fine"};
        DefaultTableModel fineModel = new DefaultTableModel(fineColumns, 0);
        JTable fineTable = new JTable(fineModel);
        JScrollPane fineScrollPane = new JScrollPane(fineTable);
        add(fineScrollPane);

        List<Object[]> fineRows = bookService.getFineTable();
        if (fineRows.isEmpty()) {
            fineModel.addRow(new Object[]{"No fines", "", "", "", ""});
        } else {
            for (Object[] row : fineRows) {
                fineModel.addRow(row);
            }
        }

        // Refresh Button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setAlignmentX(CENTER_ALIGNMENT);
        refreshButton.addActionListener(e -> {
            // Refresh overdue table
            overdueModel.setRowCount(0);
            List<Object[]> updatedOverdues = bookService.getOverdueBooks();
            if (updatedOverdues.isEmpty()) {
                overdueModel.addRow(new Object[]{"No overdue books found", "", "", "", "", "", ""});
            } else {
                for (Object[] row : updatedOverdues) {
                    overdueModel.addRow(row);
                }
            }

            // Refresh fine table
            fineModel.setRowCount(0);
            List<Object[]> updatedFines = bookService.getFineTable();
            if (updatedFines.isEmpty()) {
                fineModel.addRow(new Object[]{"No fines", "", "", "", ""});
            } else {
                for (Object[] row : updatedFines) {
                    fineModel.addRow(row);
                }
            }
        });

        add(Box.createVerticalStrut(10));
        add(refreshButton);
    }
}

