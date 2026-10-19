package com.huffman.ui;

import com.huffman.core.HuffmanTree;
import com.huffman.io.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * Left panel handling file selection, drag-and-drop, destination configuration,
 * background execution of analysis, compression, and decompression with progress feedback.
 */
public class FileOperationsPanel extends JPanel {
    private File selectedInputFile;
    private File selectedOutputFile;

    // UI Components
    private JLabel lblFileName;
    private JLabel lblFileSize;
    private JLabel lblFilePath;
    private JLabel lblOutputDestination;

    private JButton btnSelectFile;
    private JButton btnChooseOutput;
    private JButton btnAnalyze;
    private JButton btnCompress;
    private JButton btnDecompress;
    private JButton btnOpenOutputFolder;

    private JProgressBar progressBar;
    private JLabel lblStatusMessage;

    // Callbacks to MainFrame
    private Consumer<HuffmanTree> onTreeUpdatedListener;
    private Consumer<CompressionStats> onStatsUpdatedListener;
    private Consumer<String> onStatusChangedListener;

    public FileOperationsPanel() {
        setBackground(Theme.BG_PANEL);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // 1. File Selection Card (Supports Drag & Drop)
        add(createFileSelectionCard());
        add(Box.createVerticalStrut(12));

        // 2. Output Configuration Card
        add(createOutputConfigCard());
        add(Box.createVerticalStrut(12));

        // 3. Action Buttons Card
        add(createActionButtonsCard());
        add(Box.createVerticalStrut(12));

        // 4. Progress and Status Feedback Card
        add(createProgressCard());
        add(Box.createVerticalGlue());

        setupDragAndDrop();
    }

    private JPanel createFileSelectionCard() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel("Input Source File");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.CYAN_ACCENT);
        card.add(title, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        lblFileName = new JLabel("No file selected");
        lblFileName.setFont(Theme.FONT_BOLD);
        lblFileName.setForeground(Theme.TEXT_PRIMARY);

        lblFileSize = new JLabel("Size: --");
        lblFileSize.setFont(Theme.FONT_SMALL);
        lblFileSize.setForeground(Theme.TEXT_SECONDARY);

        lblFilePath = new JLabel("Drag and drop file here or browse below");
        lblFilePath.setFont(Theme.FONT_SMALL);
        lblFilePath.setForeground(Theme.TEXT_MUTED);

        infoPanel.add(lblFileName);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(lblFileSize);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(lblFilePath);

        card.add(infoPanel, BorderLayout.CENTER);

        btnSelectFile = Theme.createPrimaryButton("Browse File...");
        btnSelectFile.addActionListener(e -> browseInputFile());
        card.add(btnSelectFile, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createOutputConfigCard() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout(0, 6));

        JLabel title = new JLabel("Destination Output");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.CYAN_ACCENT);
        card.add(title, BorderLayout.NORTH);

        lblOutputDestination = new JLabel("Auto-generated based on input");
        lblOutputDestination.setFont(Theme.FONT_SMALL);
        lblOutputDestination.setForeground(Theme.TEXT_MUTED);
        card.add(lblOutputDestination, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnBar.setOpaque(false);

        btnChooseOutput = Theme.createSecondaryButton("Change...");
        btnChooseOutput.setFont(Theme.FONT_SMALL);
        btnChooseOutput.addActionListener(e -> browseOutputFile());

        btnOpenOutputFolder = Theme.createSecondaryButton("Open Folder");
        btnOpenOutputFolder.setFont(Theme.FONT_SMALL);
        btnOpenOutputFolder.setEnabled(false);
        btnOpenOutputFolder.addActionListener(e -> openOutputDirectory());

        btnBar.add(btnChooseOutput);
        btnBar.add(btnOpenOutputFolder);
        card.add(btnBar, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createActionButtonsCard() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new GridLayout(3, 1, 0, 8));

        btnAnalyze = Theme.createSecondaryButton("1. Analyze File (Frequencies & Tree)");
        btnAnalyze.setEnabled(false);
        btnAnalyze.addActionListener(e -> executeAnalysis());

        btnCompress = Theme.createPrimaryButton("2. Compress File (.huff)");
        btnCompress.setEnabled(false);
        btnCompress.addActionListener(e -> executeCompression());

        btnDecompress = Theme.createSuccessButton("3. Decompress Archive");
        btnDecompress.setEnabled(false);
        btnDecompress.addActionListener(e -> executeDecompression());

        card.add(btnAnalyze);
        card.add(btnCompress);
        card.add(btnDecompress);

        return card;
    }

    private JPanel createProgressCard() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout(0, 6));

        lblStatusMessage = new JLabel("Ready. Please select a file to begin.");
        lblStatusMessage.setFont(Theme.FONT_SMALL);
        lblStatusMessage.setForeground(Theme.TEXT_SECONDARY);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(Theme.TEAL_PRIMARY);
        progressBar.setBackground(Theme.BG_INPUT);
        progressBar.setBorder(BorderFactory.createLineBorder(Theme.BORDER_DEFAULT, 1));
        progressBar.setValue(0);

        card.add(lblStatusMessage, BorderLayout.NORTH);
        card.add(progressBar, BorderLayout.CENTER);

        return card;
    }

    private void setupDragAndDrop() {
        new DropTarget(this, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (droppedFiles != null && !droppedFiles.isEmpty()) {
                        setInputFile(droppedFiles.get(0));
                    }
                } catch (Exception ex) {
                    showError("Error dropping file: " + ex.getMessage());
                }
            }
        });
    }

    private void browseInputFile() {
        JFileChooser chooser = new JFileChooser();
        if (selectedInputFile != null && selectedInputFile.getParentFile() != null) {
            chooser.setCurrentDirectory(selectedInputFile.getParentFile());
        }
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            setInputFile(chooser.getSelectedFile());
        }
    }

    private void browseOutputFile() {
        if (selectedInputFile == null) return;
        JFileChooser chooser = new JFileChooser();
        if (selectedOutputFile != null) {
            chooser.setSelectedFile(selectedOutputFile);
        } else {
            chooser.setSelectedFile(selectedInputFile);
        }
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            selectedOutputFile = chooser.getSelectedFile();
            lblOutputDestination.setText(selectedOutputFile.getName());
            lblOutputDestination.setToolTipText(selectedOutputFile.getAbsolutePath());
        }
    }

    public void setInputFile(File file) {
        if (file == null || !file.exists()) return;
        this.selectedInputFile = file;

        lblFileName.setText(file.getName());
        lblFileSize.setText("Size: " + CompressionStats.formatBytes(file.length()));
        lblFilePath.setText(file.getAbsolutePath());
        lblFilePath.setToolTipText(file.getAbsolutePath());

        boolean isHuffArchive = file.getName().toLowerCase().endsWith(CompressedFileFormat.FILE_EXTENSION);

        // Auto determine default output file
        if (isHuffArchive) {
            String baseName = file.getName().substring(0, file.getName().length() - CompressedFileFormat.FILE_EXTENSION.length());
            selectedOutputFile = new File(file.getParentFile(), "restored_" + baseName);
            btnDecompress.setEnabled(true);
            btnCompress.setEnabled(true);
            btnAnalyze.setEnabled(true);
        } else {
            selectedOutputFile = new File(file.getParentFile(), file.getName() + CompressedFileFormat.FILE_EXTENSION);
            btnCompress.setEnabled(true);
            btnDecompress.setEnabled(false);
            btnAnalyze.setEnabled(true);
        }

        lblOutputDestination.setText(selectedOutputFile.getName());
        lblOutputDestination.setToolTipText(selectedOutputFile.getAbsolutePath());
        btnOpenOutputFolder.setEnabled(true);

        setStatus("Loaded " + file.getName(), false);
        progressBar.setValue(0);

        // Auto-run lightweight inspection or analysis
        if (isHuffArchive) {
            inspectArchiveQuickly(file);
        } else {
            executeAnalysis();
        }
    }

    private void inspectArchiveQuickly(File huffFile) {
        SwingWorker<FileDecompressor.HeaderInspectionResult, Void> worker = new SwingWorker<>() {
            @Override
            protected FileDecompressor.HeaderInspectionResult doInBackground() throws Exception {
                return FileDecompressor.inspect(huffFile);
            }

            @Override
            protected void done() {
                try {
                    var result = get();
                    if (onTreeUpdatedListener != null) {
                        onTreeUpdatedListener.accept(result.tree());
                    }
                    setStatus("Valid Huffman archive: ready to decompress (" +
                            CompressionStats.formatBytes(result.metadata().getOriginalSize()) + " original)", false);
                } catch (Exception ex) {
                    setStatus("Note: " + ex.getMessage(), true);
                }
            }
        };
        worker.execute();
    }

    private void executeAnalysis() {
        if (selectedInputFile == null) return;
        setButtonsBusy(true);
        setStatus("Analyzing byte frequencies...", false);
        progressBar.setValue(10);

        SwingWorker<HuffmanTree, Void> worker = new SwingWorker<>() {
            @Override
            protected HuffmanTree doInBackground() throws Exception {
                long[] freqs = new long[256];
                byte[] buf = new byte[65536];
                try (InputStream in = new FileInputStream(selectedInputFile)) {
                    int r;
                    while ((r = in.read(buf)) != -1) {
                        for (int i = 0; i < r; i++) {
                            freqs[buf[i] & 0xFF]++;
                        }
                    }
                }
                return new HuffmanTree(freqs);
            }

            @Override
            protected void done() {
                setButtonsBusy(false);
                try {
                    HuffmanTree tree = get();
                    progressBar.setValue(100);
                    if (onTreeUpdatedListener != null) {
                        onTreeUpdatedListener.accept(tree);
                    }
                    setStatus(String.format("Analysis complete: %d distinct symbols found.", tree.getDistinctSymbols()), false);
                } catch (Exception ex) {
                    showError("Analysis failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void executeCompression() {
        if (selectedInputFile == null || selectedOutputFile == null) return;
        setButtonsBusy(true);
        setStatus("Starting compression...", false);
        progressBar.setValue(0);

        SwingWorker<FileCompressor.CompressionResult, Integer> worker = new SwingWorker<>() {
            @Override
            protected FileCompressor.CompressionResult doInBackground() throws Exception {
                return FileCompressor.compress(selectedInputFile, selectedOutputFile, (progress, status) -> {
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue((int) (progress * 100));
                        lblStatusMessage.setText(status);
                    });
                });
            }

            @Override
            protected void done() {
                setButtonsBusy(false);
                try {
                    var result = get();
                    progressBar.setValue(100);
                    if (onTreeUpdatedListener != null) {
                        onTreeUpdatedListener.accept(result.tree());
                    }
                    if (onStatsUpdatedListener != null) {
                        onStatsUpdatedListener.accept(result.stats());
                    }
                    setStatus("Compressed successfully! Saved to: " + result.outputFile().getName(), false);

                    JOptionPane.showMessageDialog(FileOperationsPanel.this,
                            String.format("File compressed successfully!\n\n" +
                                            "Source: %s (%s)\n" +
                                            "Output: %s (%s)\n" +
                                            "Compression Ratio: %.2fx\n" +
                                            "Space Saved: %s (%.2f%%)",
                                    result.inputFile().getName(), CompressionStats.formatBytes(result.stats().getOriginalSize()),
                                    result.outputFile().getName(), CompressionStats.formatBytes(result.stats().getCompressedSize()),
                                    result.stats().getCompressionRatio(),
                                    CompressionStats.formatBytes(result.stats().getSpaceSavedBytes()),
                                    result.stats().getSpaceSavedPercentage()),
                            "Compression Complete",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    showError("Compression error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void executeDecompression() {
        if (selectedInputFile == null || selectedOutputFile == null) return;
        setButtonsBusy(true);
        setStatus("Starting decompression...", false);
        progressBar.setValue(0);

        SwingWorker<FileDecompressor.DecompressionResult, Void> worker = new SwingWorker<>() {
            @Override
            protected FileDecompressor.DecompressionResult doInBackground() throws Exception {
                return FileDecompressor.decompress(selectedInputFile, selectedOutputFile, (progress, status) -> {
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue((int) (progress * 100));
                        lblStatusMessage.setText(status);
                    });
                });
            }

            @Override
            protected void done() {
                setButtonsBusy(false);
                try {
                    var result = get();
                    progressBar.setValue(100);
                    if (onTreeUpdatedListener != null) {
                        onTreeUpdatedListener.accept(result.tree());
                    }
                    if (onStatsUpdatedListener != null) {
                        onStatsUpdatedListener.accept(result.stats());
                    }
                    setStatus("Restored original file byte-for-byte! Verified CRC-32.", false);

                    JOptionPane.showMessageDialog(FileOperationsPanel.this,
                            String.format("Decompression Complete & Verified!\n\n" +
                                            "Archive: %s (%s)\n" +
                                            "Restored File: %s (%s)\n" +
                                            "Integrity: CRC-32 matched original exactly!\n" +
                                            "Restored to: %s",
                                    result.compressedFile().getName(), CompressionStats.formatBytes(result.stats().getCompressedSize()),
                                    result.restoredFile().getName(), CompressionStats.formatBytes(result.stats().getOriginalSize()),
                                    result.restoredFile().getAbsolutePath()),
                            "Decompression Succeeded",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    showError("Decompression error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void openOutputDirectory() {
        if (selectedOutputFile != null && selectedOutputFile.getParentFile() != null) {
            try {
                Desktop.getDesktop().open(selectedOutputFile.getParentFile());
            } catch (Exception ex) {
                showError("Unable to open directory: " + ex.getMessage());
            }
        }
    }

    private void setButtonsBusy(boolean busy) {
        btnSelectFile.setEnabled(!busy);
        btnChooseOutput.setEnabled(!busy);
        btnAnalyze.setEnabled(!busy && selectedInputFile != null);
        btnCompress.setEnabled(!busy && selectedInputFile != null);
        btnDecompress.setEnabled(!busy && selectedInputFile != null);
        if (busy) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void setStatus(String message, boolean isError) {
        lblStatusMessage.setText(message);
        lblStatusMessage.setForeground(isError ? Theme.ERROR_ROSE : Theme.TEXT_SECONDARY);
        if (onStatusChangedListener != null) {
            onStatusChangedListener.accept(message);
        }
    }

    private void showError(String message) {
        setStatus(message, true);
        progressBar.setValue(0);
        JOptionPane.showMessageDialog(this, message, "Operation Failed", JOptionPane.ERROR_MESSAGE);
    }

    public void setOnTreeUpdatedListener(Consumer<HuffmanTree> listener) {
        this.onTreeUpdatedListener = listener;
    }

    public void setOnStatsUpdatedListener(Consumer<CompressionStats> listener) {
        this.onStatsUpdatedListener = listener;
    }

    public void setOnStatusChangedListener(Consumer<String> listener) {
        this.onStatusChangedListener = listener;
    }
}
