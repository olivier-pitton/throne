package com.dremio.throne.ocr;

import com.dremio.throne.db.Player;
import com.dremio.throne.db.PlayerClassLoader;
import com.dremio.throne.util.Labels;
import com.dremio.throne.util.Util;
import com.dremio.throne.validate.PlayerNameMatcher;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;

/**
 * OCR recognition class for extracting throne game data from Tesseract output.
 * Processes pipe-separated OCR text and extracts player data in CSV format.
 */
public class OCRThroneRecognition {

  private final List<String> errorLines = new ArrayList<>();
  private final Map<String, String> classes = new ConcurrentHashMap<>(PlayerClassLoader.loadPlayerClasses());
  private final String currentColor;
  private final String enemyGuild;
  private final String dateStr;

  public OCRThroneRecognition(String currentColor, String enemyGuild, String dateStr) {
    this.currentColor = currentColor;
    this.enemyGuild = enemyGuild;
    this.dateStr = dateStr;
  }

  /**
   * Extract relevant data from Tesseract output and convert to CSV format.
   * Also creates errors.csv file for invalid lines.
   *
   * @param tesseractOutput Raw Tesseract output text
   * @return List of valid CSV lines (player data with exactly 6 columns)
   */
  public List<Player> recognize(String tesseractOutput) {
    Set<Player> players = new HashSet<>();

    if (tesseractOutput == null || tesseractOutput.trim().isEmpty()) {
      return Collections.emptyList();
    }

    String[] lines = tesseractOutput.split("\n");

    for (String line : lines) {
      Player player = processLine(line.trim());
      if (player == null || !player.isValid()) {
        errorLines.add(line);
        continue;
      }
      var className = classes.getOrDefault(player.getName(), Labels.UNKNOWN);
      player.setClassName(className);
      players.add(player);

    }
    return new ArrayList<>(players);
  }

  /**
   * Extract color information from the original line.
   *
   * @param line Original OCR line
   * @return Detected color or "unknown"
   */
  private String extractColorFromColumn(String line) {
    String lowerLine = line.toLowerCase();

    if (lowerLine.contains("rouge") || lowerLine.contains("red")) {
      return "red";
    } else if (lowerLine.contains("jaune") || lowerLine.contains("yellow")) {
      return "yellow";
    }

    return "unknown";
  }


  /**
   * Process a single line and extract player data.
   *
   * @param line Raw OCR line
   * @return CSV formatted line or null if no valid data found
   */
  private Player processLine(String line) {
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

    String playerName = cleanupPlayerName(columns[colorIndex - 1]);
    if (playerName.isEmpty()) {
      return null;
    }

    // Extract numeric columns after color
    List<Long> numericValues = new ArrayList<>();
    for (int i = colorIndex + 1; i < columns.length; i++) {
      String cleanValue = cleanNumericValue(columns[i]);
      numericValues.add(Util.parseLongSafely(cleanValue));
    }

    String color = extractColorFromColumn(columns[colorIndex]);
    String guild = color.equalsIgnoreCase(currentColor) ? Labels.SUITS : enemyGuild;
    if (numericValues.size() != 5) {
      return new Player(playerName, guild, dateStr, numericValues.toArray(new Long[0]));
    }

    return new Player(playerName, guild, dateStr, numericValues.toArray(new Long[0]));
  }

  private int findColorColumn(String[] columns) {
    for (int i = 0; i < columns.length; i++) {
      String column = columns[i].toLowerCase();
      if (Labels.COLORS.contains(column)) {
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
  public static String cleanupPlayerName(String nameColumn) {
    if (nameColumn == null || nameColumn.trim().isEmpty()) {
      return "";
    }

    // Remove all non-alphanumeric characters
    String cleanName = nameColumn.replaceAll("[^a-zA-Z0-9]", "");

    // Additional cleaning for common OCR artifacts
    cleanName = cleanName.replaceAll("^[0-9]+", ""); // Remove leading numbers

    cleanName = PlayerNameMatcher.match(cleanName);
    return StringUtils.capitalize(cleanName.trim());
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

  public void writeErrorsToFile() throws IOException {
    try (FileWriter writer = new FileWriter("errors.csv")) {
      for (String errorLine : errorLines) {
        Player player = processLine(errorLine);
        if (player == null) {
          var parts = errorLine.split("\\|");
          if (parts.length > 4) {
            writer.write(errorLine.replaceAll(" *\\| *", "") + "\n");
          }
          continue;
        }
        writer.write(player.toCSV() + "\n");
      }
    }
  }
}
