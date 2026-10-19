# Huffman Studio

**Lossless File Compression & Binary Tree Algorithm Visualizer**

A desktop application built in **Java 21** and **Java Swing** implementing the **Huffman Coding** algorithm from scratch, coupled with an interactive dark-themed visualizer, real-time KPI metrics, and a step-by-step Data Structures & Algorithms (DSA) explorer.

---

## 1. Overview & Key Capabilities

- **Lossless Compression & Decompression**: Full byte-level compression supporting arbitrary binary data (images, executables, archives) as well as ASCII and UTF-8 multilingual text.
- **Custom Binary File Format (`.huff`)**: Self-contained, portable binary file structure with magic bytes, format version, CRC-32 checksum, frequency metadata table, and packed bit payload.
- **Byte-for-Byte Fidelity**: Round-trip guaranteed (`decompress(compress(data)) == data`) with automated CRC-32 integrity validation.
- **Interactive Huffman Tree Visualizer**:
  - Pan and Zoom controls (mouse drag, wheel zoom, zoom buttons, Fit to View).
  - Clear node badges (Root in Cyan, Leaves in Emerald, Internal in Slate).
  - Edges labeled with Bit `0` (Teal) and Bit `1` (Purple).
  - Real-time **Path Highlighting** (clicking any symbol in the table highlights its path from the root).
- **DSA Algorithm Explorer (Step-by-Step)**:
  - Interactive playback (`Play`, `Pause`, `Step Next`, `Step Prev`, `First`, `Final`).
  - Speed adjustments (`0.5x` to `4.0x`).
  - Live inspection of the **Minimum Priority Queue** (min-heap) as it dequeues the two smallest nodes and merges them.
  - Plain-English explanation card for every merge operation.
- **Analysis & Code Table**:
  - Sortable by Symbol, Byte (Dec/Hex), Frequency, Percentage Share, Huffman Code, and Bit Length.
  - Real-time search filter (filter by character, hexadecimal byte, or bit code).
- **Performance & Entropy Dashboard**:
  - Original vs Compressed file sizes.
  - Actual Space Saved and Compression Ratio.
  - **Shannon Entropy** $H(X)$ vs **Average Code Length** $L$ calculation.
  - Clear warning badge when small-file metadata overhead causes expansion.
- **Multi-threaded & Responsive**:
  - All file I/O and bitstream encoding run asynchronously on background threads via `SwingWorker`.
  - Non-blocking UI with live progress bar and status feedback.

---

## 2. Architecture & Code Organization

```
HuffmanCompressor/
│
├── pom.xml                                   # Maven configuration (Java 21, FlatLaf, JUnit 5)
├── run.bat                                   # 1-click launch script
├── test.bat                                  # 1-click test runner
├── README.md                                 # Technical documentation & Viva guide
│
├── src/main/java/com/huffman/
│   │
│   ├── Main.java                             # Application entry point & FlatLaf Dark setup
│   │
│   ├── core/                                 # Pure Algorithm Layer (independent of GUI)
│   │   ├── HuffmanNode.java                  # Binary tree node with deterministic tie-breaking
│   │   ├── HuffmanTree.java                  # PriorityQueue greedy construction & code generator
│   │   ├── BuildStep.java                    # Snapshot of min-heap state for step-by-step playback
│   │   ├── BitWriter.java                    # Bit-packing stream writer (MSB-first)
│   │   ├── BitReader.java                    # Bit-unpacking stream reader with padding protection
│   │   ├── HuffmanEncoder.java               # Stream and array byte-to-bit encoder
│   │   └── HuffmanDecoder.java               # Tree-walking bit-to-byte decoder
│   │
│   ├── io/                                   # File Format & Serialization Layer
│   │   ├── CompressedFileFormat.java         # Format specification & constants ("HUFF")
│   │   ├── CompressionMetadata.java          # Header fields container
│   │   ├── CompressionStats.java             # File size metrics, ratios, entropy calculations
│   │   ├── FileCompressor.java               # Compression pipeline & .huff binary writer
│   │   └── FileDecompressor.java             # Decompression pipeline & CRC-32 integrity checker
│   │
│   └── ui/                                   # Dark Modern Swing User Interface
│       ├── Theme.java                        # Color palette, custom fonts, styled cards & buttons
│       ├── MainFrame.java                    # Main application window & layout orchestrator
│       ├── FileOperationsPanel.java          # Drag-and-drop file panel, progress bar, action buttons
│       ├── HuffmanTreePanel.java             # Interactive 2D pan/zoom tree visualizer
│       ├── AlgorithmExplorerPanel.java       # Step-by-step priority queue playback controls
│       ├── CodeTablePanel.java               # Searchable & sortable symbol and code table
│       └── StatisticsPanel.java              # KPI metric cards & overhead alert banner
│
└── src/test/java/com/huffman/
    ├── HuffmanCoreTest.java                  # Unit tests for all 10 edge cases & corruption checks
    └── HuffmanWorkflowIntegrationTest.java   # Headless end-to-end integration tests
```

---

## 3. Custom Compressed File Format Specification (`.huff`)

The `.huff` format is completely self-contained. Any saved `.huff` archive can be opened in a fresh session and decompressed without requiring the original tree in memory.

### Binary Layout Diagram

```
+-------------------------------------------------------------------+
| Field Name                 | Size (Bytes) | Data Type & Notes     |
+-------------------------------------------------------------------+
| Magic Bytes ("HUFF")       | 4 bytes      | 0x48, 0x55, 0x46, 0x46|
| Format Version             | 1 byte       | 0x01 (Current: v1)    |
| Reserved Flags             | 1 byte       | 0x00                  |
| Original File Size         | 8 bytes      | int64 (big-endian)    |
| Original CRC-32 Checksum   | 8 bytes      | int64 (big-endian)    |
| Distinct Symbol Count (K)  | 2 bytes      | uint16 (0 .. 256)     |
+-------------------------------------------------------------------+
| Frequency Table (K entries)| K * 9 bytes  | For each symbol:      |
|   - Symbol Byte            | 1 byte       | uint8 (0 .. 255)      |
|   - Frequency Count        | 8 bytes      | int64 (big-endian)    |
+-------------------------------------------------------------------+
| Payload Total Bit Count    | 8 bytes      | int64 (big-endian)    |
+-------------------------------------------------------------------+
| Packed Bit Payload         | ceil(Bits/8) | MSB-to-LSB packed     |
| Trailing Padding           | 0 to 7 bits  | Discarded via BitCount|
+-------------------------------------------------------------------+
```

### Integrity & Safety Features:
1. **Magic Signature Verification**: Immediately rejects foreign or incompatible files.
2. **Frequency Table Cross-Check**: Validates that $\sum \text{frequency}_i == \text{OriginalFileSize}$. Any mismatched frequency sum indicates a truncated or altered header.
3. **Truncation Guard**: Checks that archive length $\ge \text{HeaderSize} + \lceil \text{PayloadBits} / 8 \rceil$.
4. **CRC-32 Checksum Verification**: Computes the CRC-32 hash over the decoded stream; if even a single bit was flipped in the payload, decompression fails immediately with a descriptive error.
5. **Exact Length Termination**: The decoder stops exactly when `OriginalFileSize` bytes have been emitted, guaranteeing trailing padding bits in the final byte are safely ignored.

---

## 4. DSA Theory & Interview / Viva Guide

Use this section to prepare for technical interviews, MCA viva questions, or system design reviews:

### Q1: What makes Huffman Coding an "optimal prefix code"?
A prefix code (also called a prefix-free code) is a code in which no codeword is a prefix of any other codeword. This property ensures that bit sequences can be decoded instantaneously without lookahead or delimiters. Huffman Coding is optimal because among all symbol-by-symbol prefix codes, it produces the minimal expected weighted codeword length for a given set of symbol frequencies.

### Q2: What is the Greedy Choice Property in Huffman Coding?
At every step of tree construction, Huffman’s algorithm selects the two subtrees with the absolute lowest frequencies and merges them into a single parent node. By placing less frequent symbols deeper in the binary tree (yielding longer codes) and more frequent symbols closer to the root (yielding shorter codes), the algorithm minimizes:
$$L = \sum_{i=1}^{K} P(s_i) \cdot \text{length}(code(s_i))$$

### Q3: What is the Time and Space Complexity?
- **Frequency Analysis**: $O(N)$ time where $N$ is the total number of bytes in the file.
- **Priority Queue Initialization**: $O(K \log K)$ where $K \le 256$ is the number of distinct byte symbols.
- **Tree Construction**: Merges $K-1$ times. Each merge takes 2 `poll()` operations ($2 \log K$) and 1 `offer()` operation ($\log K$), totaling $O(K \log K)$.
- **Code Generation**: $O(K)$ depth-first search traversal.
- **Bitstream Encoding**: $O(N)$ time reading each byte and outputting its variable-length bit sequence.
- **Total Time**: $O(N + K \log K)$. Since $K \le 256$ is a constant, tree construction takes $\le 256 \log_2(256) \approx 2048$ operations, meaning the algorithm runs in strictly **linear $O(N)$ time** relative to file size!
- **Space Complexity**: $O(K)$ space to store the tree and code tables in memory (at most 511 nodes). Streaming I/O uses fixed 64 KB buffers, allowing files of several gigabytes to be compressed without Out-Of-Memory errors.

### Q4: What is Shannon Entropy and how does Huffman compare?
Claude Shannon proved that the absolute theoretical limit of lossless data compression for an independent, identically distributed source is given by Shannon Entropy:
$$H(X) = -\sum_{i=1}^{K} P(s_i) \log_2 P(s_i)$$
Huffman coding is provably bounded by:
$$H(X) \le L_{\text{Huffman}} < H(X) + 1$$
Where $L_{\text{Huffman}}$ is the average bits per symbol. When symbol probabilities are negative powers of two ($2^{-k}$), Huffman achieves the exact Shannon entropy bound!

### Q5: Why can a compressed file sometimes be larger than the original?
Huffman coding must store its decoding model (the frequency table / tree metadata) inside the file header so the decompressor can work independently. For very small files (e.g., less than 300 bytes), the header overhead (~100 to 400 bytes) exceeds the bit savings in the payload. Furthermore, if a file has high entropy (e.g., already compressed JPEG, MP3, ZIP, or encrypted data), symbol frequencies are uniform ($P(s_i) \approx 1/256$), meaning each symbol requires $\approx 8$ bits, yielding 0% payload reduction plus header expansion.

### Q6: How does the implementation handle edge cases?
- **Empty File (0 bytes)**: Tree is null; header records $K=0$; 0 bits payload; decompressor restores an empty 0-byte file without division by zero.
- **Single Unique Byte (e.g. "AAAA")**: A standard binary tree needs 2 children to split '0' and '1'. Our implementation constructs a root node with the single leaf as its left child, deterministically assigning the 1-bit code `"0"`.
- **All 256 Byte Values Present**: The priority queue initializes with 256 leaves; tree height reaches up to 16 levels without stack overflow.

---

## 5. How to Build and Run

### Prerequisites
- **JDK 21** or later installed (`java -version`, `javac -version`)
- **Apache Maven 3.8+** (installed and available on PATH)

### Option A: Using the Convenience Script (Windows)
Double-click or run from PowerShell / Command Prompt:
```cmd
run.bat
```

### Option B: Using Maven
To compile and launch the GUI:
```cmd
mvn clean compile exec:java
```

### Option C: Standalone Executable Uber JAR
To build the standalone single-file JAR containing all dependencies:
```cmd
mvn clean package
java -jar target\HuffmanStudio.jar
```

---

## 6. Running Automated Tests

To execute the automated JUnit 5 test suite covering all algorithm edge cases, bitstream readers, and corruption checks:

```cmd
mvn test
```
Or use the provided script:
```cmd
test.bat
```

### Verified Test Suite:
1. `testEmptyFile`: 0-byte file compression, header validation, and empty restoration.
2. `testSingleByteFile`: 1-byte file edge case.
3. `testRepeatedBytes`: 5,000 identical bytes (1-unique-symbol prefix code handling).
4. `testPrintableAscii`: Standard English prose with punctuation.
5. `testMultilingualUtf8`: Multilingual UTF-8 text (English, Hindi, Japanese, French, Arabic, German, and modern emojis).
6. `testAll256ByteValues`: Comprehensive binary alphabet test covering all bytes $0 \dots 255$.
7. `testRandomBinary`: 16 KB pseudorandom binary payload.
8. `testLargerFile`: 1.5 MB mixed binary and text payload.
9. `testCorruptedFiles`:
   - Corrupted magic bytes detection.
   - Truncated archive detection.
   - Flipped bit payload CRC-32 integrity failure detection.
10. `testBuildSteps`: Min-heap progression recording and prefix-code verification.
11. `testEndToEndWorkflowWithUI`: Headless end-to-end integration test of UI panels.
12. `testHeaderInspection`: Standalone header inspection without decoding.
