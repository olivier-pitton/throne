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
java -jar throne-1.0-SNAPSHOT-fat.jar <image_folder> <language> <color> [guild] [output_file] [date]
```

### Parameters Explained
| Parameter | Description | Default | Example |
|-----------|-------------|---------|---------|
| `image_folder` | 📁 Folder with your screenshots | *required* | `./screenshots` |
| `language` | 🌍 OCR language code | *required* | `eng`, `fra`, `deu`, `spa` |
| `color` | 🎨 Team color filter | *required* | `y` (yellow), `r` (red) |
| `guild` | 🏰 Enemy guild name | `Enemy` | `Dragons`, `Phoenix` |
| `output_file` | 📄 Name for your CSV file | `output.csv` | `player_stats.csv` |
| `date` | 📅 Date/time for CSV entries | current date/time | `2025-09-06`, `2025-09-06 21:30` |

### Real Examples

```bash
# 🟡 Process yellow team screenshots (English)
java -jar throne-1.0-SNAPSHOT-fat.jar ./screenshots eng y

# 🔴 Process red team screenshots with custom guild
java -jar throne-1.0-SNAPSHOT-fat.jar ./screenshots eng r Dragons

# 🇫🇷 Process French screenshots for yellow team with custom output
java -jar throne-1.0-SNAPSHOT-fat.jar ./screenshots fra y Phoenix team_stats.csv

# 📅 Process with specific date and time
java -jar throne-1.0-SNAPSHOT-fat.jar ./screenshots eng r Dragons stats.csv "2025-09-06 21:30"
```

### 🎨 Color Team System

**Your Team vs Enemy Team:**
- **`y` (Yellow)**: Lines with yellow/jaune colors → `Suits`, others → `Enemy`
- **`r` (Red)**: Lines with red/rouge colors → `Suits`, others → `Enemy`

**Example**: If you choose `y`, all yellow players become "Suits" and red players become your guild name

### 🎭 Player Class System

**Optional class.csv file:**
- **Format**: `PlayerName,Class` (one per line)
- **Location**: Same directory as the JAR file
- **Behavior**:
  - If file exists → Loads player classes and includes them in output
  - If file missing → Shows warning, all players get "UNKNOWN" class

**Example class.csv:**
```csv
Charizma,Warrior
JustReky,Mage
sprad,Archer
Bilthuat,Paladin
```

### 🛠️ Convenience Scripts (Easier!)

**Linux/macOS:**
```bash
./run-ocr.sh ./screenshots eng y                    # English, yellow team
./run-ocr.sh ./screenshots fra r Dragons           # French, red team, Dragons guild
./run-ocr.sh ./screenshots eng y Phoenix my_data.csv 2025-09-06   # Full example
```

**Windows:**
```cmd
run-ocr.bat .\screenshots eng y                     # English, yellow team
run-ocr.bat .\screenshots fra r Dragons           # French, red team, Dragons guild
run-ocr.bat .\screenshots eng y Phoenix my_data.csv 2025-09-06    # Full example
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
Date,Team,PlayerName,PlayerClass,Kills,Assists,DamageDone,DamageReceived,Healing
2025-09-07 00:00:00,Suits,Charizma,Warrior,68,48,2635209,849361,22065
2025-09-07 21:30:00,Enemy,JustReky,Mage,50,62,2112643,1128903,30012
2025-09-07 00:00:00,Suits,sprad,Archer,48,82,4049870,1938548,96731
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
