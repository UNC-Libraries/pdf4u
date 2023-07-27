package ocr4u;

import ocr4u.options.OCR4UOptions;
import ocr4u.services.ImageService;
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
    private ImageService imageService = new ImageService();

    @Command(name = "pdf_add_ocr",
            description = "Perform OCR on a PDF")
    public int PdfAddOCR(@Mixin OCR4UOptions options) throws Exception {
        try {
            pdfService.addOcrToPdf(options.getInputPath(), options.getOutputPath());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the PDF", e);
            return 1;
        }
    }

    @Command(name = "pdf_redo_ocr",
            description = "Perform OCR on a PDF")
    public int PdfRedoOCR(@Mixin OCR4UOptions options) throws Exception {
        try {
            pdfService.redoExistingOCR(options.getInputPath(), options.getOutputPath());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the PDF", e);
            return 1;
        }
    }

    @Command(name = "image_add_ocr",
            description = "Perform OCR on an image or multiple images and convert to PDF")
    public int imageAddOCR(@Mixin OCR4UOptions options) throws Exception {
        try {
            imageService.addOCRToImage(options.getInputPath(), options.getOutputPath());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the image(s)", e);
            return 1;
        }
    }
}
