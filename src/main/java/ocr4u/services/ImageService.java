package ocr4u.services;

import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for adding OCR to images
 * Supported image formats: PNG, JPEG, TIFF, JP2, GIF, BMP
 * for full list of supported image formats and requirements: https://tesseract-ocr.github.io/tessdoc/InputFormats
 * @author krwong
 */
public class ImageService {
    private static final Logger log = getLogger(ImageService.class);

    /**
     * Run Tesseract, add OCR to image(s), and convert to a PDF
     * for individual images, the input path is the image filename
     * for multiple images, the input path is a txt file with a list of image filenames
     * @param inputPath an image or txt file with a list of image filenames
     * @param outputPath destination for output PDF
     */
    public Path addOCRToImage(Path inputPath, Path outputPath) throws Exception {
        String tesseract = "tesseract";
        String inputFile = String.valueOf(inputPath);
        String outputFile = String.valueOf(outputPath);
        String pdf = "pdf";
        List<String> command = Arrays.asList(tesseract, inputFile, outputFile, pdf);

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String cmdOutput = new String(process.getInputStream().readAllBytes());
            log.debug(cmdOutput);
            if (process.waitFor() != 0) {
                throw new Exception("Command exited with status code " + process.waitFor());
            }
        } catch (Exception e) {
            throw new Exception(inputPath + " failed to generate PDF with OCR.", e);
        }

        return Path.of(outputFile + ".pdf");
    }

}
