package com.huffman.core;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Packs individual bits into bytes and writes them to an underlying OutputStream.
 * Uses MSB-to-LSB packing order.
 */
public class BitWriter implements Flushable, Closeable {
    private final OutputStream out;
    private int currentByte;
    private int bitCount;
    private long totalBitsWritten;

    public BitWriter(OutputStream out) {
        if (out == null) {
            throw new IllegalArgumentException("OutputStream cannot be null");
        }
        this.out = out;
        this.currentByte = 0;
        this.bitCount = 0;
        this.totalBitsWritten = 0;
    }

    /**
     * Writes a single bit (0 or 1).
     */
    public void writeBit(int bit) throws IOException {
        currentByte = (currentByte << 1) | (bit & 1);
        bitCount++;
        totalBitsWritten++;

        if (bitCount == 8) {
            out.write(currentByte);
            currentByte = 0;
            bitCount = 0;
        }
    }

    /**
     * Writes a string of '0' and '1' characters.
     */
    public void writeBits(String bitString) throws IOException {
        if (bitString == null) return;
        for (int i = 0; i < bitString.length(); i++) {
            char c = bitString.charAt(i);
            if (c == '0') {
                writeBit(0);
            } else if (c == '1') {
                writeBit(1);
            } else {
                throw new IllegalArgumentException("Invalid bit character: '" + c + "'");
            }
        }
    }

    /**
     * Returns the total number of bits written so far.
     */
    public long getTotalBitsWritten() {
        return totalBitsWritten;
    }

    /**
     * Flushes any unwritten bits by padding with trailing zeros to complete the final byte.
     */
    @Override
    public void flush() throws IOException {
        if (bitCount > 0) {
            currentByte <<= (8 - bitCount);
            out.write(currentByte);
            currentByte = 0;
            bitCount = 0;
        }
        out.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        out.close();
    }
}
