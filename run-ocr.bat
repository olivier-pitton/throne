@echo off
REM OCR Batch Processing Script for Windows
REM Usage: run-ocr.bat <folder> <language> <color> [guild] [output.csv] [date]

java -jar throne-1.0-SNAPSHOT-fat.jar %*
