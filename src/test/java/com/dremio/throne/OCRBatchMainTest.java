package com.dremio.throne;

import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test to run OCRBatchMain and validate output matches expected results.
 */
public class OCRBatchMainTest {
    
    @Before
    public void setUp() {
        // Clean up any existing output files before each test
        cleanupFiles();
    }
    
    @Test
    public void testOCRBatchMainExecution() throws Exception {
        // Test parameters: src/test/resources/img fra r Fate output.csv "2025-09-03 21:00"
        String[] args = {
            "src/test/resources/img",
            "fra",
            "r",
            "Fate",
            "output.csv",
            "2025-09-03 21:00"
        };

        // Run the main method
        OCRBatchMain.main(args);

        // Load expected output from test resources
        URL expectedUrl = getClass().getClassLoader().getResource("output.csv");
        assertNotNull("Expected output.csv file not found in test resources", expectedUrl);

        File expectedFile = new File(expectedUrl.toURI());
        assertTrue("Expected output.csv file not accessible", expectedFile.exists());

        // Load actual generated output
        File actualFile = new File("output.csv");
        assertTrue("Generated output.csv file should exist", actualFile.exists());

        // Read both files
        List<String> expectedLines = Files.readAllLines(expectedFile.toPath());
        List<String> actualLines = Files.readAllLines(actualFile.toPath());

        // Compare line counts
        assertEquals("Output should have same number of lines as expected",
                    expectedLines.size(), actualLines.size());

        // Compare each line
        for (int i = 0; i < expectedLines.size(); i++) {
            String expectedLine = expectedLines.get(i).trim();
            String actualLine = actualLines.get(i).trim();

            assertEquals("Line " + (i + 1) + " should match expected output",
                       expectedLine, actualLine);
        }

        System.out.println("✅ OCRBatchMain execution completed and output matches expected results perfectly!");
        System.out.println("Validated " + actualLines.size() + " lines successfully");
    }
    
    private void cleanupFiles() {
        // List of files to clean up
        String[] filesToCleanup = {
            "output.csv",
            "errors.csv", 
            "tesseract_output.txt",
            "temp_ocr_output.txt",
            "integration_test_output.csv"
        };
        
        for (String filename : filesToCleanup) {
            File file = new File(filename);
            if (file.exists()) {
                file.delete();
                System.out.println("Cleaned up: " + filename);
            }
        }
    }
}
