package pdf4u.options;

import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Options for pdf4u
 * @author krwong
 */
public class Pdf4uOptions {
    @Option(names = {"-i", "--input-path"},
            required = true,
            description = "Required. Filename of PDF, image, or txt file with list of images.")
    private Path inputPath;

    @Option(names = {"-o", "--output-path"},
            required = true,
            description = "Required. Path to a directory or a file. Destination for PDF with OCR.")
    private Path outputPath;

    @Option(names = {"-t", "--text-path"},
            description = "Path to text file")
    private Path textPath;

    public Path getInputPath() {
        return inputPath;
    }

    public void setInputPath(Path inputPath) {
        this.inputPath = inputPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(Path outputPath) {
        this.outputPath = outputPath;
    }

    public Path getTextPath() {
        return textPath;
    }

    public void setTextPath(Path textPath) {
        this.textPath = textPath;
    }
}
