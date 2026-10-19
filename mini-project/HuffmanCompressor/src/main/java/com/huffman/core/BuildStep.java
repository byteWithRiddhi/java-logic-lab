package com.huffman.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Captures a single step in the Huffman tree construction process.
 * Used for DSA learning, step-by-step playback, and visualization of the
 * priority queue merge operations.
 */
public class BuildStep {
    private final int stepNumber;
    private final String description;
    private final List<HuffmanNode> queueSnapshot;
    private final HuffmanNode leftMerged;
    private final HuffmanNode rightMerged;
    private final HuffmanNode parentCreated;
    private final boolean isFinalStep;

    public BuildStep(int stepNumber,
                     String description,
                     List<HuffmanNode> queueSnapshot,
                     HuffmanNode leftMerged,
                     HuffmanNode rightMerged,
                     HuffmanNode parentCreated,
                     boolean isFinalStep) {
        this.stepNumber = stepNumber;
        this.description = description;
        this.queueSnapshot = Collections.unmodifiableList(new ArrayList<>(queueSnapshot));
        this.leftMerged = leftMerged;
        this.rightMerged = rightMerged;
        this.parentCreated = parentCreated;
        this.isFinalStep = isFinalStep;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public String getDescription() {
        return description;
    }

    public List<HuffmanNode> getQueueSnapshot() {
        return queueSnapshot;
    }

    public HuffmanNode getLeftMerged() {
        return leftMerged;
    }

    public HuffmanNode getRightMerged() {
        return rightMerged;
    }

    public HuffmanNode getParentCreated() {
        return parentCreated;
    }

    public boolean isFinalStep() {
        return isFinalStep;
    }

    @Override
    public String toString() {
        return String.format("Step %d: %s (Queue size: %d)", stepNumber, description, queueSnapshot.size());
    }
}
