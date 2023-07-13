package ocr4u.services;

import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for adding OCR to a PDF
 * @author krwong
 */
public class PDFService {
    private static final Logger log = getLogger(PDFService.class);

    /**
     * Run OCRmyPDF and add OCR to a PDF
     * This will fail if the PDF has existing OCR
     * @param inputPath a PDF
     * @param outputPath destination for output PDF
     * @return outputFile path to the output PDF with OCR
     */
    public Path addOcrToPdf(Path inputPath, Path outputPath) throws Exception {
        String ocrMyPdf = "ocrmypdf";
        String inputFile = String.valueOf(inputPath);
        String outputFile = String.valueOf(outputPath.resolve(inputPath.getFileName()));
        List<String> command = Arrays.asList(ocrMyPdf, inputFile, outputFile);

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

        return Path.of(outputFile);
    }

    /**
     * Run OCRmyPDF and redo OCR on a PDF OCRed with other OCR software or a previous version of OCRmyPDF
     * @param inputPath a PDF
     * @param outputPath destination for output PDF
     * @return outputFile path to the output PDF with OCR
     */
    public Path redoExistingOCR(Path inputPath, Path outputPath) throws Exception {
        String ocrMyPdf = "ocrmypdf";
        String redoOcr = "--redo-ocr";
        String inputFile = String.valueOf(inputPath);
        String outputFile = String.valueOf(outputPath.resolve(inputPath.getFileName()));
        List<String> command = Arrays.asList(ocrMyPdf, redoOcr, inputFile, outputFile);

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

        return Path.of(outputFile);
    }
}
