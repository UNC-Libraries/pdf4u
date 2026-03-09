package pdf4u;

import pdf4u.options.Pdf4uOptions;
import pdf4u.services.OcrMyPdfService;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParentCommand;

import static pdf4u.util.CLIConstants.outputLogger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author krwong
 */
@Command(name = "ocrmypdf",
        description = "ocrmypdf commands to add OCR to print documents or redo OCR")
public class OcrMyPdfCommand {
    private static final Logger log = getLogger(OcrMyPdfCommand.class);
    @ParentCommand
    private CLIMain parentCommand;

    private OcrMyPdfService ocrMyPdfService = new OcrMyPdfService();

    @Command(name = "add_ocr",
            description = "Perform OCR on a PDF or image. Image(s) will be converted to PDF")
    public int imageAddOcrMyPdf(@Mixin Pdf4uOptions options) throws Exception {
        try {
            ocrMyPdfService.addOcrToFile(options);
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the file", e);
            return 1;
        }
    }

    @Command(name = "pdf_redo_ocr",
            description = "Perform OCR on a PDF")
    public int pdfRedoOcrMyPdf(@Mixin Pdf4uOptions options) throws Exception {
        try {
            ocrMyPdfService.redoPdfExistingOcr(options);
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the PDF", e);
            return 1;
        }
    }
}
