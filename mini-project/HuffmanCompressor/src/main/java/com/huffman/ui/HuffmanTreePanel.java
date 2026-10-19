package com.huffman.ui;

import com.huffman.core.HuffmanNode;
import com.huffman.core.HuffmanTree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/**
 * Interactive 2D visualization panel for the Huffman Binary Tree.
 * Supports pan, zoom, auto-layout, node hover tooltips, and path highlighting.
 */
public class HuffmanTreePanel extends JPanel {
    private HuffmanTree currentTree;
    private HuffmanNode highlightedTargetNode;
    private final Set<HuffmanNode> highlightedNodes = new HashSet<>();
    private final Set<String> highlightedEdges = new HashSet<>();

    // Pan and Zoom
    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private Point dragStart;

    // Node layout cache: node -> bounding box in world coordinates
    private final Map<HuffmanNode, Rectangle> nodeBounds = new HashMap<>();
    private final Map<HuffmanNode, Integer> subtreeWidths = new HashMap<>();

    private static final int NODE_WIDTH = 64;
    private static final int NODE_HEIGHT = 38;
    private static final int LEVEL_HEIGHT = 70;
    private static final int MIN_HORIZONTAL_SPACING = 24;

    public HuffmanTreePanel() {
        setBackground(Theme.TREE_BG);
        setLayout(new BorderLayout());

        // Mouse listeners for pan & zoom
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
                    dragStart = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragStart = null;
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    double dx = e.getX() - dragStart.x;
                    double dy = e.getY() - dragStart.y;
                    offsetX += dx;
                    offsetY += dy;
                    dragStart = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double zoomFactor = e.getWheelRotation() < 0 ? 1.15 : 0.85;
                zoomAtPoint(zoomFactor, e.getPoint());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateHoverTooltip(e.getPoint());
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);

        // Control Toolbar
        JPanel controlToolbar = createControlToolbar();
        add(controlToolbar, BorderLayout.NORTH);
    }

    private JPanel createControlToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        toolbar.setOpaque(false);

        JButton btnZoomIn = createMiniButton("+ Zoom In", () -> zoomAtCenter(1.2));
        JButton btnZoomOut = createMiniButton("- Zoom Out", () -> zoomAtCenter(0.8));
        JButton btnReset = createMiniButton("Reset", this::resetView);
        JButton btnFit = createMiniButton("Fit to View", this::fitToView);

        toolbar.add(createLegend());
        toolbar.add(btnZoomIn);
        toolbar.add(btnZoomOut);
        toolbar.add(btnReset);
        toolbar.add(btnFit);

        return toolbar;
    }

    private JPanel createLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        legend.setOpaque(false);

        legend.add(createLegendItem(Theme.NODE_ROOT, "Root"));
        legend.add(createLegendItem(Theme.NODE_INTERNAL, "Internal"));
        legend.add(createLegendItem(Theme.NODE_LEAF, "Leaf"));
        legend.add(createLegendItem(Theme.NODE_HIGHLIGHT, "Active Path"));
        return legend;
    }

    private JPanel createLegendItem(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(color);
        dot.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        lbl.setFont(Theme.FONT_SMALL);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    private JButton createMiniButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_SMALL);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setBackground(Theme.BG_CARD);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_DEFAULT, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    public void setTree(HuffmanTree tree) {
        this.currentTree = tree;
        this.highlightedTargetNode = null;
        this.highlightedNodes.clear();
        this.highlightedEdges.clear();
        calculateLayout();
        fitToView();
        repaint();
    }

    /**
     * Highlights the path to a specific symbol in the tree.
     */
    public void highlightSymbol(int symbol) {
        highlightedNodes.clear();
        highlightedEdges.clear();
        highlightedTargetNode = null;

        if (currentTree == null || currentTree.getRoot() == null) {
            repaint();
            return;
        }

        List<HuffmanNode> path = currentTree.getNodePathForSymbol(symbol);
        if (!path.isEmpty()) {
            highlightedNodes.addAll(path);
            highlightedTargetNode = path.get(path.size() - 1);
            for (int i = 0; i < path.size() - 1; i++) {
                highlightedEdges.add(getEdgeKey(path.get(i), path.get(i + 1)));
            }

            // Center view on highlighted leaf
            Rectangle targetRect = nodeBounds.get(highlightedTargetNode);
            if (targetRect != null) {
                centerOnPoint(targetRect.getCenterX(), targetRect.getCenterY());
            }
        }
        repaint();
    }

    public void clearHighlight() {
        highlightedNodes.clear();
        highlightedEdges.clear();
        highlightedTargetNode = null;
        repaint();
    }

    private String getEdgeKey(HuffmanNode parent, HuffmanNode child) {
        return System.identityHashCode(parent) + "->" + System.identityHashCode(child);
    }

    private void calculateLayout() {
        nodeBounds.clear();
        subtreeWidths.clear();
        if (currentTree == null || currentTree.getRoot() == null) return;

        // Pass 1: calculate width of each subtree
        computeSubtreeWidths(currentTree.getRoot());

        // Pass 2: assign (x, y) coordinates
        int totalWidth = subtreeWidths.getOrDefault(currentTree.getRoot(), 200);
        assignNodePositions(currentTree.getRoot(), totalWidth / 2, 40, totalWidth);
    }

    private int computeSubtreeWidths(HuffmanNode node) {
        if (node == null) return 0;
        if (node.isLeaf()) {
            int w = NODE_WIDTH + MIN_HORIZONTAL_SPACING;
            subtreeWidths.put(node, w);
            return w;
        }

        int leftWidth = (node.getLeft() != null) ? computeSubtreeWidths(node.getLeft()) : 0;
        int rightWidth = (node.getRight() != null) ? computeSubtreeWidths(node.getRight()) : 0;

        int totalW = Math.max(NODE_WIDTH + MIN_HORIZONTAL_SPACING, leftWidth + rightWidth);
        subtreeWidths.put(node, totalW);
        return totalW;
    }

    private void assignNodePositions(HuffmanNode node, int x, int y, int availableWidth) {
        if (node == null) return;

        nodeBounds.put(node, new Rectangle(x - NODE_WIDTH / 2, y, NODE_WIDTH, NODE_HEIGHT));

        HuffmanNode left = node.getLeft();
        HuffmanNode right = node.getRight();

        if (left != null && right != null) {
            int leftW = subtreeWidths.getOrDefault(left, NODE_WIDTH + MIN_HORIZONTAL_SPACING);
            int rightW = subtreeWidths.getOrDefault(right, NODE_WIDTH + MIN_HORIZONTAL_SPACING);

            int leftCenterX = x - rightW / 2;
            int rightCenterX = x + leftW / 2;

            assignNodePositions(left, leftCenterX, y + LEVEL_HEIGHT, leftW);
            assignNodePositions(right, rightCenterX, y + LEVEL_HEIGHT, rightW);
        } else if (left != null) {
            assignNodePositions(left, x, y + LEVEL_HEIGHT, availableWidth);
        } else if (right != null) {
            assignNodePositions(right, x, y + LEVEL_HEIGHT, availableWidth);
        }
    }

    public void fitToView() {
        if (nodeBounds.isEmpty()) {
            resetView();
            return;
        }

        Rectangle worldBounds = null;
        for (Rectangle r : nodeBounds.values()) {
            if (worldBounds == null) {
                worldBounds = new Rectangle(r);
            } else {
                worldBounds.add(r);
            }
        }

        if (worldBounds == null) return;

        int pad = 50;
        worldBounds.grow(pad, pad);

        int viewW = Math.max(100, getWidth());
        int viewH = Math.max(100, getHeight());

        double scaleX = (double) viewW / worldBounds.width;
        double scaleY = (double) viewH / worldBounds.height;
        zoom = Math.min(Math.min(scaleX, scaleY), 1.2);
        zoom = Math.max(zoom, 0.05);

        offsetX = (viewW - worldBounds.width * zoom) / 2.0 - worldBounds.x * zoom;
        offsetY = (viewH - worldBounds.height * zoom) / 2.0 - worldBounds.y * zoom;

        repaint();
    }

    public void resetView() {
        zoom = 1.0;
        offsetX = 50;
        offsetY = 50;
        repaint();
    }

    private void zoomAtCenter(double factor) {
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        zoomAtPoint(factor, center);
    }

    private void zoomAtPoint(double factor, Point p) {
        double newZoom = Math.max(0.04, Math.min(3.0, zoom * factor));
        double ratio = newZoom / zoom;
        offsetX = p.x - ratio * (p.x - offsetX);
        offsetY = p.y - ratio * (p.y - offsetY);
        zoom = newZoom;
        repaint();
    }

    private void centerOnPoint(double worldX, double worldY) {
        offsetX = (getWidth() / 2.0) - worldX * zoom;
        offsetY = (getHeight() / 2.0) - worldY * zoom;
    }

    private void updateHoverTooltip(Point mousePoint) {
        if (currentTree == null || nodeBounds.isEmpty()) {
            setToolTipText(null);
            return;
        }

        Point2D worldPoint = screenToWorld(mousePoint);
        for (Map.Entry<HuffmanNode, Rectangle> entry : nodeBounds.entrySet()) {
            if (entry.getValue().contains(worldPoint)) {
                HuffmanNode n = entry.getKey();
                if (n.isLeaf()) {
                    String code = currentTree.getCode(n.getSymbol());
                    setToolTipText(String.format("<html><b>Leaf Node:</b> %s<br/><b>Dec:</b> %d &nbsp; <b>Hex:</b> 0x%02X<br/><b>Frequency:</b> %d<br/><b>Code:</b> <code>%s</code> (%d bits)</html>",
                            n.getDisplayLabel(), n.getSymbol(), n.getSymbol(), n.getFrequency(), code != null ? code : "None", code != null ? code.length() : 0));
                } else {
                    setToolTipText(String.format("<html><b>Internal Node</b><br/><b>Combined Frequency:</b> %d<br/><b>Subtree Leaves:</b> %d<br/><b>Subtree Height:</b> %d</html>",
                            n.getFrequency(), n.getLeafCount(), n.getHeight()));
                }
                return;
            }
        }
        setToolTipText(null);
    }

    private Point2D screenToWorld(Point p) {
        return new Point2D.Double((p.x - offsetX) / zoom, (p.y - offsetY) / zoom);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw background grid lines
        drawGrid(g2);

        if (currentTree == null || currentTree.getRoot() == null) {
            drawEmptyState(g2);
            g2.dispose();
            return;
        }

        // Apply zoom and pan transform
        AffineTransform tx = new AffineTransform();
        tx.translate(offsetX, offsetY);
        tx.scale(zoom, zoom);
        g2.transform(tx);

        // Pass 1: Draw Edges
        drawEdges(g2, currentTree.getRoot());

        // Pass 2: Draw Nodes
        drawNodes(g2, currentTree.getRoot());

        g2.dispose();
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(Theme.TREE_GRID);
        int gridSize = (int) (40 * zoom);
        if (gridSize < 15) return; // avoid dense grid moire effect

        int startX = (int) (offsetX % gridSize);
        int startY = (int) (offsetY % gridSize);

        for (int x = startX; x < getWidth(); x += gridSize) {
            g2.drawLine(x, 0, x, getHeight());
        }
        for (int y = startY; y < getHeight(); y += gridSize) {
            g2.drawLine(0, y, getWidth(), y);
        }
    }

    private void drawEmptyState(Graphics2D g2) {
        g2.setColor(Theme.TEXT_MUTED);
        g2.setFont(Theme.FONT_TITLE);
        String msg = "No Huffman Tree Loaded";
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(msg)) / 2;
        int y = getHeight() / 2 - 20;
        g2.drawString(msg, x, y);

        g2.setFont(Theme.FONT_REGULAR);
        g2.setColor(Theme.TEXT_SECONDARY);
        String sub = "Select or analyze a file on the left to visualize its binary Huffman tree.";
        FontMetrics fmSub = g2.getFontMetrics();
        int subX = (getWidth() - fmSub.stringWidth(sub)) / 2;
        g2.drawString(sub, subX, y + 30);
    }

    private void drawEdges(Graphics2D g2, HuffmanNode node) {
        if (node == null) return;

        Rectangle parentRect = nodeBounds.get(node);
        if (parentRect == null) return;

        int px = (int) parentRect.getCenterX();
        int py = (int) parentRect.getMaxY();

        // Left Edge (Bit '0')
        if (node.getLeft() != null) {
            Rectangle leftRect = nodeBounds.get(node.getLeft());
            if (leftRect != null) {
                int cx = (int) leftRect.getCenterX();
                int cy = (int) leftRect.getMinY();
                boolean isHigh = highlightedEdges.contains(getEdgeKey(node, node.getLeft()));
                drawEdgeWithBit(g2, px, py, cx, cy, "0", Theme.BIT_0_COLOR, isHigh);
                drawEdges(g2, node.getLeft());
            }
        }

        // Right Edge (Bit '1')
        if (node.getRight() != null) {
            Rectangle rightRect = nodeBounds.get(node.getRight());
            if (rightRect != null) {
                int cx = (int) rightRect.getCenterX();
                int cy = (int) rightRect.getMinY();
                boolean isHigh = highlightedEdges.contains(getEdgeKey(node, node.getRight()));
                drawEdgeWithBit(g2, px, py, cx, cy, "1", Theme.BIT_1_COLOR, isHigh);
                drawEdges(g2, node.getRight());
            }
        }
    }

    private void drawEdgeWithBit(Graphics2D g2, int x1, int y1, int x2, int y2, String bit, Color bitColor, boolean highlighted) {
        // Draw line
        if (highlighted) {
            g2.setColor(Theme.EDGE_HIGHLIGHT);
            g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        } else {
            g2.setColor(Theme.EDGE_NORMAL);
            g2.setStroke(new BasicStroke(1.8f));
        }
        g2.drawLine(x1, y1, x2, y2);

        // Draw bit pill badge in middle of edge
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        int badgeSize = 18;

        g2.setColor(highlighted ? Theme.EDGE_HIGHLIGHT : bitColor);
        g2.fillOval(midX - badgeSize / 2, midY - badgeSize / 2, badgeSize, badgeSize);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        int tx = midX - fm.stringWidth(bit) / 2;
        int ty = midY + fm.getAscent() / 2 - 1;
        g2.drawString(bit, tx, ty);
    }

    private void drawNodes(Graphics2D g2, HuffmanNode node) {
        if (node == null) return;

        Rectangle r = nodeBounds.get(node);
        if (r != null) {
            boolean isHighlighted = highlightedNodes.contains(node);
            boolean isTarget = (node == highlightedTargetNode);

            Color bgColor;
            Color borderColor;

            if (isTarget) {
                bgColor = Theme.NODE_HIGHLIGHT;
                borderColor = Color.WHITE;
            } else if (isHighlighted) {
                bgColor = Theme.WARNING_AMBER;
                borderColor = Color.WHITE;
            } else if (node.isLeaf()) {
                bgColor = Theme.NODE_LEAF;
                borderColor = new Color(0x34, 0xD3, 0x99);
            } else if (node == currentTree.getRoot()) {
                bgColor = Theme.NODE_ROOT;
                borderColor = Theme.CYAN_ACCENT;
            } else {
                bgColor = Theme.NODE_INTERNAL;
                borderColor = Theme.BORDER_DEFAULT;
            }

            // Glow for highlighted nodes
            if (isHighlighted || isTarget) {
                g2.setColor(new Color(251, 191, 36, 90));
                g2.fillRoundRect(r.x - 4, r.y - 4, r.width + 8, r.height + 8, 16, 16);
            }

            // Fill node
            g2.setColor(bgColor);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);

            // Border
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(isHighlighted ? 2.5f : 1.2f));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);

            // Text
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.FONT_SMALL);
            FontMetrics fm = g2.getFontMetrics();

            if (node.isLeaf()) {
                String symLabel = node.getDisplayLabel();
                String freqLabel = "f=" + node.getFrequency();

                int sx = r.x + (r.width - fm.stringWidth(symLabel)) / 2;
                int sy = r.y + 15;
                g2.drawString(symLabel, sx, sy);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                FontMetrics fmSmall = g2.getFontMetrics();
                int fx = r.x + (r.width - fmSmall.stringWidth(freqLabel)) / 2;
                int fy = r.y + 30;
                g2.drawString(freqLabel, fx, fy);
            } else {
                String label = (node == currentTree.getRoot()) ? "Root" : "Σ";
                String freq = String.valueOf(node.getFrequency());

                int lx = r.x + (r.width - fm.stringWidth(label)) / 2;
                int ly = r.y + 15;
                g2.drawString(label, lx, ly);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                FontMetrics fmSmall = g2.getFontMetrics();
                int fx = r.x + (r.width - fmSmall.stringWidth(freq)) / 2;
                int fy = r.y + 30;
                g2.drawString(freq, fx, fy);
            }
        }

        drawNodes(g2, node.getLeft());
        drawNodes(g2, node.getRight());
    }
}
