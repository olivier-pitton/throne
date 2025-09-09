#!/bin/bash

# OCR Batch Processing Script
# Usage: ./run-ocr.sh <folder> <language> <color> [guild] [output.csv] [date]

java -jar throne-1.0-SNAPSHOT-fat.jar "$@"
