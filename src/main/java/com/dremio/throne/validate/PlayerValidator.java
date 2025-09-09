package com.dremio.throne.validate;

import com.dremio.throne.db.Player;
import com.dremio.throne.util.Labels;
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
  public void validatePlayers(List<Player> players) {
    int validatedCount = 0;
    int warningCount = 0;
    List<Player> nonValidatedPlayers = new ArrayList<>();

    for (Player player : players) {
      try {
        if (player.getClassName().equalsIgnoreCase(Labels.UNKNOWN)) {
          continue;
        }

        boolean hasWarning = validatePlayerStats(player);
        if (hasWarning) {
          nonValidatedPlayers.add(player);
        }
      } catch (Exception e) {
        LOGGER.warning("Failed to validate player: " + player.getName() + " - " + e.getMessage());
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

  private boolean validatePlayerStats(Player player) {
    boolean hasWarning = false;

    String playerName = player.getName();
    String playerClass = player.getClassName();
    long kills = player.getKills();
    long assists = player.getAssists();
    long damageDone = player.getDamageDone();
    long damageReceived = player.getDamageReceived();
    long healing = player.getHealing();

    // Universal rules for all players
    if (kills > 200) {
      LOGGER.warning("SUSPICIOUS KILLS: " + playerName + " has " + kills + " kills (>200) - ");
      hasWarning = true;
    }

    if (assists < 5 || assists > 150) {
      LOGGER.warning("SUSPICIOUS ASSISTS: " + playerName + " has " + assists + " assists (should be 5-150) - ");
      hasWarning = true;
    }

    if (damageDone < 10000 || damageDone > 8000000) {
      LOGGER.warning("SUSPICIOUS DAMAGE DONE: " + playerName + " has " + damageDone + " damage done (should be 10,000-8,000,000) - ");
      hasWarning = true;
    }

    if (damageReceived < 200000 || damageReceived > 3000000) {
      LOGGER.warning("SUSPICIOUS DAMAGE RECEIVED: " + playerName + " has " + damageReceived + " damage received (should be 200,000-3,000,000) - ");
      hasWarning = true;
    }

    if (healing == 0) {
      LOGGER.warning("SUSPICIOUS HEALING: " + playerName + " has 0 healing - ");
      hasWarning = true;
    }

    // Class-specific rules
    if (!playerClass.equalsIgnoreCase("tank") && !playerClass.equalsIgnoreCase("healer")) {
      // Rules for non-tank, non-healer classes
      if (assists < 20) {
        LOGGER.warning("SUSPICIOUS ASSISTS (Non-Tank/Healer): " + playerName + " (" + playerClass + ") has " + assists + " assists (<20) - ");
        hasWarning = true;
      }

      if (kills < 10) {
        LOGGER.warning("SUSPICIOUS KILLS (Non-Tank/Healer): " + playerName + " (" + playerClass + ") has " + kills + " kills (<10) - ");
        hasWarning = true;
      }

      if (damageDone < 500000) {
        LOGGER.warning("SUSPICIOUS DAMAGE DONE (Non-Tank/Healer): " + playerName + " (" + playerClass + ") has " + damageDone + " damage done (<500,000) - ");
        hasWarning = true;
      }

      if (damageReceived < 300000) {
        LOGGER.warning("SUSPICIOUS DAMAGE RECEIVED (Non-Tank/Healer): " + playerName + " (" + playerClass + ") has " + damageReceived + " damage received (<300,000) - ");
        hasWarning = true;
      }
    }

    if (playerClass.equalsIgnoreCase("healer")) {
      // Rules specific to healers
      if (assists < 20) {
        LOGGER.warning("SUSPICIOUS ASSISTS (Healer): " + playerName + " has " + assists + " assists (<20) - ");
        hasWarning = true;
      }

      if (healing < 800000 || healing > 5000000) {
        LOGGER.warning("SUSPICIOUS HEALING (Healer): " + playerName + " has " + healing + " healing (should be 800,000-5,000,000) - ");
        hasWarning = true;
      }
    }

    return hasWarning;
  }


  /**
   * Append non-validated players to errors.csv file with separator lines.
   *
   * @param nonValidatedPlayers List of CSV lines for players with UNKNOWN class
   * @throws IOException if file writing fails
   */
  private void appendNonValidatedPlayersToErrors(List<Player> nonValidatedPlayers) throws IOException {
    try (FileWriter writer = new FileWriter("errors.csv", true)) { // Append mode
      // Add two separator lines
      writer.write("\n");
      writer.write("\n");

      // Add all non-validated players
      for (Player playerLine : nonValidatedPlayers) {
        writer.write(playerLine + "\n");
      }
    }

    LOGGER.info("Appended " + nonValidatedPlayers.size() + " non-validated players to errors.csv");
  }
}
