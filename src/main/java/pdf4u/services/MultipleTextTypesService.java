package pdf4u.services;

import org.slf4j.Logger;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;
import pdf4u.util.FileService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for list of images with different text types
 * @author krwong
 */
public class MultipleTextTypesService {
    private static final Logger log = getLogger(MultipleTextTypesService.class);

    private static final String IMG2PDF = "img2pdf";
    private static final String PDFUNITE = "pdfunite";

    private KrakenService krakenService = new KrakenService();

    /**
     * For lists of images with different text types, convert each image into a searchable PDF then combine all PDFs
     * @param options pdf4u options
     */
    public void addOcrToFile(Pdf4uOptions options) throws Exception {
        var textTypeList = options.getTextTypeList();

        if (textTypeList.size() == 1) {
            addOcrToSingleFile(textTypeList.getFirst(), options);
        } else {
            addOcrToMultipleFiles(options);
        }
    }

    /**
     * For multiple images with different text types, convert each image into a searchable PDF then combine all PDFs
     * @param options pdf4u options
     * @return outputFile path to the combined output PDF
     */
    public Path addOcrToMultipleFiles(Pdf4uOptions options) throws Exception {
        Path outputFile = options.getOutputPath();

        List<String> intermediatePdfs = new ArrayList<>();
        List<Path> imagePaths = FileService.readPathList(options.getInputPath());
        List<String> textTypeList = options.getTextTypeList();

        boolean needsAnyTranscripts = textTypeList.stream()
                .anyMatch(this::needsTranscript);

        List<Path> transcriptPaths = needsAnyTranscripts
                ? FileService.readPathList(options.getTranscriptPath())
                : Collections.emptyList();

        // check that list of text types and images have the same number of entries
        if (textTypeList.size() != imagePaths.size()) {
            throw new IllegalArgumentException(
                    "Text type list and image list must have the same number of entries. "
                            + "Text types = " + textTypeList.size()
                            + ", images = " + imagePaths.size());
        }

        // if transcripts needed, check that list of images and transcripts have the same number of entries
        if (needsAnyTranscripts && imagePaths.size() != transcriptPaths.size()) {
            throw new IllegalArgumentException(
                    "Image list and transcript list must have the same number of entries when transcripts are needed. "
                            + "Images = " + imagePaths.size()
                            + ", transcripts = " + transcriptPaths.size());
        }

        // for each file in the list, determine the text type then convert the file using OcrMyPdf or Kraken
        // add each file to the list of intermediate PDFs then combine all intermediate PDFs using pdfunite
        // text types: printed, typed, handwritten printed, handwritten cursive, mixed, no text
        // if printed/typed/handwritten/mixed, use kraken and transcript
        // if no text, use img2pdf to create PDF without OCR
        try {
            for (int i = 0; i < imagePaths.size(); i++) {
                List<String> textType = Collections.singletonList(textTypeList.get(i));
                Path imagePath = imagePaths.get(i);
                Path pdfPath = FileService.prepareTempPath(imagePath.toString(), ".pdf");

                Pdf4uOptions fileOptions = new Pdf4uOptions();
                fileOptions.setInputPath(imagePath);
                fileOptions.setOutputPath(pdfPath);
                fileOptions.setTextTypeList(textType);

                // set transcript path if text type is not no text, typed, or printed
                if (needsTranscript(textTypeList.get(i))) {
                    fileOptions.setTranscriptPath(transcriptPaths.get(i));
                }

                addOcrToSingleFile(textTypeList.get(i), fileOptions);

                intermediatePdfs.add(pdfPath.toString());
            }

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
     * Add OCR to one file
     * Use kraken for printed/handwritten text, and img2pdf for no text
     * @param textType 
     * @param options pdf4u options
     */
    private void addOcrToSingleFile(String textType, Pdf4uOptions options) throws Exception {
        if (textType.equalsIgnoreCase("no text")) {
            createPdfWithoutOcr(options);
        } else {
            krakenService.addOcrToFile(options);
        }
    }

    /**
     * Create PDF without OCR for images without text type
     * Use img2pdf
     * @param options pdf4u options
     */
    private void createPdfWithoutOcr(Pdf4uOptions options) throws Exception {
        String inputFile = String.valueOf(options.getInputPath());
        String output = "--output";
        String outputFile = String.valueOf(options.getOutputPath());
        // --first-frame-only: only let the first frame of every multi-frame input image be converted
        // into a page in the resulting PDF
        String firstFrameOnly = "--first-frame-only";

        var command = Arrays.asList(IMG2PDF, inputFile, output, outputFile, firstFrameOnly);

        log.debug("Running img2pdf command to generate PDF without OCR: {}", String.join(" ", command));
        CommandUtility.executeCommand(command);
    }

    private boolean needsTranscript(String textType) {
        return !textType.equalsIgnoreCase("no text");
    }

    public void setKrakenService(KrakenService krakenService) {
        this.krakenService = krakenService;
    }
}
