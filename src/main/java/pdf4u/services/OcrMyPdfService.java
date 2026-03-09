package pdf4u.services;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;
import pdf4u.util.FileService;

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
 * OCRmyPDF uses tesseract and accepts both input images and input PDFs
 * @author krwong
 */
public class OcrMyPdfService {
    private static final Logger log = getLogger(OcrMyPdfService.class);

    public Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
    public Path tmpFilesDir = tmpDir.resolve("pdf4u");
    private static final String OCRMYPDF = "ocrmypdf";
    private static final String REDO_OCR = "--redo-ocr";
    private static final String IMG2PDF = "img2pdf";

    public OcrMyPdfService() {
        try {
            initializeTempFilesDir();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Run OCRmyPDF and add OCR to a PDF or image
     * This will fail if the PDF has existing OCR
     * OCRmyPDF can only convert/add OCR to single images and requires resolution (DPI)
     * For multiple images, first convert the images to a PDF using a program such as img2pdf
     * @param options pdf4u options
     * @return outputFile path to the output PDF with OCR
     */
    public Path addOcrToFile(Pdf4uOptions options) throws Exception {
        String inputFile = String.valueOf(options.getInputPath());
        if (!FilenameUtils.getExtension(inputFile).matches("pdf")) {
            inputFile = String.valueOf(convertImagesToPdf(options.getInputPath()));
        }
        Path outputPath = options.getOutputPath();
        String outputFilename = FilenameUtils.getBaseName(inputFile);
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".pdf");

        var command = Arrays.asList(OCRMYPDF, inputFile, outputFile.toString());

        log.debug("Running ocrmypdf command: {}", String.join(" ", command));
        CommandUtility.executeCommand(command);

        // delete intermediate files after PDF generated
        if (!inputFile.matches(options.getInputPath().toString())) {
            Files.deleteIfExists(Path.of(inputFile));
        }

        return outputFile;
    }

    /**
     * Run OCRmyPDF and redo OCR on a PDF
     * @param options pdf4u options
     * @return outputFile path to the output PDF with OCR
     */
    public Path redoPdfExistingOcr(Pdf4uOptions options) throws Exception {
        String inputFile = String.valueOf(options.getInputPath());
        Path outputPath = options.getOutputPath();
        String outputFilename = FilenameUtils.getBaseName(inputFile);
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".pdf");

        var command = Arrays.asList(OCRMYPDF, REDO_OCR, inputFile, outputFile.toString());
        log.debug("Running ocrmypdf command: {}", String.join(" ", command));
        CommandUtility.executeCommand(command);

        return outputFile;
    }

    /**
     * Convert image(s) to PDF with img2pdf
     * @param inputPath a txt file with a list of image filenames
     * @return outputFile path to the output PDF with OCR
     */
    public Path convertImagesToPdf(Path inputPath) throws Exception {
        List<String> inputFiles = new ArrayList<>();
        if (FilenameUtils.getExtension(String.valueOf(inputPath)).equals("txt")) {
            inputFiles = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
        } else {
            inputFiles.add(inputPath.toString());
        }
        Path outputFile = prepareTempPath(inputPath);

        List<String> command = new ArrayList<>();
        command.add(IMG2PDF);
        command.addAll(inputFiles);
        command.add("--output");
        command.add(outputFile.toString());
        // only let the first frame of every multi-frame input image be converted into a page in the resulting PDF
        command.add("--first-frame-only");

        log.debug("Running img2pdf command: {}", String.join(" ", command));
        CommandUtility.executeCommand(command);

        return outputFile;
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
