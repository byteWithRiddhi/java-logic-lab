package com.huffman;

import com.huffman.core.*;
import com.huffman.io.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class HuffmanCoreTest {

    @TempDir
    Path tempDir;

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(42); // deterministic seed
    }

    private void assertRoundTrip(byte[] originalData, String testName) throws IOException {
        File inputFile = tempDir.resolve(testName + ".orig").toFile();
        File compressedFile = tempDir.resolve(testName + ".huff").toFile();
        File restoredFile = tempDir.resolve(testName + ".restored").toFile();

        Files.write(inputFile.toPath(), originalData);

        // Compress
        FileCompressor.CompressionResult compResult = FileCompressor.compress(inputFile, compressedFile, null);
        assertNotNull(compResult);
        assertTrue(compressedFile.exists());

        // Decompress
        FileDecompressor.DecompressionResult decompResult = FileDecompressor.decompress(compressedFile, restoredFile, null);
        assertNotNull(decompResult);
        assertTrue(restoredFile.exists());

        // Byte-for-byte exact assertion
        byte[] restoredBytes = Files.readAllBytes(restoredFile.toPath());
        assertArrayEquals(originalData, restoredBytes,
                "Restored bytes must match original bytes exactly for " + testName);
    }

    @Test
    @DisplayName("Test 1: Empty file (0 bytes)")
    void testEmptyFile() throws IOException {
        byte[] empty = new byte[0];
        assertRoundTrip(empty, "empty_file");
    }

    @Test
    @DisplayName("Test 2: Single byte file")
    void testSingleByteFile() throws IOException {
        byte[] single = new byte[] { (byte) 'X' };
        assertRoundTrip(single, "single_byte");
    }

    @Test
    @DisplayName("Test 3: File with repeated identical bytes")
    void testRepeatedBytes() throws IOException {
        byte[] repeated = new byte[5000];
        Arrays.fill(repeated, (byte) 'Z');
        assertRoundTrip(repeated, "repeated_bytes");
    }

    @Test
    @DisplayName("Test 4: All printable ASCII characters")
    void testPrintableAscii() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (char c = 32; c <= 126; c++) {
            sb.append(c);
        }
        // Repeat to have natural text distribution
        for (int i = 0; i < 50; i++) {
            sb.append("The quick brown fox jumps over the lazy dog. 1234567890!@#$%^&*()_+\n");
        }
        byte[] asciiData = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertRoundTrip(asciiData, "printable_ascii");
    }

    @Test
    @DisplayName("Test 5: UTF-8 multilingual non-English characters & Emojis")
    void testMultilingualUtf8() throws IOException {
        String multilingual = "English: Hello World!\n" +
                "Hindi: नमस्ते दुनिया, डेटा संपीड़न अल्गोरिदम\n" +
                "French: Bonjour le monde, l'accent aigu, ç, é, è, ê\n" +
                "Japanese: こんにちは世界！ハフマン符号化\n" +
                "Arabic: مرحباً بالعالم، ضغط البيانات\n" +
                "German: Grüß Gott, Maßstab, Übergrößen\n" +
                "Emojis: 🚀💡🌲✨🔥💻⚡📦🎉";
        byte[] utf8Data = multilingual.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertRoundTrip(utf8Data, "multilingual_utf8");
    }

    @Test
    @DisplayName("Test 6: All 256 byte values (0 to 255)")
    void testAll256ByteValues() throws IOException {
        byte[] allBytes = new byte[256 * 10];
        for (int i = 0; i < allBytes.length; i++) {
            allBytes[i] = (byte) (i % 256);
        }
        assertRoundTrip(allBytes, "all_256_bytes");
    }

    @Test
    @DisplayName("Test 7: Random binary bytes")
    void testRandomBinary() throws IOException {
        byte[] randomBytes = new byte[16384]; // 16 KB random bytes
        random.nextBytes(randomBytes);
        assertRoundTrip(randomBytes, "random_binary");
    }

    @Test
    @DisplayName("Test 8: Larger file (1.5 MB)")
    void testLargerFile() throws IOException {
        byte[] largeData = new byte[1_500_000];
        // Mix patterned text and pseudo-random data
        byte[] pattern = "HuffmanCodingLosslessCompressionAlgorithmDataStructuresAndAlgorithms".getBytes();
        for (int i = 0; i < largeData.length; i++) {
            if (i % 3 == 0) {
                largeData[i] = pattern[i % pattern.length];
            } else {
                largeData[i] = (byte) (i % 128);
            }
        }
        assertRoundTrip(largeData, "larger_file");
    }

    @Test
    @DisplayName("Test 9: Corrupted files rejection")
    void testCorruptedFiles() throws IOException {
        byte[] data = "Corrupted file test payload for Huffman".getBytes();
        File inputFile = tempDir.resolve("corrupt_src.txt").toFile();
        File compressedFile = tempDir.resolve("corrupt.huff").toFile();
        File restoredFile = tempDir.resolve("corrupt_restored.txt").toFile();

        Files.write(inputFile.toPath(), data);
        FileCompressor.compress(inputFile, compressedFile, null);

        byte[] rawArchive = Files.readAllBytes(compressedFile.toPath());

        // Case A: Corrupted Magic Bytes
        byte[] badMagic = Arrays.copyOf(rawArchive, rawArchive.length);
        badMagic[0] = 'B';
        badMagic[1] = 'A';
        badMagic[2] = 'D';
        badMagic[3] = '!';
        File badMagicFile = tempDir.resolve("bad_magic.huff").toFile();
        Files.write(badMagicFile.toPath(), badMagic);
        assertThrows(IOException.class, () -> FileDecompressor.decompress(badMagicFile, restoredFile, null));

        // Case B: Truncated file
        byte[] truncated = Arrays.copyOf(rawArchive, 15); // cut off header
        File truncatedFile = tempDir.resolve("truncated.huff").toFile();
        Files.write(truncatedFile.toPath(), truncated);
        assertThrows(IOException.class, () -> FileDecompressor.decompress(truncatedFile, restoredFile, null));

        // Case C: Bit flipped in payload (CRC-32 failure)
        if (rawArchive.length > 20) {
            byte[] flippedPayload = Arrays.copyOf(rawArchive, rawArchive.length);
            flippedPayload[rawArchive.length - 1] ^= 0x55; // flip bits in payload
            File flippedFile = tempDir.resolve("flipped.huff").toFile();
            Files.write(flippedFile.toPath(), flippedPayload);
            assertThrows(IOException.class, () -> FileDecompressor.decompress(flippedFile, restoredFile, null));
        }
    }

    @Test
    @DisplayName("Test 10: Build steps recording and Priority Queue progression")
    void testBuildSteps() {
        long[] freqs = new long[256];
        freqs['a'] = 5;
        freqs['b'] = 9;
        freqs['c'] = 12;
        freqs['d'] = 13;
        freqs['e'] = 16;
        freqs['f'] = 45;

        HuffmanTree tree = new HuffmanTree(freqs);
        assertEquals(6, tree.getDistinctSymbols());
        assertEquals(100, tree.getTotalFrequency());
        assertNotNull(tree.getRoot());

        // Steps should be: initial (step 0) + 5 merges (step 1..5) = 6 steps
        assertEquals(6, tree.getBuildSteps().size());
        assertTrue(tree.getBuildSteps().get(5).isFinalStep());
        assertEquals(tree.getRoot(), tree.getBuildSteps().get(5).getParentCreated());

        // Test prefix code property: No code is a prefix of another code
        var codes = tree.getSymbolToCode().values();
        for (String c1 : codes) {
            for (String c2 : codes) {
                if (c1 != c2) {
                    assertFalse(c1.startsWith(c2), "Code " + c2 + " must not be prefix of " + c1);
                }
            }
        }
    }
}
