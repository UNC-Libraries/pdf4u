package pdf4u;

import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pdf4u.options.Pdf4uOptions;
import pdf4u.services.HocrToPdfService;
import pdf4u.services.KrakenService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KrakenCommandIT {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @TempDir
    public Path tmpFolder;

    private HocrToPdfService hocrToPdfService;
    private KrakenService krakenService;

    @BeforeEach
    public void setup() throws Exception {
        hocrToPdfService = new HocrToPdfService();
        krakenService = new KrakenService();
        krakenService.setHocrToPdfService(hocrToPdfService);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void testAddOcrToImage() throws Exception {
        Path testFile = Path.of("src/test/resources/alt21.jpg");
        Path mockedHocr = tmpFolder.resolve("test_hocr.hocr");
        Files.copy(Paths.get("src/test/resources/alt21.hocr"), mockedHocr);
        Path textFile = Path.of("src/test/resources/alt21.txt");
        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(tmpFolder.resolve("alt21"));
        options.setTranscriptPath(textFile);

        Path testHocr = krakenService.generateHocrFromImage(options.getInputPath(), options.getOutputPath());

        assertEquals(tmpFolder.resolve("alt21.hocr"), testHocr);
        assertTrue(Files.exists(tmpFolder.resolve("alt21.hocr")));
    }

    @Test
    public void testAddOcrToMultipleImages() throws Exception {
        Path testFile = tmpFolder.resolve("listofimages.txt");
        Files.copy(Paths.get("src/test/resources/listofimageshandwritten.txt"), testFile);
        Path transcriptFile = tmpFolder.resolve("listoftranscripts.txt");
        Files.copy(Paths.get("src/test/resources/listoftranscripts.txt"), transcriptFile);
        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(tmpFolder.resolve("handwritten"));
        options.setTranscriptPath(transcriptFile);

        Path testOutput = krakenService.addOcrToMultipleFiles(options);

        assertTrue(Files.exists(testOutput));
    }

    @Test
    public void testConvertHocrToPdf() throws Exception {
        Path testFile = Path.of("src/test/resources/alt38.jpg");
        Path textFile = Path.of("src/test/resources/alt38.txt");
        Path outputPath = tmpFolder.resolve("alt38");
        Path mockedHocr = tmpFolder.resolve("alt38.hocr");
        Files.copy(Paths.get("src/test/resources/alt38.hocr"), mockedHocr);

        Path testOutput = hocrToPdfService.convertHocrToPdf(testFile, mockedHocr, outputPath, textFile);
        String testOutputText = new Tika().parseToString(testOutput);

        assertEquals(tmpFolder.resolve("alt38.pdf"), testOutput);
        assertTrue(Files.exists(tmpFolder.resolve("alt38.pdf")));
        assertTrue(testOutputText.contains("Received from Mr James Alexander this 17th of July"));
    }

    @Test
    public void testAddOcrToUnsupportedPdfFail() throws Exception {
        Path testFile = Path.of("src/test/resources/Cat-Wikipedia.pdf");
        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(tmpFolder.resolve("Cat-Wikipedia"));

        var e = assertThrows(IllegalArgumentException.class, () -> {
            krakenService.generateHocrFromImage(options.getInputPath(), options.getOutputPath());
        });
        assertTrue(e.getMessage().contains("kraken does not accept input PDFs"));
    }
}
