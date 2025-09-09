package com.dremio.throne.main;

import com.dremio.throne.db.Player;
import com.dremio.throne.ocr.OCRFileProcessor;
import com.dremio.throne.ocr.OCRService;
import com.dremio.throne.ocr.OCRThroneRecognition;
import com.dremio.throne.validate.PlayerValidator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    color = (color.equalsIgnoreCase("y") ? "yellow" : "red");

    LOGGER.info("=== OCR Batch Processing ===");
    LOGGER.info("Image folder: " + imageFolder);
    LOGGER.info("Language: " + language);
    LOGGER.info("Color filter: " + color + " = Suits, other = " + guild);
    LOGGER.info("Guild name: " + guild);
    LOGGER.info("Output CSV: " + outputCsv);
    LOGGER.info("Date/Time: " + dateTimeStr);

    try {
      OCRBatchMain processor = new OCRBatchMain();

      processor.processImages(imageFolder, language, color, guild, outputCsv, dateTimeStr);

      LOGGER.info("✅ Processing complete!");
      LOGGER.info("Results written to: " + outputCsv);

    } catch (Exception e) {
      LOGGER.severe("❌ Processing failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void processImages(String imageFolder, String language, String color, String guild, String outputCsv, String dateTimeStr) throws Exception {
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
      return;
    }

    LOGGER.info("Found " + imageFiles.length + " image files to process");

    // Create OCR service with specified language
    OCRService ocrService = new OCRService(language);

    StringBuilder sb = new StringBuilder();
    // Process each image sequentially
    for (File imageFile : imageFiles) {
      try {
        LOGGER.info("Processing: " + imageFile.getName());

        // Create OCRFileProcessor for this image
        OCRFileProcessor processor = new OCRFileProcessor(
            imageFile.getAbsolutePath(),
            ocrService
        );

        // Process the image (appends to temp file)
        String result = processor.call();
        if (result != null && !result.trim().isEmpty()) {
          sb.append(result).append('\n');
        } else {
          LOGGER.warning("No OCR output for " + imageFile.getName());
        }

      } catch (Exception e) {
        LOGGER.warning("Failed to process " + imageFile.getName() + ": " + e.getMessage());
      }
    }

    if (sb.length() == 0) {
      LOGGER.warning("No OCR output for any images");
      return;
    }

    // Process aggregated OCR output to CSV
    String aggregatedOcrText = sb.toString();

    // Use OCRThroneRecognition to extract and clean data
    OCRThroneRecognition recognition = new OCRThroneRecognition(color, guild, dateTimeStr);
    List<Player> players = recognition.recognize(aggregatedOcrText);

    // Write to output CSV
    writeCSV(players, outputCsv);

    // Write errors to file with proper formatting
    try {
      recognition.writeErrorsToFile();
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
    validator.validatePlayers(players);
  }

  /**
   * Write CSV lines to file.
   *
   * @param csvLines List of CSV lines to write
   * @param filename Output filename
   * @throws IOException if writing fails
   */
  private void writeCSV(List<Player> csvLines, String filename) throws IOException {
    List<String> lines = csvLines.stream().sorted().map(Player::toCSV).collect(Collectors.toList());
    try (FileWriter writer = new FileWriter(filename)) {
      for (String line : lines) {
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
