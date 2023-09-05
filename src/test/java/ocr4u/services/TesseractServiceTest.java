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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TesseractServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @TempDir
    public Path tmpFolder;

    private TesseractService tesseractService;

    @BeforeEach
    public void setup() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));

        tesseractService = new TesseractService();
    }

    @Test
    public void testAddOcrToImage() throws Exception {
        String testFile = "src/test/resources/dog-wikipedia.png";

        Path testOutput = tesseractService.addOcrToImage(Path.of(testFile), tmpFolder.resolve("dog"));
        String testOutputText = new Tika().parseToString(testOutput);

        assertEquals(tmpFolder.resolve("dog.pdf"), testOutput);
        assertTrue(Files.exists(tmpFolder.resolve("dog.pdf")));
        assertTrue(testOutputText.contains("man's best friend"));
    }

    @Test
    public void testTesseractAddOCRToMultipleImages() throws Exception {
        String testFile = "src/test/resources/listofimages.txt";

        Path testOutput = tesseractService.addOcrToImage(Path.of(testFile), tmpFolder.resolve("multipleimages"));

        String testOutputText = new Tika().parseToString(testOutput);

        assertEquals(tmpFolder.resolve("multipleimages.pdf"), testOutput);
        assertTrue(Files.exists(tmpFolder.resolve("multipleimages.pdf")));
        assertTrue(testOutputText.toLowerCase().contains("man's best friend"));   // PNG
        assertTrue(testOutputText.toLowerCase().contains("student affairs"));     // JPEG
        assertTrue(testOutputText.toLowerCase().contains("hope house"));          // TIF
        assertTrue(testOutputText.toLowerCase().contains("there were no"));       // JP2
        assertTrue(testOutputText.toLowerCase().contains("friday"));              // GIF
        assertTrue(testOutputText.toLowerCase().contains("unc students"));        // BMP
    }

    @Test
    public void testTesseractAddOCRToUnsupportedImageFormatFail() throws Exception {
        String testFile = "src/test/resources/Cat-Wikipedia.pdf";

        try {
            tesseractService.addOcrToImage(Path.of(testFile), tmpFolder.resolve("Cat-Wikipedia"));
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("failed to generate PDF with OCR."));
        }
    }
}
