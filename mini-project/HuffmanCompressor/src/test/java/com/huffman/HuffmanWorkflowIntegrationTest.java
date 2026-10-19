package com.huffman;

import com.huffman.core.HuffmanTree;
import com.huffman.io.*;
import com.huffman.ui.CodeTablePanel;
import com.huffman.ui.HuffmanTreePanel;
import com.huffman.ui.StatisticsPanel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class HuffmanWorkflowIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Verify Full End-to-End Workflow with UI Components Headless")
    void testEndToEndWorkflowWithUI() throws Exception {
        // Set headless flag so GUI components can run in headless CI/test environments
        System.setProperty("java.awt.headless", "true");

        String sampleText = "Huffman coding is a particular type of optimal prefix code that is commonly used for lossless data compression.";
        File sourceFile = tempDir.resolve("sample.txt").toFile();
        File compressedFile = tempDir.resolve("sample.txt.huff").toFile();
        File restoredFile = tempDir.resolve("sample_restored.txt").toFile();

        Files.writeString(sourceFile.toPath(), sampleText);

        // 1. Test Compression
        FileCompressor.CompressionResult compResult = FileCompressor.compress(sourceFile, compressedFile, null);
        assertNotNull(compResult);
        assertTrue(compressedFile.exists());
        assertTrue(compressedFile.length() > 0);

        HuffmanTree tree = compResult.tree();
        assertNotNull(tree);
        assertTrue(tree.getDistinctSymbols() > 0);

        // 2. Test Decompression
        FileDecompressor.DecompressionResult decompResult = FileDecompressor.decompress(compressedFile, restoredFile, null);
        assertNotNull(decompResult);
        assertTrue(restoredFile.exists());

        // Verify content byte-for-byte
        String restoredText = Files.readString(restoredFile.toPath());
        assertEquals(sampleText, restoredText, "Restored text must match sample text");

        // 3. Test Statistics calculation
        CompressionStats stats = compResult.stats();
        assertNotNull(stats);
        assertEquals(sourceFile.length(), stats.getOriginalSize());
        assertEquals(compressedFile.length(), stats.getCompressedSize());
        assertTrue(stats.getDistinctSymbols() > 0);
        assertTrue(stats.getShannonEntropy() > 0);

        // 4. Test UI Panels can receive data without errors
        SwingUtilities.invokeAndWait(() -> {
            HuffmanTreePanel treePanel = new HuffmanTreePanel();
            treePanel.setSize(800, 600);
            treePanel.setTree(tree);
            treePanel.highlightSymbol('H');
            treePanel.fitToView();

            CodeTablePanel codeTablePanel = new CodeTablePanel();
            codeTablePanel.setTree(tree);

            StatisticsPanel statsPanel = new StatisticsPanel();
            statsPanel.updateStats(stats);
        });
    }

    @Test
    @DisplayName("Verify Standalone Header Inspection without Decompressing")
    void testHeaderInspection() throws IOException {
        String content = "Testing header inspection independently";
        File src = tempDir.resolve("inspect_src.txt").toFile();
        File huff = tempDir.resolve("inspect.huff").toFile();
        Files.writeString(src.toPath(), content);

        FileCompressor.compress(src, huff, null);

        FileDecompressor.HeaderInspectionResult inspection = FileDecompressor.inspect(huff);
        assertNotNull(inspection);
        assertEquals(src.length(), inspection.metadata().getOriginalSize());
        assertEquals(inspection.metadata().getDistinctSymbols(), inspection.tree().getDistinctSymbols());
        assertEquals(1, inspection.metadata().getVersion());
    }
}
