package com.dremio.throne;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class CSVClassAppender {
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java CSVClassAppender <input.csv>");
            System.exit(1);
        }
        
        String inputFile = args[0];
        File input = new File(inputFile);
        
        if (!input.exists()) {
            System.err.println("Input file does not exist: " + inputFile);
            System.exit(1);
        }
        
        Map<String, String> playerClasses = PlayerClassLoader.loadPlayerClasses();
        
        String outputFile = getOutputFileName(inputFile);
        
        try {
            List<String> inputLines = Files.readAllLines(Paths.get(inputFile));
            
            try (FileWriter writer = new FileWriter(outputFile)) {
                for (String line : inputLines) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        String playerName = parts[2];
                        String playerClass = PlayerClassLoader.getPlayerClass(playerName, playerClasses);
                        
                        StringBuilder newLine = new StringBuilder();
                        newLine.append(parts[0]).append(",");
                        newLine.append(parts[1]).append(",");
                        newLine.append(parts[2]).append(",");
                        newLine.append(playerClass);
                        
                        for (int i = 3; i < parts.length; i++) {
                            newLine.append(",").append(parts[i]);
                        }
                        
                        writer.write(newLine + "\n");
                    } else {
                        writer.write(line + "\n");
                    }
                }
            }
            
            System.out.println("Output written to: " + outputFile);
            
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            System.exit(1);
        }
    }
    

    
    private static String getOutputFileName(String inputFile) {
        int lastDot = inputFile.lastIndexOf('.');
        if (lastDot > 0) {
            return inputFile.substring(0, lastDot) + "_with_classes.csv";
        } else {
            return inputFile + "_with_classes.csv";
        }
    }
}
