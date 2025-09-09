package com.dremio.throne.db;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility class for loading player classes from class.csv file.
 */
public class PlayerClassLoader {
    
    private static final Logger LOGGER = Logger.getLogger(PlayerClassLoader.class.getName());
    
    /**
     * Load player classes from class.csv file in the current directory.
     * 
     * @return Map of player names to their classes
     */
    public static Map<String, String> loadPlayerClasses() {
        return loadPlayerClasses("class.csv");
    }

    private static File fromURL(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Load player classes from specified CSV file.
     * 
     * @param filename Path to the class CSV file
     * @return Map of player names to their classes
     */
    public static Map<String, String> loadPlayerClasses(String filename) {
        Map<String, String> playerClasses = new HashMap<>();
        File classFile = new File(filename);
        
        if (!classFile.exists()) {
            URL resource = PlayerClassLoader.class.getClassLoader().getResource("class.csv");
            if (resource == null) {
                LOGGER.warning(filename + " file not found in current directory");
                System.exit(-1);
            }
            classFile = fromURL(resource);
        }
        
        try {
            List<String> lines = Files.readAllLines(classFile.toPath());
            
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && line.contains(",")) {
                    String[] parts = line.split(",", 2);
                    if (parts.length == 2) {
                        String playerName = parts[0].trim();
                        String playerClass = parts[1].trim();
                        playerClasses.put(playerName, playerClass);
                    }
                }
            }
            
            LOGGER.info("Loaded " + playerClasses.size() + " player classes from " + filename);
            
        } catch (IOException e) {
            LOGGER.warning("Failed to read " + filename + ": " + e.getMessage() + " - player classes will be UNKNOWN");
        }
        
        return playerClasses;
    }
    
    /**
     * Get player class with case-insensitive lookup.
     * 
     * @param playerName Name of the player
     * @param playerClasses Map of player names to classes
     * @return Player class or "UNKNOWN" if not found
     */
    public static String getPlayerClass(String playerName, Map<String, String> playerClasses) {
        // Case-insensitive lookup
        for (Map.Entry<String, String> entry : playerClasses.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(playerName)) {
                return entry.getValue();
            }
        }
        
        // Fallback to exact match
        return playerClasses.getOrDefault(playerName, "UNKNOWN");
    }
}
