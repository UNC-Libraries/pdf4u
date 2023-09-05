package ocr4u;

import ocr4u.services.OcrMyPdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OCRMyPDFCommandIT extends AbstractCommandIT {
    private OcrMyPdfService pdfService;

    @BeforeEach
    public void setup() throws Exception {
        pdfService = new OcrMyPdfService();
    }

    @Test
    public void addOcrToPdfTest() throws Exception {
        String testFile = "src/test/resources/cat.pdf";
        String[] args = new String[] {
                "ocrmypdf",
                "pdf_add_ocr", "-i", testFile, "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }

    @Test
    public void redoExistingOcrTest() throws Exception {
        String testFile = "src/test/resources/Cat-Wikipedia.pdf";
        String[] args = new String[] {
                "ocrmypdf",
                "pdf_redo_ocr", "-i", testFile, "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }

    @Test
    public void addOcrtoImageOcrMyPdfTest() throws Exception {
        String testFile = "src/test/resources/dog-wikipedia.png";
        String[] args = new String[] {
                "ocrmypdf",
                "image_add_ocr", "-i", testFile, "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }

    @Test
    public void addOcrtoMultipleImagesOcrMyPdfTest() throws Exception {
        String testFile = "src/test/resources/listofimages.txt";
        String[] args = new String[] {
                "ocrmypdf",
                "image_add_ocr", "-i", testFile, "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }
}
