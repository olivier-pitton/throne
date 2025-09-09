package com.dremio.throne.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.io.File;
import java.util.logging.Logger;

/**
 * OCR Service using Tesseract for text extraction from images.
 */
public class OCRService {
    
    private final Tesseract tesseract;
    
    /**
     * Initialize OCR service with specified language.
     * 
     * @param language OCR language code (e.g., "eng", "fra", "deu")
     */
    public OCRService(String language) {
        this.tesseract = new Tesseract();
        this.tesseract.setLanguage(language);
        
        // Set tessdata path if available
        String tessdataPath = System.getenv("TESSDATA_PREFIX");
        if (tessdataPath != null) {
            this.tesseract.setDatapath(tessdataPath);
        }
    }
    
    /**
     * Extract text from an image file.
     * 
     * @param imageFile The image file to process
     * @return Extracted text from the image
     * @throws OCRException if OCR processing fails
     */
    public String extractText(File imageFile) throws OCRException {
        if (!imageFile.exists()) {
            throw new OCRException("Image file does not exist: " + imageFile.getAbsolutePath());
        }
        
        try {
            String result = tesseract.doOCR(imageFile);
            return result != null ? result.trim() : "";
        } catch (TesseractException e) {
            throw new OCRException("Failed to extract text from image: " + e.getMessage(), e);
        }
    }

    /**
     * Custom exception for OCR-related errors.
     */
    public static class OCRException extends Exception {
        public OCRException(String message) {
            super(message);
        }

        public OCRException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
