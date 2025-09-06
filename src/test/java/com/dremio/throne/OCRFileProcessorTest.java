package com.dremio.throne;

import org.junit.Test;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Unit test for OCRFileProcessor.
 */
public class OCRFileProcessorTest {
    
    private static final Logger LOGGER = Logger.getLogger(OCRFileProcessorTest.class.getName());
    
    @Test
    public void testProcessAllImagesWithFrenchOCR() throws Exception {
        // Setup
        String imageFolder = "src/test/resources/img";
        File imgDir = new File(imageFolder);

        // Verify test folder exists
        if (!imgDir.exists() || !imgDir.isDirectory()) {
            LOGGER.warning("Test image folder not found: " + imageFolder);
            return; // Skip test if folder doesn't exist
        }

        // Get all image files
        File[] imageFiles = imgDir.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                   lower.endsWith(".tiff") || lower.endsWith(".tif") || lower.endsWith(".bmp") ||
                   lower.endsWith(".gif");
        });

        if (imageFiles == null || imageFiles.length == 0) {
            LOGGER.warning("No image files found in: " + imageFolder);
            return;
        }

        LOGGER.info("=== OCR File Processor Test - All Images ===");
        LOGGER.info("Found " + imageFiles.length + " images to process");

        // Create French OCR service
        OCRService frenchOCR = new OCRService("fra");

        // Use a test-specific output file to avoid overwriting any existing files
        String testOutputFile = "test_tesseract_output.txt";
        File tesseractFile = new File(testOutputFile);

        // Clean up any existing test output file
        if (tesseractFile.exists()) {
            tesseractFile.delete();
        }

        int totalOutputLength = 0;

        // Process each image and append to test output file
        for (File imageFile : imageFiles) {
            LOGGER.info("Processing: " + imageFile.getName());

            // Create processor for this image with custom output file
            OCRFileProcessor processor = new OCRFileProcessor(imageFile.getAbsolutePath(), frenchOCR, testOutputFile);

            // Execute processing (this appends to test output file)
            String ocrOutput = processor.call();

            // Verify output
            assertNotNull("OCR output should not be null for " + imageFile.getName(), ocrOutput);
            totalOutputLength += ocrOutput.length();
        }

        // Verify test output file was created and contains all outputs
        assertTrue("Test output file should be created", tesseractFile.exists());

        String finalContent = new String(Files.readAllBytes(Paths.get(testOutputFile)));
        assertFalse("Final tesseract.txt should not be empty", finalContent.trim().isEmpty());

        LOGGER.info("Total images processed: " + imageFiles.length);
        LOGGER.info("Total OCR output length: " + totalOutputLength + " characters");
        LOGGER.info("Final test output file size: " + tesseractFile.length() + " bytes");
        LOGGER.info("✅ Test completed successfully");

        // Clean up test file after verification
        if (tesseractFile.exists()) {
            tesseractFile.delete();
        }
    }
    
    @Test
    public void testProcessNonExistentFile() {
        // Setup
        String nonExistentPath = "src/test/resources/img/nonexistent.png";
        OCRService frenchOCR = new OCRService("fra");
        OCRFileProcessor processor = new OCRFileProcessor(nonExistentPath, frenchOCR);
        
        // Execute and verify exception
        try {
            processor.call();
            fail("Should throw exception for non-existent file");
        } catch (Exception e) {
            assertTrue("Should be IOException", e instanceof java.io.IOException);
            assertTrue("Error message should mention file not found", 
                      e.getMessage().contains("does not exist"));
        }
    }
}
