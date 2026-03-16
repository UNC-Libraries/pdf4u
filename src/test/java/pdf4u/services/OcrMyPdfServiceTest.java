package pdf4u.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pdf4u.errors.CommandException;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.openMocks;

public class OcrMyPdfServiceTest {
    private AutoCloseable closeable;
    private OcrMyPdfService ocrMyPdfService;

    @TempDir
    public Path tmpFolder;

    @Captor
    private ArgumentCaptor<List<String>> captor;

    @BeforeEach
    public void setup() throws Exception {
        closeable = openMocks(this);
        ocrMyPdfService = new OcrMyPdfService();
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
            Path mockedOutput = tmpFolder.resolve("test_output.pdf");
            Pdf4uOptions options = new Pdf4uOptions();
            options.setInputPath(mockedInput);
            options.setOutputPath(tmpFolder.resolve("test_output"));
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(mockedOutput.toString());

            OcrMyPdfService ocrMyPdfService = new OcrMyPdfService();
            ocrMyPdfService.addOcrToFile(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(captor.capture()), times(2));
            List<String> cmd = captor.getValue();
            assertNotNull(cmd);
            assertTrue(cmd.contains("ocrmypdf"));
            assertTrue(FilenameUtils.getBaseName(cmd.get(1)).startsWith("test_input"));
            assertTrue(FilenameUtils.getExtension(cmd.get(1)).contains("pdf"));
            assertEquals(mockedOutput.toString(), cmd.get(2));
        }
    }

    @Test
    public void testAddOcrToPdfWithoutOcr() throws Exception {
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
    public void testConvertImagesToPdf() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = tmpFolder.resolve("listofimages.txt");
            Files.copy(Paths.get("src/test/resources/listofimages.txt"), mockedInput);
            Path mockedOutput = tmpFolder.resolve("listofimages.pdf");
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(mockedOutput.toString());

            OcrMyPdfService ocrMyPdfService = new OcrMyPdfService();
            ocrMyPdfService.convertImagesToPdf(mockedInput);

            mockedStatic.verify(() -> CommandUtility.executeCommand(captor.capture()));
            List<String> cmd = captor.getValue();
            assertNotNull(cmd);
            assertTrue(cmd.contains("img2pdf"));
            assertTrue(cmd.contains("--from-file"));
            assertTrue(FilenameUtils.getBaseName(cmd.get(2)).startsWith("listofimages"));
            assertTrue(FilenameUtils.getExtension(cmd.get(2)).contains("lst"));
            assertTrue(cmd.contains("--output"));
            assertTrue(FilenameUtils.getBaseName(cmd.get(4)).startsWith("listofimages"));
            assertTrue(FilenameUtils.getExtension(cmd.get(4)).contains("pdf"));
            assertTrue(cmd.contains("--first-frame-only"));
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