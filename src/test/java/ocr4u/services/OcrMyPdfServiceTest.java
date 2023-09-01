package ocr4u.services;

import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OcrMyPdfServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @TempDir
    public Path tmpFolder;

    private OcrMyPdfService ocrMyPdfService;

    @BeforeEach
    public void setup() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));

        ocrMyPdfService = new OcrMyPdfService();
    }

    @Test
    public void testPdfWithoutOcr() throws Exception {
        // screenshot pasted into LibreOffice Writer document and print to PDF
        String testFile = "src/test/resources/cat.pdf";

        Path testOutput = ocrMyPdfService.addOcrToPdf(Path.of(testFile), tmpFolder);
        String testOutputText = new Tika().parseToString(testOutput);

        assertEquals(tmpFolder.resolve("cat.pdf"), testOutput);
        assertTrue(Files.exists(tmpFolder.resolve("cat.pdf")));
        assertTrue(testOutputText.contains("kittens"));
    }

    @Test
    public void testPdfWithExistingOcr() throws Exception {
        // print to PDF
        String testFile = "src/test/resources/Cat-Wikipedia.pdf";

        Path testOutput = ocrMyPdfService.redoPdfExistingOcr(Path.of(testFile), tmpFolder);
        String testOutputText = new Tika().parseToString(testOutput);

        assertEquals(tmpFolder.resolve("Cat-Wikipedia.pdf"), testOutput);
        assertTrue(Files.exists(tmpFolder.resolve("Cat-Wikipedia.pdf")));
        assertTrue(testOutputText.contains("kittens"));
    }

    @Test
    public void testConvertImageToPdf() throws Exception {
        String testFile = "src/test/resources/dog-wikipedia.png";

        Path testOutput = ocrMyPdfService.convertImagesToPdf(Path.of(testFile));

        assertEquals(ocrMyPdfService.tmpFilesDir.resolve("dog-wikipedia.pdf"), testOutput);
        assertTrue(Files.exists(tmpFolder.resolve("dog-wikipedia.pdf")));
    }

    @Test
    public void testConvertMultipleImagesToPdf() throws Exception {
        String testFile = "src/test/resources/listofimages.txt";

        Path testOutput = ocrMyPdfService.convertImagesToPdf(Path.of(testFile));

        assertEquals(ocrMyPdfService.tmpFilesDir.resolve("listofimages.pdf"), testOutput);
    }

    @Test
    public void testAddOcrToImage() throws Exception {
        String testFile = "src/test/resources/dog-wikipedia.png";

        Path testOutput = ocrMyPdfService.addOcrToImage(Path.of(testFile), tmpFolder.resolve("dog"));
        String testOutputText = new Tika().parseToString(testOutput);

        assertEquals(tmpFolder.resolve("dog.pdf"), testOutput);
        assertTrue(Files.exists(tmpFolder.resolve("dog.pdf")));
        assertTrue(testOutputText.contains("man's best friend"));
    }

    @Test
    public void testAddOcrToMultipleImages() throws Exception {
        String testFile = "src/test/resources/listofimages.txt";

        Path testOutput = ocrMyPdfService.addOcrToImage(Path.of(testFile), tmpFolder.resolve("multipleimages"));
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
        String testFile = "src/test/resources/Cat-Wikipedia.pdf";

        Exception exception = assertThrows(Exception.class,
                () -> ocrMyPdfService.addOcrToImage(Path.of(testFile), tmpFolder.resolve("Cat-Wikipedia")),
                "src/test/resources/Cat-Wikipedia.pdf failed to generate PDF");

        assertTrue(exception.getMessage().contains("failed to generate PDF"));
    }

}