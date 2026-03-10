package pdf4u.services;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;
import pdf4u.util.FileService;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;
import static pdf4u.util.CLIConstants.outputLogger;

/**
 * Service for converting HOCR to searchable PDF
 * @author krwong
 */
public class HocrToPdfService {
    private static final Logger log = getLogger(HocrToPdfService.class);

    private static final String HOCR2PDF = "hocr2pdf";

    /**
     * Replace text in HOCR with text from TXT file
     * HOCR file's bounding boxes are at the line-level. The TXT file is output from the alt text chatbot
     * and likely matches the HOCR line for line.
     * @param hocrFile a .hocr file
     * @param textFile a .txt file
     */
    public String replaceHocrText(Path hocrFile, Path textFile) throws Exception {
        Document hocrDoc = Jsoup.parse(new File(hocrFile.toString()), "UTF-8");
        List<String> text = Files.readAllLines(textFile);
        Elements lines = hocrDoc.getElementsByClass("ocr_line");
        int n = Math.min(lines.size(), text.size());
        for (int i=0; i<n; i++) {
            Element line = lines.get(i);
            // remove children
            line.empty();
            // insert GT line text (jsoup will escape text automatically)
            line.appendText(text.get(i));
        }
        Files.write(hocrFile, hocrDoc.outerHtml().getBytes(StandardCharsets.UTF_8));

        return hocrFile.toString();
    }

    /**
     * Convert HOCR to PDF using hocr2pdf
     * @param options pdf4u options
     * @param hocrFile path to a .hocr file
     * @return outputFile path to the output PDF with OCR
     */
    public Path convertHocrToPdf(Pdf4uOptions options, Path hocrFile) throws Exception {
        String i = "-i";
        String inputFile = options.getInputPath().toString();
        String o = "-o";
        Path outputPath = options.getOutputPath();
        String outputFilename = FilenameUtils.getBaseName(inputFile);
        Path outputFile = FileService.buildOutputFile(outputPath, outputFilename, ".pdf");
        String r = "-r";
        String dpi = "dpi";
        String editedHocr = replaceHocrText(hocrFile, options.getTextPath());


        var command = Arrays.asList(HOCR2PDF, i, inputFile, o, outputFile.toString(), r, dpi);
        log.debug("Running hocr2pdf command: {}", String.join(" ", command));
        CommandUtility.executeCommandInputFile(command, editedHocr);

        // delete intermediate files after PDF generated
        Files.deleteIfExists(Path.of(editedHocr));

        return outputFile;
    }
}
