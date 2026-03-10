package pdf4u.util;

import static org.slf4j.LoggerFactory.getLogger;

import pdf4u.errors.CommandException;
import pdf4u.errors.CommandTimeoutException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Utility for executing commands
 * @author krwong
 */
public class CommandUtility {
    private static final Logger log = getLogger(CommandUtility.class);
    private static final int MAX_TIMEOUT_SECONDS = 60 * 5;

    private CommandUtility() {
    }

    /**
     * Run a given command
     * @param command the command to be executed
     * @return command output
     */
    public static String executeCommand(List<String> command) {
        log.debug("Executing command with timeout {}s: {}", MAX_TIMEOUT_SECONDS, String.join(" ", command));
        CommandLine cmdLine = CommandLine.parse(command.getFirst());
        command.subList(1, command.size()).forEach(arg -> cmdLine.addArgument(arg, false));

        DefaultExecutor executor = DefaultExecutor.builder().get();
        ExecuteWatchdog watchdog = null;
        if (MAX_TIMEOUT_SECONDS > 0) {
            watchdog = EscalatingExecuteWatchdog.create(Duration.ofSeconds(MAX_TIMEOUT_SECONDS));
            executor.setWatchdog(watchdog);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(outputStream, errorStream));

        try {
            executor.execute(cmdLine);
            return outputStream + "\n" + errorStream;
        } catch (ExecuteException e) {
            String output = outputStream.toString();
            int exitValue = e.getExitValue();

            if (watchdog != null && watchdog.killedProcess()) {
                throw new CommandTimeoutException("Command timed out after " + MAX_TIMEOUT_SECONDS + " seconds",
                        command, output);
            }
            throw new CommandException("Command failed to execute", command, output, exitValue, e);
        } catch (IOException e) {
            String output = outputStream + "\n" + errorStream;
            throw new CommandException("Command failed to execute", command, output, e);
        }
    }

    public static String executeCommandInputFile(List<String> command, String inputFile) {
        log.debug("Executing command with timeout {}s: {}", MAX_TIMEOUT_SECONDS, String.join(" ", command));
        CommandLine cmdLine = CommandLine.parse(command.getFirst());
        command.subList(1, command.size()).forEach(arg -> cmdLine.addArgument(arg, false));

        DefaultExecutor executor = DefaultExecutor.builder().get();
        ExecuteWatchdog watchdog = null;
        if (MAX_TIMEOUT_SECONDS > 0) {
            watchdog = EscalatingExecuteWatchdog.create(Duration.ofSeconds(MAX_TIMEOUT_SECONDS));
            executor.setWatchdog(watchdog);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        try (FileInputStream fileInputStream = new FileInputStream(inputFile);) {
            executor.setStreamHandler(new PumpStreamHandler(outputStream, errorStream, fileInputStream));
            executor.execute(cmdLine);
            return outputStream + "\n" + errorStream;
        } catch (ExecuteException e) {
            String output = outputStream.toString();
            int exitValue = e.getExitValue();

            if (watchdog != null && watchdog.killedProcess()) {
                throw new CommandTimeoutException("Command timed out after " + MAX_TIMEOUT_SECONDS + " seconds",
                        command, inputFile + "\n" + output);
            }
            throw new CommandException("Command failed to execute", command, inputFile + "\n" + output, exitValue, e);
        } catch (IOException e) {
            String output = outputStream + "\n" + errorStream;
            throw new CommandException("Command failed to execute", command, inputFile + "\n" + output, e);
        }
    }
}
