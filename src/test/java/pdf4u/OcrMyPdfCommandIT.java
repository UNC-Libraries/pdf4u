package pdf4u;

import org.apache.tika.Tika;
import org.junit.jupiter.api.io.TempDir;
import pdf4u.errors.CommandException;
import pdf4u.options.Pdf4uOptions;
import pdf4u.services.OcrMyPdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OcrMyPdfCommandIT {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @TempDir
    public Path tmpFolder;

    private OcrMyPdfService ocrMyPdfService;

    @BeforeEach
    public void setup() throws Exception {
        ocrMyPdfService = new OcrMyPdfService();
        ocrMyPdfService.tmpFilesDir = tmpFolder;

        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void testPdfWithoutOcr() throws Exception {
        // screenshot pasted into LibreOffice Writer document and print to PDF
        Path testFile = Path.of("src/test/resources/cat.pdf");
        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(tmpFolder.resolve("cat"));

        Path testOutput = ocrMyPdfService.addOcrToFile(options);
        String testOutputText = new Tika().parseToString(testOutput);

        assertEquals(tmpFolder.resolve("cat.pdf"), testOutput);
        assertTrue(Files.exists(tmpFolder.resolve("cat.pdf")));
        assertTrue(testOutputText.contains("kittens"));
    }

    @Test
    public void testPdfWithExistingOcr() throws Exception {
        // print to PDF
        Path testFile = Path.of("src/test/resources/Cat-Wikipedia.pdf");
        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(tmpFolder.resolve("Cat-Wikipedia"));

        Path testOutput = ocrMyPdfService.redoPdfExistingOcr(options);
        String testOutputText = new Tika().parseToString(testOutput);

        assertEquals(tmpFolder.resolve("Cat-Wikipedia.pdf"), testOutput);
        assertTrue(Files.exists(tmpFolder.resolve("Cat-Wikipedia.pdf")));
        assertTrue(testOutputText.contains("kittens"));
    }

    @Test
    public void testConvertMultipleImagesToPdf() throws Exception {
        String testFile = "src/test/resources/listofimages.txt";

        Path testOutput = ocrMyPdfService.convertImagesToPdf(Path.of(testFile));

        assertEquals(tmpFolder.resolve("listofimages.pdf"), testOutput);
        assertTrue(Files.exists(tmpFolder.resolve("listofimages.pdf")));
    }

    @Test
    public void testAddOcrToImage() throws Exception {
        Path testFile = Path.of("src/test/resources/dog-wikipedia.png");
        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(tmpFolder.resolve("dog"));

        Path testOutput = ocrMyPdfService.addOcrToFile(options);
        String testOutputText = new Tika().parseToString(testOutput);

        assertEquals(tmpFolder.resolve("dog.pdf"), testOutput);
        assertTrue(Files.exists(tmpFolder.resolve("dog.pdf")));
        assertTrue(testOutputText.contains("man's best friend"));
    }

    @Test
    public void testAddOcrToMultipleImages() throws Exception {
        Path testFile = Path.of("src/test/resources/listofimages.txt");
        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(testFile);
        options.setOutputPath(tmpFolder.resolve("multipleimages"));

        Path testOutput = ocrMyPdfService.addOcrToFile(options);
        String testOutputText = new Tika().parseToString(testOutput);

        assertEquals(tmpFolder.resolve("multipleimages.pdf"), testOutput);
        assertTrue(Files.exists(tmpFolder.resolve("multipleimages.pdf")));
        // OCRMyPDF appears to be unable to perform OCR on the test JP2 and GIF images
        assertTrue(testOutputText.toLowerCase().contains("man's best friend"));   // PNG
        assertTrue(testOutputText.toLowerCase().contains("student affairs"));     // JPEG
        assertTrue(testOutputText.toLowerCase().contains("hope house"));          // TIF
        assertTrue(testOutputText.toLowerCase().contains("unc students"));        // BMP
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
