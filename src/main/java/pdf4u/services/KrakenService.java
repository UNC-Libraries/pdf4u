package pdf4u.services;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;
import pdf4u.util.FileService;

import java.nio.file.Path;
import java.util.Arrays;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for Kraken (handwritten text recognition)
 * Kraken accepts input images
 * @author krwong
 */
public class KrakenService {
    private static final Logger log = getLogger(KrakenService.class);

    private static final String KRAKEN = "kraken";

    /**
     * Run Kraken, add OCR to image(s), and create an .hocr file
     * @param options pdf4u options
     */
    public Path addOcrToImage(Pdf4uOptions options) throws Exception {
        if (FilenameUtils.getExtension(options.getInputPath().toString()).matches("pdf")) {
            throw new IllegalArgumentException("kraken does not accept input PDFs, "
                + options.getInputPath().toString() + " not allowed");
        }
        String h = "-h";
        String i = "-i";
        String inputFile = options.getInputPath().toString();
        Path outputPath = options.getOutputPath();
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
}
