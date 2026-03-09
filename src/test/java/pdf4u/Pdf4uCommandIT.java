package pdf4u;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import pdf4u.services.HocrToPdfService;
import pdf4u.services.KrakenService;
import pdf4u.services.OcrMyPdfService;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;
import static org.slf4j.LoggerFactory.getLogger;

public class Pdf4uCommandIT {
    private static final Logger log = getLogger(Pdf4uCommandIT.class);
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    protected final PrintStream originalOut = System.out;
    protected final ByteArrayOutputStream out = new ByteArrayOutputStream();
    protected String output;

    protected CommandLine command;

    private HocrToPdfService hocrToPdfService;
    private KrakenService krakenService;
    private OcrMyPdfService ocrMyPdfService;

    @TempDir
    public Path tmpFolder;

    @BeforeEach
    public void setup() throws Exception {
        command = new CommandLine(new CLIMain());
        System.setOut(new PrintStream(outputStreamCaptor));

        hocrToPdfService = new HocrToPdfService();
        krakenService = new KrakenService();
        ocrMyPdfService = new OcrMyPdfService();
        ocrMyPdfService.tmpFilesDir = tmpFolder;
    }

    @Test
    public void testOcrMyPdfAddOcrToPdf() throws Exception {
        String testFile = "src/test/resources/cat.pdf";
        String[] args = new String[] {
                "ocrmypdf",
                "add_ocr", "-i", testFile, "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }

    @Test
    public void testOcrmyPdfRedoExistingOcr() throws Exception {
        String testFile = "src/test/resources/Cat-Wikipedia.pdf";
        String[] args = new String[] {
                "ocrmypdf",
                "pdf_redo_ocr", "-i", testFile, "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }

    @Test
    public void testOcrMyPdfAddOcrToImage() throws Exception {
        String testFile = "src/test/resources/dog-wikipedia.png";
        String[] args = new String[] {
                "ocrmypdf",
                "add_ocr", "-i", testFile, "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }

    @Test
    public void testOcrMyPdfAddOcrtoMultipleImages() throws Exception {
        String testFile = "src/test/resources/listofimages.txt";
        String[] args = new String[] {
                "ocrmypdf",
                "add_ocr", "-i", testFile, "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }

    @Test
    public void testKrakenAddOcrToImage() throws Exception {
        String testFile = "src/test/resources/alt21.jpg";
        String textFile = "src/test/resources/alt21.txt";
        String[] args = new String[] {
                "kraken",
                "add_ocr", "-i", testFile, "-o", tmpFolder.toString(), "-t", textFile
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
