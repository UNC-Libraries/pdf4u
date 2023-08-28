# ocr4u
OCR: optical character recognition

ocr4u uses OCRmyPDF to add OCR to a PDF or redo OCR for a PDF with existing OCR.
ocr4u uses Tesseract to add OCR to an image or multiple images and outputs a PDF.
Users can also request Tesseract as the OCR engine instead of OCRmyPDF.

Supported image formats: PNG, JPEG, TIFF, JP2, GIF, BMP
For a full list of Tesseract's input formats, refer to https://tesseract-ocr.github.io/tessdoc/InputFormats.
Tesseract does not support PDFs.

##Commands
- `ocr4u ocrmypdf pdf_add_ocr -i <inputPath> -o <outputPath>`: add OCR to a PDF
- `ocr4u ocrmypdf pdf_redo_ocr -i <inputPath> -o <outputPath>`: redo existing OCR in a PDF
- `ocr4u ocrmypdf image_add_ocr -i <inputPath> -o <outputPath>`: add OCR to an image or multiple images using OCRmyPDF.
For multiple images, use a txt file with a list of image filenames for the inputPath.
- `ocr4u tesseract image_add_ocr -i <inputPath> -o <outputPath>`: add OCR to an image or multiple images using Tesseract. 
For multiple images, use a txt file with a list of image filenames for the inputPath.
