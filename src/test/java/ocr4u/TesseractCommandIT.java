package ocr4u;

import ocr4u.services.TesseractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TesseractCommandIT extends AbstractCommandIT {
    private TesseractService tesseractService;

    @BeforeEach
    public void setup() throws Exception {
        tesseractService = new TesseractService();
    }

    @Test
    public void addOcrtoImageTesseractTest() throws Exception {
        String testFile = "src/test/resources/dog-wikipedia.png";
        String[] args = new String[] {
                "tesseract",
                "image_add_ocr", "-i", testFile, "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }

    @Test
    public void addOcrtoMultipleImagesTesseractTest() throws Exception {
        String testFile = "src/test/resources/listofimages.txt";
        String[] args = new String[] {
                "tesseract",
                "image_add_ocr", "-i", testFile, "-o", tmpFolder.toString()
        };

        executeExpectSuccess(args);
    }
}
