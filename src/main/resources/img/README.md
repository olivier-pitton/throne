# Test Images

This folder contains test images for OCR processing tests.

**IMPORTANT**: This folder and its contents must never be removed as tests depend on it.

The tests will fail if:
- This img folder doesn't exist
- The folder is not accessible
- No image files are found in this folder

## Test Images

Add your test images here in the following formats:
- PNG (.png)
- JPEG (.jpg, .jpeg)
- TIFF (.tiff, .tif)
- BMP (.bmp)
- GIF (.gif)

## For Developers

When adding new test images:
1. Place them in this folder
2. Update tests if needed
3. Never remove this folder or its contents
4. Ensure images contain pipe-separated tabular data for OCR testing
