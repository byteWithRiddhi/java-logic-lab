package com.huffman.io;

/**
 * Encapsulates performance metrics, size calculations, and compression statistics.
 */
public class CompressionStats {
    private final long originalSize;
    private final long compressedSize;
    private final int distinctSymbols;
    private final long durationMs;
    private final double shannonEntropy;
    private final double averageCodeLength;
    private final boolean isCompression; // true for compression, false for decompression

    public CompressionStats(long originalSize,
                            long compressedSize,
                            int distinctSymbols,
                            long durationMs,
                            double shannonEntropy,
                            double averageCodeLength,
                            boolean isCompression) {
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
        this.distinctSymbols = distinctSymbols;
        this.durationMs = durationMs;
        this.shannonEntropy = shannonEntropy;
        this.averageCodeLength = averageCodeLength;
        this.isCompression = isCompression;
    }

    public long getOriginalSize() {
        return originalSize;
    }

    public long getCompressedSize() {
        return compressedSize;
    }

    public long getSpaceSavedBytes() {
        return originalSize - compressedSize;
    }

    public double getCompressionRatio() {
        if (compressedSize <= 0) return 0.0;
        return (double) originalSize / compressedSize;
    }

    public double getSpaceSavedPercentage() {
        if (originalSize <= 0) return 0.0;
        return ((double) (originalSize - compressedSize) / originalSize) * 100.0;
    }

    public boolean isExpandedDueToOverhead() {
        return originalSize > 0 && compressedSize > originalSize;
    }

    public int getDistinctSymbols() {
        return distinctSymbols;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public double getShannonEntropy() {
        return shannonEntropy;
    }

    public double getAverageCodeLength() {
        return averageCodeLength;
    }

    public boolean isCompression() {
        return isCompression;
    }

    public static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "-" + formatBytes(-bytes);
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(java.util.Locale.US, "%.2f %sB (%d B)", bytes / Math.pow(1024, exp), pre, bytes);
    }

    public static String formatCompactBytes(long bytes) {
        if (bytes < 0) {
            return "-" + formatCompactBytes(-bytes);
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(java.util.Locale.US, "%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
