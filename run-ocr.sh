#!/bin/bash

# OCR Batch Processing Script
# Usage: ./run-ocr.sh <folder> [output.csv] [language]

JAR_FILE="throne-1.0-SNAPSHOT-fat.jar"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    echo "Please run: mvn clean package"
    exit 1
fi

# Check if at least one argument is provided
if [ $# -lt 1 ]; then
    echo "OCR Batch Processing"
    echo "Usage: $0 <folder> [output.csv] [language]"
    echo ""
    echo "Examples:"
    echo "  $0 ./images"
    echo "  $0 ./images results.csv"
    echo "  $0 ./images results.csv fra"
    exit 1
fi

# Run the JAR with provided arguments
echo "🚀 Running OCR Batch Processing..."
java -jar "$JAR_FILE" "$@"
