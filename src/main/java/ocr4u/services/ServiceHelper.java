package ocr4u.services;

import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class ServiceHelper {
    private static final Logger log = getLogger(ServiceHelper.class);

    public void commandProcess(List<String> commandList) throws Exception {
        var command = commandList;

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String cmdOutput = new String(process.getInputStream().readAllBytes());
        log.debug(cmdOutput);
        if (process.waitFor() != 0) {
            throw new Exception("Command exited with status code " + process.waitFor());
        }

    }
}
