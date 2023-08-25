package ocr4u.services;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for OCRMyPDF (add OCR to a PDF)
 * @author krwong
 */
public class OcrMyPdfService {
    private static final Logger log = getLogger(OcrMyPdfService.class);

    public Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
    public Path tmpFilesDir = tmpDir.resolve("ocr4u");

    public OcrMyPdfService() {
        try {
            initializeTempFilesDir();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


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
    public Path redoPdfExistingOcr(Path inputPath, Path outputPath) throws Exception {
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

    /**
     * Run OCRmyPDF, add OCR to an image, and convert to PDF
     * OCRmyPDF can only convert/add OCR to single images. For multiple images, first
     * convert the images to a PDF using a program such as img2pdf.
     * @param inputPath an image file
     * @param outputPath destination for output PDF
     */
    public Path addOcrToImage(Path inputPath, Path outputPath) throws Exception {
        String ocrMyPdf = "ocrmypdf";
        String inputFile = String.valueOf(convertImagesToPdf(inputPath));
        String outputFile = outputPath + ".pdf";
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
     * Convert image(s) to PDF with img2pdf
     * @param inputPath an image or txt file with a list of image filenames
     */
    public Path convertImagesToPdf(Path inputPath) throws Exception {
        ArrayList<String> command = new ArrayList<>();
        command.add("img2pdf");
        if (FilenameUtils.getExtension(String.valueOf(inputPath)).equals("txt")) {
            List<String> listOfFiles = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
            command.addAll(listOfFiles);
        } else {
            command.add(inputPath.toString());
        }
        command.add("--output");
        String outputFile = String.valueOf(prepareTempPath(inputPath));
        command.add(outputFile);
        // only let the first frame of every multi-frame input image be converted into a page in the resulting PDF
        command.add("--first-frame-only");

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
            throw new Exception(inputPath + " failed to generate PDF.", e);
        }

        return Path.of(outputFile);
    }

    /**
     * Create tmp files directory for temporary files
     * @return tmpFilesDirectoryPath
     */
    public Path initializeTempFilesDir() throws Exception {
        Path path = tmpFilesDir;
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    /**
     * Create temporary image file path and delete temporary file if it already exists
     * @return tmpImageFilesDirectoryPath
     */
    private Path prepareTempPath(Path inputPath) throws Exception {
        Path tempPath = tmpFilesDir.resolve(FilenameUtils.getBaseName(String.valueOf(inputPath)) + ".pdf");
        Files.deleteIfExists(tempPath);
        return tempPath;
    }
}
