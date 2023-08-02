package ocr4u.options;

import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Options for OCR4U
 * @author krwong
 */
public class OCR4UOptions {
    @Option(names = {"-i", "--input-path"},
            required = true,
            description = "Required. Filename of PDF, image, or txt file with list of images.")
    private Path inputPath;

    @Option(names = {"-o", "--output-path"},
            required = true,
            description = "Required. Path to a directory or a file. Destination for PDF with OCR.")
    private Path outputPath;

    public Path getInputPath() {
        return inputPath;
    }

    public void setInputPath(Path inputPath) {
        if (Files.exists(inputPath)) {
            this.inputPath = inputPath;
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
