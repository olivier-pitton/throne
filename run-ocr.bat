@echo off
REM OCR Batch Processing Script for Windows
REM Usage: run-ocr.bat <folder> [output.csv] [language]

set JAR_FILE=throne-1.0-SNAPSHOT-fat.jar

REM Check if JAR exists
if not exist "%JAR_FILE%" (
    echo ❌ JAR file not found: %JAR_FILE%
    echo Please run: mvn clean package
    exit /b 1
)

REM Check if at least one argument is provided
if "%1"=="" (
    echo OCR Batch Processing
    echo Usage: %0 ^<folder^> [output.csv] [language]
    echo.
    echo Examples:
    echo   %0 .\images
    echo   %0 .\images results.csv
    echo   %0 .\images results.csv fra
    exit /b 1
)

REM Run the JAR with provided arguments
echo 🚀 Running OCR Batch Processing...
java -jar "%JAR_FILE%" %*
