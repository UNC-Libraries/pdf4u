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

public class PDFServiceTest {
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @TempDir
    public Path tmpFolder;

    private PDFService pdfService;

    @BeforeEach
    public void setup() throws Exception {
        System.setOut(new PrintStream(outputStreamCaptor));

        pdfService = new PDFService();
    }

    @Test
    public void testPDFWithoutOCR() throws Exception {
        // screenshot pasted into LibreOffice Writer document and print to PDF
        String testFile = "src/test/resources/cat.pdf";

        Path testOutput = pdfService.addOcrToPdf(Path.of(testFile), tmpFolder);
        String testOutputText = new Tika().parseToString(testOutput);

        assertEquals(Path.of(String.valueOf(tmpFolder), "cat.pdf"), testOutput);
        assertTrue(Files.exists(Path.of(String.valueOf(tmpFolder), "cat.pdf")));
        assertTrue(testOutputText.contains("kittens"));
    }

    @Test
    public void testPDFWithExistingOCR() throws Exception {
        // print to PDF
        String testFile = "src/test/resources/Cat-Wikipedia.pdf";

        Path testOutput = pdfService.redoExistingOCR(Path.of(testFile), tmpFolder);
        String testOutputText = new Tika().parseToString(testOutput);

        assertEquals(Path.of(String.valueOf(tmpFolder), "Cat-Wikipedia.pdf"), testOutput);
        assertTrue(Files.exists(Path.of(String.valueOf(tmpFolder), "Cat-Wikipedia.pdf")));
        assertTrue(testOutputText.contains("kittens"));
    }

}