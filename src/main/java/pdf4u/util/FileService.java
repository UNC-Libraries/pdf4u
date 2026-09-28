package pdf4u.util;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service for constructing files and file validation
 * @author krwong
 */
public class FileService {
    private static final Logger log = getLogger(FileService.class);

    private FileService() {}

    /**
     * Build the output file path.
     * If outputPath is a directory, return outputPath/outputFilename.extension.
     * If outputPath is a file path, return that path with the requested extension
     * @param outputPath pdf4u options' output path
     * @param outputFilename base name of pdf4u options' input path
     * @param extension output file type
     * @return outputPath output path for file
     */
    public static Path buildOutputFile(Path outputPath, String outputFilename, String extension)
            throws Exception {
        if (outputPath == null) {
            throw new FileNotFoundException("Output path is null.");
        }

        String normalizedExtension = extension.startsWith(".") ? extension : "." + extension;

        if (Files.isDirectory(outputPath)) {
            return outputPath.resolve(outputFilename + normalizedExtension);
            // if the output path is a file
        } else if (Files.exists(outputPath.getParent())) {
            String outputWithoutExtension = FilenameUtils.removeExtension(outputPath.toString());
            return Path.of(outputWithoutExtension + normalizedExtension);
        } else {
            throw new FileNotFoundException(outputPath + " does not exist.");
        }
    }

    /**
     * Create temporary file path and delete temporary file if it already exists
     * @return temp path for file
     */
    public static Path prepareTempPath(String fileName, String extension) throws Exception {
        String baseName = FilenameUtils.getBaseName(fileName);
        String uniqueName = baseName + "_" + UUID.randomUUID() + extension;
        return Path.of(System.getProperty("java.io.tmpdir"), uniqueName);
    }

    /**
     * Read list of paths
     * @return list of file paths
     */
    public static List<Path> readPathList(Path txtFile) throws IOException {
        List<Path> paths = new ArrayList<>();
        for (String line : Files.readAllLines(txtFile, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                paths.add(Path.of(trimmed));
            }
        }
        return paths;
    }
}
