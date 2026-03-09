# pdf4u
OCR: optical character recognition

pdf4u uses OCRmyPDF to add OCR to an image/PDF or redo OCR for a PDF with existing OCR. 
OCRmyPDF uses Tesseract as its OCR engine.

pdf4u uses kraken to add OCR to an image or multiple images and outputs a PDF.

OCRmyPDF supported formats: PNG, JPEG, TIFF, JP2, GIF, BMP, PDF
For a full list of OCRmyPDF/Tesseract's input formats, refer to the [Tesseract documentation](https://tesseract-ocr.github.io/tessdoc/InputFormats).

Kraken does not list its supported formats.

##Commandline Utilities
OCRmyPDF version 14.4.0 ([installation instructions](https://ocrmypdf.readthedocs.io/en/latest/installation.html))

##Commands
- `pdf4u ocrmypdf add_ocr -i <inputPath> -o <outputPath>`: add OCR to an image or PDF
- `pdf4u ocrmypdf redo_ocr -i <inputPath> -o <outputPath>`: redo existing OCR in a PDF
- `pdf4u kraken add_ocr -i <inputPath> -o <outputPath> -t <textPath>`: add OCR to an image
