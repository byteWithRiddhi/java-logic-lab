package com.huffman.ui;

import com.huffman.core.HuffmanTree;
import com.huffman.io.CompressionStats;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Main application window for Huffman Studio.
 * Orchestrates file operations, tree visualization, algorithm explorer,
 * code/frequency tables, and statistics dashboard.
 */
public class MainFrame extends JFrame {
    private final FileOperationsPanel fileOpsPanel;
    private final HuffmanTreePanel treePanel;
    private final AlgorithmExplorerPanel explorerPanel;
    private final CodeTablePanel codeTablePanel;
    private final StatisticsPanel statsPanel;

    private JLabel lblStatusBadge;
    private final JTabbedPane centerTabbedPane;

    public MainFrame() {
        super("Huffman Studio — Lossless File Compression & Algorithm Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1340, 840);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        // 1. Top Header Bar
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // 2. Instantiate Main Subpanels
        fileOpsPanel = new FileOperationsPanel();
        treePanel = new HuffmanTreePanel();
        explorerPanel = new AlgorithmExplorerPanel();
        codeTablePanel = new CodeTablePanel();
        statsPanel = new StatisticsPanel();

        // 3. Center Tabs: Tree View vs Algorithm Explorer
        centerTabbedPane = new JTabbedPane();
        centerTabbedPane.setBackground(Theme.BG_CARD);
        centerTabbedPane.setForeground(Theme.TEXT_PRIMARY);
        centerTabbedPane.setFont(Theme.FONT_BOLD);
        centerTabbedPane.addTab("🌳 Huffman Binary Tree", treePanel);
        centerTabbedPane.addTab("🔍 DSA Algorithm Explorer (Step-by-Step)", explorerPanel);

        // Center Vertical Split: Tabs (Top) + Code & Frequency Table (Bottom)
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplit.setOpaque(false);
        centerSplit.setBorder(null);
        centerSplit.setDividerSize(6);
        centerSplit.setTopComponent(centerTabbedPane);

        // Titled bottom container for Code Table
        JPanel tableContainer = new JPanel(new BorderLayout(0, 4));
        tableContainer.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Frequency & Generated Prefix Code Table (Click row to highlight in tree)");
        lblTableTitle.setFont(Theme.FONT_HEADER);
        lblTableTitle.setForeground(Theme.CYAN_ACCENT);
        lblTableTitle.setBorder(new EmptyBorder(4, 8, 2, 8));
        tableContainer.add(lblTableTitle, BorderLayout.NORTH);
        tableContainer.add(codeTablePanel, BorderLayout.CENTER);

        centerSplit.setBottomComponent(tableContainer);
        centerSplit.setResizeWeight(0.65);

        // Right Container: Statistics & Learning Overview
        JPanel rightContainer = new JPanel(new BorderLayout(0, 4));
        rightContainer.setOpaque(false);
        rightContainer.setPreferredSize(new Dimension(300, 600));
        JLabel lblStatsTitle = new JLabel("Compression Performance");
        lblStatsTitle.setFont(Theme.FONT_HEADER);
        lblStatsTitle.setForeground(Theme.CYAN_ACCENT);
        lblStatsTitle.setBorder(new EmptyBorder(8, 8, 4, 8));
        rightContainer.add(lblStatsTitle, BorderLayout.NORTH);
        rightContainer.add(statsPanel, BorderLayout.CENTER);

        // Horizontal Split: (Center + Right)
        JSplitPane centerAndRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerAndRightSplit.setOpaque(false);
        centerAndRightSplit.setBorder(null);
        centerAndRightSplit.setDividerSize(6);
        centerAndRightSplit.setLeftComponent(centerSplit);
        centerAndRightSplit.setRightComponent(rightContainer);
        centerAndRightSplit.setResizeWeight(0.75);

        // Main Horizontal Split: Left File Panel + (Center + Right)
        JPanel leftContainer = new JPanel(new BorderLayout(0, 4));
        leftContainer.setOpaque(false);
        leftContainer.setPreferredSize(new Dimension(330, 600));
        leftContainer.add(fileOpsPanel, BorderLayout.CENTER);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setOpaque(false);
        mainSplit.setBorder(null);
        mainSplit.setDividerSize(6);
        mainSplit.setLeftComponent(leftContainer);
        mainSplit.setRightComponent(centerAndRightSplit);
        mainSplit.setResizeWeight(0.25);

        add(mainSplit, BorderLayout.CENTER);

        // 4. Wire Interactions
        setupInteractions();

        // 5. Setup Menu Bar
        setJMenuBar(createAppMenuBar());
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(Theme.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DEFAULT),
                new EmptyBorder(12, 20, 12, 20)
        ));

        // Left Branding
        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Huffman Studio");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Lossless File Compression & Data Structures Visualizer");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_MUTED);

        brandPanel.add(title);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(subtitle);

        // Right Status Badge and Demo Button
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightHeader.setOpaque(false);

        JButton btnQuickDemo = Theme.createAccentButton("⚡ Load Sample Text");
        btnQuickDemo.setFont(Theme.FONT_SMALL);
        btnQuickDemo.addActionListener(e -> loadSampleText("quick_sample.txt", getDemoText()));

        lblStatusBadge = new JLabel("● Ready");
        lblStatusBadge.setFont(Theme.FONT_BOLD);
        lblStatusBadge.setForeground(Theme.SUCCESS_EMERALD);
        lblStatusBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_DEFAULT, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        rightHeader.add(btnQuickDemo);
        rightHeader.add(lblStatusBadge);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(rightHeader, BorderLayout.EAST);

        return header;
    }

    private JMenuBar createAppMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(Theme.BG_PANEL);
        mb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DEFAULT));

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(Theme.TEXT_PRIMARY);
        JMenuItem miExit = new JMenuItem("Exit");
        miExit.addActionListener(e -> System.exit(0));
        fileMenu.add(miExit);

        // Samples Menu
        JMenu samplesMenu = new JMenu("Samples & Demos");
        samplesMenu.setForeground(Theme.TEXT_PRIMARY);

        JMenuItem sm1 = new JMenuItem("1. Standard English Text (Alice snippet)");
        sm1.addActionListener(e -> loadSampleText("alice_snippet.txt", getDemoText()));

        JMenuItem sm2 = new JMenuItem("2. Single Repeated Byte ('AAAA...')");
        sm2.addActionListener(e -> loadSampleText("repeated_a.txt", "A".repeat(5000)));

        JMenuItem sm3 = new JMenuItem("3. Multilingual UTF-8 Text (Hindi, Japanese, French, Emoji)");
        sm3.addActionListener(e -> loadSampleText("multilingual.txt", getMultilingualDemoText()));

        JMenuItem sm4 = new JMenuItem("4. All 256 Byte Values (Binary)");
        sm4.addActionListener(e -> loadBinaryAllBytesSample());

        samplesMenu.add(sm1);
        samplesMenu.add(sm2);
        samplesMenu.add(sm3);
        samplesMenu.add(sm4);

        // Help Menu
        JMenu helpMenu = new JMenu("Help & Theory");
        helpMenu.setForeground(Theme.TEXT_PRIMARY);

        JMenuItem miViva = new JMenuItem("DSA Viva & Algorithm Concepts");
        miViva.addActionListener(e -> showVivaDialog());

        JMenuItem miAbout = new JMenuItem("About Huffman Studio");
        miAbout.addActionListener(e -> showAboutDialog());

        helpMenu.add(miViva);
        helpMenu.add(miAbout);

        mb.add(fileMenu);
        mb.add(samplesMenu);
        mb.add(helpMenu);

        return mb;
    }

    private void setupInteractions() {
        // When tree is updated from file operations
        fileOpsPanel.setOnTreeUpdatedListener(tree -> {
            treePanel.setTree(tree);
            explorerPanel.setTree(tree);
            codeTablePanel.setTree(tree);
        });

        // When stats are updated
        fileOpsPanel.setOnStatsUpdatedListener(statsPanel::updateStats);

        // When status message changes
        fileOpsPanel.setOnStatusChangedListener(status -> {
            lblStatusBadge.setText("● " + status);
            if (status.toLowerCase().contains("error") || status.toLowerCase().contains("fail")) {
                lblStatusBadge.setForeground(Theme.ERROR_ROSE);
            } else if (status.toLowerCase().contains("compressing") || status.toLowerCase().contains("decoding") || status.toLowerCase().contains("analyzing")) {
                lblStatusBadge.setForeground(Theme.CYAN_ACCENT);
            } else {
                lblStatusBadge.setForeground(Theme.SUCCESS_EMERALD);
            }
        });

        // When row is selected in Code Table -> highlight path in tree
        codeTablePanel.setOnSymbolSelectedListener(symbol -> {
            if (symbol >= 0) {
                treePanel.highlightSymbol(symbol);
                // Switch to Tree Tab if currently elsewhere
                centerTabbedPane.setSelectedIndex(0);
            } else {
                treePanel.clearHighlight();
            }
        });
    }

    private void loadSampleText(String filename, String content) {
        try {
            File temp = File.createTempFile("huffman_", "_" + filename);
            temp.deleteOnExit();
            try (FileWriter fw = new FileWriter(temp)) {
                fw.write(content);
            }
            fileOpsPanel.setInputFile(temp);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to create sample file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBinaryAllBytesSample() {
        try {
            File temp = File.createTempFile("huffman_allbytes_", ".bin");
            temp.deleteOnExit();
            byte[] allBytes = new byte[256 * 10];
            for (int i = 0; i < allBytes.length; i++) {
                allBytes[i] = (byte) (i % 256);
            }
            java.nio.file.Files.write(temp.toPath(), allBytes);
            fileOpsPanel.setInputFile(temp);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to create sample file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showVivaDialog() {
        String msg = """
                HUFFMAN CODING — CORE CONCEPTS & INTERVIEW GUIDE:

                1. Greedy Choice Property:
                   At each step, we merge the two lowest-frequency trees.
                   This guarantees that symbols with higher frequencies have shorter codes
                   and symbols with lower frequencies have longer codes.

                2. Prefix Property:
                   No valid code word is a prefix of any other valid code word.
                   This makes the bit stream uniquely and instantaneously decodable
                   without requiring delimiter symbols.

                3. Time & Space Complexity:
                   - Frequency Counting: O(N) where N is file size.
                   - Priority Queue Tree Construction: O(K log K) where K <= 256 distinct symbols.
                   - Total Compression Time: O(N + K log K) -> linear in file size!
                   - Space Complexity: O(K) memory for the tree and frequency tables.

                4. Shannon Entropy:
                   H(X) = - Σ P(x) * log2(P(x))
                   Represents the theoretical minimum expected bits per symbol.
                   Huffman Coding approaches H(X) within 1 bit per symbol:
                   H(X) <= Average Code Length < H(X) + 1.

                5. Small File Overhead:
                   The custom .huff file header contains magic bytes, CRC-32 checksum,
                   and the symbol frequency table. For files smaller than ~300 bytes,
                   header metadata may exceed payload savings, resulting in file expansion.
                """;
        JTextArea ta = new JTextArea(msg);
        ta.setFont(Theme.FONT_MONO);
        ta.setEditable(false);
        ta.setBackground(Theme.BG_CARD);
        ta.setForeground(Theme.TEXT_PRIMARY);
        ta.setRows(22);
        ta.setColumns(60);
        JScrollPane sp = new JScrollPane(ta);
        JOptionPane.showMessageDialog(this, sp, "Huffman Coding DSA Interview & Viva Guide", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        String about = """
                Huffman Studio v1.0.0
                Lossless File Compression & Binary Tree Visualizer
                
                Developed in Java 21 with Swing.
                Features:
                - From-scratch Huffman Coding & MSB BitWriter/BitReader
                - Custom robust binary .huff file format with CRC-32 verification
                - Interactive Pan & Zoom Tree Visualization
                - Step-by-Step Priority Queue Algorithm Explorer
                - Sortable Symbol & Code Table with Path Highlighting
                - Real-time KPI Statistics Dashboard
                """;
        JOptionPane.showMessageDialog(this, about, "About Huffman Studio", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getDemoText() {
        return """
                Alice was beginning to get very tired of sitting by her sister on the bank,
                and of having nothing to do: once or twice she had peeped into the book
                her sister was reading, but it had no pictures or conversations in it,
                "and what is the use of a book," thought Alice "without pictures or conversations?"

                So she was considering in her own mind (as well as she could, for the hot day made
                her feel very sleepy and stupid), whether the pleasure of making a daisy-chain would
                be worth the trouble of getting up and picking the daisies, when suddenly a White
                Rabbit with pink eyes ran close by her.
                """;
    }

    private String getMultilingualDemoText() {
        return """
                Multilingual UTF-8 Test Corpus:
                - English: Lossless data compression using Huffman coding algorithm.
                - Hindi: हफमैन कोडिंग एक कुशल और सटीक डेटा संपीड़न तकनीक है।
                - Japanese: ハフマン符号は可変長符号化を用いた可逆データ圧縮アルゴリズムです。
                - French: Compression de données sans perte avec visualisation d'arbre binaire.
                - Arabic: خوارزمية هوفمان لضغط البيانات غير المفقودة.
                - German: Verlustfreie Datenkompression und binäre Baumvisualisierung.
                - Emojis: 🌲⚡📦🚀💡✨🎉💻
                """;
    }
}
