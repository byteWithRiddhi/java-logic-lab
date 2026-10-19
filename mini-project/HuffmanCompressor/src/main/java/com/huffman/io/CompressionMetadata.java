package com.huffman.io;

import java.util.Arrays;

/**
 * Encapsulates the metadata extracted from or written to a compressed Huffman file header.
 */
public class CompressionMetadata {
    private final byte version;
    private final byte flags;
    private final long originalSize;
    private final long crc32Checksum;
    private final int distinctSymbols;
    private final long[] frequencies;
    private final long payloadBits;

    public CompressionMetadata(byte version,
                               byte flags,
                               long originalSize,
                               long crc32Checksum,
                               int distinctSymbols,
                               long[] frequencies,
                               long payloadBits) {
        this.version = version;
        this.flags = flags;
        this.originalSize = originalSize;
        this.crc32Checksum = crc32Checksum;
        this.distinctSymbols = distinctSymbols;
        this.frequencies = frequencies != null ? Arrays.copyOf(frequencies, 256) : new long[256];
        this.payloadBits = payloadBits;
    }

    public byte getVersion() {
        return version;
    }

    public byte getFlags() {
        return flags;
    }

    public long getOriginalSize() {
        return originalSize;
    }

    public long getCrc32Checksum() {
        return crc32Checksum;
    }

    public int getDistinctSymbols() {
        return distinctSymbols;
    }

    public long[] getFrequencies() {
        return Arrays.copyOf(frequencies, frequencies.length);
    }

    public long getPayloadBits() {
        return payloadBits;
    }

    public long getPayloadByteCount() {
        return (payloadBits + 7) / 8;
    }

    /**
     * Estimated header size in bytes.
     */
    public int getHeaderSizeBytes() {
        // 4 (magic) + 1 (version) + 1 (flags) + 8 (origSize) + 8 (crc32) + 2 (K) + (K * 9) + 8 (payloadBits)
        return 4 + 1 + 1 + 8 + 8 + 2 + (distinctSymbols * 9) + 8;
    }
}
