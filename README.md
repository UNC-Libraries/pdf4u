# pdf4u
OCR: optical character recognition

[hOCR](https://en.wikipedia.org/wiki/HOCR): open standard of data representation for formatted text obtained from OCR, 
file extension `.hocr`

pdf4u uses OCRmyPDF to add OCR to an image/PDF or redo OCR for a PDF with existing OCR. 
OCRmyPDF uses Tesseract as its OCR engine and works best on clear images with print text.

pdf4u uses kraken to add OCR to an image or multiple images and outputs a HOCR. The text in the HOCR file is replaced 
with more accurate text from a .txt file. Then the HOCR file is converted to a PDF with OCR. 
This kraken/hocr2pdf combo works for handwritten text.

## Dependencies

- [Hocr2Pdf](https://github.com/eloops/hocr2pdf) 
- [ImageMagick](https://imagemagick.org/script/)
- [Kraken](https://github.com/mittagessen/kraken)
- [OCRmyPDF](https://ocrmypdf.readthedocs.io/en/latest/installation.html)

## Supported Formats
OCRmyPDF supported formats: PNG, JPEG, TIFF, JP2, GIF, BMP, PDF
For a full list of OCRmyPDF/Tesseract's input formats, refer to the [Tesseract documentation](https://tesseract-ocr.github.io/tessdoc/InputFormats).

Kraken does not list its supported formats.

## Commands
- `pdf4u ocrmypdf add_ocr -i <inputPath> -o <outputPath>`: add OCR to an image or PDF
- `pdf4u ocrmypdf redo_ocr -i <inputPath> -o <outputPath>`: redo existing OCR in a PDF
- `pdf4u kraken add_ocr -i <inputPath> -o <outputPath> -t <textPath>`: add OCR to an image
