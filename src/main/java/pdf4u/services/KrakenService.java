package pdf4u.services;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;
import pdf4u.util.FileService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for Kraken (handwritten text recognition)
 * Kraken accepts input images
 * @author krwong
 */
public class KrakenService {
    private static final Logger log = getLogger(KrakenService.class);

    private static final String KRAKEN = "kraken";
    private static final String PDFUNITE = "pdfunite";

    private HocrToPdfService hocrToPdfService = new HocrToPdfService();

    /**
     * Run kraken to create hOCR then convert to a searcahble PDF
     * For multiple images, convert each image individually then combine into PDF
     * @param options pdf4u options
     * @return outputFile path to the output PDF with OCR
     */
    public void addOcrToFile(Pdf4uOptions options) throws Exception {
        List<Path> intermediateFiles = new ArrayList<>();
        try {
            if (!FilenameUtils.getExtension(options.getInputPath().toString()).equalsIgnoreCase("txt")) {
                Path hocrFile = generateHocrFromImage(options.getInputPath(), options.getOutputPath());
                hocrToPdfService.convertHocrToPdf(options.getInputPath(), hocrFile, options.getOutputPath(),
                        options.getTranscriptPath());
                intermediateFiles.add(hocrFile);
            } else {
                addOcrToMultipleFiles(options);
            }
        } finally {
            // delete intermediate files after PDF generated
            for (Path intermediateFile : intermediateFiles) {
                Files.deleteIfExists(intermediateFile);
            }
        }
    }

    /**
     * For multiple images, convert each image into a searchable PDF then combine all PDFs
     * @param options pdf4u options
     * @return outputFile path to the combined output PDF
     */
    public Path addOcrToMultipleFiles(Pdf4uOptions options) throws Exception {
        List<Path> intermediateFiles = new ArrayList<>();
        List<String> intermediatePdfs = new ArrayList<>();

        Path outputPath = options.getOutputPath();
        String outputFilename = FilenameUtils.getBaseName(outputPath.toString());
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".pdf");

        List<Path> imagePaths = readPathList(options.getInputPath());
        List<Path> transcriptPaths = readPathList(options.getTranscriptPath());

        if (imagePaths.size() != transcriptPaths.size()) {
            throw new IllegalArgumentException(
                    "Image list and transcript list must have the same number of entries. " +
                            "Images = " + imagePaths.size() + ", transcripts = " + transcriptPaths.size());
        }

        try {
            for (int i = 0; i < imagePaths.size(); i++) {
                Path imagePath = imagePaths.get(i);
                Path transcriptPath = transcriptPaths.get(i);
                Path pdfPath = FileService.prepareTempPath(imagePath.toString(), "");

                Path hocrOutput = FileService.prepareTempPath(imagePath.toString(), "");
                Path hocrFile = generateHocrFromImage(imagePath, hocrOutput);
                Path individualPdf = hocrToPdfService.convertHocrToPdf(imagePath, hocrFile, pdfPath, transcriptPath);
                intermediatePdfs.add(individualPdf.toString());
                intermediateFiles.add(hocrFile);
            }

            List<String> command = new ArrayList<>();
            command.add(PDFUNITE);
            command.addAll(intermediatePdfs);
            command.add(outputFile.toString());

            log.debug("Combining intermediate PDFs: {}", String.join(" ", command));
            CommandUtility.executeCommand(command);

        } finally {
            // delete intermediate files after combined PDF generated
            for (Path intermediateFile : intermediateFiles) {
                Files.deleteIfExists(intermediateFile);
            }
            for (String intermediatePdf : intermediatePdfs) {
                Files.deleteIfExists(Path.of(intermediatePdf));
            }
        }

        return outputFile;
    }

    /**
     * Run Kraken and create an .hocr file
     * @param inputPath, outputPath
     */
    public Path generateHocrFromImage(Path inputPath, Path outputPath) throws Exception {
        if (FilenameUtils.getExtension(inputPath.toString()).matches("pdf")) {
            throw new IllegalArgumentException("kraken does not accept input PDFs, " + inputPath + " not allowed");
        }
        String h = "-h";
        String i = "-i";
        String inputFile = inputPath.toString();
        String outputFilename = FilenameUtils.getBaseName(inputFile);
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".hocr");
        // kraken segments line-level boxes
        String segment = "segment";
        String bl = "-bl";
        String ocr = "ocr";
        String m = "-m";
        // ManuMcFondue.mlmodel: Model train on openly licensed data from HTR-United from the 17th century to the 21st were used
        // `kraken list` for full list of available models and `kraken get <model>` to download model
        String model = "ManuMcFondue.mlmodel";

        var command = Arrays.asList(KRAKEN, h, i, inputFile, outputFile.toString(), segment, bl, ocr, m, model);
        log.debug("Running kraken command: {}", String.join(" ", command));
        CommandUtility.executeCommand(command);

        return outputFile;
    }

    private List<Path> readPathList(Path txtFile) throws IOException {
        List<Path> paths = new ArrayList<>();
        for (String line : Files.readAllLines(txtFile, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                paths.add(Path.of(trimmed));
            }
        }
        return paths;
    }

    public void setHocrToPdfService(HocrToPdfService hocrToPdfService) {
        this.hocrToPdfService = hocrToPdfService;
    }
}
