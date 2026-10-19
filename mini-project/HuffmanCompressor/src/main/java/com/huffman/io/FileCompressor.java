package com.huffman.io;

import com.huffman.core.HuffmanEncoder;
import com.huffman.core.HuffmanTree;

import java.io.*;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;
import java.util.zip.CRC32;

/**
 * Executes the complete file compression pipeline:
 * 1. Read input and compute byte frequencies and CRC-32 checksum.
 * 2. Construct Huffman tree and generate codes.
 * 3. Write structured binary header (.huff format).
 * 4. Encode and pack bitstream payload.
 * 5. Verify and return compression statistics.
 */
public class FileCompressor {

    public record CompressionResult(
            File inputFile,
            File outputFile,
            HuffmanTree tree,
            CompressionMetadata metadata,
            CompressionStats stats
    ) {}

    /**
     * Compresses the specified source file to the target destination.
     *
     * @param inputFile Source file to compress.
     * @param outputFile Destination .huff file.
     * @param progressListener Callback for progress (0.0 to 1.0) and status description.
     * @return CompressionResult containing tree, metadata, and statistics.
     */
    public static CompressionResult compress(File inputFile,
                                             File outputFile,
                                             BiConsumer<Double, String> progressListener) throws IOException {
        if (inputFile == null || !inputFile.exists() || !inputFile.isFile()) {
            throw new FileNotFoundException("Invalid source file: " + (inputFile == null ? "null" : inputFile.getAbsolutePath()));
        }
        if (outputFile == null) {
            throw new IllegalArgumentException("Destination output file cannot be null");
        }

        // Ensure parent directory exists
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        long startTime = System.currentTimeMillis();
        long fileSize = inputFile.length();

        if (progressListener != null) {
            progressListener.accept(0.05, "Analyzing byte frequencies and computing checksum...");
        }

        // Step 1: Count byte frequencies and calculate CRC-32
        long[] frequencies = new long[256];
        CRC32 crc = new CRC32();
        long totalBytesRead = 0;

        byte[] buffer = new byte[65536];
        try (InputStream fis = new BufferedInputStream(new FileInputStream(inputFile))) {
            int read;
            while ((read = fis.read(buffer)) != -1) {
                crc.update(buffer, 0, read);
                for (int i = 0; i < read; i++) {
                    frequencies[buffer[i] & 0xFF]++;
                }
                totalBytesRead += read;
            }
        }

        if (totalBytesRead != fileSize) {
            fileSize = totalBytesRead; // adjust if file changed during read
        }

        if (progressListener != null) {
            progressListener.accept(0.25, "Building Huffman tree and generating prefix codes...");
        }

        // Step 2: Build Huffman Tree
        HuffmanTree tree = new HuffmanTree(frequencies);
        Map<Integer, String> codes = tree.getSymbolToCode();
        long payloadBits = tree.calculateTotalPayloadBits();
        long checksum = crc.getValue();

        if (progressListener != null) {
            progressListener.accept(0.40, "Writing compressed file header...");
        }

        // Step 3 & 4: Write Custom Format Header & Bit Payload
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            // Magic bytes "HUFF"
            dos.write(CompressedFileFormat.MAGIC_BYTES);

            // Version & Flags
            dos.writeByte(CompressedFileFormat.CURRENT_VERSION);
            dos.writeByte(0x00); // flags

            // Original file size & CRC-32
            dos.writeLong(fileSize);
            dos.writeLong(checksum);

            // Distinct symbol count K
            int distinct = tree.getDistinctSymbols();
            dos.writeShort(distinct);

            // Frequency table (K entries)
            for (int i = 0; i < 256; i++) {
                if (frequencies[i] > 0) {
                    dos.writeByte(i);
                    dos.writeLong(frequencies[i]);
                }
            }

            // Total Payload Bit Count
            dos.writeLong(payloadBits);

            // Step 4: Stream and encode file payload
            if (fileSize > 0) {
                if (progressListener != null) {
                    progressListener.accept(0.50, "Encoding bitstream payload...");
                }

                try (InputStream fis = new BufferedInputStream(new FileInputStream(inputFile))) {
                    DoubleConsumer streamProgress = p -> {
                        if (progressListener != null) {
                            progressListener.accept(0.50 + (p * 0.45), String.format("Encoding payload... %d%%", (int)(p * 100)));
                        }
                    };
                    HuffmanEncoder.encodeStream(fis, fileSize, codes, dos, streamProgress);
                }
            }
            dos.flush();
        }

        long endTime = System.currentTimeMillis();
        long compressedSize = outputFile.length();
        long duration = Math.max(1, endTime - startTime);

        CompressionMetadata metadata = new CompressionMetadata(
                CompressedFileFormat.CURRENT_VERSION,
                (byte) 0x00,
                fileSize,
                checksum,
                tree.getDistinctSymbols(),
                frequencies,
                payloadBits
        );

        CompressionStats stats = new CompressionStats(
                fileSize,
                compressedSize,
                tree.getDistinctSymbols(),
                duration,
                tree.calculateShannonEntropy(),
                tree.calculateAverageCodeLength(),
                true
        );

        if (progressListener != null) {
            progressListener.accept(1.0, "Compression complete!");
        }

        return new CompressionResult(inputFile, outputFile, tree, metadata, stats);
    }
}
