package pdf4u.util;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        Path tempPath = Files.createTempFile(FilenameUtils.getBaseName(fileName), extension);
        // delete temporary path so that it can be written over by whatever utility has requested a path
        Files.delete(tempPath);
        return tempPath;
    }
}
