package com.huffman.ui;

import com.huffman.core.BuildStep;
import com.huffman.core.HuffmanNode;
import com.huffman.core.HuffmanTree;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Interactive Algorithm Explorer panel demonstrating the greedy step-by-step
 * priority queue merge operations and Huffman tree construction.
 */
public class AlgorithmExplorerPanel extends JPanel {
    private HuffmanTree currentTree;
    private List<BuildStep> steps;
    private int currentStepIndex = 0;
    private Timer playTimer;
    private boolean isPlaying = false;

    // Controls
    private JButton btnFirst;
    private JButton btnPrev;
    private JButton btnPlayPause;
    private JButton btnNext;
    private JButton btnLast;
    private JSlider stepSlider;
    private JLabel lblStepCounter;
    private JComboBox<String> speedCombo;

    // Display components
    private JLabel lblStepTitle;
    private JTextArea txtExplanation;
    private PriorityQueueCanvas pqCanvas;
    private HuffmanTreePanel stepTreeCanvas;

    public AlgorithmExplorerPanel() {
        setBackground(Theme.BG_PANEL);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // 1. Top Control Bar & Explanatory Card
        JPanel topContainer = new JPanel(new BorderLayout(0, 8));
        topContainer.setOpaque(false);

        topContainer.add(createPlaybackToolbar(), BorderLayout.NORTH);
        topContainer.add(createExplanationCard(), BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);

        // 2. Center Split: Priority Queue Snapshot + Partial Tree Visualization
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplit.setOpaque(false);
        centerSplit.setBorder(null);
        centerSplit.setDividerSize(6);

        pqCanvas = new PriorityQueueCanvas();
        stepTreeCanvas = new HuffmanTreePanel();

        JPanel pqWrapper = createTitledWrapper("Current Priority Queue (Min-Heap Elements)", pqCanvas);
        JPanel treeWrapper = createTitledWrapper("Resulting Subtree / Structure at Current Step", stepTreeCanvas);

        centerSplit.setTopComponent(pqWrapper);
        centerSplit.setBottomComponent(treeWrapper);
        centerSplit.setResizeWeight(0.35);

        add(centerSplit, BorderLayout.CENTER);

        // Setup timer for auto playback
        playTimer = new Timer(1000, e -> stepForward());
    }

    private JPanel createTitledWrapper(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(Theme.FONT_HEADER);
        lbl.setForeground(Theme.CYAN_ACCENT);
        lbl.setBorder(new EmptyBorder(0, 4, 4, 4));
        p.add(lbl, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private JPanel createPlaybackToolbar() {
        JPanel bar = Theme.createCardPanel();
        bar.setLayout(new BorderLayout(12, 0));

        // Left controls: playback buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttonsPanel.setOpaque(false);

        btnFirst = createIconButton("|◀", "First Step", () -> goToStep(0));
        btnPrev = createIconButton("◀", "Previous Step", this::stepBackward);
        btnPlayPause = Theme.createPrimaryButton("▶ Play");
        btnPlayPause.addActionListener(e -> togglePlayPause());
        btnNext = createIconButton("▶", "Next Step", this::stepForward);
        btnLast = createIconButton("▶|", "Final Step", () -> {
            if (steps != null && !steps.isEmpty()) {
                goToStep(steps.size() - 1);
            }
        });

        buttonsPanel.add(btnFirst);
        buttonsPanel.add(btnPrev);
        buttonsPanel.add(btnPlayPause);
        buttonsPanel.add(btnNext);
        buttonsPanel.add(btnLast);

        // Center: slider + step counter
        JPanel sliderPanel = new JPanel(new BorderLayout(8, 0));
        sliderPanel.setOpaque(false);

        lblStepCounter = new JLabel("Step 0 / 0");
        lblStepCounter.setFont(Theme.FONT_BOLD);
        lblStepCounter.setForeground(Theme.TEXT_PRIMARY);

        stepSlider = new JSlider(0, 0, 0);
        stepSlider.setOpaque(false);
        stepSlider.setEnabled(false);
        stepSlider.addChangeListener(e -> {
            if (stepSlider.getValueIsAdjusting() && !isPlaying) {
                goToStep(stepSlider.getValue());
            }
        });

        sliderPanel.add(lblStepCounter, BorderLayout.WEST);
        sliderPanel.add(stepSlider, BorderLayout.CENTER);

        // Right: speed combo
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightPanel.setOpaque(false);
        JLabel lblSpeed = new JLabel("Speed:");
        lblSpeed.setFont(Theme.FONT_SMALL);
        lblSpeed.setForeground(Theme.TEXT_SECONDARY);

        speedCombo = new JComboBox<>(new String[] { "0.5x (Slow)", "1.0x (Normal)", "2.0x (Fast)", "4.0x (Very Fast)" });
        speedCombo.setSelectedIndex(1);
        speedCombo.setBackground(Theme.BG_CARD);
        speedCombo.setForeground(Theme.TEXT_PRIMARY);
        speedCombo.addActionListener(e -> updateTimerDelay());

        rightPanel.add(lblSpeed);
        rightPanel.add(speedCombo);

        bar.add(buttonsPanel, BorderLayout.WEST);
        bar.add(sliderPanel, BorderLayout.CENTER);
        bar.add(rightPanel, BorderLayout.EAST);

        return bar;
    }

    private JPanel createExplanationCard() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout(0, 4));

        lblStepTitle = new JLabel("Greedy Tree Construction Steps");
        lblStepTitle.setFont(Theme.FONT_HEADER);
        lblStepTitle.setForeground(Theme.TEAL_PRIMARY);

        txtExplanation = new JTextArea("No tree loaded. Open or compress a file to explore the step-by-step algorithm.");
        txtExplanation.setFont(Theme.FONT_REGULAR);
        txtExplanation.setForeground(Theme.TEXT_PRIMARY);
        txtExplanation.setOpaque(false);
        txtExplanation.setEditable(false);
        txtExplanation.setLineWrap(true);
        txtExplanation.setWrapStyleWord(true);
        txtExplanation.setRows(2);

        card.add(lblStepTitle, BorderLayout.NORTH);
        card.add(txtExplanation, BorderLayout.CENTER);
        return card;
    }

    private JButton createIconButton(String text, String tooltip, Runnable action) {
        JButton btn = Theme.createSecondaryButton(text);
        btn.setToolTipText(tooltip);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    public void setTree(HuffmanTree tree) {
        this.currentTree = tree;
        if (playTimer.isRunning()) {
            playTimer.stop();
            isPlaying = false;
            btnPlayPause.setText("▶ Play");
        }

        if (tree != null) {
            this.steps = tree.getBuildSteps();
            stepSlider.setMaximum(Math.max(0, steps.size() - 1));
            stepSlider.setValue(0);
            stepSlider.setEnabled(steps.size() > 1);
            goToStep(0);
        } else {
            this.steps = null;
            stepSlider.setValue(0);
            stepSlider.setMaximum(0);
            stepSlider.setEnabled(false);
            lblStepCounter.setText("Step 0 / 0");
            lblStepTitle.setText("Greedy Tree Construction Steps");
            txtExplanation.setText("No tree loaded.");
            pqCanvas.setSnapshot(null, null, null);
            stepTreeCanvas.setTree(null);
        }
    }

    private void goToStep(int index) {
        if (steps == null || steps.isEmpty()) return;
        index = Math.max(0, Math.min(index, steps.size() - 1));
        currentStepIndex = index;
        stepSlider.setValue(index);
        lblStepCounter.setText(String.format("Step %d / %d", index, steps.size() - 1));

        BuildStep step = steps.get(index);
        lblStepTitle.setText(String.format("Step %d: %s", step.getStepNumber(),
                step.isFinalStep() ? "Final Huffman Root Node" : (index == 0 ? "Initial Min-Priority Queue" : "Merge Operation")));
        txtExplanation.setText(step.getDescription());

        // Update priority queue canvas
        pqCanvas.setSnapshot(step.getQueueSnapshot(), step.getLeftMerged(), step.getRightMerged());

        // Update tree canvas: if final step, show complete tree; otherwise, if a parent was created, show subtree
        if (index == steps.size() - 1 && currentTree != null) {
            stepTreeCanvas.setTree(currentTree);
        } else if (step.getParentCreated() != null) {
            // Build temporary tree or highlight
            stepTreeCanvas.setTree(currentTree);
            stepTreeCanvas.clearHighlight();
        }

        // Enable / disable buttons
        btnFirst.setEnabled(index > 0);
        btnPrev.setEnabled(index > 0);
        btnNext.setEnabled(index < steps.size() - 1);
        btnLast.setEnabled(index < steps.size() - 1);

        if (index == steps.size() - 1 && isPlaying) {
            togglePlayPause(); // Stop when reaching end
        }
    }

    private void stepForward() {
        if (steps != null && currentStepIndex < steps.size() - 1) {
            goToStep(currentStepIndex + 1);
        } else if (isPlaying) {
            togglePlayPause();
        }
    }

    private void stepBackward() {
        if (steps != null && currentStepIndex > 0) {
            goToStep(currentStepIndex - 1);
        }
    }

    private void togglePlayPause() {
        if (steps == null || steps.isEmpty()) return;

        if (isPlaying) {
            playTimer.stop();
            isPlaying = false;
            btnPlayPause.setText("▶ Play");
        } else {
            if (currentStepIndex >= steps.size() - 1) {
                goToStep(0);
            }
            updateTimerDelay();
            playTimer.start();
            isPlaying = true;
            btnPlayPause.setText("⏸ Pause");
        }
    }

    private void updateTimerDelay() {
        int idx = speedCombo.getSelectedIndex();
        int delay = switch (idx) {
            case 0 -> 2000;
            case 2 -> 500;
            case 3 -> 250;
            default -> 1000;
        };
        playTimer.setDelay(delay);
    }

    /**
     * Custom canvas showing the elements currently inside the Min-Priority Queue.
     */
    private static class PriorityQueueCanvas extends JPanel {
        private List<HuffmanNode> queueSnapshot;
        private HuffmanNode leftMerged;
        private HuffmanNode rightMerged;

        public PriorityQueueCanvas() {
            setBackground(Theme.BG_CARD);
            setPreferredSize(new Dimension(600, 100));
        }

        public void setSnapshot(List<HuffmanNode> snapshot, HuffmanNode left, HuffmanNode right) {
            this.queueSnapshot = snapshot;
            this.leftMerged = left;
            this.rightMerged = right;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (queueSnapshot == null || queueSnapshot.isEmpty()) {
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(Theme.FONT_REGULAR);
                g2.drawString("No priority queue state to display.", 20, 40);
                g2.dispose();
                return;
            }

            int cardWidth = 75;
            int cardHeight = 55;
            int gap = 8;
            int startX = 12;
            int startY = 16;

            int maxVisible = Math.min(queueSnapshot.size(), (getWidth() - startX) / (cardWidth + gap));
            if (maxVisible <= 0) maxVisible = 1;

            for (int i = 0; i < queueSnapshot.size(); i++) {
                int x = startX + i * (cardWidth + gap);
                if (x + cardWidth > getWidth() - 20 && i < queueSnapshot.size() - 1) {
                    // Draw overflow indicator
                    int remaining = queueSnapshot.size() - i;
                    g2.setColor(Theme.TEXT_MUTED);
                    g2.setFont(Theme.FONT_BOLD);
                    g2.drawString("+" + remaining + " more...", x, startY + 30);
                    break;
                }

                HuffmanNode node = queueSnapshot.get(i);
                boolean isMerged = (node == leftMerged || node == rightMerged);

                // Card background
                Color bg = isMerged ? Theme.WARNING_AMBER : (node.isLeaf() ? Theme.NODE_LEAF : Theme.NODE_INTERNAL);
                g2.setColor(bg);
                g2.fillRoundRect(x, startY, cardWidth, cardHeight, 8, 8);

                g2.setColor(isMerged ? Color.WHITE : Theme.BORDER_DEFAULT);
                g2.drawRoundRect(x, startY, cardWidth, cardHeight, 8, 8);

                // Priority Rank #1, #2, ...
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString("#" + (i + 1), x + 6, startY + 14);

                // Symbol or Subtree label
                g2.setFont(Theme.FONT_SMALL);
                String label = node.isLeaf() ? node.getDisplayLabel() : "Subtree";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, x + (cardWidth - fm.stringWidth(label)) / 2, startY + 30);

                // Frequency
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String freqStr = "f=" + node.getFrequency();
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(freqStr, x + (cardWidth - fm2.stringWidth(freqStr)) / 2, startY + 46);
            }

            g2.dispose();
        }
    }
}
