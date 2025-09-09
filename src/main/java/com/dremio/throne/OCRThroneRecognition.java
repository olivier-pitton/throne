package com.dremio.throne;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * OCR recognition class for extracting throne game data from Tesseract output.
 * Processes pipe-separated OCR text and extracts player data in CSV format.
 */
public class OCRThroneRecognition {
    
    private static final Logger LOGGER = Logger.getLogger(OCRThroneRecognition.class.getName());
    private static final Set<String> COLOR_WORDS = Set.of("rouge", "jaune", "yellow", "red");

    private List<String> errorLines = new ArrayList<>();
    private List<String> colorInfo = new ArrayList<>(); // Track color for each line
    
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
        colorInfo.clear(); // Clear previous color info

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
                    // Track the original color for this line
                    String detectedColor = extractColorFromLine(line.trim());
                    colorInfo.add(detectedColor);
                } else {
                    // Add to error lines (pure row only)
                    errorLines.add(csvLine);
                }
            }
        }

        // Note: Error file writing is now handled in OCRBatchMain with proper formatting

        return csvLines;
    }

    /**
     * Write error lines to errors.csv file with date/time, guild, and class information.
     *
     * @param dateTimeStr Date and time string to prepend to each error line
     * @param guild Guild name for team assignment
     * @param playerClasses Map of player names to their classes
     * @throws IOException if file writing fails
     */
    public void writeErrorsToFile(String dateTimeStr, String guild, Map<String, String> playerClasses) throws IOException {
        try (FileWriter writer = new FileWriter("errors.csv")) {
            for (String errorLine : errorLines) {
                String[] parts = errorLine.split(",");
                if (parts.length > 0) {
                    // Extract and clean player name (first column)
                    String rawPlayerName = parts[0];
                    String playerName = PlayerNameMatcher.match(rawPlayerName);

                    // Get player class from map or default to UNKNOWN (case-insensitive lookup)
                    String playerClass = PlayerClassLoader.getPlayerClass(playerName, playerClasses);

                    // Build error line with same format as main output: dateTime,guild,playerName,playerClass,stats...
                    StringBuilder formattedErrorLine = new StringBuilder();
                    formattedErrorLine.append(dateTimeStr).append(",");
                    formattedErrorLine.append(guild).append(",");
                    formattedErrorLine.append(playerName).append(",");
                    formattedErrorLine.append(playerClass);

                    // Add remaining stats
                    for (int i = 1; i < parts.length; i++) {
                        formattedErrorLine.append(",").append(parts[i]);
                    }

                    writer.write(formattedErrorLine.toString() + "\n");
                } else {
                    // Fallback for malformed lines
                    writer.write(dateTimeStr + "," + guild + ",UNKNOWN,UNKNOWN," + errorLine + "\n");
                }
            }
        }

        if (!errorLines.isEmpty()) {
            LOGGER.info("Wrote " + errorLines.size() + " error lines to errors.csv");
        }
    }

    /**
     * Extract color information from the original line.
     *
     * @param line Original OCR line
     * @return Detected color or "unknown"
     */
    private String extractColorFromLine(String line) {
        String lowerLine = line.toLowerCase();

        if (lowerLine.contains("rouge") || lowerLine.contains("red")) {
            return "red";
        } else if (lowerLine.contains("jaune") || lowerLine.contains("yellow")) {
            return "yellow";
        }

        return "unknown";
    }

    /**
     * Get color information for each processed line.
     *
     * @return List of colors corresponding to each CSV line
     */
    public List<String> getColorInfo() {
        return new ArrayList<>(colorInfo);
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
           if (COLOR_WORDS.contains(column)) {
               return i;
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
    public static String extractPlayerName(String nameColumn) {
        if (nameColumn == null || nameColumn.trim().isEmpty()) {
            return "";
        }
        
        // Remove all non-alphanumeric characters
        String cleanName = nameColumn.replaceAll("[^a-zA-Z0-9]", "");
        
        // Additional cleaning for common OCR artifacts
        cleanName = cleanName.replaceAll("^[0-9]+", ""); // Remove leading numbers

        cleanName = PlayerNameMatcher.match(cleanName);

        return cleanName.trim();
    }
    
    /**
     * Clean numeric value by replacing OCR errors and removing non-digit characters.
     *
     * @param value Raw numeric value
     * @return Clean numeric string (digits only, "0" for blank values)
     */
    private String cleanNumericValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "0";
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

        // If after cleaning we have an empty string, return "0"
        if (cleaned.isEmpty()) {
            return "0";
        }

        return cleaned;
    }
}
