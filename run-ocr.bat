@echo off
REM OCR Batch Processing Script for Windows
REM Usage: run-ocr.bat <folder> <language> <color> [output.csv] [date]

set JAR_FILE=throne-1.0-SNAPSHOT-fat.jar

REM Check if JAR exists
if not exist "%JAR_FILE%" (
    echo ❌ JAR file not found: %JAR_FILE%
    echo Please run: mvn clean package
    exit /b 1
)

REM Check if at least three arguments are provided
if "%1"=="" (
    echo OCR Batch Processing
    echo Usage: %0 ^<folder^> ^<language^> ^<color^> [output.csv] [date]
    echo.
    echo Parameters:
    echo   folder   - Path to folder containing images (required)
    echo   language - OCR language code (required)
    echo   color    - Color filter: 'y' (yellow) or 'r' (red) (required)
    echo.
    echo Examples:
    echo   %0 .\images eng y
    echo   %0 .\images fra r results.csv
    echo   %0 .\images eng y results.csv 2025-09-06
    exit /b 1
)
if "%2"=="" (
    echo Error: Language parameter is required
    echo Usage: %0 ^<folder^> ^<language^> ^<color^> [output.csv] [date]
    exit /b 1
)
if "%3"=="" (
    echo Error: Color parameter is required
    echo Usage: %0 ^<folder^> ^<language^> ^<color^> [output.csv] [date]
    exit /b 1
)

REM Run the JAR with provided arguments
echo 🚀 Running OCR Batch Processing...
java -jar "%JAR_FILE%" %*
