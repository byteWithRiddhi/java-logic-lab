package com.huffman.ui;

import com.huffman.core.HuffmanNode;
import com.huffman.core.HuffmanTree;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Displays the sortable and searchable Frequency and Generated Huffman Code table.
 * Interacts with the HuffmanTreePanel to highlight tree paths on row selection.
 */
public class CodeTablePanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField;
    private final JLabel lblStatsSummary;
    private Consumer<Integer> onSymbolSelectedListener;

    private static final String[] COLUMN_NAMES = {
            "Symbol", "Byte (Dec / Hex)", "Frequency", "Share (%)", "Huffman Code", "Bits"
    };

    public CodeTablePanel() {
        setBackground(Theme.BG_PANEL);
        setLayout(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        // Top Filter Bar
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchPanel.setOpaque(false);
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(Theme.FONT_BOLD);
        lblSearch.setForeground(Theme.TEXT_SECONDARY);

        searchField = new JTextField(15);
        searchField.setBackground(Theme.BG_INPUT);
        searchField.setForeground(Theme.TEXT_PRIMARY);
        searchField.setCaretColor(Theme.CYAN_ACCENT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_DEFAULT, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        searchPanel.add(lblSearch);
        searchPanel.add(searchField);

        lblStatsSummary = new JLabel("0 symbols loaded");
        lblStatsSummary.setFont(Theme.FONT_SMALL);
        lblStatsSummary.setForeground(Theme.TEXT_MUTED);

        topBar.add(searchPanel, BorderLayout.WEST);
        topBar.add(lblStatsSummary, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Table Model
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 2 -> Long.class; // Frequency numeric sorting
                    case 3 -> Double.class; // Share % numeric sorting
                    case 5 -> Integer.class; // Code length numeric sorting
                    default -> String.class;
                };
            }
        };

        table = new JTable(tableModel);
        table.setBackground(Theme.BG_CARD);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setGridColor(Theme.BORDER_SUBTLE);
        table.setRowHeight(24);
        table.setFont(Theme.FONT_REGULAR);
        table.setSelectionBackground(Theme.TEAL_DARK);
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);

        // Table Header styling
        table.getTableHeader().setBackground(new Color(0x18, 0x24, 0x38));
        table.getTableHeader().setForeground(Theme.CYAN_ACCENT);
        table.getTableHeader().setFont(Theme.FONT_BOLD);
        table.getTableHeader().setReorderingAllowed(false);

        // Custom Cell Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(new SymbolRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(new PercentageRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new CodeRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Sorter
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Selection Listener for Tree Highlighting
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && onSymbolSelectedListener != null) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    Object symObj = tableModel.getValueAt(modelRow, 0);
                    if (symObj instanceof SymbolEntry entry) {
                        onSymbolSelectedListener.accept(entry.symbol);
                    }
                } else {
                    onSymbolSelectedListener.accept(-1);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Theme.BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_DEFAULT, 1));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setOnSymbolSelectedListener(Consumer<Integer> listener) {
        this.onSymbolSelectedListener = listener;
    }

    public void setTree(HuffmanTree tree) {
        tableModel.setRowCount(0);
        if (tree == null || tree.getDistinctSymbols() == 0) {
            lblStatsSummary.setText("0 symbols loaded");
            return;
        }

        long totalFreq = tree.getTotalFrequency();
        long[] freqs = tree.getFrequencies();
        Map<Integer, String> codes = tree.getSymbolToCode();

        for (int i = 0; i < 256; i++) {
            if (freqs[i] > 0) {
                String code = codes.getOrDefault(i, "");
                double share = totalFreq > 0 ? ((double) freqs[i] / totalFreq) * 100.0 : 0.0;
                String hexDec = String.format("%d (0x%02X)", i, i);

                tableModel.addRow(new Object[] {
                        new SymbolEntry(i, HuffmanNode.formatByteSymbol(i)),
                        hexDec,
                        freqs[i],
                        share,
                        code,
                        code.length()
                });
            }
        }

        lblStatsSummary.setText(String.format("%d distinct symbols (%s total bytes)",
                tree.getDistinctSymbols(),
                java.text.NumberFormat.getIntegerInstance().format(totalFreq)));

        // Default sort by frequency descending
        sorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(2, SortOrder.DESCENDING)));
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
    }

    public record SymbolEntry(int symbol, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private static class SymbolRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(Theme.FONT_BOLD);
            setForeground(isSelected ? Color.WHITE : Theme.CYAN_ACCENT);
            setHorizontalAlignment(SwingConstants.CENTER);
            return this;
        }
    }

    private static class PercentageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Double d) {
                setText(String.format(java.util.Locale.US, "%.2f %%", d));
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(isSelected ? Color.WHITE : Theme.TEXT_SECONDARY);
            return this;
        }
    }

    private static class CodeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(Theme.FONT_MONO_BOLD);
            setForeground(isSelected ? Color.WHITE : Theme.EMERALD_SUCCESS);
            setHorizontalAlignment(SwingConstants.CENTER);
            return this;
        }
    }
}
