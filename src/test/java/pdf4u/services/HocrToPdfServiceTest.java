package pdf4u.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.MockitoAnnotations.openMocks;

public class HocrToPdfServiceTest {
    private AutoCloseable closeable;
    private HocrToPdfService hocrToPdfService;

    @TempDir
    public Path tmpFolder;

    @BeforeEach
    public void setup() throws Exception {
        closeable = openMocks(this);
        hocrToPdfService = new HocrToPdfService();
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void testReplaceHocrText() throws Exception {
        Path mockedHocr = tmpFolder.resolve("test_hocr.hocr");
        Files.copy(Paths.get("src/test/resources/alt21.hocr"), mockedHocr);
        Path textFile = Path.of("src/test/resources/alt21.txt");

        String output = hocrToPdfService.replaceHocrText(mockedHocr, textFile);
        Document doc = Jsoup.parse(new File(output), "UTF-8");
        Elements lines = doc.getElementsByClass("ocr_line");
        assertTrue(lines.first().text().contains("Blue Ridge Parkway—Doughton Meadows"));
        assertTrue(lines.last().text().contains("NORTH CAROLINA NEWS BUREAU DEPT. CONSERVATION & DEVELOPMENT " +
                "P. O. BOX 2719 RALEIGH, NORTH CAROLINA [Alleghany County] 30958"));
    }

    @Test
    public void testConvertHocrToPdf() throws Exception {
        try (MockedStatic<CommandUtility> mockedStatic = Mockito.mockStatic(CommandUtility.class)) {
            Path mockedInput = tmpFolder.resolve("test_input.jpg");
            Path mockedHocr = tmpFolder.resolve("test_hocr.hocr");
            Files.copy(Paths.get("src/test/resources/alt21.hocr"), mockedHocr);
            Path mockedText = tmpFolder.resolve("alt21.txt");
            Files.copy(Paths.get("src/test/resources/alt21.txt"), mockedText);
            Path mockedOutput = tmpFolder.resolve("test_output.pdf");
            Pdf4uOptions options = new Pdf4uOptions();
            options.setInputPath(mockedInput);
            options.setOutputPath(tmpFolder.resolve("test_output"));
            options.setTranscriptPath(mockedText);
            mockedStatic.when(() -> CommandUtility.executeCommand(anyList()))
                    .thenReturn("600");
            mockedStatic.when(() -> CommandUtility.executeCommandInputFile(anyList(), anyString()))
                    .thenReturn(mockedOutput.toString());

            HocrToPdfService hocrToPdfService = new HocrToPdfService();
            hocrToPdfService.convertHocrToPdf(options, mockedHocr);

            mockedStatic.verify(() -> CommandUtility.executeCommand(
                    Arrays.asList("gm", "identify", "-format", "\"%x\"", mockedInput.toString())));
            mockedStatic.verify(() -> CommandUtility.executeCommandInputFile(
                    Arrays.asList("hocr2pdf", "-i", mockedInput.toString(), "-o", mockedOutput.toString(), "-r",
                            "600"), mockedHocr.toString()));

        }
    }
}
