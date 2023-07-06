package ocr4u.services;

import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class PDFService {
    private static final Logger log = getLogger(PDFService.class);

    /**
     * Run OCRmyPDF and add OCR to a PDF
     * This will fail if the PDF has existing OCR
     * @param fileName a PDF, outputPath
     * @return outputFile path to the output PDF with OCR
     */
    public String addOcrtoPdf(Path fileName, Path outputPath) throws Exception {
        String ocrMyPdf = "ocrmypdf";
        String inputFile = String.valueOf(fileName);
        String outputFile = String.valueOf(outputPath.resolve(fileName.getFileName()));
        List<String> command = Arrays.asList(ocrMyPdf, inputFile, outputFile);

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String cmdOutput = new String(process.getInputStream().readAllBytes());
            log.debug(cmdOutput);
        } catch (Exception e) {
            throw new Exception(fileName + " failed to generate PDF with OCR.", e);
        }

        return outputFile;
    }

    /**
     * Run OCRmyPDF and redo OCR on a PDF OCRed with other OCR software or a previous version of OCRmyPDF
     * @param fileName a PDF, outputPath
     * @return outputFile path to the output PDF with OCR
     */
    public String redoExistingOCR(Path fileName, Path outputPath) throws Exception {
        String ocrMyPdf = "ocrmypdf";
        String redoOcr = "--redo-ocr";
        String inputFile = String.valueOf(fileName);
        String outputFile = String.valueOf(outputPath.resolve(fileName.getFileName()));
        List<String> command = Arrays.asList(ocrMyPdf, redoOcr, inputFile, outputFile);

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String cmdOutput = new String(process.getInputStream().readAllBytes());
            log.debug(cmdOutput);
        } catch (Exception e) {
            throw new Exception(fileName + " failed to generate PDF with OCR.", e);
        }

        return outputFile;
    }
}
