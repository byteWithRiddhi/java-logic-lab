@echo off
setlocal
echo ===================================================
echo Starting Huffman Studio (Java 21 Desktop GUI)
echo ===================================================

if exist "target\HuffmanStudio.jar" (
    echo Launching packaged standalone JAR...
    java -jar "target\HuffmanStudio.jar"
) else (
    echo Packaging not found. Launching via Maven...
    mvn compile exec:java
)

endlocal
