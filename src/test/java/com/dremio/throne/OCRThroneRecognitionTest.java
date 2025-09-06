package com.dremio.throne;

import org.junit.Test;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Unit test for OCRThroneRecognition.
 */
public class OCRThroneRecognitionTest {
    
    private static final Logger LOGGER = Logger.getLogger(OCRThroneRecognitionTest.class.getName());
    
    @Test
    public void testExtractSpecificExamples() {
        OCRThroneRecognition recognition = new OCRThroneRecognition();
        
        LOGGER.info("=== OCR Throne Recognition Test ===");
        
        // Test case 1: Simple line
        String input1 = "28 | PT (Fate x | TurboDedek | Jaune | 13 | 40 | 1343286 | 703574 | 23911";
        List<String> result1 = recognition.extractToCSV(input1);
        
        assertEquals("Should extract one line", 1, result1.size());
        String expected1 = "TurboDedek,13,40,1343286,703574,23911"; // Player name preserved, only numeric values cleaned
        assertEquals("Should match expected output", expected1, result1.get(0));
        LOGGER.info("✅ Test 1 passed: " + result1.get(0));

        // Test case 2: Complex line with artifacts
        String input2 = "EE ——  —\"—]— ]——]—\"—]—]——]- \"|\" —\"— —\"——— \"|. \"…\"….…. —\" —.]\"| |. -—]-—-- \" ( No as Le D. -_— 2]. ZE DE —61 | OI \\KF Suits® | RoiBoo | Rouge | 0 | 49 | 170 976 | 1062532 | 2743712";
        List<String> result2 = recognition.extractToCSV(input2);

        assertEquals("Should extract one line", 1, result2.size());
        String expected2 = "RoiBoo,0,49,170976,1062532,2743712"; // Player name preserved, only numeric values cleaned
        assertEquals("Should match expected output", expected2, result2.get(0));
        LOGGER.info("✅ Test 2 passed: " + result2.get(0));
    }

    @Test
    public void testLReplacementInNumericColumns() {
        OCRThroneRecognition recognition = new OCRThroneRecognition();

        LOGGER.info("=== OCR L to 1 Replacement Test ===");

        // Test case: Line with 'L' in numeric column (your example)
        String input = "51 | cn | L\\12 Suits Y | Listrinda | Rouge | L | 16 | 407 594 | 969 239 | 58 864";
        List<String> result = recognition.extractToCSV(input);

        assertEquals("Should extract one line", 1, result.size());
        String expected = "Listrinda,1,16,407594,969239,58864"; // Player name preserved, only standalone 'L' in numeric column replaced
        assertEquals("Should replace L with 1 only in numeric columns, preserve player names", expected, result.get(0));
        LOGGER.info("✅ L replacement test passed: " + result.get(0));
    }

    @Test
    public void testOReplacementInNumericColumns() {
        OCRThroneRecognition recognition = new OCRThroneRecognition();

        LOGGER.info("=== OCR o to 0 Replacement Test ===");

        // Test case: Line with 'o' in numeric column (your example)
        String input = "Triber | Jaune |  o | 90 | 513 371 | 1319237 | 2566961";
        List<String> result = recognition.extractToCSV(input);

        assertEquals("Should extract one line", 1, result.size());
        String expected = "Triber,0,90,513371,1319237,2566961"; // 'o' replaced with '0'
        assertEquals("Should replace o with 0 in numeric columns", expected, result.get(0));
        LOGGER.info("✅ o replacement test passed: " + result.get(0));
    }

    @Test
    public void testCombinedOCRErrorReplacements() {
        OCRThroneRecognition recognition = new OCRThroneRecognition();

        LOGGER.info("=== Combined OCR Error Replacement Test ===");

        // Test case: Line with both standalone 'L' and 'o' errors
        String input = "Player | Rouge | L | o | 123 | 456 789 | 105";
        List<String> result = recognition.extractToCSV(input);

        assertEquals("Should extract one line", 1, result.size());
        String expected = "Player,1,0,123,456789,105"; // Standalone 'L'→'1', 'o'→'0'
        assertEquals("Should replace standalone L and o correctly", expected, result.get(0));
        LOGGER.info("✅ Combined replacement test passed: " + result.get(0));
    }
    
    @Test
    public void testMultipleLines() {
        OCRThroneRecognition recognition = new OCRThroneRecognition();
        
        String multiLineInput = 
            "28 | PT (Fate x | TurboDedek | Jaune | 13 | 40 | 1343286 | 703574 | 23911\n" +
            "29 | SC | À Suits | Charizma | Rouge | 68 | 48 | 2635209 | 849361 | 22065\n" +
            "Invalid line without color\n" +
            "30 | DD | 4 suits | JustReky | Jaune | 50 | 62 | 2112643 | 1128903 | 30012";
        
        List<String> results = recognition.extractToCSV(multiLineInput);
        
        assertEquals("Should extract 3 valid lines", 3, results.size());

        assertEquals("TurboDedek,13,40,1343286,703574,23911", results.get(0)); // Player name preserved
        assertEquals("Charizma,68,48,2635209,849361,22065", results.get(1));
        assertEquals("JustReky,50,62,2112643,1128903,30012", results.get(2));
        
        LOGGER.info("✅ Multi-line test passed:");
        for (int i = 0; i < results.size(); i++) {
            LOGGER.info("  Line " + (i + 1) + ": " + results.get(i));
        }
    }
    
    @Test
    public void testEdgeCases() {
        OCRThroneRecognition recognition = new OCRThroneRecognition();
        
        // Empty input
        List<String> emptyResult = recognition.extractToCSV("");
        assertTrue("Empty input should return empty list", emptyResult.isEmpty());
        
        // Null input
        List<String> nullResult = recognition.extractToCSV(null);
        assertTrue("Null input should return empty list", nullResult.isEmpty());
        
        // Line without color
        String noColorLine = "1 | 2 | 3 | 4 | 5";
        List<String> noColorResult = recognition.extractToCSV(noColorLine);
        assertTrue("Line without color should return empty list", noColorResult.isEmpty());
        
        // Line with color but no name column before it
        String noNameLine = "Rouge | 1 | 2 | 3";
        List<String> noNameResult = recognition.extractToCSV(noNameLine);
        assertTrue("Line without name before color should return empty list", noNameResult.isEmpty());
        
        LOGGER.info("✅ Edge cases test passed");
    }
    
    @Test
    public void testRealTesseractOutput() {
        OCRThroneRecognition recognition = new OCRThroneRecognition();
        
        // Sample from actual tesseract output
        String realOutput = 
            "70 DT Fate x | Layali | | 0 | 8 | 24593 | 51555 | 250 663\n" +
            "71 | PP (Fate x | Macell | Jaune | 0 | 3 | 63155 | 535 321 | 57020 |\n" +
            "61 | OI \\KF Suits® | RoiBoo | Rouge | 0 | 49 | 170 976 | 1062532 | 2743712\n" +
            "25 | OT 1 Fate x | Kagiba | Jaune | 16 C7 | 1523416 | 33557 1";
        
        List<String> results = recognition.extractToCSV(realOutput);
        
        assertFalse("Should extract some lines", results.isEmpty());
        
        LOGGER.info("✅ Real tesseract output test:");
        for (int i = 0; i < results.size(); i++) {
            LOGGER.info("  Extracted " + (i + 1) + ": " + results.get(i));
        }
        
        // Verify specific extractions (player names are now preserved)
        boolean foundMacell = results.stream().anyMatch(line -> line.startsWith("Macell,"));
        boolean foundRoiBoo = results.stream().anyMatch(line -> line.startsWith("RoiBoo,")); // Player name preserved

        assertTrue("Should find Macell", foundMacell);
        assertTrue("Should find RoiBoo (player name preserved)", foundRoiBoo);

        // Note: Kagiba might be filtered out if it doesn't have exactly 6 columns
        // This is expected behavior with the new validation
    }
}
