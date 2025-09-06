# 🏰 Throne OCR - Game Data Extractor

**Transform your game screenshots into clean, structured data!**

Throne OCR is a powerful tool that automatically extracts player statistics from game screenshots. It uses advanced OCR (Optical Character Recognition) to read tabular data from images and converts it into clean CSV files ready for analysis.

## ✨ What It Does

📸 **Scans game screenshots** → 🔍 **Finds player data tables** → 📊 **Exports clean CSV files**

Perfect for:
- 🎮 Game statistics tracking
- 🏆 Leaderboard analysis
- 📈 Player performance monitoring
- 🔢 Data analysis and visualization

## 🚀 Quick Start

**Already have the JAR file?** → See [INSTALLATION.md](INSTALLATION.md) for setup instructions.

**Want to build from source?**
```bash
# Build the application
mvn clean package

# Run with English OCR on your images
java -jar target/throne-1.0-SNAPSHOT-fat.jar ./images

# Run with French language support
java -jar target/throne-1.0-SNAPSHOT-fat.jar ./images results.csv fra

# Use convenience scripts
./run-ocr.sh ./images results.csv fra    # Linux/macOS
run-ocr.bat .\images results.csv fra     # Windows
```

## 📋 Requirements

### For End Users (JAR file)
- ☕ **Java 21+** - The runtime environment
- 👁️ **Tesseract OCR** - The OCR engine that reads your images
  - ⚠️ **Install multiple languages**: At minimum English (`eng`) and French (`fra`)
- 🐍 **Python 3.7+** *(optional)* - For analyzing the CSV output

### For Developers (Building from source)
- ☕ **Java 21+**
- 🔨 **Maven 3.6+**
- 👁️ **Tesseract OCR** (with English and French language packs)

**Need help installing?** → Check out our detailed [INSTALLATION.md](INSTALLATION.md) guide!

## 🎯 How to Use

### Basic Command
```bash
java -jar throne-1.0-SNAPSHOT-fat.jar <image_folder> [output_file] [language]
```

### Parameters Explained
| Parameter | Description | Default | Example |
|-----------|-------------|---------|---------|
| `image_folder` | 📁 Folder with your screenshots | *required* | `./screenshots` |
| `output_file` | 📄 Name for your CSV file | `output.csv` | `player_stats.csv` |
| `language` | 🌍 OCR language code | `eng` | `fra`, `deu`, `spa` |

### Real Examples

```bash
# 🇺🇸 Process English screenshots (simplest)
java -jar throne-1.0-SNAPSHOT-fat.jar ./screenshots

# 🇫🇷 Process French screenshots with custom output
java -jar throne-1.0-SNAPSHOT-fat.jar ./screenshots french_data.csv fra

# 🇩🇪 Process German screenshots
java -jar throne-1.0-SNAPSHOT-fat.jar ./screenshots german_stats.csv deu
```

### 🛠️ Convenience Scripts (Easier!)

**Linux/macOS:**
```bash
./run-ocr.sh ./screenshots                    # Quick start
./run-ocr.sh ./screenshots my_data.csv       # Custom output
./run-ocr.sh ./screenshots my_data.csv fra   # French language
```

**Windows:**
```cmd
run-ocr.bat .\screenshots                     # Quick start
run-ocr.bat .\screenshots my_data.csv        # Custom output
run-ocr.bat .\screenshots my_data.csv fra    # French language
```

## 🔧 How It Works (The Magic Behind the Scenes)

1. 🔍 **Scans your images** for table-like data (looks for pipe `|` separators)
2. 🎨 **Finds color indicators** (rouge, jaune, yellow, red, etc.) to identify data rows
3. ✂️ **Extracts player data** starting from the column before the color
4. 🧹 **Cleans the data**:
   - Fixes common OCR errors (`o` → `0`, `L` → `1`)
   - Removes spaces from numbers
   - Converts blank values to `0`
5. ✅ **Validates data** (keeps only rows with exactly 6 columns)
6. 📊 **Exports clean CSV** ready for analysis

## ⚡ Performance Features

### 🚀 **Multi-Threading Support**
- **Automatic CPU detection**: Uses all available processor cores
- **Parallel processing**: Processes multiple images simultaneously
- **Thread-safe operations**: Synchronized file writing prevents data corruption
- **Optimal performance**: Scales with your hardware (2 cores = 2x faster, 8 cores = 8x faster!)

**Example**: Processing 100 images on an 8-core machine processes ~8 images at once instead of one-by-one!

## 🛡️ Smart Error Handling

Throne OCR checks everything before processing:
- ✅ **Java installation** - Makes sure you have Java 21+
- ✅ **Tesseract setup** - Verifies OCR engine is working
- ✅ **Image folder** - Confirms your screenshots exist
- ✅ **File formats** - Supports PNG, JPG, TIFF, BMP, GIF
- ✅ **Data quality** - Creates `errors.csv` for problematic rows

**No crashes, just helpful error messages!** 🎯

## 📊 Output Format

Your data comes out clean and ready to use!

### Main Output: `output.csv`
```csv
PlayerName,Score1,Score2,Value1,Value2,Value3
Charizma,68,48,2635209,849361,22065
JustReky,50,62,2112643,1128903,30012
sprad,48,82,4049870,1938548,96731
```

### Error Log: `errors.csv`
Rows that couldn't be processed (wrong number of columns, etc.)
```csv
IncompletePlayer,12,34
PlayerWithTooManyColumns,1,2,3,4,5,6,7,8,9
```

## 🌍 Supported Languages

| Language | Code | Example |
|----------|------|---------|
| 🇺🇸 English | `eng` | `java -jar throne.jar ./images output.csv eng` |
| 🇫🇷 French | `fra` | `java -jar throne.jar ./images output.csv fra` |
| 🇩🇪 German | `deu` | `java -jar throne.jar ./images output.csv deu` |
| 🇪🇸 Spanish | `spa` | `java -jar throne.jar ./images output.csv spa` |
| 🇮🇹 Italian | `ita` | `java -jar throne.jar ./images output.csv ita` |
| 🇵🇹 Portuguese | `por` | `java -jar throne.jar ./images output.csv por` |
| 🇷🇺 Russian | `rus` | `java -jar throne.jar ./images output.csv rus` |
| 🇨🇳 Chinese | `chi_sim` | `java -jar throne.jar ./images output.csv chi_sim` |
| 🇯🇵 Japanese | `jpn` | `java -jar throne.jar ./images output.csv jpn` |

## 🐍 Python Integration

Want to analyze your data with Python? Here's a quick start:

```python
import pandas as pd
import matplotlib.pyplot as plt

# Load your OCR results
df = pd.read_csv('output.csv')

# Quick analysis
print(f"Found {len(df)} players")
print(f"Top player: {df.iloc[0]['PlayerName']}")

# Simple visualization
df.plot(x='PlayerName', y='Score1', kind='bar')
plt.show()
```

## 👨‍💻 For Developers

### Building from Source
```bash
# Build and test
mvn clean package

# Run tests
mvn test

# Development mode
mvn compile exec:java -Dexec.mainClass="com.dremio.throne.OCRBatchMain" -Dexec.args="./images"
```

### Architecture
- **OCRBatchMain** - User-friendly CLI interface
- **OCRFileProcessor** - Individual image processing
- **OCRThroneRecognition** - Data extraction and cleaning
- **OCRService** - Tesseract integration

## 🆘 Need Help?

1. **Installation issues?** → Check [INSTALLATION.md](INSTALLATION.md)
2. **OCR not working?** → Verify Tesseract installation: `tesseract --version`
3. **No data extracted?** → Check if your images contain pipe-separated tables
4. **Wrong language?** → Make sure you have the right language pack installed

**Still stuck?** Open an issue with your error message and we'll help! 🚀
