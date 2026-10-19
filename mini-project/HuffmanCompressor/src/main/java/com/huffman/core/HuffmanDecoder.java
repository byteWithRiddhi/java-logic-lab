package com.huffman.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.DoubleConsumer;

/**
 * Handles decoding of Huffman bit sequences back into original raw bytes.
 */
public class HuffmanDecoder {

    /**
     * Decodes the bitstream from an InputStream using the Huffman tree root
     * until originalSize bytes have been restored.
     *
     * @param in InputStream containing compressed bit payload.
     * @param payloadBits Total number of valid bits in the payload.
     * @param root Root node of the reconstructed Huffman tree.
     * @param originalSize Exact number of uncompressed bytes to restore.
     * @param out Destination OutputStream for restored bytes.
     * @param progressListener Optional callback for progress (0.0 to 1.0).
     * @return Total number of bytes decoded.
     */
    public static long decodeStream(InputStream in,
                                    long payloadBits,
                                    HuffmanNode root,
                                    long originalSize,
                                    OutputStream out,
                                    DoubleConsumer progressListener) throws IOException {
        if (originalSize == 0) {
            return 0;
        }
        if (root == null) {
            throw new IllegalArgumentException("Huffman root node cannot be null for non-empty file");
        }

        BitReader bitReader = new BitReader(in, payloadBits);
        byte[] writeBuffer = new byte[65536];
        int bufferPos = 0;
        long decodedCount = 0;
        long lastReportedCount = 0;

        // Special case: Single symbol leaf directly at root
        if (root.isLeaf()) {
            int sym = root.getSymbol();
            for (long i = 0; i < originalSize; i++) {
                writeBuffer[bufferPos++] = (byte) sym;
                if (bufferPos == writeBuffer.length) {
                    out.write(writeBuffer, 0, bufferPos);
                    bufferPos = 0;
                }
                decodedCount++;
            }
            if (bufferPos > 0) {
                out.write(writeBuffer, 0, bufferPos);
            }
            if (progressListener != null) {
                progressListener.accept(1.0);
            }
            return decodedCount;
        }

        HuffmanNode current = root;

        while (decodedCount < originalSize) {
            int bit = bitReader.readBit();
            if (bit == -1) {
                throw new EOFException(String.format(
                        "Premature end of compressed bitstream: expected %d bytes, reached EOF after %d bytes",
                        originalSize, decodedCount));
            }

            current = (bit == 0) ? current.getLeft() : current.getRight();
            if (current == null) {
                throw new IllegalStateException("Corrupt bitstream: bit path led to null node in Huffman tree");
            }

            if (current.isLeaf()) {
                writeBuffer[bufferPos++] = (byte) current.getSymbol();
                decodedCount++;
                current = root; // Reset to root for next symbol

                if (bufferPos == writeBuffer.length) {
                    out.write(writeBuffer, 0, bufferPos);
                    bufferPos = 0;
                }

                if (progressListener != null && (decodedCount - lastReportedCount >= 131072 || decodedCount == originalSize)) {
                    progressListener.accept((double) decodedCount / originalSize);
                    lastReportedCount = decodedCount;
                }
            }
        }

        if (bufferPos > 0) {
            out.write(writeBuffer, 0, bufferPos);
        }
        out.flush();

        return decodedCount;
    }
}
