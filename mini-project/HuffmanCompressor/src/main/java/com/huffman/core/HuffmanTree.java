package com.huffman.core;

import java.util.*;

/**
 * Builds and maintains the Huffman Binary Tree, generates prefix codes,
 * and records construction steps for algorithmic visualization.
 */
public class HuffmanTree {
    private final long[] frequencies;
    private final HuffmanNode root;
    private final Map<Integer, String> symbolToCode;
    private final Map<String, Integer> codeToSymbol;
    private final List<BuildStep> buildSteps;
    private final int distinctSymbols;
    private final long totalFrequency;

    /**
     * Constructs a Huffman Tree from an array of 256 byte frequencies.
     *
     * @param frequencies Array of length 256 containing frequencies for each byte value (0..255).
     */
    public HuffmanTree(long[] frequencies) {
        if (frequencies == null || frequencies.length != 256) {
            throw new IllegalArgumentException("Frequencies array must be non-null and exactly of length 256");
        }
        this.frequencies = Arrays.copyOf(frequencies, 256);
        this.symbolToCode = new HashMap<>();
        this.codeToSymbol = new HashMap<>();
        this.buildSteps = new ArrayList<>();

        int distinct = 0;
        long total = 0;
        for (int i = 0; i < 256; i++) {
            if (frequencies[i] > 0) {
                distinct++;
                total += frequencies[i];
            }
        }
        this.distinctSymbols = distinct;
        this.totalFrequency = total;

        if (distinct == 0) {
            this.root = null;
            buildSteps.add(new BuildStep(0, "File is empty: no symbols to encode.",
                    Collections.emptyList(), null, null, null, true));
            return;
        }

        int creationSeq = 0;
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();

        // Add all distinct leaf nodes to priority queue
        for (int i = 0; i < 256; i++) {
            if (frequencies[i] > 0) {
                HuffmanNode leaf = new HuffmanNode(i, frequencies[i], creationSeq++);
                pq.offer(leaf);
            }
        }

        // Record Initial State (Step 0)
        List<HuffmanNode> initialSnapshot = new ArrayList<>(pq);
        buildSteps.add(new BuildStep(
                0,
                String.format("Initialized priority queue with %d distinct leaf symbol%s.",
                        distinct, distinct == 1 ? "" : "s"),
                initialSnapshot,
                null,
                null,
                null,
                distinct == 1
        ));

        // Edge case: Exactly 1 distinct symbol
        if (distinct == 1) {
            HuffmanNode singleLeaf = pq.poll();
            // Create root parent with single leaf as left child (code "0")
            this.root = new HuffmanNode(singleLeaf, creationSeq++);
            symbolToCode.put(singleLeaf.getSymbol(), "0");
            codeToSymbol.put("0", singleLeaf.getSymbol());

            buildSteps.add(new BuildStep(
                    1,
                    String.format("Single distinct symbol %s: assigned 1-bit code '0'.",
                            singleLeaf.getDisplayLabel()),
                    Collections.singletonList(this.root),
                    singleLeaf,
                    null,
                    this.root,
                    true
            ));
            return;
        }

        // Greedy Huffman Tree Construction
        int stepNum = 1;
        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();

            HuffmanNode parent = new HuffmanNode(left, right, creationSeq++);
            pq.offer(parent);

            List<HuffmanNode> currentSnapshot = new ArrayList<>(pq);
            boolean isFinal = (pq.size() == 1);
            String desc = String.format("Step %d: Dequeued [%s (freq=%d)] and [%s (freq=%d)]. Created parent (freq=%d).",
                    stepNum,
                    left.getDisplayLabel(), left.getFrequency(),
                    right.getDisplayLabel(), right.getFrequency(),
                    parent.getFrequency());

            buildSteps.add(new BuildStep(
                    stepNum++,
                    desc,
                    currentSnapshot,
                    left,
                    right,
                    parent,
                    isFinal
            ));
        }

        this.root = pq.poll();

        // Generate Prefix Codes
        generateCodes(this.root, new StringBuilder());
    }

    private void generateCodes(HuffmanNode node, StringBuilder currentPath) {
        if (node == null) return;

        if (node.isLeaf()) {
            String code = currentPath.isEmpty() ? "0" : currentPath.toString();
            symbolToCode.put(node.getSymbol(), code);
            codeToSymbol.put(code, node.getSymbol());
            return;
        }

        // Left traversal -> '0'
        if (node.getLeft() != null) {
            currentPath.append('0');
            generateCodes(node.getLeft(), currentPath);
            currentPath.deleteCharAt(currentPath.length() - 1);
        }

        // Right traversal -> '1'
        if (node.getRight() != null) {
            currentPath.append('1');
            generateCodes(node.getRight(), currentPath);
            currentPath.deleteCharAt(currentPath.length() - 1);
        }
    }

    public HuffmanNode getRoot() {
        return root;
    }

    public Map<Integer, String> getSymbolToCode() {
        return Collections.unmodifiableMap(symbolToCode);
    }

    public Map<String, Integer> getCodeToSymbol() {
        return Collections.unmodifiableMap(codeToSymbol);
    }

    public String getCode(int symbol) {
        return symbolToCode.get(symbol);
    }

    public List<BuildStep> getBuildSteps() {
        return Collections.unmodifiableList(buildSteps);
    }

    public long[] getFrequencies() {
        return Arrays.copyOf(frequencies, frequencies.length);
    }

    public int getDistinctSymbols() {
        return distinctSymbols;
    }

    public long getTotalFrequency() {
        return totalFrequency;
    }

    /**
     * Finds the node path from root down to the leaf for the given symbol.
     * Useful for visualizer path highlighting.
     */
    public List<HuffmanNode> getNodePathForSymbol(int symbol) {
        List<HuffmanNode> path = new ArrayList<>();
        if (root == null) return path;
        findNodePath(root, symbol, path);
        return path;
    }

    private boolean findNodePath(HuffmanNode current, int targetSymbol, List<HuffmanNode> path) {
        if (current == null) return false;
        path.add(current);

        if (current.isLeaf() && current.getSymbol() == targetSymbol) {
            return true;
        }

        if (findNodePath(current.getLeft(), targetSymbol, path)) {
            return true;
        }
        if (findNodePath(current.getRight(), targetSymbol, path)) {
            return true;
        }

        path.remove(path.size() - 1);
        return false;
    }

    /**
     * Calculates Shannon Entropy H(X) = - Σ (P(x) * log2(P(x))).
     */
    public double calculateShannonEntropy() {
        if (totalFrequency == 0) return 0.0;
        double entropy = 0.0;
        for (int i = 0; i < 256; i++) {
            if (frequencies[i] > 0) {
                double p = (double) frequencies[i] / totalFrequency;
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }
        return entropy;
    }

    /**
     * Calculates the Average Code Length L = Σ (P(x) * len(code(x))).
     */
    public double calculateAverageCodeLength() {
        if (totalFrequency == 0) return 0.0;
        double avgLength = 0.0;
        for (Map.Entry<Integer, String> entry : symbolToCode.entrySet()) {
            int sym = entry.getKey();
            int codeLen = entry.getValue().length();
            double p = (double) frequencies[sym] / totalFrequency;
            avgLength += p * codeLen;
        }
        return avgLength;
    }

    /**
     * Calculates theoretical payload size in bits = Σ (frequency[i] * codeLen[i]).
     */
    public long calculateTotalPayloadBits() {
        long totalBits = 0;
        for (Map.Entry<Integer, String> entry : symbolToCode.entrySet()) {
            int sym = entry.getKey();
            int len = entry.getValue().length();
            totalBits += frequencies[sym] * len;
        }
        return totalBits;
    }
}
