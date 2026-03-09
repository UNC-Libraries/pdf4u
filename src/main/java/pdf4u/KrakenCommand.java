package pdf4u;

import org.slf4j.Logger;

import pdf4u.options.Pdf4uOptions;
import pdf4u.services.HocrToPdfService;
import pdf4u.services.KrakenService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParentCommand;

import java.nio.file.Path;

import static org.slf4j.LoggerFactory.getLogger;
import static pdf4u.util.CLIConstants.outputLogger;

/**
 * @author krwong
 */
@Command(name = "kraken",
        description = "kraken commands to add OCR to handwritten or hard-to-read documents")
public class KrakenCommand {
    private static final Logger log = getLogger(KrakenCommand.class);
    @ParentCommand
    private CLIMain parentCommand;

    private KrakenService krakenService = new KrakenService();
    private HocrToPdfService hocrToPdfService = new HocrToPdfService();

    @Command(name = "add_ocr",
        description = "Using Kraken, perform OCR on an image and convert to HOCR. " +
            "Replace the text in the HOCR with the text in the TXT file. Then convert to PDF.")
    public int imageAddOcrKraken(@Mixin Pdf4uOptions options) throws Exception {
        try {
            Path hocrFile = krakenService.addOcrToImage(options);
            hocrToPdfService.convertHocrToPdf(options, hocrFile);
            return 0;
        } catch (Exception e) {
            outputLogger.info("{}", e.getMessage());
            log.error("Failed to OCR the image(s)", e);
            return 1;
        }
    }
}
