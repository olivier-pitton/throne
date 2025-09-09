package com.dremio.throne;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Validates player statistics and logs warnings for suspicious data.
 */
public class PlayerValidator {
    
    private static final Logger LOGGER = Logger.getLogger(PlayerValidator.class.getName());
    
    /**
     * Validate all players and log warnings for suspicious statistics.
     * Also append non-validated players to errors.csv file.
     *
     * @param csvLines List of CSV lines to validate (format: date,team,playerName,playerClass,kills,assists,damageDone,damageReceived,healing)
     */
    public void validatePlayers(List<String> csvLines) {
        int validatedCount = 0;
        int warningCount = 0;
        List<String> nonValidatedPlayers = new ArrayList<>();

        for (String csvLine : csvLines) {
            String[] parts = csvLine.split(",");
            if (parts.length >= 9) {
                try {
                    String rawPlayerName = parts[2];
                    String playerName = PlayerNameMatcher.match(rawPlayerName);
                    String playerClass = parts[3].toLowerCase();

                    // Skip validation if class is unknown, but collect for errors.csv
                    if (playerClass.equalsIgnoreCase("UNKNOWN")) {
                        nonValidatedPlayers.add(csvLine);
                        continue;
                    }

                    int kills = parseIntSafely(parts[4]);
                    int assists = parseIntSafely(parts[5]);
                    long damageDone = parseLongSafely(parts[6]);
                    long damageReceived = parseLongSafely(parts[7]);
                    long healing = parseLongSafely(parts[8]);

                    boolean hasWarning = validatePlayerStats(csvLine, playerName, playerClass, kills, assists, damageDone, damageReceived, healing);
                    if (hasWarning) {
                        warningCount++;
                    }
                    validatedCount++;
                    
                } catch (Exception e) {
                    LOGGER.warning("Failed to parse player data: " + csvLine + " - " + e.getMessage());
                }
            }
        }

        // Append non-validated players to errors.csv
        if (!nonValidatedPlayers.isEmpty()) {
            try {
                appendNonValidatedPlayersToErrors(nonValidatedPlayers);
            } catch (IOException e) {
                LOGGER.warning("Failed to append non-validated players to errors.csv: " + e.getMessage());
            }
        }

        LOGGER.info("Player validation complete: " + validatedCount + " players validated, " + warningCount + " warnings logged, " + nonValidatedPlayers.size() + " non-validated players appended to errors.csv");
    }
    
    /**
     * Validate individual player statistics.
     * 
     * @param csvLine Original CSV line for logging
     * @param playerName Player name
     * @param playerClass Player class (lowercase)
     * @param kills Number of kills
     * @param assists Number of assists
     * @param damageDone Damage dealt
     * @param damageReceived Damage received
     * @param healing Healing done
     * @return true if any warnings were logged
     */
    private boolean validatePlayerStats(String csvLine, String playerName, String playerClass, 
                                      int kills, int assists, long damageDone, long damageReceived, long healing) {
        boolean hasWarning = false;
        
        // Universal rules for all players
        if (kills > 200) {
            LOGGER.warning("SUSPICIOUS KILLS: " + playerName + " has " + kills + " kills (>200) - " + csvLine);
            hasWarning = true;
        }
        
        if (assists < 5 || assists > 150) {
            LOGGER.warning("SUSPICIOUS ASSISTS: " + playerName + " has " + assists + " assists (should be 5-150) - " + csvLine);
            hasWarning = true;
        }
        
        if (damageDone < 10000 || damageDone > 8000000) {
            LOGGER.warning("SUSPICIOUS DAMAGE DONE: " + playerName + " has " + damageDone + " damage done (should be 10,000-8,000,000) - " + csvLine);
            hasWarning = true;
        }
        
        if (damageReceived < 200000 || damageReceived > 3000000) {
            LOGGER.warning("SUSPICIOUS DAMAGE RECEIVED: " + playerName + " has " + damageReceived + " damage received (should be 200,000-3,000,000) - " + csvLine);
            hasWarning = true;
        }
        
        if (healing == 0) {
            LOGGER.warning("SUSPICIOUS HEALING: " + playerName + " has 0 healing - " + csvLine);
            hasWarning = true;
        }
        
        // Class-specific rules
        if (!playerClass.equalsIgnoreCase("tank") && !playerClass.equalsIgnoreCase("healer")) {
            // Rules for non-tank, non-healer classes
            if (assists < 20) {
                LOGGER.warning("SUSPICIOUS ASSISTS (Non-Tank/Healer): " + playerName + " (" + playerClass + ") has " + assists + " assists (<20) - " + csvLine);
                hasWarning = true;
            }
            
            if (kills < 10) {
                LOGGER.warning("SUSPICIOUS KILLS (Non-Tank/Healer): " + playerName + " (" + playerClass + ") has " + kills + " kills (<10) - " + csvLine);
                hasWarning = true;
            }
            
            if (damageDone < 500000) {
                LOGGER.warning("SUSPICIOUS DAMAGE DONE (Non-Tank/Healer): " + playerName + " (" + playerClass + ") has " + damageDone + " damage done (<500,000) - " + csvLine);
                hasWarning = true;
            }
            
            if (damageReceived < 300000) {
                LOGGER.warning("SUSPICIOUS DAMAGE RECEIVED (Non-Tank/Healer): " + playerName + " (" + playerClass + ") has " + damageReceived + " damage received (<300,000) - " + csvLine);
                hasWarning = true;
            }
        }
        
        if (playerClass.equalsIgnoreCase("healer")) {
            // Rules specific to healers
            if (assists < 20) {
                LOGGER.warning("SUSPICIOUS ASSISTS (Healer): " + playerName + " has " + assists + " assists (<20) - " + csvLine);
                hasWarning = true;
            }
            
            if (healing < 800000 || healing > 5000000) {
                LOGGER.warning("SUSPICIOUS HEALING (Healer): " + playerName + " has " + healing + " healing (should be 800,000-5,000,000) - " + csvLine);
                hasWarning = true;
            }
        }
        
        return hasWarning;
    }
    
    /**
     * Safely parse integer from string, returning 0 if parsing fails.
     */
    private int parseIntSafely(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Safely parse long from string, returning 0 if parsing fails.
     */
    private long parseLongSafely(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * Append non-validated players to errors.csv file with separator lines.
     *
     * @param nonValidatedPlayers List of CSV lines for players with UNKNOWN class
     * @throws IOException if file writing fails
     */
    private void appendNonValidatedPlayersToErrors(List<String> nonValidatedPlayers) throws IOException {
        try (FileWriter writer = new FileWriter("errors.csv", true)) { // Append mode
            // Add two separator lines
            writer.write("\n");
            writer.write("\n");

            // Add all non-validated players
            for (String playerLine : nonValidatedPlayers) {
                writer.write(playerLine + "\n");
            }
        }

        LOGGER.info("Appended " + nonValidatedPlayers.size() + " non-validated players to errors.csv");
    }
}
