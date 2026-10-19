package com.huffman;

import com.formdev.flatlaf.FlatDarkLaf;
import com.huffman.ui.MainFrame;
import com.huffman.ui.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Main application entry point for Huffman Studio.
 */
public class Main {
    public static void main(String[] args) {
        // High-DPI and font antialiasing properties
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Initialize Look and Feel
        try {
            FlatDarkLaf.setup();
            // Customize default control colors to match our theme
            UIManager.put("Component.accentColor", Theme.TEAL_PRIMARY);
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TabbedPane.selectedBackground", Theme.BG_CARD);
            UIManager.put("TabbedPane.selectedForeground", Theme.CYAN_ACCENT);
            UIManager.put("ScrollBar.thumbArc", 8);
        } catch (Exception ex) {
            System.err.println("Note: FlatLaf initialization failed (" + ex.getMessage() + "), falling back to system Look & Feel.");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
