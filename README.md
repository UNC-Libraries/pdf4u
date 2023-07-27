# ocr4u
OCR: optical character recognition

ocr4u uses OCRmyPDF to add OCR to a PDF or redo OCR for a PDF with existing OCR.
ocr4u uses Tesseract to add OCR to an image or multiple images and outputs a PDF.

Supported image formats: PNG, JPEG, TIFF, JP2, GIF, BMP

##Commands
- `ocr4u pdf_add_ocr -i <inputPath> -o <outputPath>`: add OCR to a PDF
- `ocr4u pdf_redo_ocr -i <inputPath> -o <outputPath>`: redo existing OCR in a PDF
- `ocr4u image_add_ocr -i <inputPath> -o <outputPath>`: add OCR to an image or multiple images. For multiple images, 
use a txt file with a list of image filenames for the inputPath.