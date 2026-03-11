package pdf4u.services;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import pdf4u.errors.CommandException;
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

/**
 * Service for converting HOCR to searchable PDF
 * @author krwong
 */
public class HocrToPdfService {
    private static final Logger log = getLogger(HocrToPdfService.class);

    private static final String HOCR2PDF = "hocr2pdf";

    /**
     * Replace text in HOCR with text from TXT file
     * HOCR file's bounding boxes are at the line-level. The TXT file is the LLM generated transcript
     * and likely matches the HOCR line for line.
     * @param hocrFile a .hocr file
     * @param textFile a .txt file
     */
    public String replaceHocrText(Path hocrFile, Path textFile) throws Exception {
        Document hocrDoc = Jsoup.parse(new File(hocrFile.toString()), "UTF-8");
        List<String> transcriptLines = Files.readAllLines(textFile);
        Elements hocrLines = hocrDoc.getElementsByClass("ocr_line");

        int transcriptLineCount = transcriptLines.size();
        int hocrLineCount = hocrLines.size();

        if (hocrLineCount == 0) {
            if (transcriptLineCount == 0) {
                log.warn("HOCR file contains no ocr_line elements and transcript is empty; nothing to embed");
            } else {
                log.warn("HOCR file contains no ocr_line elements; creating a synthetic line using the page bbox");
                // Find the page element for its bbox, fall back to "0 0 0 0" if absent
                Element pageEl = hocrDoc.getElementsByClass("ocr_page").first();
                String pageBbox = "bbox 0 0 0 0";
                if (pageEl != null) {
                    String pageTitle = pageEl.attr("title");
                    // title attr is e.g. "bbox 0 0 779 969; image alt21.jpg" — extract just the bbox portion
                    String[] parts = pageTitle.split(";");
                    for (String part : parts) {
                        if (part.trim().startsWith("bbox")) {
                            pageBbox = part.trim();
                            break;
                        }
                    }
                }
                String fullTranscript = String.join(" ", transcriptLines);
                Element syntheticLine = hocrDoc.body()
                        .appendElement("span")
                        .attr("class", "ocr_line")
                        .attr("title", pageBbox);
                syntheticLine.appendText(fullTranscript);
            }
        } else {
            transcriptLinesExceedHocrLines(transcriptLines, hocrLines, transcriptLineCount, hocrLineCount);
        }

        Files.write(hocrFile, hocrDoc.outerHtml().getBytes(StandardCharsets.UTF_8));

        return hocrFile.toString();
    }

    /**
     * When transcript lines exceed the number of hOCR lines, replace each hOCR line with the corresponding
     * transcript line then append overflow transcript lines to the last hOCr element
     * @param transcriptLines transcript lines
     * @param hocrLines hocr_line elements
     * @param transcriptLineCount number of transcript lines
     * @param hocrLineCount number of hocr_line elements
     */
    private void transcriptLinesExceedHocrLines(List<String> transcriptLines, Elements hocrLines,
                                                int transcriptLineCount, int hocrLineCount) {
        if (transcriptLineCount > hocrLineCount) {
            log.warn("Transcript has {} lines but HOCR only has {} line elements; {} overflow line(s) will be " +
                            "appended to the last HOCR line", transcriptLineCount, hocrLineCount,
                    transcriptLineCount - hocrLineCount);
        }

        // Replace each hOCR line with the corresponding transcript line
        int n = Math.min(hocrLineCount, transcriptLineCount);
        for (int i = 0; i < n; i++) {
            Element line = hocrLines.get(i);
            line.empty();
            line.appendText(transcriptLines.get(i));
        }

        // Append any overflow transcript lines to the last hOCR line element
        if (transcriptLineCount > hocrLineCount) {
            Element lastLine = hocrLines.last();
            if (lastLine != null) {
                for (int i = hocrLineCount; i < transcriptLineCount; i++) {
                    String overflow = transcriptLines.get(i);
                    if (!overflow.isBlank()) {
                        lastLine.appendText(" " + overflow);
                    }
                }
            }
        }
    }

    /**
     * Convert hOCR to PDF using hocr2pdf
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
        String dpi = getDpi(inputFile);
        String editedHocr = replaceHocrText(hocrFile, options.getTranscriptPath());


        var command = Arrays.asList(HOCR2PDF, i, inputFile, o, outputFile.toString(), r, dpi);
        log.debug("Running hocr2pdf command: {}", String.join(" ", command));
        CommandUtility.executeCommandInputFile(command, editedHocr);

        // delete intermediate files after PDF generated
        Files.deleteIfExists(Path.of(editedHocr));

        return outputFile;
    }

    /**
     * Run GraphicsMagick identify command and return DPI
     * DPI needed to prevent text layer from shifting/scaling in the PDF
     * @param fileName an image file
     * @return dpi the image dpi
     */
    private String getDpi(String fileName) {
        // default dpi = 300
        String dpi = "300";
        String gm = "gm";
        String identify = "identify";
        String format = "-format";
        String x = "\"%x\"";
        var command = Arrays.asList(gm, identify, format, x, fileName);

        try {
            dpi = CommandUtility.executeCommand(command);
        } catch (CommandException e) {
            log.warn("Colorspace not identified: {}", e.getMessage());
        }

        return dpi;
    }
}