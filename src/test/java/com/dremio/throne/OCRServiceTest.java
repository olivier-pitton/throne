package com.dremio.throne;

import org.junit.Test;
import java.io.File;
import java.util.logging.Logger;

/**
 * Simple test for OCR functionality.
 */
public class OCRServiceTest {
    
    private static final Logger LOGGER = Logger.getLogger(OCRServiceTest.class.getName());
    
    @Test
    public void testBatchOCRAndCreateCSV() {
        // Process all images in the img/ folder
        String inputFolder = "src/test/resources/img";
        String outputCSV = "output.csv";

        File imgFolder = new File(inputFolder);
        if (!imgFolder.exists() || !imgFolder.isDirectory()) {
            LOGGER.warning("Image folder not found: " + imgFolder.getAbsolutePath());
            return;
        }

        LOGGER.info("=== Batch OCR + CSV Test for All Images ===");
        LOGGER.info("Processing folder: " + imgFolder.getAbsolutePath());

        try {
            // Process all images in the folder using batch processor with French language
            OCRBatchProcessor batchProcessor = new OCRBatchProcessor("fra");
            int processedCount = batchProcessor.processAllImagesToCSV(inputFolder, outputCSV);

            // Verify CSV file was created
            File csvFile = new File(outputCSV);
            if (csvFile.exists()) {
                LOGGER.info("CSV file created successfully: " + csvFile.getAbsolutePath());
                LOGGER.info("File size: " + csvFile.length() + " bytes");
                LOGGER.info("Images processed: " + processedCount);
            } else {
                LOGGER.warning("CSV file was not created");
            }

        } catch (Exception e) {
            LOGGER.warning("Error in batch OCR + CSV test: " + e.getMessage());
        }

        LOGGER.info("=== End of Batch OCR + CSV Test ===");
    }
}
