package com.huffman.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.DoubleConsumer;

/**
 * Handles encoding of raw bytes into Huffman bit sequences.
 */
public class HuffmanEncoder {

    /**
     * Encodes raw data from an InputStream into an OutputStream via a BitWriter.
     *
     * @param in Raw byte stream to compress.
     * @param totalBytes Total number of bytes in the stream (for progress calculation).
     * @param codes Map of byte symbol (0..255) to Huffman bit string.
     * @param out Destination OutputStream.
     * @param progressListener Optional callback for progress (0.0 to 1.0).
     * @return Total number of bits written to the payload.
     */
    public static long encodeStream(InputStream in,
                                    long totalBytes,
                                    Map<Integer, String> codes,
                                    OutputStream out,
                                    DoubleConsumer progressListener) throws IOException {
        BitWriter bitWriter = new BitWriter(out);
        byte[] buffer = new byte[65536]; // 64 KB buffer for streaming
        int bytesRead;
        long processedBytes = 0;
        long lastReportedBytes = 0;

        while ((bytesRead = in.read(buffer)) != -1) {
            for (int i = 0; i < bytesRead; i++) {
                int sym = buffer[i] & 0xFF;
                String code = codes.get(sym);
                if (code == null) {
                    throw new IllegalStateException("No Huffman code found for symbol: " + sym);
                }
                bitWriter.writeBits(code);
            }
            processedBytes += bytesRead;

            if (progressListener != null && totalBytes > 0 && (processedBytes - lastReportedBytes >= 131072 || processedBytes == totalBytes)) {
                progressListener.accept((double) processedBytes / totalBytes);
                lastReportedBytes = processedBytes;
            }
        }

        bitWriter.flush();
        return bitWriter.getTotalBitsWritten();
    }

    /**
     * Helper to encode an in-memory byte array.
     */
    public static byte[] encodeBytes(byte[] data, Map<Integer, String> codes, long[] bitsWrittenHolder) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        long bits = encodeStream(new java.io.ByteArrayInputStream(data), data.length, codes, baos, null);
        if (bitsWrittenHolder != null && bitsWrittenHolder.length > 0) {
            bitsWrittenHolder[0] = bits;
        }
        return baos.toByteArray();
    }
}
