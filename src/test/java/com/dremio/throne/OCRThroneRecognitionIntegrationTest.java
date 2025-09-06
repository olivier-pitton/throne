package com.dremio.throne;

import org.junit.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Integration test for OCRThroneRecognition using real tesseract.txt file.
 */
public class OCRThroneRecognitionIntegrationTest {
    
    private static final Logger LOGGER = Logger.getLogger(OCRThroneRecognitionIntegrationTest.class.getName());
    
    @Test
    public void testWithRealTesseractFile() throws Exception {
        LOGGER.info("=== Integration Test with Real Tesseract Output ===");

        // Load tesseract_input.txt from classpath resources
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tesseract/tesseract_input.txt");

        if (inputStream == null) {
            fail("tesseract_input.txt file not found in classpath at /tesseract/tesseract_input.txt");
        }

        // Read the content from the input stream
        String tesseractContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        inputStream.close();
        
        // Process with OCRThroneRecognition
        OCRThroneRecognition recognition = new OCRThroneRecognition();
        List<String> csvLines = recognition.extractToCSV(tesseractContent);
        
        LOGGER.info("Input content length: " + tesseractContent.length() + " characters");
        LOGGER.info("Extracted CSV lines: " + csvLines.size());
        
        // Verify we extracted some data
        assertFalse("Should extract some CSV lines", csvLines.isEmpty());

        // Load expected.csv from classpath resources
        InputStream expectedStream = getClass().getClassLoader().getResourceAsStream("expected.csv");

        if (expectedStream == null) {
            fail("expected.csv file not found in classpath at /expected.csv");
        }

        // Read the expected content
        String expectedContent = new String(expectedStream.readAllBytes(), StandardCharsets.UTF_8);
        expectedStream.close();

        // Parse expected lines
        String[] expectedLines = expectedContent.trim().split("\n");
        Set<String> expectedSet = new HashSet<>();
        for (String line : expectedLines) {
            if (!line.trim().isEmpty()) {
                expectedSet.add(line.trim());
            }
        }

        // Convert actual results to set for comparison (removes duplicates)
        Set<String> actualSet = new HashSet<>(csvLines);

        LOGGER.info("Expected lines: " + expectedSet.size());
        LOGGER.info("Actual lines extracted: " + csvLines.size());
        LOGGER.info("Actual unique lines: " + actualSet.size());

        // Check that all expected lines are present
        for (String expectedLine : expectedSet) {
            assertTrue("Missing expected line: " + expectedLine, actualSet.contains(expectedLine));
        }

        // Check that no unexpected lines are present
        for (String actualLine : actualSet) {
            assertTrue("Unexpected line found: " + actualLine, expectedSet.contains(actualLine));
        }

        // Verify exact match
        assertEquals("Number of lines should match exactly", expectedSet.size(), actualSet.size());

        LOGGER.info("✅ Integration test completed successfully - all lines match expected.csv");
    }
}
