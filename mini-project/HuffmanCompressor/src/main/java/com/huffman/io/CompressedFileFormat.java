package com.huffman.io;

/**
 * Specification constants and header layout for the custom Huffman compressed file format.
 *
 * <p>File Structure (.huff):</p>
 * <pre>
 * +---------------------------------------------------------+
 * | Magic Bytes: "HUFF" (4 bytes: 0x48, 0x55, 0x46, 0x46)   |
 * +---------------------------------------------------------+
 * | Version: 1 byte (0x01)                                  |
 * +---------------------------------------------------------+
 * | Flags: 1 byte (0x00 reserved)                           |
 * +---------------------------------------------------------+
 * | Original File Size: 8 bytes (long, big-endian)          |
 * +---------------------------------------------------------+
 * | Original CRC-32 Checksum: 8 bytes (long, big-endian)    |
 * +---------------------------------------------------------+
 * | Distinct Symbol Count K: 2 bytes (short, 0..256)        |
 * +---------------------------------------------------------+
 * | Frequency Table: K entries                              |
 * |   - Symbol: 1 byte (0..255)                             |
 * |   - Frequency: 8 bytes (long, big-endian)               |
 * +---------------------------------------------------------+
 * | Total Payload Bit Count: 8 bytes (long, big-endian)     |
 * +---------------------------------------------------------+
 * | Packed Bit Payload: ceil(TotalBits / 8) bytes           |
 * +---------------------------------------------------------+
 * </pre>
 */
public final class CompressedFileFormat {
    private CompressedFileFormat() {}

    public static final byte[] MAGIC_BYTES = new byte[] { 'H', 'U', 'F', 'F' };
    public static final byte CURRENT_VERSION = 1;
    public static final String FILE_EXTENSION = ".huff";
    public static final String FILE_DESCRIPTION = "Huffman Compressed Archive (*.huff)";
}
