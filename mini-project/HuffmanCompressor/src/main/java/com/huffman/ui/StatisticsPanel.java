package com.huffman.ui;

import com.huffman.io.CompressionStats;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Metric dashboard displaying compression statistics, space saved,
 * compression ratio, Shannon entropy, and file overhead indicators.
 */
public class StatisticsPanel extends JPanel {
    private final MetricCard cardOriginalSize;
    private final MetricCard cardCompressedSize;
    private final MetricCard cardSpaceSaved;
    private final MetricCard cardRatio;
    private final MetricCard cardPercentage;
    private final MetricCard cardDistinct;
    private final MetricCard cardEntropy;
    private final MetricCard cardDuration;

    private final JPanel overheadBanner;
    private final JLabel lblOverheadMessage;

    public StatisticsPanel() {
        setBackground(Theme.BG_PANEL);
        setLayout(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        // Overhead Warning Banner (hidden by default)
        overheadBanner = new JPanel(new BorderLayout(8, 0));
        overheadBanner.setBackground(Theme.WARNING_BG);
        overheadBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.WARNING_AMBER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        overheadBanner.setVisible(false);

        JLabel lblWarnIcon = new JLabel("⚠");
        lblWarnIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblWarnIcon.setForeground(Theme.WARNING_AMBER);

        lblOverheadMessage = new JLabel("Note: Header metadata overhead exceeded payload reduction for this small file.");
        lblOverheadMessage.setFont(Theme.FONT_SMALL);
        lblOverheadMessage.setForeground(Color.WHITE);

        overheadBanner.add(lblWarnIcon, BorderLayout.WEST);
        overheadBanner.add(lblOverheadMessage, BorderLayout.CENTER);
        add(overheadBanner, BorderLayout.NORTH);

        // Grid of metric cards
        JPanel cardsGrid = new JPanel(new GridLayout(4, 2, 8, 8));
        cardsGrid.setOpaque(false);

        cardOriginalSize = new MetricCard("ORIGINAL SIZE", "--", Theme.TEXT_PRIMARY);
        cardCompressedSize = new MetricCard("COMPRESSED SIZE", "--", Theme.CYAN_ACCENT);
        cardSpaceSaved = new MetricCard("SPACE SAVED", "--", Theme.SUCCESS_EMERALD);
        cardRatio = new MetricCard("COMPRESSION RATIO", "--", Theme.TEAL_PRIMARY);
        cardPercentage = new MetricCard("SPACE SAVED %", "--", Theme.SUCCESS_EMERALD);
        cardDistinct = new MetricCard("DISTINCT SYMBOLS", "--", Theme.TEXT_PRIMARY);
        cardEntropy = new MetricCard("SHANNON ENTROPY", "--", Theme.INDIGO_ACCENT);
        cardDuration = new MetricCard("PROCESSING TIME", "--", Theme.TEXT_SECONDARY);

        cardsGrid.add(cardOriginalSize);
        cardsGrid.add(cardCompressedSize);
        cardsGrid.add(cardSpaceSaved);
        cardsGrid.add(cardRatio);
        cardsGrid.add(cardPercentage);
        cardsGrid.add(cardDistinct);
        cardsGrid.add(cardEntropy);
        cardsGrid.add(cardDuration);

        add(cardsGrid, BorderLayout.CENTER);
    }

    public void updateStats(CompressionStats stats) {
        if (stats == null) {
            resetStats();
            return;
        }

        cardOriginalSize.setValue(CompressionStats.formatCompactBytes(stats.getOriginalSize()),
                stats.getOriginalSize() + " bytes");
        cardCompressedSize.setValue(CompressionStats.formatCompactBytes(stats.getCompressedSize()),
                stats.getCompressedSize() + " bytes");

        long saved = stats.getSpaceSavedBytes();
        if (saved >= 0) {
            cardSpaceSaved.setValue(CompressionStats.formatCompactBytes(saved),
                    "+" + saved + " bytes");
            cardSpaceSaved.setValueColor(Theme.SUCCESS_EMERALD);
        } else {
            cardSpaceSaved.setValue(CompressionStats.formatCompactBytes(saved),
                    saved + " bytes (overhead)");
            cardSpaceSaved.setValueColor(Theme.WARNING_AMBER);
        }

        if (stats.getOriginalSize() == 0) {
            cardRatio.setValue("N/A", "Empty file");
            cardPercentage.setValue("0.00 %", "No data");
        } else {
            cardRatio.setValue(String.format(java.util.Locale.US, "%.2fx", stats.getCompressionRatio()),
                    stats.getCompressionRatio() >= 1.0 ? "Reduction" : "Expansion");
            cardPercentage.setValue(String.format(java.util.Locale.US, "%.2f %%", stats.getSpaceSavedPercentage()),
                    stats.isExpandedDueToOverhead() ? "Expanded" : "Saved");
            cardPercentage.setValueColor(stats.isExpandedDueToOverhead() ? Theme.WARNING_AMBER : Theme.SUCCESS_EMERALD);
        }

        cardDistinct.setValue(stats.getDistinctSymbols() + " / 256",
                String.format(java.util.Locale.US, "%.1f%% alphabet", (stats.getDistinctSymbols() / 256.0) * 100));

        cardEntropy.setValue(String.format(java.util.Locale.US, "%.2f bits", stats.getShannonEntropy()),
                String.format(java.util.Locale.US, "Avg Code: %.2f bits", stats.getAverageCodeLength()));

        cardDuration.setValue(stats.getDurationMs() + " ms",
                stats.isCompression() ? "Compression" : "Decompression");

        // Overhead warning banner
        if (stats.isExpandedDueToOverhead()) {
            overheadBanner.setVisible(true);
            lblOverheadMessage.setText(String.format(
                    "Note: Header metadata overhead exceeded payload reduction (file expanded by %s).",
                    CompressionStats.formatBytes(-saved)));
        } else {
            overheadBanner.setVisible(false);
        }

        revalidate();
        repaint();
    }

    public void resetStats() {
        cardOriginalSize.setValue("--", "");
        cardCompressedSize.setValue("--", "");
        cardSpaceSaved.setValue("--", "");
        cardRatio.setValue("--", "");
        cardPercentage.setValue("--", "");
        cardDistinct.setValue("--", "");
        cardEntropy.setValue("--", "");
        cardDuration.setValue("--", "");
        overheadBanner.setVisible(false);
        revalidate();
        repaint();
    }

    private static class MetricCard extends JPanel {
        private final JLabel lblTitle;
        private final JLabel lblValue;
        private final JLabel lblSubtext;

        public MetricCard(String title, String initialValue, Color valueColor) {
            setOpaque(false);
            setLayout(new BorderLayout(0, 2));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER_DEFAULT, 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));

            lblTitle = new JLabel(title);
            lblTitle.setFont(Theme.FONT_SMALL);
            lblTitle.setForeground(Theme.TEXT_MUTED);

            lblValue = new JLabel(initialValue);
            lblValue.setFont(Theme.FONT_HEADER);
            lblValue.setForeground(valueColor);

            lblSubtext = new JLabel(" ");
            lblSubtext.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblSubtext.setForeground(Theme.TEXT_SECONDARY);

            add(lblTitle, BorderLayout.NORTH);
            add(lblValue, BorderLayout.CENTER);
            add(lblSubtext, BorderLayout.SOUTH);
        }

        public void setValue(String value, String subtext) {
            lblValue.setText(value);
            lblSubtext.setText(subtext != null && !subtext.isEmpty() ? subtext : " ");
        }

        public void setValueColor(Color color) {
            lblValue.setForeground(color);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.BG_CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
