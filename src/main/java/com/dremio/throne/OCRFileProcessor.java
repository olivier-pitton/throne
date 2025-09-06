package com.dremio.throne;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Callable service for processing a single image file with Tesseract OCR.
 * Writes the raw OCR output to tesseract.txt file.
 */
public class OCRFileProcessor implements Callable<String> {

    private static final Logger LOGGER = Logger.getLogger(OCRFileProcessor.class.getName());
    
    private final String filename;
    private final OCRService ocrService;
    private final String outputFile;
    
    /**
     * Constructor for OCR file processor.
     *
     * @param filename Path to the image file to process
     * @param ocrService Preconfigured OCR service
     */
    public OCRFileProcessor(String filename, OCRService ocrService) {
        this(filename, ocrService, "tesseract.txt");
    }

    /**
     * Constructor for OCR file processor with custom output file.
     *
     * @param filename Path to the image file to process
     * @param ocrService Preconfigured OCR service
     * @param outputFile Path to the output file
     */
    public OCRFileProcessor(String filename, OCRService ocrService, String outputFile) {
        this.filename = filename;
        this.ocrService = ocrService;
        this.outputFile = outputFile;
    }
    
    /**
     * Process the image file and write raw OCR output to tesseract.txt.
     * 
     * @return The raw OCR text output
     * @throws Exception if OCR processing or file writing fails
     */
    @Override
    public String call() throws Exception {
        File imageFile = new File(filename);
        
        if (!imageFile.exists()) {
            throw new IOException("Image file does not exist: " + filename);
        }
        
        LOGGER.info("Processing image: " + filename);
        
        // Extract text using OCR
        String ocrOutput = ocrService.extractText(imageFile);
        
        // Write raw output to tesseract.txt
        writeToTesseractFile(ocrOutput);
        
        LOGGER.info("OCR output written to " + outputFile);

        return ocrOutput;
    }
    
    /**
     * Append the OCR output to the configured output file.
     *
     * @param ocrOutput The raw OCR text to append
     * @throws IOException if file writing fails
     */
    private void writeToTesseractFile(String ocrOutput) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile, true)) { // true = append mode
            writer.write(ocrOutput);
        }
    }
}
