package com.huffman.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads individual bits from an underlying InputStream.
 * Matches the MSB-to-LSB order written by BitWriter and can enforce a maximum
 * bit limit to safely discard trailing padding bits.
 */
public class BitReader implements Closeable {
    private final InputStream in;
    private final long maxBitsToRead;
    private int currentByte;
    private int bitPos; // 0 to 7 (0 = MSB, 7 = LSB). 8 means buffer empty.
    private long totalBitsRead;

    /**
     * Reads with no limit (until stream EOF).
     */
    public BitReader(InputStream in) {
        this(in, -1);
    }

    /**
     * Reads up to maxBitsToRead bits.
     *
     * @param in InputStream to read from.
     * @param maxBitsToRead Exact number of valid payload bits, or -1 for unlimited.
     */
    public BitReader(InputStream in, long maxBitsToRead) {
        if (in == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        this.in = in;
        this.maxBitsToRead = maxBitsToRead;
        this.currentByte = 0;
        this.bitPos = 8;
        this.totalBitsRead = 0;
    }

    /**
     * Reads the next single bit (0 or 1).
     *
     * @return 0 or 1, or -1 if end of stream or maxBitsToRead reached.
     */
    public int readBit() throws IOException {
        if (maxBitsToRead >= 0 && totalBitsRead >= maxBitsToRead) {
            return -1;
        }

        if (bitPos >= 8) {
            currentByte = in.read();
            if (currentByte == -1) {
                return -1; // EOF
            }
            bitPos = 0;
        }

        int bit = (currentByte >> (7 - bitPos)) & 1;
        bitPos++;
        totalBitsRead++;
        return bit;
    }

    /**
     * Returns total number of bits read so far.
     */
    public long getTotalBitsRead() {
        return totalBitsRead;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
