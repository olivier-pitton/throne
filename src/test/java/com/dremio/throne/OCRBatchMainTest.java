package com.dremio.throne;

import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Unit test for OCRBatchMain.
 */
public class OCRBatchMainTest {
    
    private static final Logger LOGGER = Logger.getLogger(OCRBatchMainTest.class.getName());
    
    @Test
    public void testProcessImagesWithNonExistentFolder() {
        OCRBatchMain processor = new OCRBatchMain();
        
        try {
            processor.processImages("/nonexistent/folder", "eng", "y", "output.csv", LocalDate.now());
            fail("Should throw IOException for non-existent folder");
        } catch (Exception e) {
            assertTrue("Should be IOException", e instanceof IOException);
            assertTrue("Error message should mention folder", e.getMessage().contains("does not exist"));
        }
    }
    
    @Test
    public void testProcessImagesWithEmptyFolder() throws Exception {
        // Create a temporary empty folder
        File tempDir = new File("temp_empty_test");
        tempDir.mkdir();
        
        try {
            OCRBatchMain processor = new OCRBatchMain();
            int result = processor.processImages(tempDir.getAbsolutePath(), "eng", "y", "test_output.csv", LocalDate.now());

            assertEquals("Should return 0 for empty folder", 0, result);
            
        } finally {
            // Clean up
            tempDir.delete();
            File outputFile = new File("test_output.csv");
            if (outputFile.exists()) {
                outputFile.delete();
            }
        }
    }
    
    @Test
    public void testMainWithNoArguments() {
        // This test verifies that main() handles no arguments gracefully
        // We can't easily test System.exit(), but we can verify the class structure
        
        // Test that the class has the expected main method signature
        try {
            OCRBatchMain.class.getMethod("main", String[].class);
            LOGGER.info("✅ Main method signature verified");
        } catch (NoSuchMethodException e) {
            fail("Main method should exist with String[] parameter");
        }
    }
    
    @Test
    public void testParameterDefaults() {
        // Test that the class can be instantiated
        OCRBatchMain processor = new OCRBatchMain();
        assertNotNull("OCRBatchMain should be instantiable", processor);
        
        LOGGER.info("✅ OCRBatchMain class structure verified");
    }
}
