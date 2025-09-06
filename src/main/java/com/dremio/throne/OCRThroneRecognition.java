package com.dremio.throne;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * OCR recognition class for extracting throne game data from Tesseract output.
 * Processes pipe-separated OCR text and extracts player data in CSV format.
 */
public class OCRThroneRecognition {
    
    private static final Logger LOGGER = Logger.getLogger(OCRThroneRecognition.class.getName());
    private static final String[] COLOR_WORDS = {"rouge", "jaune", "yellow", "red"};

    private List<String> errorLines = new ArrayList<>();
    
    /**
     * Extract relevant data from Tesseract output and convert to CSV format.
     * Also creates errors.csv file for invalid lines.
     *
     * @param tesseractOutput Raw Tesseract output text
     * @return List of valid CSV lines (player data with exactly 6 columns)
     */
    public List<String> extractToCSV(String tesseractOutput) {
        List<String> csvLines = new ArrayList<>();
        errorLines.clear(); // Clear previous errors

        if (tesseractOutput == null || tesseractOutput.trim().isEmpty()) {
            return csvLines;
        }

        String[] lines = tesseractOutput.split("\n");

        for (String line : lines) {
            String csvLine = processLine(line.trim());
            if (csvLine != null && !csvLine.isEmpty()) {
                // Check if line has exactly 6 columns
                String[] columns = csvLine.split(",");
                if (columns.length == 6) {
                    // Add valid lines (numeric cleaning happens in cleanNumericValue)
                    csvLines.add(csvLine);
                } else {
                    // Add to error lines (pure row only)
                    errorLines.add(csvLine);
                }
            }
        }

        // Write errors to file
        try {
            writeErrorsToFile();
        } catch (IOException e) {
            LOGGER.warning("Failed to write errors.csv: " + e.getMessage());
        }

        return csvLines;
    }

    /**
     * Write error lines to errors.csv file (no header, pure lines only).
     *
     * @throws IOException if file writing fails
     */
    private void writeErrorsToFile() throws IOException {
        try (FileWriter writer = new FileWriter("errors.csv")) {
            for (String errorLine : errorLines) {
                writer.write(errorLine + "\n");
            }
        }

        if (!errorLines.isEmpty()) {
            LOGGER.info("Wrote " + errorLines.size() + " error lines to errors.csv");
        }
    }
    
    /**
     * Process a single line and extract player data.
     * 
     * @param line Raw OCR line
     * @return CSV formatted line or null if no valid data found
     */
    private String processLine(String line) {
        if (line.isEmpty()) {
            return null;
        }
        
        // Split by pipe character
        String[] columns = line.split("\\|");
        
        // Clean up each column
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].trim();
        }
        
        // Find color column
        int colorIndex = findColorColumn(columns);
        if (colorIndex == -1) {
            return null; // No color found, skip this line
        }
        
        // Extract player name (column before color)
        if (colorIndex == 0) {
            return null; // No column before color
        }
        
        String playerName = extractPlayerName(columns[colorIndex - 1]);
        if (playerName.isEmpty()) {
            return null;
        }
        
        // Extract numeric columns after color
        List<String> numericValues = new ArrayList<>();
        for (int i = colorIndex + 1; i < columns.length; i++) {
            String cleanValue = cleanNumericValue(columns[i]);
            numericValues.add(cleanValue);
        }
        
        // Build CSV line: PlayerName,num1,num2,num3,num4,num5
        StringBuilder csvLine = new StringBuilder();
        csvLine.append(playerName);
        
        for (String value : numericValues) {
            csvLine.append(",").append(value);
        }
        
        return csvLine.toString();
    }
    
    /**
     * Find the index of the color column.
     * 
     * @param columns Array of column values
     * @return Index of color column, or -1 if not found
     */
    private int findColorColumn(String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i].toLowerCase();
            for (String color : COLOR_WORDS) {
                if (column.contains(color)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * Extract clean player name from a column.
     * 
     * @param nameColumn Raw name column value
     * @return Clean player name (alphanumeric only)
     */
    private String extractPlayerName(String nameColumn) {
        if (nameColumn == null || nameColumn.trim().isEmpty()) {
            return "";
        }
        
        // Remove all non-alphanumeric characters
        String cleanName = nameColumn.replaceAll("[^a-zA-Z0-9]", "");
        
        // Additional cleaning for common OCR artifacts
        cleanName = cleanName.replaceAll("^[0-9]+", ""); // Remove leading numbers

        return cleanName.trim();
    }
    
    /**
     * Clean numeric value by replacing OCR errors and removing non-digit characters.
     *
     * @param value Raw numeric value
     * @return Clean numeric string (digits only)
     */
    private String cleanNumericValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        String cleaned = value.trim();

        // Replace 'L' with '1' only when it's standalone or clearly a numeric error
        // This handles cases like " L " or "L" but not "Listrinda"
        if (cleaned.equals("L") || cleaned.matches("\\s*L\\s*")) {
            cleaned = cleaned.replace("L", "1");
        }

        // Always replace 'o' with '0' in numeric contexts
        cleaned = cleaned.replace("o", "0");

        // Keep only digits
        cleaned = cleaned.replaceAll("[^0-9]", "");

        return cleaned;
    }
}
