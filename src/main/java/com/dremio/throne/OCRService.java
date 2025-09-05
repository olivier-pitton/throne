package com.dremio.throne;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * OCR Service using Tesseract OCR engine via Tess4J wrapper.
 * Provides text extraction capabilities from images.
 */
public class OCRService {
    
    private static final Logger LOGGER = Logger.getLogger(OCRService.class.getName());
    private final Tesseract tesseract;
    
    /**
     * Constructor that initializes the Tesseract instance with specified language.
     *
     * @param language Language code for OCR (e.g., "eng", "fra", "deu", "spa")
     */
    public OCRService(String language) {
        this.tesseract = new Tesseract();
        tesseract.setLanguage(language);

        // Optional: Set tessdata path if you have custom location
        // tesseract.setDatapath("/path/to/tessdata");


    }

    /**
     * Constructor that allows custom tessdata path and language.
     *
     * @param language Language code for OCR (e.g., "eng", "fra", "deu", "spa")
     * @param tessdataPath Path to the tessdata directory containing language files
     */
    public OCRService(String language, String tessdataPath) {
        this.tesseract = new Tesseract();
        tesseract.setDatapath(tessdataPath);
        tesseract.setLanguage(language);

        LOGGER.info("OCR Service initialized with language: " + language + " and tessdata path: " + tessdataPath);
    }
    
    /**
     * Extract text from an image file.
     * 
     * @param imageFile The image file to process
     * @return Extracted text from the image
     * @throws OCRException if OCR processing fails
     */
    public String extractText(File imageFile) throws OCRException {
        if (imageFile == null || !imageFile.exists()) {
            throw new OCRException("Image file does not exist: " + imageFile);
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
