package pdf4u;

import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import pdf4u.options.Pdf4uOptions;
import pdf4u.services.HocrToPdfService;
import pdf4u.services.KrakenService;
import pdf4u.services.MultipleTextTypesService;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private MultipleTextTypesService multipleTextTypesService;

    @TempDir
    public Path tmpFolder;

    @BeforeEach
    public void setup() throws Exception {
        command = new CommandLine(new CLIMain());
        System.setOut(new PrintStream(outputStreamCaptor));

        hocrToPdfService = new HocrToPdfService();
        krakenService = new KrakenService();
        krakenService.setHocrToPdfService(hocrToPdfService);
        multipleTextTypesService = new MultipleTextTypesService();
        multipleTextTypesService.setKrakenService(krakenService);
    }

    @Test
    public void testMultipleImagesTextTypeHandwritten() throws Exception {
        String testFile = "src/test/resources/alt21.jpg";
        String textFile = "src/test/resources/alt21.txt";
        String[] args = new String[] {
                "pdf4u",
                "add_ocr", "-i", testFile, "-o", tmpFolder.resolve("alt21.pdf").toString(), "-t", textFile,
                "-tt", "handwritten_cursive"
        };

        executeExpectSuccess(args);
    }

    @Test
    public void testMultipleImagesTextTypePrinted() throws Exception {
        String testFile = "src/test/resources/alt21.jpg";
        String textFile = "src/test/resources/alt21.txt";
        String[] args = new String[] {
                "pdf4u",
                "add_ocr", "-i", testFile, "-o", tmpFolder.resolve("alt21.pdf").toString(), "-t", textFile,
                "-tt", "printed"
        };

        executeExpectSuccess(args);
    }

    @Test
    public void testMultipleImagesTextTypeNoText() throws Exception {
        String testFile = "src/test/resources/alt21.jpg";
        String[] args = new String[] {
                "pdf4u",
                "add_ocr", "-i", testFile, "-o", tmpFolder.resolve("alt21.pdf").toString(),
                "-tt", "no text"
        };

        executeExpectSuccess(args);
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
        options.setTextTypeList(List.of("handwritten_print"));

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
    public void testAddOcrToFileTxtInputNoTranscriptTxt() throws Exception {
        List<String> lines = List.of("src/test/resources/alt21.jpg");
        Path inputPath = tmpFolder.resolve("alt21.txt");
        Files.write(inputPath, lines, StandardCharsets.UTF_8);
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
        options.setTextTypeList(List.of("printed", "handwritten_print", "mixed", "handwritten_cursive", "no text"));

        multipleTextTypesService.addOcrToMultipleFiles(options);

        assertTrue(Files.exists(outputPath));
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
