package pdf4u;

import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pdf4u.options.Pdf4uOptions;
import pdf4u.services.HocrToPdfService;
import pdf4u.services.KrakenService;
import pdf4u.services.MultipleTextTypesService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Pdf4uCommandsIT {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @TempDir
    public Path tmpFolder;

    private HocrToPdfService hocrToPdfService;
    private KrakenService krakenService;
    private MultipleTextTypesService multipleTextTypesService;

    @BeforeEach
    public void setup() throws Exception {
        hocrToPdfService = new HocrToPdfService();
        krakenService = new KrakenService();
        krakenService.setHocrToPdfService(hocrToPdfService);
        multipleTextTypesService = new MultipleTextTypesService();
        multipleTextTypesService.setKrakenService(krakenService);
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
    public void testConvertHocrToPdf() throws Exception {
        Path testFile = Path.of("src/test/resources/alt38.jpg");
        Path textFile = Path.of("src/test/resources/alt38.txt");
        Path outputPath = tmpFolder.resolve("alt38.pdf");
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

    @Test
    public void testAddOcrToFilePrintedTextTypeSuccess() throws Exception {
        Path inputPath = Path.of("src/test/resources/alt21.jpg");
        Path outputPath = tmpFolder.resolve("alt21.pdf");
        Path transcriptPath = Path.of("src/test/resources/alt21.txt");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptPath);
        options.setTextTypeList(List.of("printed"));

        multipleTextTypesService.addOcrToFile(options);

        assertTrue(Files.exists(outputPath));
    }

    @Test
    public void testAddOcrToFileHandwrittenTextTypeSuccess() throws Exception {
        Path inputPath = Path.of("src/test/resources/alt21.jpg");
        Path outputPath = tmpFolder.resolve("alt21.pdf");
        Path transcriptPath = Path.of("src/test/resources/alt21.txt");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptPath);
        options.setTextTypeList(List.of("handwritten print"));

        multipleTextTypesService.addOcrToFile(options);

        assertTrue(Files.exists(outputPath));
    }

    @Test
    public void testAddOcrToFileMixedTextTypeNoTranscript() throws Exception {
        Path inputPath = Path.of("src/test/resources/alt21.jpg");
        Path outputPath = tmpFolder.resolve("alt21.pdf");
        Path transcriptPath = Path.of("src/test/resources/alt21_notranscript.txt");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptPath);
        options.setTextTypeList(List.of("mixed"));

        multipleTextTypesService.addOcrToFile(options);

        assertTrue(Files.exists(outputPath));
    }

    @Test
    public void testAddOcrToFileNoTextTextTypeSuccess() throws Exception {
        Path inputPath = Path.of("src/test/resources/dog-wikipedia.png");
        Path outputPath = tmpFolder.resolve("dog-wikipedia.pdf");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("no text"));

        multipleTextTypesService.addOcrToFile(options);

        assertTrue(Files.exists(outputPath));
    }

    @Test
    public void testAddOcrToMultipleFilesPrintedMixedTypeSuccess() throws Exception {
        Path inputPath = Path.of("src/test/resources/listofimageshandwritten.txt");
        Path transcriptPath = Path.of("src/test/resources/listoftranscripts.txt");
        Path outputPath = tmpFolder.resolve("handwrittenimages.pdf");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptPath);
        options.setTextTypeList(List.of("printed", "mixed"));

        multipleTextTypesService.addOcrToMultipleFiles(options);

        assertTrue(Files.exists(outputPath));
    }

    @Test
    public void testAddOcrToMultipleFilesNoTextTextTypeSuccess() throws Exception {
        Path inputPath = Path.of("src/test/resources/listofimages.txt");
        Path outputPath = tmpFolder.resolve("multipleimages.pdf");
        Path transcriptPath = tmpFolder.resolve("transcript.txt");
        List<String> lines =
                Arrays.asList("no transcript", "no transcript", "no transcript", "no transcript", "no transcript");
        Files.write(transcriptPath, lines, StandardCharsets.UTF_8);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptPath);
        options.setTextTypeList(List.of("no text", "no text", "no text", "no text", "no text"));

        multipleTextTypesService.addOcrToMultipleFiles(options);

        assertTrue(Files.exists(outputPath));
    }

    @Test
    public void testAddOcrToMultipleFilesWithTextTypeAndNoTranscriptSuccess() throws Exception {
        Path inputPath = Path.of("src/test/resources/listofimages.txt");
        Path outputPath = tmpFolder.resolve("multipleimages.pdf");
        Path transcriptPath = tmpFolder.resolve("transcript.txt");
        List<String> lines =
                Arrays.asList("no transcript", "no transcript", "no transcript", "no transcript", "no transcript");
        Files.write(transcriptPath, lines, StandardCharsets.UTF_8);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptPath);
        options.setTextTypeList(List.of("printed", "handwritten", "mixed", "handwritten", "no text"));

        multipleTextTypesService.addOcrToMultipleFiles(options);

        assertTrue(Files.exists(outputPath));
    }
}
