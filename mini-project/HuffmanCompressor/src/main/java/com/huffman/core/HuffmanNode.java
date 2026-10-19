package com.huffman.core;

import java.util.Objects;

/**
 * Represents a node in the Huffman Tree.
 * Can be a leaf node (holding a byte symbol 0..255) or an internal node.
 */
public class HuffmanNode implements Comparable<HuffmanNode> {
    private final int symbol; // 0..255 for leaf, or -1 for internal node
    private final long frequency;
    private final HuffmanNode left;
    private final HuffmanNode right;
    private final int minSymbol; // Lowest leaf symbol in subtree for deterministic tie-breaking
    private final int creationOrder; // Stable sequence id for deterministic tie-breaking

    /**
     * Leaf node constructor
     */
    public HuffmanNode(int symbol, long frequency, int creationOrder) {
        if (symbol < 0 || symbol > 255) {
            throw new IllegalArgumentException("Byte symbol must be in range 0..255: " + symbol);
        }
        this.symbol = symbol;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
        this.minSymbol = symbol;
        this.creationOrder = creationOrder;
    }

    /**
     * Internal node constructor
     */
    public HuffmanNode(HuffmanNode left, HuffmanNode right, int creationOrder) {
        this.symbol = -1;
        long f = 0;
        int min = Integer.MAX_VALUE;
        if (left != null) {
            f += left.frequency;
            min = Math.min(min, left.minSymbol);
        }
        if (right != null) {
            f += right.frequency;
            min = Math.min(min, right.minSymbol);
        }
        this.frequency = f;
        this.left = left;
        this.right = right;
        this.minSymbol = (min == Integer.MAX_VALUE) ? -1 : min;
        this.creationOrder = creationOrder;
    }

    /**
     * Single-symbol root wrapper constructor (when file has only 1 distinct symbol)
     */
    public HuffmanNode(HuffmanNode singleChild, int creationOrder) {
        this.symbol = -1;
        this.frequency = singleChild != null ? singleChild.frequency : 0;
        this.left = singleChild;
        this.right = null;
        this.minSymbol = singleChild != null ? singleChild.minSymbol : -1;
        this.creationOrder = creationOrder;
    }

    public boolean isLeaf() {
        return left == null && right == null && symbol != -1;
    }

    public int getSymbol() {
        return symbol;
    }

    public long getFrequency() {
        return frequency;
    }

    public HuffmanNode getLeft() {
        return left;
    }

    public HuffmanNode getRight() {
        return right;
    }

    public int getMinSymbol() {
        return minSymbol;
    }

    public int getCreationOrder() {
        return creationOrder;
    }

    /**
     * Calculates the height of the subtree rooted at this node.
     */
    public int getHeight() {
        if (isLeaf()) return 1;
        int lh = (left != null) ? left.getHeight() : 0;
        int rh = (right != null) ? right.getHeight() : 0;
        return 1 + Math.max(lh, rh);
    }

    /**
     * Calculates the total number of leaves in this subtree.
     */
    public int getLeafCount() {
        if (isLeaf()) return 1;
        int count = 0;
        if (left != null) count += left.getLeafCount();
        if (right != null) count += right.getLeafCount();
        return count;
    }

    /**
     * Human readable display label for UI rendering.
     */
    public String getDisplayLabel() {
        if (isLeaf()) {
            return formatByteSymbol(symbol);
        }
        return "Σ=" + frequency;
    }

    public static String formatByteSymbol(int b) {
        if (b < 0 || b > 255) return "?";
        if (b == ' ') return "[SPACE]";
        if (b == '\n') return "[LF]";
        if (b == '\r') return "[CR]";
        if (b == '\t') return "[TAB]";
        if (b >= 33 && b <= 126) {
            return "'" + (char) b + "'";
        }
        return String.format("0x%02X", b);
    }

    @Override
    public int compareTo(HuffmanNode o) {
        if (o == null) return 1;
        // 1. Primary order: frequency ascending
        int cmp = Long.compare(this.frequency, o.frequency);
        if (cmp != 0) return cmp;

        // 2. Secondary order: minimum symbol ascending
        cmp = Integer.compare(this.minSymbol, o.minSymbol);
        if (cmp != 0) return cmp;

        // 3. Deterministic order: creation order
        return Integer.compare(this.creationOrder, o.creationOrder);
    }

    @Override
    public String toString() {
        if (isLeaf()) {
            return String.format("Leaf(%s, freq=%d)", formatByteSymbol(symbol), frequency);
        }
        return String.format("Internal(freq=%d, left=%s, right=%s)",
                frequency,
                left != null ? "Node" : "null",
                right != null ? "Node" : "null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HuffmanNode that)) return false;
        return symbol == that.symbol &&
                frequency == that.frequency &&
                minSymbol == that.minSymbol &&
                creationOrder == that.creationOrder &&
                Objects.equals(left, that.left) &&
                Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, frequency, minSymbol, creationOrder);
    }
}
