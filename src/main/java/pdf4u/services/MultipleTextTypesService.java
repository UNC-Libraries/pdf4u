package pdf4u.services;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;
import pdf4u.util.FileService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for list of images with different text types
 * @author krwong
 */
public class MultipleTextTypesService {
    private static final Logger log = getLogger(MultipleTextTypesService.class);

    private static final String PDFUNITE = "pdfunite";

    private static final Set<String> TEXT_TYPES_REQUIRING_TRANSCRIPTS = new HashSet<>(
            Arrays.asList("printed", "typed", "handwritten_print", "handwritten_cursive", "mixed")
    );

    private KrakenService krakenService = new KrakenService();

    /**
     * Add OCR to one or more files depending on the supplied text type list.
     *  If there is one text type, process one file.
     *  If there are multiple text types, process multiple files and combine the PDFs.
     * @param options pdf4u options
     */
    public void addOcrToFile(Pdf4uOptions options) throws Exception {
        if (options.getTextTypeList().size() == 1) {
            prepareSingleFileOptions(options);
            addOcrToSingleFile(options);
            return;
        }

        addOcrToMultipleFiles(options);
    }

    /**
     * For multiple images with different text types, convert each image into a PDF then combine all PDFs
     * @param options pdf4u options
     * @return outputFile path to the combined output PDF
     */
    public Path addOcrToMultipleFiles(Pdf4uOptions options) throws Exception {
        Path outputFile = FileService.buildOutputFile(options.getOutputPath(),
                FilenameUtils.getBaseName(options.getInputPath().toString()), ".pdf");

        List<Path> imagePaths = FileService.readPathList(options.getInputPath());
        List<String> textTypes = options.getTextTypeList();
        List<Path> transcriptPaths = readTranscriptPathsIfNeeded(options, textTypes);

        validateInputListSizes(imagePaths, textTypes, transcriptPaths);

        List<String> intermediatePdfs = new ArrayList<>();

        try {
            createIntermediatePdfs(imagePaths, textTypes, transcriptPaths, intermediatePdfs);

            // combine pdfs
            List<String> command = new ArrayList<>();

            command.add(PDFUNITE);
            command.addAll(intermediatePdfs);
            command.add(outputFile.toString());

            log.debug("Combining intermediate PDFs: {}", String.join(" ", command));

            CommandUtility.executeCommand(command);
        } finally {
            // delete intermediate files after combined PDF generated
            for (String intermediatePdf : intermediatePdfs) {
                Files.deleteIfExists(Path.of(intermediatePdf));
            }
        }

        return outputFile;
    }

    /**
     * Add OCR to one file.
     *  Uses Kraken when the file has OCR-able text and a usable transcript.
     *  Uses GraphicsMagick when the file has no text or no usable transcript.
     * @param options pdf4u options
     */
    private void addOcrToSingleFile(Pdf4uOptions options) throws Exception {
        String textType = options.getTextTypeList().getFirst();

        log.debug("Text type received by addOcrToSingleFile: [{}]", textType);

        if (isNoText(textType) || options.getTranscriptPath() == null) {
            createPdfWithoutOcr(options);
        } else {
            krakenService.addOcrToFile(options);
        }
    }

    /**
     * Normalize options for the single-file case.
     *
     * If the input path points to a .txt file, use the first path listed in that file.
     * If the transcript path points to a list file, use the first transcript path listed in that file.
     */
    private void prepareSingleFileOptions(Pdf4uOptions options) throws Exception {
        if (FilenameUtils.getExtension(options.getInputPath().toString()).equalsIgnoreCase("txt")) {
            Path firstInputPath = FileService.readPathList(options.getInputPath()).getFirst();
            options.setInputPath(firstInputPath);
        }

        options.setOutputPath(FileService.buildOutputFile(options.getOutputPath(),
                FilenameUtils.getBaseName(options.getInputPath().toString()), ".pdf"));

        if (options.getTranscriptPath() == null) {
            return;
        }

        Path firstTranscriptPath = FileService.readPathList(options.getTranscriptPath()).getFirst();

        if (firstTranscriptPath.toString().strip().equalsIgnoreCase("no transcript")) {
            options.setTranscriptPath(null);
        }
    }

    private void createIntermediatePdfs(List<Path> imagePaths, List<String> textTypes, List<Path> transcriptPaths,
            List<String> intermediatePdfs) throws Exception {
        for (int i = 0; i < imagePaths.size(); i++) {
            Path imagePath = imagePaths.get(i);
            String textType = textTypes.get(i);
            Path pdfPath = FileService.prepareTempPath(imagePath.toString(), ".pdf");

            Pdf4uOptions fileOptions = new Pdf4uOptions();
            fileOptions.setInputPath(imagePath);
            fileOptions.setOutputPath(pdfPath);
            fileOptions.setTextTypeList(Collections.singletonList(textType));

            if (needsTranscript(textType)) {
                Path transcriptPath = transcriptPaths.get(i);

                if (transcriptPath != null) {
                    fileOptions.setTranscriptPath(transcriptPath);
                } else {
                    log.debug("No usable transcript for file {}", imagePath);
                }
            }

            addOcrToSingleFile(fileOptions);
            intermediatePdfs.add(pdfPath.toString());
        }
    }

    /**
     * Read list of transcript paths
     * The sentinel value "no transcript" is normalized to null immediately.
     */
    private List<Path> readTranscriptPathsIfNeeded(Pdf4uOptions options, List<String> textTypes) throws Exception {
        if (textTypes.stream().noneMatch(this::needsTranscript)) {
            return Collections.emptyList();
        }

        List<Path> paths = new ArrayList<>();

        for (String line : Files.readAllLines(options.getTranscriptPath(), StandardCharsets.UTF_8)) {
            String trimmed = line.trim();

            if (!trimmed.isEmpty()) {
                paths.add(trimmed.equalsIgnoreCase("no transcript") ? null : Path.of(trimmed));
            }
        }

        return paths;
    }

    private void validateInputListSizes(List<Path> imagePaths, List<String> textTypes, List<Path> transcriptPaths) {
        if (textTypes.size() != imagePaths.size()) {
            throw new IllegalArgumentException(
                    "Text type list and image list must have the same number of entries. "
                            + "Text types = " + textTypes.size()
                            + ", images = " + imagePaths.size()
            );
        }

        if (!transcriptPaths.isEmpty() && imagePaths.size() != transcriptPaths.size()) {
            throw new IllegalArgumentException(
                    "Image list and transcript list must have the same number of entries when transcripts are needed. "
                            + "Images = " + imagePaths.size()
                            + ", transcripts = " + transcriptPaths.size()
            );
        }
    }

    /**
     * Create PDF without OCR for images without text type
     * Use graphicsmagick
     * @param options pdf4u options
     */
    private void createPdfWithoutOcr(Pdf4uOptions options) throws Exception {
        String inputFile = String.valueOf(options.getInputPath());
        String outputFile = String.valueOf(options.getOutputPath());

        var command = Arrays.asList("gm", "convert", "-auto-orient", inputFile, outputFile);

        log.debug("Running graphicsmagick command to generate PDF without OCR: {}", String.join(" ", command));
        CommandUtility.executeCommand(command);
    }

    private boolean isNoText(String textType) {
        return textType != null && textType.equalsIgnoreCase("no text");
    }

    private boolean needsTranscript(String textType) {
        return TEXT_TYPES_REQUIRING_TRANSCRIPTS.contains(textType.toLowerCase());
    }

    public void setKrakenService(KrakenService krakenService) {
        this.krakenService = krakenService;
    }
}
