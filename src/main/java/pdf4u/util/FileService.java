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
     * Build the output file path
     * If the 'outputPath' param is a directory, create the file path with /outputPath/outputFilename.extension
     *    e.g. inputs '/path, filename, .pdf' return '/path/filename.pdf'
     * If the 'outputPath' param is a file, create the file path with /outputPath.extension
     *    e.g. inputs '/path/otherfile, filename, .pdf' return '/path/otherfile.pdf'
     * @param outputPath pdf4u options' output path
     * @param outputFilename base name of pdf4u options' input path
     * @param extension output file type
     * @return outputPath output path for file
     */
    public static Path buildOutputFile(Path outputPath, String outputFilename, String extension)
            throws Exception {
        // if the output path is a directory
        if (Files.isDirectory(outputPath)) {
            return outputPath.resolve(outputFilename + extension);
            // if the output path is a file
        } else if (Files.exists(outputPath.getParent())) {
            return Path.of(outputPath + extension);
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
