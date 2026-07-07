package pdf4u;

import org.slf4j.Logger;
import pdf4u.options.Pdf4uOptions;
import pdf4u.services.KrakenService;
import pdf4u.services.MultipleTextTypesService;
import pdf4u.services.OcrMyPdfService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParentCommand;

import static org.slf4j.LoggerFactory.getLogger;
import static pdf4u.util.CLIConstants.outputLogger;

/**
 * @author krwong
 */
@Command(name = "multiple_images",
        description = "pdf4u commands to add OCR to documents with multiple text types")
public class MultipleImagesCommand {
    private static final Logger log = getLogger(MultipleImagesCommand.class);
    @ParentCommand
    private CLIMain parentCommand;

    private KrakenService krakenService = new KrakenService();
    private OcrMyPdfService ocrMyPdfService = new OcrMyPdfService();
    private MultipleTextTypesService multipleTextTypesService = new MultipleTextTypesService();

    @Command(name = "add_ocr",
            description = "Perform OCR on a PDF or image. Image(s) will be converted to PDF")
    public int fileAddOcr(@Mixin Pdf4uOptions options) throws Exception {
        try {
            multipleTextTypesService.setKrakenService(krakenService);
            multipleTextTypesService.setOcrMyPdfService(ocrMyPdfService);
            multipleTextTypesService.addOcrToFile(options);
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the file", e);
            return 1;
        }
    }
}
