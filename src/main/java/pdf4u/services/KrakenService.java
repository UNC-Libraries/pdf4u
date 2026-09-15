package pdf4u.services;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;
import pdf4u.util.FileService;

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
            }
        } finally {
            // delete intermediate files after PDF generated
            for (Path intermediateFile : intermediateFiles) {
                Files.deleteIfExists(intermediateFile);
            }
        }
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
        // McCATMuS_nfd_nofix_V1.mlmodel: McCATMuS - Transcription model for handwritten, printed and typewritten
        //         documents from the 16th century to the 21st century
        // `kraken list` for full list of available models and `kraken get <model>` to download model
        String model = "McCATMuS_nfd_nofix_V1.mlmodel";

        var command = Arrays.asList(KRAKEN, h, i, inputFile, outputFile.toString(), segment, bl, ocr, m, model);
        log.debug("Running kraken command: {}", String.join(" ", command));
        CommandUtility.executeCommand(command);

        return outputFile;
    }

    public void setHocrToPdfService(HocrToPdfService hocrToPdfService) {
        this.hocrToPdfService = hocrToPdfService;
    }
}
