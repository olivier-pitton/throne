package com.dremio.throne;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Optimized OCR output parser for pipe-separated tabular data.
 * Processes, filters, cleans, sorts and exports to CSV in a single pipeline.
 */
public class OCROutputParser {
    
    private static final Logger LOGGER = Logger.getLogger(OCROutputParser.class.getName());
    private static final String[] COLOR_WORDS = {"rouge", "jaune", "yellow", "red"};
    
    /**
     * Complete processing pipeline: parse OCR text, filter by color, clean data, sort and write CSV.
     */
    public void processToCSV(String ocrText, String csvFilename) throws IOException {
        if (ocrText == null || ocrText.trim().isEmpty()) return;
        
        List<String[]> rows = parseAndFilter(ocrText);
        if (rows.isEmpty()) return;
        
        rows.sort(createColumnComparator());
        writeCSV(rows, csvFilename);
    }
    
    /**
     * Parse OCR text and filter rows containing color words.
     */
    private List<String[]> parseAndFilter(String ocrText) {
        List<String[]> result = new ArrayList<>();
        String[] lines = ocrText.split("\n");
        
        // Skip header (first line)
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            String[] columns = line.split("\\|");
            for (int j = 0; j < columns.length; j++) {
                columns[j] = columns[j].trim();
            }
            
            // Find color column and extract relevant data
            int colorIndex = findColorColumn(columns);
            if (colorIndex > 0) {
                String[] filtered = extractColumns(columns, colorIndex);
                if (filtered.length > 0) {
                    cleanRow(filtered);
                    validateAndAdd(filtered, result);
                }
            }
        }
        return result;
    }
    
    /**
     * Find the index of the color column.
     */
    private int findColorColumn(String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            String cell = columns[i].toLowerCase();
            for (String color : COLOR_WORDS) {
                if (cell.contains(color)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * Extract columns: previous column before color + all columns after color.
     * Remove empty columns if line has more than 6 columns.
     */
    private String[] extractColumns(String[] original, int colorIndex) {
        if (colorIndex == 0) return new String[0];

        List<String> extracted = new ArrayList<>();
        extracted.add(original[colorIndex - 1]); // Previous column (name)

        // Add all columns after color column
        for (int i = colorIndex + 1; i < original.length; i++) {
            extracted.add(original[i]);
        }

        // If we have more than 6 columns, remove empty ones
        if (extracted.size() > 6) {
            extracted = removeEmptyColumns(extracted);
        }

        return extracted.toArray(new String[0]);
    }

    /**
     * Remove empty columns from the list, keeping only non-empty ones.
     */
    private List<String> removeEmptyColumns(List<String> columns) {
        List<String> filtered = new ArrayList<>();
        for (String column : columns) {
            if (column != null && !column.trim().isEmpty()) {
                filtered.add(column);
            }
        }
        return filtered;
    }
    
    /**
     * Clean row data: alphanumeric names, numeric-only other columns.
     */
    private void cleanRow(String[] row) {
        if (row.length == 0) return;
        
        // Clean first column (name) - keep only alphanumeric
        row[0] = row[0].replaceAll("[^a-zA-Z0-9]", "");
        
        // Clean numeric columns - keep only digits
        for (int i = 1; i < row.length; i++) {
            row[i] = row[i].replaceAll("[^0-9]", "");
        }
    }
    
    /**
     * Validate row format and add to result with warning if needed.
     */
    private void validateAndAdd(String[] row, List<String[]> result) {
        if (row.length != 6) {
            LOGGER.warning("⚠️  MANUAL PROCESSING NEEDED - Player '" + row[0] + 
                         "' has " + row.length + " columns instead of expected 6 (name + 5 numbers): " + 
                         java.util.Arrays.toString(row));
        }
        result.add(row);
    }
    
    /**
     * Create comparator for sorting by second column (descending, empty values last).
     */
    private Comparator<String[]> createColumnComparator() {
        return (row1, row2) -> {
            String val1 = (row1.length > 1) ? row1[1].trim() : "";
            String val2 = (row2.length > 1) ? row2[1].trim() : "";
            
            int num1 = parseIntOrZero(val1);
            int num2 = parseIntOrZero(val2);
            
            // Empty values (0) go to end
            if (num1 == 0 && !val1.equals("0") && num2 != 0) return 1;
            if (num2 == 0 && !val2.equals("0") && num1 != 0) return -1;
            
            return Integer.compare(num2, num1); // Descending order
        };
    }
    
    /**
     * Parse string to integer, return 0 if invalid.
     */
    private int parseIntOrZero(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Write data to CSV file with proper escaping.
     */
    private void writeCSV(List<String[]> rows, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) writer.write(",");
                    
                    String field = row[i];
                    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                        field = "\"" + field.replace("\"", "\"\"") + "\"";
                    }
                    writer.write(field);
                }
                writer.write("\n");
            }
        }
    }
    
    // Legacy methods for backward compatibility
    public List<String[]> parseOCROutput(String ocrText) {
        return parseAndFilter(ocrText);
    }
    
    public List<String[]> filterFromColorColumn(List<String[]> rows) {
        return rows; // Already filtered in parseAndFilter
    }
    
    public List<String[]> sortBySecondColumn(List<String[]> rows) {
        rows.sort(createColumnComparator());
        return rows;
    }
    
    public void writeToCSV(List<String[]> rows, String filename) throws IOException {
        writeCSV(rows, filename);
    }
    
    public void parseAndWriteToCSV(String ocrText) throws IOException {
        processToCSV(ocrText, "output.csv");
    }
}
