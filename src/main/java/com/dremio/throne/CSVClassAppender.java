package com.dremio.throne;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CSVClassAppender {

    private static final String FOLDER = "C:\\Users\\Windows\\Downloads\\parser\\wg";
    
    public static void main(String[] args) {
        Arrays.stream(Objects.requireNonNull(new File(FOLDER).listFiles(pathname -> pathname != null && pathname.isDirectory()))).map(file -> file.getAbsolutePath() + File.separator + "output.csv").forEach(CSVClassAppender::process);
    }

    private static void process(String inputFile) {
        File input = new File(inputFile);
        System.out.println("Processing file " + inputFile);

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
                        String playerName = PlayerNameMatcher.match(parts[2]);
                        String playerClass = PlayerClassLoader.getPlayerClass(playerName, playerClasses);

                        StringBuilder newLine = new StringBuilder();
                        newLine.append(parts[0]).append(",");
                        newLine.append(parts[1]).append(",");
                        newLine.append(playerName).append(",");
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
