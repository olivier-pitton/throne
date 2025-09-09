package com.dremio.throne;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Main class for batch OCR processing using OCRFileProcessor.
 * Usage: java OCRBatchMain <folder> <language> <color> [guild] [output.csv] [date]
 */
public class OCRBatchMain {

  private static final Logger LOGGER = Logger.getLogger(OCRBatchMain.class.getName());

  public static void main(String[] args) {
    if (args.length < 3) {
      printUsage();
      System.exit(1);
    }

    String imageFolder = args[0];
    String language = args[1];
    String color = args[2];
    String guild = args.length > 3 ? args[3] : "Enemy";
    String outputCsv = args.length > 4 ? args[4] : "output.csv";
    String dateStr = args.length > 5 ? args[5] : null;

    // Validate color parameter
    if (!color.equalsIgnoreCase("y") && !color.equalsIgnoreCase("r")) {
      LOGGER.severe("❌ Invalid color parameter: " + color + ". Must be 'y' (yellow) or 'r' (red)");
      System.exit(1);
    }

    // Parse or use current date and time
    String dateTimeStr;
    if (dateStr != null) {
      try {
        // Check if time is included
        if (dateStr.contains(" ")) {
          // Format: "yyyy-MM-dd HH:mm" - append seconds
          dateTimeStr = dateStr + ":00";
          // Validate the format
          LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else {
          // Format: "yyyy-MM-dd" - append default time
          LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
          dateTimeStr = dateStr + " 00:00:00";
        }
      } catch (DateTimeParseException e) {
        LOGGER.severe("❌ Invalid date format: " + dateStr + ". Must be yyyy-MM-dd or yyyy-MM-dd HH:mm");
        System.exit(1);
        return;
      }
    } else {
      // Use current date and time
      dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    LOGGER.info("=== OCR Batch Processing ===");
    LOGGER.info("Image folder: " + imageFolder);
    LOGGER.info("Language: " + language);
    LOGGER.info("Color filter: " + (color.equalsIgnoreCase("y") ? "yellow" : "red") + " = Suits, other = " + guild);
    LOGGER.info("Guild name: " + guild);
    LOGGER.info("Output CSV: " + outputCsv);
    LOGGER.info("Date/Time: " + dateTimeStr);

    try {
      OCRBatchMain processor = new OCRBatchMain();

      // Load player classes from class.csv if it exists
      Map<String, String> playerClasses = PlayerClassLoader.loadPlayerClasses();

      int processedCount = processor.processImages(imageFolder, language, color, guild, outputCsv, dateTimeStr, playerClasses);

      LOGGER.info("✅ Processing complete!");
      LOGGER.info("Images processed: " + processedCount);
      LOGGER.info("Results written to: " + outputCsv);

    } catch (Exception e) {
      LOGGER.severe("❌ Processing failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }



  /**
   * Process all images in the folder using OCRFileProcessor.
   *
   * @param imageFolder Path to folder containing images
   * @param language OCR language code
   * @param color Color filter ('y' for yellow, 'r' for red)
   * @param guild Guild name to use instead of "Enemy"
   * @param outputCsv Path to output CSV file
   * @param dateTimeStr Date and time string to prepend to each CSV line
   * @param playerClasses Map of player names to their classes
   * @return Number of images processed
   * @throws Exception if processing fails
   */
  public int processImages(String imageFolder, String language, String color, String guild, String outputCsv, String dateTimeStr, Map<String, String> playerClasses) throws Exception {
    File folder = new File(imageFolder);

    // Validate input folder
    if (!folder.exists() || !folder.isDirectory()) {
      throw new IOException("Image folder does not exist or is not a directory: " + imageFolder);
    }

    // Get all image files
    File[] imageFiles = folder.listFiles((dir, name) -> {
      String lower = name.toLowerCase();
      return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
          lower.endsWith(".tiff") || lower.endsWith(".tif") || lower.endsWith(".bmp") ||
          lower.endsWith(".gif");
    });

    if (imageFiles == null || imageFiles.length == 0) {
      LOGGER.warning("No image files found in: " + imageFolder);
      return 0;
    }

    LOGGER.info("Found " + imageFiles.length + " image files to process");

    // Create OCR service with specified language
    OCRService ocrService = new OCRService(language);

    // Create temporary file for aggregated OCR output
    String tempOcrFile = "temp_ocr_output.txt";
    File tempFile = new File(tempOcrFile);
    if (tempFile.exists()) {
      tempFile.delete();
    }

    int processedCount = 0;

    // Process each image sequentially
    for (File imageFile : imageFiles) {
      try {
        LOGGER.info("Processing: " + imageFile.getName());

        // Create OCRFileProcessor for this image
        OCRFileProcessor processor = new OCRFileProcessor(
            imageFile.getAbsolutePath(),
            ocrService,
            tempOcrFile
        );

        // Process the image (appends to temp file)
        String result = processor.call();
        if (result != null && !result.trim().isEmpty()) {
          processedCount++;
        }

      } catch (Exception e) {
        LOGGER.warning("Failed to process " + imageFile.getName() + ": " + e.getMessage());
      }
    }

    // Process aggregated OCR output to CSV
    if (tempFile.exists() && tempFile.length() > 0) {
      String aggregatedOcrText = new String(java.nio.file.Files.readAllBytes(tempFile.toPath()));

      // Use OCRThroneRecognition to extract and clean data
      OCRThroneRecognition recognition = new OCRThroneRecognition();
      List<String> csvLines = recognition.extractToCSV(aggregatedOcrText);
      List<String> colorInfo = recognition.getColorInfo();

      // Transform CSV lines with date, color mapping, guild, and player classes
      List<String> transformedLines = transformCsvLines(csvLines, colorInfo, color, guild, dateTimeStr, playerClasses);

      // Write to output CSV
      writeCSV(transformedLines, outputCsv);

      // Write errors to file with proper formatting
      try {
        recognition.writeErrorsToFile(dateTimeStr, guild, playerClasses);
      } catch (Exception e) {
        LOGGER.warning("Failed to write errors.csv: " + e.getMessage());
      }

      // Write pure Tesseract output to file
      try {
        writeTesseractOutput(aggregatedOcrText);
      } catch (Exception e) {
        LOGGER.warning("Failed to write tesseract_output.txt: " + e.getMessage());
      }

      // Validate player statistics
      PlayerValidator validator = new PlayerValidator();
      validator.validatePlayers(transformedLines);

      LOGGER.info("Extracted " + csvLines.size() + " valid data lines");
    }

    // Clean up temporary file
    if (tempFile.exists()) {
      tempFile.delete();
    }

    return processedCount;
  }

  /**
   * Transform CSV lines by adding date/time, mapping colors to Suits/Guild, and adding player classes.
   *
   * @param csvLines Original CSV lines
   * @param colorInfo Color information for each line
   * @param color Color filter ('y' for yellow, 'r' for red)
   * @param guild Guild name to use instead of "Enemy"
   * @param dateTimeStr Date and time string to prepend
   * @param playerClasses Map of player names to their classes
   * @return Transformed CSV lines
   */
  private List<String> transformCsvLines(List<String> csvLines, List<String> colorInfo, String color, String guild, String dateTimeStr, Map<String, String> playerClasses) {
    List<String> transformedLines = new ArrayList<>();

    for (int i = 0; i < csvLines.size(); i++) {
      String csvLine = csvLines.get(i);
      String[] parts = csvLine.split(",");
      if (parts.length >= 6) {
        // Extract and clean player name and stats
        String rawPlayerName = parts[0];
        String playerName = PlayerNameMatcher.match(rawPlayerName);
        String[] stats = new String[parts.length - 1];
        System.arraycopy(parts, 1, stats, 0, parts.length - 1);

        // Determine team based on color filter and detected color
        String detectedColor = i < colorInfo.size() ? colorInfo.get(i) : "UNKNOWN";
        String team = determineTeam(detectedColor, color, guild);

        // Get player class from map or default to UNKNOWN (case-insensitive lookup)
        String playerClass = PlayerClassLoader.getPlayerClass(playerName, playerClasses);

        // Build new CSV line: dateTime,team,playerName,playerClass,stat1,stat2,...
        StringBuilder newLine = new StringBuilder();
        newLine.append(dateTimeStr).append(",");
        newLine.append(team).append(",");
        newLine.append(playerName).append(",");
        newLine.append(playerClass);
        for (String stat : stats) {
          newLine.append(",").append(stat);
        }

        transformedLines.add(newLine.toString());
      }
    }

    return transformedLines;
  }

  /**
   * Determine team (Suits or Guild) based on detected color and filter.
   *
   * @param detectedColor Color detected from OCR ("red", "yellow", or "unknown")
   * @param color Color filter ('y' for yellow, 'r' for red)
   * @param guild Guild name to use instead of "Enemy"
   * @return "Suits" if color matches filter, guild name otherwise
   */
  private String determineTeam(String detectedColor, String color, String guild) {
    if (color.equalsIgnoreCase("y") && "yellow".equalsIgnoreCase(detectedColor)) {
      return "Suits";
    } else if (color.equalsIgnoreCase("r") && "red".equalsIgnoreCase(detectedColor)) {
      return "Suits";
    }

    return guild;
  }

  /**
   * Write CSV lines to file.
   *
   * @param csvLines List of CSV lines to write
   * @param filename Output filename
   * @throws IOException if writing fails
   */
  private void writeCSV(List<String> csvLines, String filename) throws IOException {
    try (FileWriter writer = new FileWriter(filename)) {
      for (String line : csvLines) {
        writer.write(line + "\n");
      }
    }
  }

  /**
   * Write pure Tesseract output to tesseract_output.txt file.
   *
   * @param tesseractOutput Raw OCR output from Tesseract
   * @throws IOException if writing fails
   */
  private void writeTesseractOutput(String tesseractOutput) throws IOException {
    try (FileWriter writer = new FileWriter("tesseract_output.txt")) {
      writer.write(tesseractOutput);
    }
    LOGGER.info("Pure Tesseract output written to tesseract_output.txt");
  }

  /**
   * Print usage information.
   */
  private static void printUsage() {
    System.out.println("OCR Batch Processing with OCRFileProcessor");
    System.out.println("Usage: java OCRBatchMain <folder> <language> <color> [guild] [output.csv] [date]");
    System.out.println();
    System.out.println("Parameters:");
    System.out.println("  folder      - Path to folder containing images (required)");
    System.out.println("  language    - OCR language code (required)");
    System.out.println("  color       - Color filter: 'y' (yellow) or 'r' (red) (required)");
    System.out.println("  guild       - Guild name for non-matching colors (default: Enemy)");
    System.out.println("  output.csv  - Output CSV filename (default: output.csv)");
    System.out.println("  date        - Date/time in yyyy-MM-dd or yyyy-MM-dd HH:mm format (default: current date/time)");
    System.out.println();
    System.out.println("Color mapping:");
    System.out.println("  - Lines matching your color → 'Suits'");
    System.out.println("  - Lines with other colors → [guild name]");
    System.out.println();
    System.out.println("Output format: date,team,playerName,playerClass,Kills,Assists,DamageDone,DamageReceived,Healing");
    System.out.println();
    System.out.println("Examples:");
    System.out.println("  java OCRBatchMain ./images eng y");
    System.out.println("  java OCRBatchMain ./images fra r Dragons");
    System.out.println("  java OCRBatchMain ./images eng y Phoenix results.csv");
    System.out.println("  java OCRBatchMain ./images fra r Dragons results.csv \"2025-09-06 21:30\"");
    System.out.println();
    System.out.println("Supported languages: eng, fra, deu, spa, ita, por, etc.");
  }
}
