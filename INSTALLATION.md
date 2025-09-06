# Throne OCR - Installation Guide

This guide helps you install and set up Throne OCR on your system. You only need the JAR file (`throne-1.0-SNAPSHOT-fat.jar`) to get started.

## System Requirements

- **Java 21 or higher**
- **Tesseract OCR engine**
- **Python 3.7+ (optional, for data analysis)**

## Step 1: Install Java

### Windows
1. Download Java from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)
2. Run the installer and follow the setup wizard
3. Verify installation:
   ```cmd
   java -version
   ```

### macOS
```bash
# Using Homebrew (recommended)
brew install openjdk@21

# Or download from Oracle/OpenJDK websites
```

### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-21-jdk

# Verify installation
java -version
```

### Linux (CentOS/RHEL)
```bash
sudo yum install java-21-openjdk-devel

# Or for newer versions
sudo dnf install java-21-openjdk-devel
```

## Step 2: Install Tesseract OCR

### Windows
1. **Download Tesseract**:
   - Go to [UB-Mannheim Tesseract](https://github.com/UB-Mannheim/tesseract/wiki)
   - Download the latest Windows installer
   - **⚠️ IMPORTANT**: During installation, select "Additional language data" and choose:
     - English (eng) - Usually included by default
     - French (fra) - Essential for French text recognition
     - Any other languages you need (German, Spanish, etc.)

2. **Set Environment Variable**:

   **Method 1: Using Environment Variables Panel**
   - Press `Win + R`, type `sysdm.cpl`, press Enter
   - Click "Advanced" tab → "Environment Variables" button
   - Under "System variables", find and select "Path" → Click "Edit"
   - Click "New" and add: `C:\Program Files\Tesseract-OCR`
   - Click "OK" to save all dialogs

   **Method 2: Command Line**
   ```cmd
   setx PATH "%PATH%;C:\Program Files\Tesseract-OCR"
   ```

   **Alternative: Set TESSDATA_PREFIX**
   - In Environment Variables panel, click "New" under System variables
   - Variable name: `TESSDATA_PREFIX`
   - Variable value: `C:\Program Files\Tesseract-OCR\tessdata`

   📖 **Detailed guide**: [Microsoft's Environment Variables Documentation](https://docs.microsoft.com/en-us/windows/win32/procthread/environment-variables)

3. **Verify Installation**:
   ```cmd
   tesseract --version
   ```

### macOS
```bash
# Install Tesseract with ALL language packs (recommended)
brew install tesseract tesseract-lang

# Or install specific languages only
brew install tesseract
brew install tesseract-lang  # This includes French, German, Spanish, etc.

# Verify installation
tesseract --version

# Check available languages (should include 'eng' and 'fra' at minimum)
tesseract --list-langs
```

**⚠️ IMPORTANT**: Make sure you see both `eng` and `fra` in the language list!

### Linux (Ubuntu/Debian)
```bash
# Install Tesseract and essential languages
sudo apt update
sudo apt install tesseract-ocr tesseract-ocr-eng tesseract-ocr-fra tesseract-ocr-deu tesseract-ocr-spa

# Install additional languages if needed
sudo apt install tesseract-ocr-ita tesseract-ocr-por tesseract-ocr-rus

# Verify installation
tesseract --version

# Check available languages (must include 'eng' and 'fra')
tesseract --list-langs
```

**⚠️ IMPORTANT**: Ensure you see both `eng` and `fra` in the output!

### Linux (CentOS/RHEL)
```bash
# Enable EPEL repository first
sudo yum install epel-release

# Install Tesseract with essential languages
sudo yum install tesseract tesseract-langpack-eng tesseract-langpack-fra tesseract-langpack-deu

# For newer systems, use dnf
sudo dnf install tesseract tesseract-langpack-eng tesseract-langpack-fra tesseract-langpack-deu

# Verify installation
tesseract --version

# Check languages (must include 'eng' and 'fra')
tesseract --list-langs
```

## Step 3: Install Python (Optional)

Python is useful for analyzing the CSV output data.

### Windows
1. Download Python from [python.org](https://www.python.org/downloads/)
2. Run installer and **check "Add Python to PATH"**
3. Verify: `python --version`

### macOS
```bash
# Using Homebrew
brew install python

# Verify installation
python3 --version
```

### Linux
```bash
# Ubuntu/Debian
sudo apt install python3 python3-pip

# CentOS/RHEL
sudo yum install python3 python3-pip

# Verify installation
python3 --version
```

## Step 4: Set Up Tesseract Environment (If Needed)

If Tesseract is not found automatically, set these environment variables:

### Windows
```cmd
set TESSDATA_PREFIX=C:\Program Files\Tesseract-OCR\tessdata
set PATH=%PATH%;C:\Program Files\Tesseract-OCR
```

### macOS/Linux
```bash
# Add to ~/.bashrc or ~/.zshrc
export TESSDATA_PREFIX=/usr/local/share/tessdata
export PATH=$PATH:/usr/local/bin
```

## Step 5: Test Your Installation

1. **Test Java**:
   ```bash
   java -version
   ```

2. **Test Tesseract**:
   ```bash
   tesseract --version
   tesseract --list-langs
   ```
   **⚠️ IMPORTANT**: You should see at least `eng` and `fra` in the language list!

3. **Test Throne OCR**:
   ```bash
   java -jar throne-1.0-SNAPSHOT-fat.jar
   ```

## Troubleshooting

### "Tesseract not found" Error
- **Windows**: Ensure Tesseract is in your PATH or set TESSDATA_PREFIX
- **macOS/Linux**: Try `which tesseract` to find the installation path
- **All platforms**: Restart your terminal/command prompt after installation

### "Java not found" Error
- Ensure Java 21+ is installed: `java -version`
- Check JAVA_HOME environment variable is set correctly

### Language Pack Issues
- Install additional language packs:
  - **Windows**: Re-run installer with language options
  - **macOS**: `brew install tesseract-lang`
  - **Linux**: `sudo apt install tesseract-ocr-[lang]`

### Permission Issues (Linux/macOS)
```bash
# Make sure you have read permissions on tessdata
ls -la /usr/share/tesseract-ocr/*/tessdata/
```

## Supported Languages

Common language codes for OCR:
- `eng` - English
- `fra` - French  
- `deu` - German
- `spa` - Spanish
- `ita` - Italian
- `por` - Portuguese
- `rus` - Russian
- `chi_sim` - Chinese Simplified
- `jpn` - Japanese

## Next Steps

Once everything is installed, see the main README.md for usage instructions and examples.
