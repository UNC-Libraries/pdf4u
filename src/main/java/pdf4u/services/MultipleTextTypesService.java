package pdf4u.services;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;
import pdf4u.util.FileService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for list of images with different text types
 * @author krwong
 */
public class MultipleTextTypesService {
    private static final Logger log = getLogger(MultipleTextTypesService.class);

    private static final String PDFUNITE = "pdfunite";

    private KrakenService krakenService = new KrakenService();
    private HocrToPdfService hocrToPdfService = new HocrToPdfService();
    private OcrMyPdfService ocrMyPdfService = new OcrMyPdfService();

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
        Path outputPath = options.getOutputPath();
        String outputFilename = FilenameUtils.getBaseName(outputPath.toString());
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".pdf");

        List<String> intermediatePdfs = new ArrayList<>();
        List<Path> imagePaths = FileService.readPathList(options.getInputPath());
        List<Path> transcriptPaths = FileService.readPathList(options.getTranscriptPath());
        List<String> textTypeList = options.getTextTypeList();

        // check that list of text types, images, and transcripts have the same number of entries
        if (textTypeList.size() != imagePaths.size() || imagePaths.size() != transcriptPaths.size()) {
            throw new IllegalArgumentException(
                    "Text type list, image list, and transcript list must have the same number of entries. "
                            + "Text types = " + textTypeList.size() + ", images = " + imagePaths.size()
                            + ", transcripts = " + transcriptPaths.size());
        }

        // for each file in the list, determine the text type then convert the file using OcrMyPdf or Kraken
        // add each file to the list of intermediate PDFs then combine all intermediate PDFs using pdfunite
        // text types: printed, typed, handwritten printed, handwritten cursive, mixed
        // if printed/typed text, use ocrmypdf to perform OCR
        // if handwritten/mixed, use kraken and transcript
        try {
            for (int i = 0; i < imagePaths.size(); i++) {
                List<String> textType = Collections.singletonList(textTypeList.get(i));
                Path imagePath = imagePaths.get(i);
                Path transcriptPath = transcriptPaths.get(i);
                Path pdfPath = FileService.prepareTempPath(imagePath.toString(), ".pdf");

                Pdf4uOptions fileOptions = new Pdf4uOptions();
                fileOptions.setInputPath(imagePath);
                fileOptions.setOutputPath(pdfPath);
                fileOptions.setTranscriptPath(transcriptPath);
                fileOptions.setTextTypeList(textType);

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
     * Use ocrmypdf for printed text and kraken for handwritten text
     * @param textType 
     * @param options pdf4u options
     */
    private void addOcrToSingleFile(String textType, Pdf4uOptions options) throws Exception {
        if (textType.equalsIgnoreCase("printed") || textType.equalsIgnoreCase("typed")) {
            ocrMyPdfService.addOcrToFile(options);
        } else {
            krakenService.addOcrToFile(options);
        }
    }

    public void setKrakenService(KrakenService krakenService) {
        this.krakenService = krakenService;
    }

    public void setHocrToPdfService(HocrToPdfService hocrToPdfService) {
        this.hocrToPdfService = hocrToPdfService;
    }

    public void setOcrMyPdfService(OcrMyPdfService ocrMyPdfService) {
        this.ocrMyPdfService = ocrMyPdfService;
    }
}
