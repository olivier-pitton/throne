package com.dremio.throne;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Main class for batch OCR processing using OCRFileProcessor.
 * Usage: java OCRBatchMain <folder> <language> <color> [output.csv] [date]
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
        String outputCsv = args.length > 3 ? args[3] : "output.csv";
        String dateStr = args.length > 4 ? args[4] : null;

        // Validate color parameter
        if (!color.equals("y") && !color.equals("r")) {
            LOGGER.severe("❌ Invalid color parameter: " + color + ". Must be 'y' (yellow) or 'r' (red)");
            System.exit(1);
        }

        // Parse or use current date
        LocalDate date;
        if (dateStr != null) {
            try {
                date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                LOGGER.severe("❌ Invalid date format: " + dateStr + ". Must be yyyy-MM-dd");
                System.exit(1);
                return;
            }
        } else {
            date = LocalDate.now();
        }

        LOGGER.info("=== OCR Batch Processing ===");
        LOGGER.info("Image folder: " + imageFolder);
        LOGGER.info("Language: " + language);
        LOGGER.info("Color filter: " + (color.equals("y") ? "yellow" : "red") + " = Suits, other = Enemy");
        LOGGER.info("Output CSV: " + outputCsv);
        LOGGER.info("Date: " + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        try {
            OCRBatchMain processor = new OCRBatchMain();
            int processedCount = processor.processImages(imageFolder, language, color, outputCsv, date);

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
     * @param outputCsv Path to output CSV file
     * @param date Date to prepend to each CSV line
     * @return Number of images processed
     * @throws Exception if processing fails
     */
    public int processImages(String imageFolder, String language, String color, String outputCsv, LocalDate date) throws Exception {
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

            // Transform CSV lines with date and color mapping
            List<String> transformedLines = transformCsvLines(csvLines, colorInfo, color, date);

            // Write to output CSV
            writeCSV(transformedLines, outputCsv);

            LOGGER.info("Extracted " + csvLines.size() + " valid data lines");
        }

        // Clean up temporary file
        if (tempFile.exists()) {
            tempFile.delete();
        }

        return processedCount;
    }
    
    /**
     * Transform CSV lines by adding date and mapping colors to Suits/Enemy.
     *
     * @param csvLines Original CSV lines
     * @param colorInfo Color information for each line
     * @param color Color filter ('y' for yellow, 'r' for red)
     * @param date Date to prepend
     * @return Transformed CSV lines
     */
    private List<String> transformCsvLines(List<String> csvLines, List<String> colorInfo, String color, LocalDate date) {
        List<String> transformedLines = new ArrayList<>();
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        for (int i = 0; i < csvLines.size(); i++) {
            String csvLine = csvLines.get(i);
            String[] parts = csvLine.split(",");
            if (parts.length >= 6) {
                // Extract player name and stats
                String playerName = parts[0];
                String[] stats = new String[parts.length - 1];
                System.arraycopy(parts, 1, stats, 0, parts.length - 1);

                // Determine team based on color filter and detected color
                String detectedColor = i < colorInfo.size() ? colorInfo.get(i) : "unknown";
                String team = determineTeam(detectedColor, color);

                // Build new CSV line: date,team,playerName,stat1,stat2,...
                StringBuilder newLine = new StringBuilder();
                newLine.append(dateStr).append(",");
                newLine.append(team).append(",");
                newLine.append(playerName);
                for (String stat : stats) {
                    newLine.append(",").append(stat);
                }

                transformedLines.add(newLine.toString());
            }
        }

        return transformedLines;
    }

    /**
     * Determine team (Suits or Enemy) based on detected color and filter.
     *
     * @param detectedColor Color detected from OCR ("red", "yellow", or "unknown")
     * @param color Color filter ('y' for yellow, 'r' for red)
     * @return "Suits" if color matches filter, "Enemy" otherwise
     */
    private String determineTeam(String detectedColor, String color) {
        if (color.equals("y") && "yellow".equals(detectedColor)) {
            return "Suits";
        } else if (color.equals("r") && "red".equals(detectedColor)) {
            return "Suits";
        }

        return "Enemy";
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
     * Print usage information.
     */
    private static void printUsage() {
        System.out.println("OCR Batch Processing with OCRFileProcessor");
        System.out.println("Usage: java OCRBatchMain <folder> <language> <color> [output.csv] [date]");
        System.out.println();
        System.out.println("Parameters:");
        System.out.println("  folder      - Path to folder containing images (required)");
        System.out.println("  language    - OCR language code (required)");
        System.out.println("  color       - Color filter: 'y' (yellow) or 'r' (red) (required)");
        System.out.println("  output.csv  - Output CSV filename (default: output.csv)");
        System.out.println("  date        - Date in yyyy-MM-dd format (default: current date)");
        System.out.println();
        System.out.println("Color mapping:");
        System.out.println("  - Lines matching your color → 'Suits'");
        System.out.println("  - Lines with other colors → 'Enemy'");
        System.out.println();
        System.out.println("Output format: date,team,playerName,Kills,Assists,DamageDone,DamageReceived,Healing");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java OCRBatchMain ./images eng y");
        System.out.println("  java OCRBatchMain ./images fra r results.csv");
        System.out.println("  java OCRBatchMain ./images eng y results.csv 2025-09-06");
        System.out.println();
        System.out.println("Supported languages: eng, fra, deu, spa, ita, por, etc.");
    }
}
