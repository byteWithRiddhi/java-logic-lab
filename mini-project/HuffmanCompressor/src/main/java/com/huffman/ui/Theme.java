package com.huffman.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Visual styling theme, palette constants, and component styling helpers
 * for the dark modern Huffman Studio interface.
 */
public final class Theme {
    private Theme() {}

    // Backgrounds
    public static final Color BG_DARK = new Color(0x0A, 0x0F, 0x1D);
    public static final Color BG_PANEL = new Color(0x0F, 0x17, 0x2A);
    public static final Color BG_CARD = new Color(0x1E, 0x29, 0x3B);
    public static final Color BG_CARD_HOVER = new Color(0x28, 0x35, 0x4B);
    public static final Color BG_INPUT = new Color(0x14, 0x1E, 0x2E);

    // Borders
    public static final Color BORDER_DEFAULT = new Color(0x33, 0x41, 0x55);
    public static final Color BORDER_FOCUS = new Color(0x06, 0xB6, 0xD4);
    public static final Color BORDER_SUBTLE = new Color(0x1E, 0x29, 0x3B);

    // Typography Colors
    public static final Color TEXT_PRIMARY = new Color(0xF8, 0xFA, 0xFC);
    public static final Color TEXT_SECONDARY = new Color(0x94, 0xA3, 0xB8);
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);

    // Brand & Accents
    public static final Color TEAL_PRIMARY = new Color(0x06, 0xB6, 0xD4);
    public static final Color TEAL_DARK = new Color(0x08, 0x91, 0xB2);
    public static final Color CYAN_ACCENT = new Color(0x38, 0xBD, 0xF8);
    public static final Color INDIGO_ACCENT = new Color(0x63, 0x66, 0xF1);

    // Status Colors
    public static final Color SUCCESS_EMERALD = new Color(0x10, 0xB9, 0x81);
    public static final Color EMERALD_SUCCESS = SUCCESS_EMERALD;
    public static final Color SUCCESS_BG = new Color(0x06, 0x4E, 0x3B);
    public static final Color WARNING_AMBER = new Color(0xF5, 0x9E, 0x0B);
    public static final Color WARNING_BG = new Color(0x78, 0x35, 0x0F);
    public static final Color ERROR_ROSE = new Color(0xEF, 0x44, 0x44);
    public static final Color ERROR_BG = new Color(0x7F, 0x1D, 0x1D);

    // Tree Visualizer Colors
    public static final Color TREE_BG = new Color(0x0B, 0x11, 0x20);
    public static final Color TREE_GRID = new Color(0x13, 0x1C, 0x30);
    public static final Color NODE_ROOT = new Color(0x02, 0x84, 0xC7);
    public static final Color NODE_INTERNAL = new Color(0x33, 0x41, 0x55);
    public static final Color NODE_LEAF = new Color(0x05, 0x96, 0x69);
    public static final Color NODE_HIGHLIGHT = new Color(0xF5, 0x9E, 0x0B);
    public static final Color EDGE_NORMAL = new Color(0x47, 0x55, 0x69);
    public static final Color EDGE_HIGHLIGHT = new Color(0xFB, 0xBF, 0x24);
    public static final Color BIT_0_COLOR = new Color(0x06, 0xB6, 0xD4);
    public static final Color BIT_1_COLOR = new Color(0xA8, 0x55, 0xF7);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_MONO_BOLD = new Font("Consolas", Font.BOLD, 12);

    /**
     * Creates a styled card JPanel with dark background, rounded border, and padding.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(BORDER_DEFAULT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        return panel;
    }

    /**
     * Creates a modern styled JButton with hover transition and custom colors.
     */
    public static JButton createStyledButton(String text, Color bg, Color hoverBg, Color textColor) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovered = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(new Color(0x25, 0x30, 0x42));
                } else if (hovered) {
                    g2.setColor(hoverBg);
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(textColor);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, TEAL_PRIMARY, TEAL_DARK, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, new Color(0x33, 0x41, 0x55), new Color(0x47, 0x55, 0x69), TEXT_PRIMARY);
    }

    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS_EMERALD, new Color(0x05, 0x96, 0x69), Color.WHITE);
    }

    public static JButton createAccentButton(String text) {
        return createStyledButton(text, INDIGO_ACCENT, new Color(0x4F, 0x46, 0xE5), Color.WHITE);
    }
}
