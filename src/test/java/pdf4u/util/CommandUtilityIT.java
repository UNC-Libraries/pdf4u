package pdf4u.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pdf4u.errors.CommandTimeoutException;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandUtilityIT {
    @TempDir
    public Path tmpFolder;

    @Test
    public void testTimeout() {
        assertThrows(CommandTimeoutException.class,
                () -> CommandUtility.executeCommand(List.of("sleep", "5"), 1));
    }

    @Test
    public void testNoTimeout() {
        CommandUtility.executeCommand(List.of("sleep", "1"));
    }

    @Test
    public void testOcrMyPdf() {
        //TODO
        String inputPath = "src/test/resources/cat.pdf";
        String outputPath = tmpFolder.resolve("cat.pdf").toString();
        var command = Arrays.asList("ocrmypdf", inputPath, outputPath);
        assertThrows(CommandTimeoutException.class,
                () -> CommandUtility.executeCommand(command, 1));
    }
}
