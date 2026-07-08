package pdf4u;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pdf4u.options.Pdf4uOptions;
import pdf4u.services.HocrToPdfService;
import pdf4u.services.KrakenService;
import pdf4u.services.MultipleTextTypesService;
import pdf4u.services.OcrMyPdfService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultipleImagesCommandIT {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @TempDir
    public Path tmpFolder;

    private HocrToPdfService hocrToPdfService;
    private KrakenService krakenService;
    private MultipleTextTypesService multipleTextTypesService;
    private OcrMyPdfService ocrMyPdfService;

    @BeforeEach
    public void setup() throws Exception {
        hocrToPdfService = new HocrToPdfService();
        krakenService = new KrakenService();
        krakenService.setHocrToPdfService(hocrToPdfService);
        ocrMyPdfService = new OcrMyPdfService();
        multipleTextTypesService = new MultipleTextTypesService();
        multipleTextTypesService.setKrakenService(krakenService);
        multipleTextTypesService.setOcrMyPdfService(ocrMyPdfService);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void testAddOcrToFilePrintedTextTypeSuccess() throws Exception {
        Path inputPath = Path.of("src/test/resources/alt21.jpg");
        Path outputPath = tmpFolder.resolve("dog-wikipedia.pdf");
        Path transcriptPath = Path.of("src/test/resources/alt21.txt");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptPath);
        options.setTextTypeList(List.of("no text"));

        multipleTextTypesService.addOcrToFile(options);

        assertTrue(Files.exists(outputPath));
    }

    @Test
    public void testAddOcrToFileHandwrittenTextTypeSuccess() throws Exception {
        Path inputPath = Path.of("src/test/resources/alt21.jpg");
        Path outputPath = tmpFolder.resolve("dog-wikipedia.pdf");
        Path transcriptPath = Path.of("src/test/resources/alt21.txt");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptPath);
        options.setTextTypeList(List.of("no text"));

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
    public void testAddOcrToMultipleFilesPrintedTextTypeSuccess() throws Exception {
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
        options.setTextTypeList(List.of("printed", "typed", "printed", "typed", "printed"));

        multipleTextTypesService.addOcrToMultipleFiles(options);

        assertTrue(Files.exists(outputPath));
    }

    @Test
    public void testAddOcrToMultipleFilesHandwrittenTextTypeSuccess() throws Exception {
        Path inputPath = Path.of("src/test/resources/listofimageshandwritten.txt");
        Path transcriptPath = Path.of("src/test/resources/listoftranscripts.txt");
        Path outputPath = tmpFolder.resolve("handwrittenimages.pdf");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptPath);
        options.setTextTypeList(List.of("handwritten print", "mixed"));

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
}
