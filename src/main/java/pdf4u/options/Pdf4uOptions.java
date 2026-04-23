package pdf4u.options;

import picocli.CommandLine.Option;

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

    @Option(names = {"-t", "--transcript-path"},
            description = "Path to text file of the image's LLM generated transcript.")
    private Path transcriptPath;

    @Option(names = {"-tt", "--text-type"},
        description = "Type of text in file. Handwritten or printed text.")
    private String textType;

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

    public Path getTranscriptPath() {
        return transcriptPath;
    }

    public void setTranscriptPath(Path transcriptPath) {
        this.transcriptPath = transcriptPath;
    }

    public String getTextType() {
        return textType;
    }

    public void setTextType(String textType) {
        this.textType = textType;
    }
}
