package ocr4u;

import ocr4u.options.OCR4UOptions;
import ocr4u.services.TesseractService;
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
@Command(name = "ocr4u",
        description = "")
public class OCR4UCommand {
    private static final Logger log = getLogger(OCR4UCommand.class);
    @ParentCommand
    private CLIMain parentCommand;

    private OcrMyPdfService ocrMyPdfService = new OcrMyPdfService();
    private TesseractService tesseractService = new TesseractService();

    @Command(name = "pdf_add_ocr",
            description = "Perform OCR on a PDF")
    public int pdfAddOcr(@Mixin OCR4UOptions options) throws Exception {
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
    public int pdfRedoOCR(@Mixin OCR4UOptions options) throws Exception {
        try {
            ocrMyPdfService.redoPdfExistingOcr(options.getInputPath(), options.getOutputPath());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the PDF", e);
            return 1;
        }
    }

    @Command(name = "image_add_ocr_tesseract",
            description = "Perform OCR on an image or multiple images and convert to PDF")
    public int imageAddOCRTesseract(@Mixin OCR4UOptions options) throws Exception {
        try {
            tesseractService.addOcrToImage(options.getInputPath(), options.getOutputPath());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the image(s)", e);
            return 1;
        }
    }

    @Command(name = "image_add_ocr_ocrmypdf",
            description = "Convert an image or multiple images to PDF and perform OCR")
    public int imageAddOCRmyPDF(@Mixin OCR4UOptions options) throws Exception {
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
