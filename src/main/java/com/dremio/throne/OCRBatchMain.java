package com.dremio.throne;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Main class for batch OCR processing using OCRFileProcessor.
 * Usage: java OCRBatchMain <folder> [output.csv] [language]
 */
public class OCRBatchMain {
    
    private static final Logger LOGGER = Logger.getLogger(OCRBatchMain.class.getName());
    
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }
        
        String imageFolder = args[0];
        String outputCsv = args.length > 1 ? args[1] : "output.csv";
        String language = args.length > 2 ? args[2] : "eng";
        
        LOGGER.info("=== OCR Batch Processing ===");
        LOGGER.info("Image folder: " + imageFolder);
        LOGGER.info("Output CSV: " + outputCsv);
        LOGGER.info("Language: " + language);
        
        try {
            OCRBatchMain processor = new OCRBatchMain();
            int processedCount = processor.processImages(imageFolder, outputCsv, language);
            
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
     * @param outputCsv Path to output CSV file
     * @param language OCR language code
     * @return Number of images processed
     * @throws Exception if processing fails
     */
    public int processImages(String imageFolder, String outputCsv, String language) throws Exception {
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
        
        // Process each image using OCRFileProcessor
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
                processor.call();
                processedCount++;
                
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
            
            // Write to output CSV
            writeCSV(csvLines, outputCsv);
            
            LOGGER.info("Extracted " + csvLines.size() + " valid data lines");
        }
        
        // Clean up temporary file
        if (tempFile.exists()) {
            tempFile.delete();
        }
        
        return processedCount;
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
        System.out.println("Usage: java OCRBatchMain <folder> [output.csv] [language]");
        System.out.println();
        System.out.println("Parameters:");
        System.out.println("  folder      - Path to folder containing images (required)");
        System.out.println("  output.csv  - Output CSV filename (default: output.csv)");
        System.out.println("  language    - OCR language code (default: eng)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java OCRBatchMain ./images");
        System.out.println("  java OCRBatchMain ./images results.csv");
        System.out.println("  java OCRBatchMain ./images results.csv fra");
        System.out.println();
        System.out.println("Supported languages: eng, fra, deu, spa, ita, por, etc.");
    }
}
