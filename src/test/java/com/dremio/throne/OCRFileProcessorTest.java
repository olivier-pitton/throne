package com.dremio.throne;

import org.junit.Test;
import java.io.File;
import java.net.URI;
import java.net.URL;
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
        // Get img folder from classpath
        URL imgUrl = getClass().getClassLoader().getResource("img");

        if (imgUrl == null) {
            fail("Test image folder not found in classpath: /img - img folder must exist for tests to run");
        }

        File imgDir = new File(imgUrl.toURI());

        // Verify test folder exists
        if (!imgDir.exists() || !imgDir.isDirectory()) {
            fail("Test image folder not accessible: " + imgDir.getAbsolutePath() + " - img folder must be accessible for tests to run");
        }

        // Get all image files
        File[] imageFiles = imgDir.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                   lower.endsWith(".tiff") || lower.endsWith(".tif") || lower.endsWith(".bmp") ||
                   lower.endsWith(".gif");
        });

        if (imageFiles == null || imageFiles.length == 0) {
            fail("No image files found in: " + imgDir.getAbsolutePath() + " - img folder must contain test images");
        }

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

        LOGGER.info("Processed " + imageFiles.length + " images, output: " + totalOutputLength + " characters");

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
