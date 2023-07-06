package ocr4u;

import ocr4u.options.OCR4UOptions;
import ocr4u.services.PDFService;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParentCommand;

import static ocr4u.util.CLIConstants.outputLogger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author krwong
 */
@Command(name = "ocr4u",
        description = "")
public class OCR4UCommand {
    private static final Logger log = getLogger(OCR4UCommand.class);
    @ParentCommand
    private CLIMain parentCommand;

    private PDFService pdfService = new PDFService();

    @Command(name = "add_ocr",
            description = "Perform OCR on a PDF")
    public int addOCR(@Mixin OCR4UOptions options) throws Exception {
        try {
            pdfService.addOcrtoPdf(options.getFileName(), options.getOutputPath());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the PDF", e);
            return 1;
        }
    }

    @Command(name = "redo_ocr",
            description = "Perform OCR on a PDF")
    public int redoOCR(@Mixin OCR4UOptions options) throws Exception {
        try {
            pdfService.redoExistingOCR(options.getFileName(), options.getOutputPath());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the PDF", e);
            return 1;
        }
    }
}
