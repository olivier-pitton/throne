package com.dremio.throne.ocr;

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

    /**
     * Constructor for OCR file processor with custom output file.
     *
     * @param filename Path to the image file to process
     * @param ocrService Preconfigured OCR service
     */
    public OCRFileProcessor(String filename, OCRService ocrService) {
        this.filename = filename;
        this.ocrService = ocrService;
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
        return ocrService.extractText(imageFile);
    }
}
