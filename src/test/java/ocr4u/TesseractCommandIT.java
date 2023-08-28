package ocr4u;

import ocr4u.services.TesseractService;
import ocr4u.services.OcrMyPdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;
import static org.slf4j.LoggerFactory.getLogger;

public class TesseractCommandIT {
    private static final Logger log = getLogger(OCRMyPDFCommandIT.class);
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    protected final PrintStream originalOut = System.out;
    protected final ByteArrayOutputStream out = new ByteArrayOutputStream();
    protected String output;

    protected CommandLine command;

    @TempDir
    public Path tmpFolder;

    private OcrMyPdfService pdfService;
    private TesseractService imageService;

    @BeforeEach
    public void setup() throws Exception {
        command = new CommandLine(new CLIMain());
        System.setOut(new PrintStream(outputStreamCaptor));

        pdfService = new OcrMyPdfService();
        imageService = new TesseractService();
    }

    @Test
    public void addOcrtoImageTesseractTest() throws Exception {
        String testFile = "src/test/resources/dog-wikipedia.png";
        String[] args = new String[] {
                "tesseract",
                "image_add_ocr", "-i", testFile, "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }

    @Test
    public void addOcrtoMultipleImagesTesseractTest() throws Exception {
        String testFile = "src/test/resources/listofimages.txt";
        String[] args = new String[] {
                "tesseract",
                "image_add_ocr", "-i", testFile, "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }

    protected void executeExpectSuccess(String[] args) {
        int result = command.execute(args);
        output = out.toString();
        if (result != 0) {
            System.setOut(originalOut);
            // Can't see the output from the command without this
            System.out.println(output);
            fail("Expected command to result in success: " + String.join(" ", args) + "\nWith output:\n" + output);
        }
    }

    protected void executeExpectFailure(String[] args) {
        int result = command.execute(args);
        output = out.toString();
        if (result == 0) {
            System.setOut(originalOut);
            log.error(output);
            fail("Expected command to result in failure: " + String.join(" ", args));
        }
    }
}
