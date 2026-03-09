package pdf4u.services;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pdf4u.errors.CommandException;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;

import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.MockitoAnnotations.openMocks;

public class OcrMyPdfServiceTest {
    private AutoCloseable closeable;
    private OcrMyPdfService ocrMyPdfService;

    @TempDir
    public Path tmpFolder;

    @BeforeEach
    public void setup() throws Exception {
        closeable = openMocks(this);
        ocrMyPdfService = new OcrMyPdfService();
        tmpFolder = ocrMyPdfService.tmpFilesDir;
    }

    @AfterEach
    public void close() throws Exception {
        FileUtils.deleteDirectory(tmpFolder.toFile());
        closeable.close();
    }

    @Test
    public void testAddOcrToImage() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = tmpFolder.resolve("test_input.png");
            String mockedIntermediatePdf = tmpFolder.resolve("test_input.pdf").toString();
            Path mockedOutput = tmpFolder.resolve("test_output.pdf");
            Pdf4uOptions options = new Pdf4uOptions();
            options.setInputPath(mockedInput);
            options.setOutputPath(tmpFolder.resolve("test_output"));
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(mockedOutput.toString());

            OcrMyPdfService ocrMyPdfService = new OcrMyPdfService();
            ocrMyPdfService.addOcrToFile(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("img2pdf", mockedInput.toString(), "--output",
                            mockedIntermediatePdf, "--first-frame-only")));
            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("ocrmypdf", mockedIntermediatePdf, mockedOutput.toString())));
        }
    }

    @Test
    public void testAddOcrToPdfWithoutOcr() throws Exception {
        // screenshot pasted into LibreOffice Writer document and print to PDF
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = tmpFolder.resolve("test_input.pdf");
            Path mockedOutput = tmpFolder.resolve("test_output.pdf");
            Pdf4uOptions options = new Pdf4uOptions();
            options.setInputPath(mockedInput);
            options.setOutputPath(tmpFolder.resolve("test_output"));
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(mockedOutput.toString());

            OcrMyPdfService ocrMyPdfService = new OcrMyPdfService();
            ocrMyPdfService.addOcrToFile(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("ocrmypdf", mockedInput.toString(), mockedOutput.toString())));
        }
    }

    @Test
    public void testAddOcrToPdfWithExistingOcr() throws Exception {
        // print to PDF
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = tmpFolder.resolve("test_input.pdf");
            Path mockedOutput = tmpFolder.resolve("test_output.pdf");
            Pdf4uOptions options = new Pdf4uOptions();
            options.setInputPath(mockedInput);
            options.setOutputPath(tmpFolder.resolve("test_output"));
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(mockedOutput.toString());

            OcrMyPdfService ocrMyPdfService = new OcrMyPdfService();
            ocrMyPdfService.redoPdfExistingOcr(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("ocrmypdf", "--redo-ocr", mockedInput.toString(), mockedOutput.toString())));
        }
    }

    @Test
    public void testConvertImageToPdf() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path input = Path.of("src/test/resources/listofimages.txt");
            Path mockedOutput = tmpFolder.resolve("listofimages.pdf");
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(mockedOutput.toString());

            OcrMyPdfService ocrMyPdfService = new OcrMyPdfService();
            Path output = ocrMyPdfService.convertImagesToPdf(input);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("img2pdf", "src/test/resources/dog-wikipedia.png",
                            "src/test/resources/img308b-YY1975-Dean.Boulton.jpeg",
                            "src/test/resources/SAAACAM-HopeHouse_transparency_with-title_merged.tif",
                            "src/test/resources/friday.gif", "src/test/resources/goals.bmp",
                            "--output", output.toString(), "--first-frame-only")));
        }
    }

    @Test
    public void testAddOcrToUnsupportedImageFormatFail() throws Exception {
        Path testFile = Path.of("src/test/resources/Cat-Wikipedia.pdf");
        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(tmpFolder.resolve("Cat-Wikipedia"));

        var e = assertThrows(CommandException.class, () -> {
            ocrMyPdfService.addOcrToFile(options);
        });
        assertTrue(e.getMessage().contains("Command failed to execute"));
    }
}