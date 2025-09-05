package com.dremio.throne;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Main entry point for OCR batch processing with configurable language.
 * Usage: java -jar throne.jar <language> [input_folder] [output_csv]
 */
public class OCRMain {

    private static final Logger LOGGER = Logger.getLogger(OCRMain.class.getName());

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        String language = args[0];
        String inputFolder = args.length > 1 ? args[1] : "img";
        String outputCsv = args.length > 2 ? args[2] : "output.csv";

        // Validate Tesseract installation
        if (!isTesseractInstalled()) {
            System.err.println("❌ ERROR: Tesseract is not installed or not found in PATH");
            System.err.println("Please install Tesseract OCR:");
            System.err.println("  macOS: brew install tesseract");
            System.err.println("  Ubuntu: sudo apt-get install tesseract-ocr");
            System.err.println("  Windows: Download from https://github.com/UB-Mannheim/tesseract/wiki");
            System.exit(1);
        }

        // Validate input folder
        File inputDir = new File(inputFolder);
        if (!inputDir.exists()) {
            System.err.println("❌ ERROR: Input folder does not exist: " + inputDir.getAbsolutePath());
            System.exit(1);
        }

        if (!inputDir.isDirectory()) {
            System.err.println("❌ ERROR: Input path is not a directory: " + inputDir.getAbsolutePath());
            System.exit(1);
        }

        // Check for image files
        File[] imageFiles = inputDir.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                   lower.endsWith(".tiff") || lower.endsWith(".tif") || lower.endsWith(".bmp") ||
                   lower.endsWith(".gif");
        });

        if (imageFiles == null || imageFiles.length == 0) {
            System.err.println("❌ ERROR: No image files found in: " + inputDir.getAbsolutePath());
            System.err.println("Supported formats: PNG, JPG, JPEG, TIFF, TIF, BMP, GIF");
            System.exit(1);
        }

        LOGGER.info("=== OCR Batch Processing ===");
        LOGGER.info("Language: " + language);
        LOGGER.info("Input folder: " + inputDir.getAbsolutePath());
        LOGGER.info("Output CSV: " + outputCsv);
        LOGGER.info("Images found: " + imageFiles.length);

        try {
            OCRBatchProcessor processor = new OCRBatchProcessor(language);
            int processedCount = processor.processAllImagesToCSV(inputFolder, outputCsv);

            File csvFile = new File(outputCsv);
            if (csvFile.exists()) {
                LOGGER.info("✅ Processing complete!");
                LOGGER.info("Images processed: " + processedCount);
                LOGGER.info("CSV file: " + csvFile.getAbsolutePath());
                LOGGER.info("File size: " + csvFile.length() + " bytes");
            } else {
                System.err.println("❌ ERROR: No CSV file was created - no valid data found");
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR: Processing failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Check if Tesseract is installed and accessible.
     */
    private static boolean isTesseractInstalled() {
        try {
            ProcessBuilder pb = new ProcessBuilder("tesseract", "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
    
    private static void printUsage() {
        System.out.println("OCR Batch Processor");
        System.out.println("Usage: java -jar throne.jar <language> [input_folder] [output_csv]");
        System.out.println();
        System.out.println("Parameters:");
        System.out.println("  language      - OCR language code (e.g., 'eng', 'fra', 'deu', 'spa')");
        System.out.println("  input_folder  - Path to folder containing images (default: img)");
        System.out.println("  output_csv    - Output CSV filename (default: output.csv)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar throne.jar fra");
        System.out.println("  java -jar throne.jar eng /path/to/images");
        System.out.println("  java -jar throne.jar fra /path/to/images results.csv");
        System.out.println();
        System.out.println("Language codes: eng, fra, deu, spa, ita, por, rus, chi_sim, jpn");
    }
}
