package ocr4u;

import ocr4u.options.OCR4UOptions;
import ocr4u.services.TesseractService;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParentCommand;

import static ocr4u.util.CLIConstants.outputLogger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author krwong
 */
@Command(name = "tesseract",
        description = "")
public class TesseractCommand {
    private static final Logger log = getLogger(OCRMyPDFCommand.class);
    @ParentCommand
    private CLIMain parentCommand;

    private TesseractService tesseractService = new TesseractService();

    @Command(name = "image_add_ocr",
            description = "Using Tesseract, perform OCR on an image or multiple images and convert to PDF")
    public int imageAddOcrTesseract(@Mixin OCR4UOptions options) throws Exception {
        try {
            tesseractService.addOcrToImage(options.getInputPath(), options.getOutputPath());
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the image(s)", e);
            return 1;
        }
    }
}
