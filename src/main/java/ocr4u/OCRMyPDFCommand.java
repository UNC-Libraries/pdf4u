package ocr4u;

import ocr4u.options.OCR4UOptions;
import ocr4u.services.OcrMyPdfService;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParentCommand;

import static ocr4u.util.CLIConstants.outputLogger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author krwong
 */
@Command(name = "ocrmypdf",
        description = "")
public class OCRMyPDFCommand {
    private static final Logger log = getLogger(OCRMyPDFCommand.class);
    @ParentCommand
    private CLIMain parentCommand;

    private OcrMyPdfService ocrMyPdfService = new OcrMyPdfService();

    @Command(name = "pdf_add_ocr",
            description = "Perform OCR on a PDF")
    public int pdfAddOcrMyPdf(@Mixin OCR4UOptions options) throws Exception {
        try {
            ocrMyPdfService.addOcrToPdf(options.getInputPath(), options.getOutputPath());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the PDF", e);
            return 1;
        }
    }

    @Command(name = "pdf_redo_ocr",
            description = "Perform OCR on a PDF")
    public int pdfRedoOcrMyPdf(@Mixin OCR4UOptions options) throws Exception {
        try {
            ocrMyPdfService.redoPdfExistingOcr(options.getInputPath(), options.getOutputPath());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the PDF", e);
            return 1;
        }
    }

    @Command(name = "image_add_ocr",
            description = "Using OCRMyPDF, convert an image or multiple images to PDF and perform OCR")
    public int imageAddOcrMyPdf(@Mixin OCR4UOptions options) throws Exception {
        try {
            ocrMyPdfService.addOcrToImage(options.getInputPath(), options.getOutputPath());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the image(s)", e);
            return 1;
        }
    }
}
