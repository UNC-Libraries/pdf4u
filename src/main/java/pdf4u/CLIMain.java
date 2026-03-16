package pdf4u;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Main class for the CLI utils
 * @author krwong
 */
@Command(subcommands = {
        KrakenCommand.class,
        OcrMyPdfCommand.class
})
public class CLIMain {

    protected CLIMain() {
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CLIMain()).execute(args);
        System.exit(exitCode);
    }
}
