package pdf4u.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;

import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.MockitoAnnotations.openMocks;

public class KrakenServiceTest {
    private AutoCloseable closeable;
    private KrakenService krakenService;

    @TempDir
    public Path tmpFolder;

    @BeforeEach
    public void setup() throws Exception {
        closeable = openMocks(this);
        krakenService = new KrakenService();
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void testGenerateHocrFromImage() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = tmpFolder.resolve("test_input.png");
            Path mockedOutput = tmpFolder.resolve("test_output.hocr");
            Pdf4uOptions options = new Pdf4uOptions();
            options.setInputPath(mockedInput);
            options.setOutputPath(tmpFolder.resolve("test_output"));
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn(mockedOutput.toString());

            KrakenService krakenService = new KrakenService();
            krakenService.generateHocrFromImage(options);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("kraken", "-h", "-i", mockedInput.toString(), mockedOutput.toString(), "segment",
                            "-bl", "ocr", "-m", "ManuMcFondue.mlmodel")));
        }
    }

    @Test
    public void testKrakenAddOcrToUnsupportedFormatFail() throws Exception {
        Path testFile = Path.of("src/test/resources/Cat-Wikipedia.pdf");
        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(tmpFolder.resolve("Cat-Wikipedia"));

        var e = assertThrows(IllegalArgumentException.class, () -> {
            krakenService.generateHocrFromImage(options);
        });
        assertTrue(e.getMessage().contains("kraken does not accept input PDFs"));
    }
}
