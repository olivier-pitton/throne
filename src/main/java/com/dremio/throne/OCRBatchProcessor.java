package com.dremio.throne;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Batch OCR processor that extracts text from all images in a folder
 * and saves the results to text files in an output folder.
 */
public class OCRBatchProcessor {
    
    private static final Logger LOGGER = Logger.getLogger(OCRBatchProcessor.class.getName());
    
    // Supported image file extensions
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(
        ".png", ".jpg", ".jpeg", ".tiff", ".tif", ".bmp", ".gif"
    );
    
    private final OCRService ocrService;

    /**
     * Initialize the batch processor with specified OCR language.
     *
     * @param language OCR language code (e.g., "eng", "fra", "deu", "spa")
     */
    public OCRBatchProcessor(String language) {
        this.ocrService = new OCRService(language);
    }

    /**
     * Initialize the batch processor with French OCR service (default).
     * @deprecated Use OCRBatchProcessor(String language) instead
     */
    @Deprecated
    public OCRBatchProcessor() {
        this("fra"); // Default to French for backward compatibility
    }
    
    /**
     * Main method to run the batch OCR processor.
     * @deprecated Use OCRMain class instead for better parameter handling
     *
     * @param args Command line arguments: [input_folder] [output_folder]
     */
    @Deprecated
    public static void main(String[] args) {
        if (args.length != 2) {
            LOGGER.severe("Usage: java OCRBatchProcessor <input_folder> <output_folder>");
            LOGGER.info("Note: Use OCRMain for language parameter support");
            System.exit(1);
        }

        String inputFolder = args[0];
        String outputFolder = args[1];

        OCRBatchProcessor processor = new OCRBatchProcessor(); // Uses default French

        try {
            int processedCount = processor.processFolder(inputFolder, outputFolder);
            LOGGER.info("Successfully processed " + processedCount + " images.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing folder: " + e.getMessage(), e);
            System.exit(1);
        }
    }
    
    /**
     * Process all images in the input folder and save extracted text to output folder.
     * 
     * @param inputFolderPath Path to folder containing images
     * @param outputFolderPath Path to folder where text files will be saved
     * @return Number of images successfully processed
     * @throws IOException if folder operations fail
     */
    public int processFolder(String inputFolderPath, String outputFolderPath) throws IOException {
        File inputFolder = new File(inputFolderPath);
        File outputFolder = new File(outputFolderPath);
        
        // Validate input folder
        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            throw new IOException("Input folder does not exist or is not a directory: " + inputFolderPath);
        }
        
        // Create output folder if it doesn't exist
        if (!outputFolder.exists()) {
            if (!outputFolder.mkdirs()) {
                throw new IOException("Failed to create output folder: " + outputFolderPath);
            }
            LOGGER.info("Created output folder: " + outputFolderPath);
        }
        
        // Get all image files
        File[] imageFiles = inputFolder.listFiles(this::isImageFile);
        if (imageFiles == null || imageFiles.length == 0) {
            LOGGER.info("No image files found in: " + inputFolderPath);
            return 0;
        }

        LOGGER.info("Found " + imageFiles.length + " image files to process...");
        
        int processedCount = 0;
        int errorCount = 0;
        
        for (File imageFile : imageFiles) {
            try {
                processImage(imageFile, outputFolder);
                processedCount++;
                LOGGER.info("✓ Processed: " + imageFile.getName());
            } catch (Exception e) {
                errorCount++;
                LOGGER.log(Level.WARNING, "✗ Failed to process: " + imageFile.getName() + " - " + e.getMessage(), e);
            }
        }

        LOGGER.info("Processing complete:");
        LOGGER.info("  Successfully processed: " + processedCount);
        LOGGER.info("  Errors: " + errorCount);
        LOGGER.info("  Total files: " + imageFiles.length);
        
        return processedCount;
    }
    
    /**
     * Process a single image file and save the extracted text.
     * 
     * @param imageFile The image file to process
     * @param outputFolder The folder to save the text file
     * @throws OCRService.OCRException if OCR processing fails
     * @throws IOException if file operations fail
     */
    private void processImage(File imageFile, File outputFolder) throws OCRService.OCRException, IOException {
        // Extract text using OCR
        String extractedText = ocrService.extractText(imageFile);
        
        // Generate output filename (replace image extension with .txt)
        String imageName = imageFile.getName();
        String baseName = imageName.substring(0, imageName.lastIndexOf('.'));
        String outputFileName = baseName + ".txt";
        
        // Create output file
        File outputFile = new File(outputFolder, outputFileName);
        
        // Write extracted text to file
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("OCR Results for: " + imageName + "\n");
            writer.write("=" + "=".repeat(imageName.length() + 16) + "\n\n");
            
            if (extractedText.trim().isEmpty()) {
                writer.write("No text found in image.\n");
            } else {
                writer.write(extractedText);
            }
            
            writer.write("\n\n--- End of OCR Results ---\n");
        }
        
        LOGGER.info("Saved OCR results to: " + outputFile.getAbsolutePath());
    }
    
    /**
     * Check if a file is a supported image file.
     * 
     * @param file The file to check
     * @return true if the file is a supported image format
     */
    private boolean isImageFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    /**
     * Process all images in a folder and write combined results to CSV.
     */
    public int processAllImagesToCSV(String inputFolderPath, String csvOutputPath) throws IOException {
        File inputFolder = new File(inputFolderPath);
        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            throw new IOException("Input folder does not exist: " + inputFolderPath);
        }

        File[] imageFiles = inputFolder.listFiles(this::isImageFile);
        if (imageFiles == null || imageFiles.length == 0) return 0;

        StringBuilder allOcrText = new StringBuilder();
        int processedCount = 0;

        // Collect OCR text from all images
        for (File imageFile : imageFiles) {
            try {
                LOGGER.info("Processing: " + imageFile.getName());
                String extractedText = ocrService.extractText(imageFile);

                if (!extractedText.trim().isEmpty()) {
                    allOcrText.append(extractedText).append("\n");
                    processedCount++;
                }
            } catch (Exception e) {
                LOGGER.warning("Failed to process: " + imageFile.getName() + " - " + e.getMessage());
            }
        }

        // Process all collected text at once
        if (allOcrText.length() > 0) {
            new OCROutputParser().processToCSV(allOcrText.toString(), csvOutputPath);
        }

        return processedCount;
    }

}
