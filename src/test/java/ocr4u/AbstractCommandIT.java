package ocr4u;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;
import static org.slf4j.LoggerFactory.getLogger;

public class AbstractCommandIT {
    private static final Logger log = getLogger(AbstractCommandIT.class);

    @TempDir
    public Path tmpFolder;

    protected final PrintStream originalOut = System.out;
    protected final ByteArrayOutputStream out = new ByteArrayOutputStream();
    protected String output;
    protected CommandLine command;

    @AfterEach
    public void resetOut() {
        System.setOut(originalOut);
    }

    @BeforeEach
    public void setup() throws Exception {
        out.reset();
        System.setOut(new PrintStream(out));
        output = null;

        command = new CommandLine(new CLIMain());
    }

    protected void executeExpectSuccess(String[] args) {
        int result = command.execute(args);
        output = out.toString();
        if (result != 0) {
            System.setOut(originalOut);
            // Can't see the output from the command without this
            System.out.println(output);
            fail("Expected command to result in success: " + String.join(" ", args) + "\nWith output:\n" + output);
        }
    }

    protected void executeExpectFailure(String[] args) {
        int result = command.execute(args);
        output = out.toString();
        if (result == 0) {
            System.setOut(originalOut);
            log.error(output);
            fail("Expected command to result in failure: " + String.join(" ", args));
        }
    }
}
