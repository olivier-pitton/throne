#!/bin/bash

# OCR Batch Processing Script
# Usage: ./run-ocr.sh <folder> <language> <color> [output.csv] [date]

JAR_FILE="throne-1.0-SNAPSHOT-fat.jar"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    echo "Please run: mvn clean package"
    exit 1
fi

# Check if at least three arguments are provided
if [ $# -lt 3 ]; then
    echo "OCR Batch Processing"
    echo "Usage: $0 <folder> <language> <color> [output.csv] [date]"
    echo ""
    echo "Parameters:"
    echo "  folder   - Path to folder containing images (required)"
    echo "  language - OCR language code (required)"
    echo "  color    - Color filter: 'y' (yellow) or 'r' (red) (required)"
    echo ""
    echo "Examples:"
    echo "  $0 ./images eng y"
    echo "  $0 ./images fra r results.csv"
    echo "  $0 ./images eng y results.csv 2025-09-06"
    exit 1
fi

# Run the JAR with provided arguments
echo "🚀 Running OCR Batch Processing..."
java -jar "$JAR_FILE" "$@"
