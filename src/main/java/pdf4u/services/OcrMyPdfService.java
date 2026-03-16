package pdf4u.services;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;
import pdf4u.util.FileService;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for OCRMyPDF (print text recognition)
 * OCRmyPDF uses tesseract and accepts both input images and input PDFs.
 * It can add OCR to images/PDFs and also redo OCR for PDFs with OCR.
 * @author krwong
 */
public class OcrMyPdfService {
    private static final Logger log = getLogger(OcrMyPdfService.class);
    private static final String OCRMYPDF = "ocrmypdf";
    private static final String REDO_OCR = "--redo-ocr";
    private static final String IMG2PDF = "img2pdf";

    /**
     * Run OCRmyPDF and add OCR to a PDF or image
     * This will fail if the PDF has existing OCR
     * OCRmyPDF can only convert/add OCR to single images and requires resolution (DPI)
     * For multiple images, first convert the images to a PDF using a program such as img2pdf
     * @param options pdf4u options
     * @return outputFile path to the output PDF with OCR
     */
    public void addOcrToFile(Pdf4uOptions options) throws Exception {
        String inputFile = String.valueOf(options.getInputPath());
        if (!FilenameUtils.getExtension(inputFile).equalsIgnoreCase("pdf")) {
            inputFile = String.valueOf(convertImagesToPdf(options.getInputPath()));
        }

        try {
            Path outputPath = options.getOutputPath();
            String outputFilename = FilenameUtils.getBaseName(inputFile);
            Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".pdf");

            var command = Arrays.asList(OCRMYPDF, inputFile, outputFile.toString());

            log.debug("Running ocrmypdf command: {}", String.join(" ", command));
            CommandUtility.executeCommand(command);
        } finally {
            // delete intermediate files after PDF generated
            if (!inputFile.equals(options.getInputPath().toString())) {
                Files.deleteIfExists(Path.of(inputFile));
            }
        }
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
     * @param inputPath single file or a txt file with a list of image filenames.
     *                  List behavior only triggered if path is a txt file
     * @return outputFile path to the output PDF
     */
    public Path convertImagesToPdf(Path inputPath) throws Exception {
        String inputFile = String.valueOf(inputPath);
        String output = "--output";
        Path outputPath = prepareTempPath(inputFile, ".pdf");
        // --first-frame-only: only let the first frame of every multi-frame input image be converted
        // into a page in the resulting PDF
        String firstFrameOnly = "--first-frame-only";
        boolean isTxtFile = FilenameUtils.getExtension(inputFile).equals("txt");

        try {
            List<String> command = new ArrayList<>();
            command.add(IMG2PDF);
            // add --from-file to read list of inputs from a text file
            if (isTxtFile) {
                command.add("--from-file");
                inputFile = createLst(inputPath).toString();
            }
            command.addAll(Arrays.asList(inputFile, output, outputPath.toString(), firstFrameOnly));

            log.debug("Running img2pdf command: {}", String.join(" ", command));
            CommandUtility.executeCommand(command);
        } finally {
            // delete intermediate files after PDF generated
            if (isTxtFile) {
                Files.deleteIfExists(Path.of(inputFile));
            }
        }

        return outputPath;
    }

    /**
     * When receiving a .txt input, build a NUL-separated lst file
     * img2pdf's --from-file option only accepts a NUL-separated list, not newlines
     * @param inputPath pdf4u options' input path
     * @return outputPath lst file with NUL-separated list
     */
    private Path createLst(Path inputPath) throws Exception {
        Path lstPath = prepareTempPath(inputPath.toString(), ".lst");
        try (OutputStream os = Files.newOutputStream(lstPath)) {
            // read lines and write each followed by a NUL byte
            for (String line : Files.readAllLines(inputPath, StandardCharsets.UTF_8)) {
                if (line.isEmpty()) continue; // optional: skip empty lines
                os.write(line.getBytes(StandardCharsets.UTF_8));
                os.write(0); // NUL separator required by --from-file
            }
        }

        return lstPath;
    }

    /**
     * Create temporary file path and delete temporary file if it already exists
     * @return temp path for file
     */
    private Path prepareTempPath(String fileName, String extension) throws Exception {
        Path tempPath = Files.createTempFile(FilenameUtils.getBaseName(fileName), extension);
        // delete temporary path so that it can be written over by whatever utility has requested a path
        Files.delete(tempPath);
        return tempPath;
    }
}