# Throne OCR

Java OCR application using Tesseract for batch image processing with configurable language support.

## Quick Start

```bash
# Build runnable JAR
mvn clean package

# Run with French OCR
java -jar target/throne-1.0-SNAPSHOT.jar fra

# Run with English OCR on custom folder
java -jar target/throne-1.0-SNAPSHOT.jar eng /path/to/images output.csv
```

## Prerequisites

- **Java 11+**
- **Maven 3.6+** 
- **Tesseract OCR** installed and in PATH

### Install Tesseract
```bash
# macOS
brew install tesseract tesseract-lang

# Ubuntu/Debian
sudo apt-get install tesseract-ocr tesseract-ocr-fra tesseract-ocr-deu

# Windows
# Download from https://github.com/UB-Mannheim/tesseract/wiki
```

## Usage

```bash
java -jar throne.jar <language> [input_folder] [output_csv]
```

**Parameters:**
- `language` - OCR language code (eng, fra, deu, spa, ita, por, etc.)
- `input_folder` - Image directory (default: `img`)
- `output_csv` - Output file (default: `output.csv`)

**Examples:**
```bash
java -jar throne.jar fra                           # French, default paths
java -jar throne.jar eng ./images                  # English, custom input
java -jar throne.jar deu ./images results.csv      # German, custom paths
```

## What It Does

1. **Scans images** for pipe-separated tabular data
2. **Filters rows** containing color words (rouge, jaune, yellow, red, etc.)
3. **Extracts columns** starting from the column before the color column
4. **Cleans data** - alphanumeric names, numeric-only values
5. **Removes empty columns** when rows have >6 columns
6. **Sorts by second column** (descending, empty values last)
7. **Outputs CSV** with clean, structured data

## Error Handling

The application validates:
- ✅ Tesseract installation
- ✅ Input folder existence
- ✅ Image files presence
- ✅ OCR processing success

Exits with clear error messages if any validation fails.

## Development

```bash
# Build and test
mvn clean compile test

# Run tests only
mvn test

# Package JAR
mvn package

# Run from source (development)
mvn compile exec:java -Dexec.mainClass="com.dremio.throne.OCRMain" -Dexec.args="fra"
```

## Architecture

- **OCRMain** - CLI entry point with validation
- **OCRBatchProcessor** - Batch processing coordinator  
- **OCRService** - Tesseract wrapper
- **OCROutputParser** - Data parsing and CSV export

## Output Format

CSV with structure: `Name,Num1,Num2,Num3,Num4,Num5`

Example:
```csv
Charizma,68,48,2635209,849361,22065
JustReky,50,62,2112643,1128903,30012
sprad,48,82,4049870,1938548,96731
```

Players with incorrect column counts are flagged for manual processing.
