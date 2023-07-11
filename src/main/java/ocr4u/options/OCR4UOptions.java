package ocr4u.options;

import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Options for OCR4U
 * @author krwong
 */
public class OCR4UOptions {
    @Option(names = {"-f", "--filename"},
            required = true,
            description = "Required. Filename of PDF.")
    private Path fileName;

    @Option(names = {"-o", "--output-path"},
            required = true,
            description = "Destination for PDF with OCR. You must set the output path manually, no default.")
    private Path outputPath;

    public Path getFileName() {
        return fileName;
    }

    public void setFileName(Path fileName) {
        if (Files.exists(fileName)) {
            this.fileName = fileName;
        }
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(Path outputPath) {
        if (Files.isWritable(outputPath)) {
            this.outputPath = outputPath;
        }
    }
}
