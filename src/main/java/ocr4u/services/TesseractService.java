package ocr4u.services;

import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for Tesseract OCR (add OCR to images)
 * Supported image formats: PNG, JPEG, TIFF, JP2, GIF, BMP
 * for full list of supported image formats and requirements: https://tesseract-ocr.github.io/tessdoc/InputFormats
 * @author krwong
 */
public class TesseractService {
    private static final Logger log = getLogger(TesseractService.class);

    private ServiceHelper serviceHelper = new ServiceHelper();

    /**
     * Run Tesseract, add OCR to image(s), and convert to a PDF
     * for individual images, the input path is the image filename
     * for multiple images, the input path is a txt file with a list of image filenames
     * @param inputPath an image or txt file with a list of image filenames
     * @param outputPath destination for output PDF
     */
    public Path addOcrToImage(Path inputPath, Path outputPath) throws Exception {
        String tesseract = "tesseract";
        String inputFile = String.valueOf(inputPath);
        String outputFile = String.valueOf(outputPath);
        String pdf = "pdf";
        var command = Arrays.asList(tesseract, inputFile, outputFile, pdf);

        try {
            serviceHelper.commandProcess(command);
        } catch (Exception e) {
            throw new Exception(inputPath + " failed to generate PDF with OCR.", e);
        }

        return Path.of(outputFile + ".pdf");
    }
}
