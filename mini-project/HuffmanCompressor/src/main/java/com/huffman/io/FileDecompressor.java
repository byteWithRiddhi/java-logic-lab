package com.huffman.io;

import com.huffman.core.HuffmanDecoder;
import com.huffman.core.HuffmanTree;

import java.io.*;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

/**
 * Executes the complete file decompression pipeline:
 * 1. Read and validate .huff magic bytes and version header.
 * 2. Parse frequency table and verify header integrity.
 * 3. Reconstruct the identical Huffman tree and prefix codes.
 * 4. Decode the bitstream payload into restored raw bytes.
 * 5. Verify the restored file CRC-32 checksum matches the stored original.
 */
public class FileDecompressor {

    public record DecompressionResult(
            File compressedFile,
            File restoredFile,
            HuffmanTree tree,
            CompressionMetadata metadata,
            CompressionStats stats
    ) {}

    /**
     * Reads only the header metadata and reconstructs the tree without decompressing the payload.
     * Useful for file inspection and UI preview.
     */
    public static HeaderInspectionResult inspect(File compressedFile) throws IOException {
        if (compressedFile == null || !compressedFile.exists() || !compressedFile.isFile()) {
            throw new FileNotFoundException("Invalid compressed file: " + (compressedFile == null ? "null" : compressedFile.getAbsolutePath()));
        }

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(compressedFile)))) {
            CompressionMetadata metadata = readAndValidateHeader(dis, compressedFile.length());
            HuffmanTree tree = new HuffmanTree(metadata.getFrequencies());
            return new HeaderInspectionResult(metadata, tree);
        }
    }

    public record HeaderInspectionResult(CompressionMetadata metadata, HuffmanTree tree) {}

    /**
     * Decompresses the specified .huff archive to the restored output destination.
     *
     * @param compressedFile Source .huff file.
     * @param restoredFile Destination file for restored content.
     * @param progressListener Callback for progress (0.0 to 1.0) and status description.
     * @return DecompressionResult containing restored file, tree, and stats.
     */
    public static DecompressionResult decompress(File compressedFile,
                                                 File restoredFile,
                                                 BiConsumer<Double, String> progressListener) throws IOException {
        if (compressedFile == null || !compressedFile.exists() || !compressedFile.isFile()) {
            throw new FileNotFoundException("Invalid compressed file: " + (compressedFile == null ? "null" : compressedFile.getAbsolutePath()));
        }
        if (restoredFile == null) {
            throw new IllegalArgumentException("Destination restored file cannot be null");
        }

        File parentDir = restoredFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        long startTime = System.currentTimeMillis();
        long compressedSize = compressedFile.length();

        if (progressListener != null) {
            progressListener.accept(0.10, "Validating header and reconstructing Huffman tree...");
        }

        CompressionMetadata metadata;
        HuffmanTree tree;

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(compressedFile)))) {
            metadata = readAndValidateHeader(dis, compressedSize);
            tree = new HuffmanTree(metadata.getFrequencies());

            if (progressListener != null) {
                progressListener.accept(0.30, "Decoding bitstream payload...");
            }

            // Stream and decode output with CRC-32 tracking
            CRC32 crc = new CRC32();
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(restoredFile));
                 CheckedOutputStream cos = new CheckedOutputStream(fos, crc)) {

                if (metadata.getOriginalSize() > 0) {
                    DoubleConsumer streamProgress = p -> {
                        if (progressListener != null) {
                            progressListener.accept(0.30 + (p * 0.65), String.format("Restoring bytes... %d%%", (int)(p * 100)));
                        }
                    };
                    HuffmanDecoder.decodeStream(
                            dis,
                            metadata.getPayloadBits(),
                            tree.getRoot(),
                            metadata.getOriginalSize(),
                            cos,
                            streamProgress
                    );
                }
                cos.flush();
            }

            // Verify integrity
            long calculatedChecksum = crc.getValue();
            if (calculatedChecksum != metadata.getCrc32Checksum()) {
                // Delete corrupt partial file
                restoredFile.delete();
                throw new IOException(String.format(
                        "Data integrity error: CRC-32 checksum mismatch! Expected 0x%08X, calculated 0x%08X. File may be corrupted.",
                        metadata.getCrc32Checksum(), calculatedChecksum));
            }
        }

        long endTime = System.currentTimeMillis();
        long restoredSize = restoredFile.length();
        long duration = Math.max(1, endTime - startTime);

        if (restoredSize != metadata.getOriginalSize()) {
            throw new IOException(String.format(
                    "Restored file size mismatch! Expected %d bytes, wrote %d bytes.",
                    metadata.getOriginalSize(), restoredSize));
        }

        CompressionStats stats = new CompressionStats(
                metadata.getOriginalSize(),
                compressedSize,
                metadata.getDistinctSymbols(),
                duration,
                tree.calculateShannonEntropy(),
                tree.calculateAverageCodeLength(),
                false
        );

        if (progressListener != null) {
            progressListener.accept(1.0, "Decompression and checksum verification complete!");
        }

        return new DecompressionResult(compressedFile, restoredFile, tree, metadata, stats);
    }

    private static CompressionMetadata readAndValidateHeader(DataInputStream dis, long fileLength) throws IOException {
        // Read Magic Bytes
        byte[] magic = new byte[4];
        dis.readFully(magic);
        if (!Arrays.equals(magic, CompressedFileFormat.MAGIC_BYTES)) {
            throw new IOException("Invalid file signature. File is not a valid Huffman archive (.huff).");
        }

        // Read Version
        byte version = dis.readByte();
        if (version != CompressedFileFormat.CURRENT_VERSION) {
            throw new IOException(String.format("Unsupported archive version: %d (Supported version: %d)",
                    version, CompressedFileFormat.CURRENT_VERSION));
        }

        // Read Flags
        byte flags = dis.readByte();

        // Read Original File Size
        long originalSize = dis.readLong();
        if (originalSize < 0) {
            throw new IOException("Corrupt header: negative original file size.");
        }

        // Read CRC-32
        long crc32 = dis.readLong();

        // Read Distinct Symbols K
        int distinctSymbols = dis.readUnsignedShort();
        if (distinctSymbols < 0 || distinctSymbols > 256) {
            throw new IOException("Corrupt header: invalid distinct symbol count: " + distinctSymbols);
        }

        // Read Frequency Table
        long[] frequencies = new long[256];
        long sumFrequencies = 0;
        for (int i = 0; i < distinctSymbols; i++) {
            int sym = dis.readByte() & 0xFF;
            long freq = dis.readLong();
            if (freq <= 0) {
                throw new IOException(String.format("Corrupt header: non-positive frequency for symbol %d: %d", sym, freq));
            }
            if (frequencies[sym] > 0) {
                throw new IOException(String.format("Corrupt header: duplicate symbol %d in frequency table", sym));
            }
            frequencies[sym] = freq;
            sumFrequencies += freq;
        }

        if (sumFrequencies != originalSize) {
            throw new IOException(String.format(
                    "Corrupt header: sum of symbol frequencies (%d) does not match declared original file size (%d).",
                    sumFrequencies, originalSize));
        }

        // Read Payload Bits
        long payloadBits = dis.readLong();
        if (payloadBits < 0) {
            throw new IOException("Corrupt header: negative payload bit count: " + payloadBits);
        }

        long expectedPayloadBytes = (payloadBits + 7) / 8;
        long headerSize = 4 + 1 + 1 + 8 + 8 + 2 + (distinctSymbols * 9L) + 8;
        if (fileLength > 0 && fileLength < headerSize + expectedPayloadBytes) {
            throw new IOException(String.format(
                    "Truncated file: archive is %d bytes, but header and payload require at least %d bytes.",
                    fileLength, headerSize + expectedPayloadBytes));
        }

        return new CompressionMetadata(version, flags, originalSize, crc32, distinctSymbols, frequencies, payloadBits);
    }
}
