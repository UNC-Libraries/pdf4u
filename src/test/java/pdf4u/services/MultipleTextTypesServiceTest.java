package pdf4u.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import pdf4u.options.Pdf4uOptions;
import pdf4u.util.CommandUtility;
import pdf4u.util.FileService;

public class MultipleTextTypesServiceTest {
    @TempDir
    Path tempDir;

    private MultipleTextTypesService service;

    private KrakenService krakenService;

    @BeforeEach
    public void setup() {
        service = new MultipleTextTypesService();

        krakenService = mock(KrakenService.class);

        service.setKrakenService(krakenService);
    }

    @Test
    public void addOcrToFileWithSinglePrintedTextType() throws Exception {
        Path inputPath = Path.of("src/test/resources/alt21.jpg");
        Path outputPath = tempDir.resolve("alt21.pdf");
        Path transcriptPath = Path.of("src/test/resources/alt21.txt");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setTextTypeList(List.of("printed"));
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptPath);

        try (MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class)) {
            fileServiceMock.when(() -> FileService.buildOutputFile(outputPath, "alt21", ".pdf"))
                    .thenReturn(outputPath);

            fileServiceMock.when(() -> FileService.readPathList(transcriptPath)).thenReturn(List.of(transcriptPath));

            service.addOcrToFile(options);

            verify(krakenService).addOcrToFile(options);
            assertEquals(inputPath, options.getInputPath());
            assertEquals(outputPath, options.getOutputPath());
            assertEquals(transcriptPath, options.getTranscriptPath());
        }
    }

    @Test
    public void addOcrToFileWithSingleTypedTextType() throws Exception {
        Path inputPath = Path.of("src/test/resources/alt21.jpg");
        Path outputPath = tempDir.resolve("alt21.pdf");
        Path transcriptPath = Path.of("src/test/resources/alt21.txt");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setTextTypeList(List.of("typed"));
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptPath);

        try (MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class)) {
            fileServiceMock.when(() -> FileService.buildOutputFile(outputPath, "alt21", ".pdf"))
                    .thenReturn(outputPath);

            fileServiceMock.when(() -> FileService.readPathList(transcriptPath)).thenReturn(List.of(transcriptPath));

            service.addOcrToFile(options);

            verify(krakenService).addOcrToFile(options);
            assertEquals(inputPath, options.getInputPath());
            assertEquals(outputPath, options.getOutputPath());
            assertEquals(transcriptPath, options.getTranscriptPath());
        }
    }

    @Test
    public void addOcrToFileWithSingleHandwrittenTextType() throws Exception {
        Path inputPath = Path.of("src/test/resources/alt38.jpg");
        Path outputPath = tempDir.resolve("alt38.pdf");
        Path transcriptPath = Path.of("src/test/resources/alt38.txt");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setTextTypeList(List.of("handwritten_cursive"));
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptPath);

        try (MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class)) {
            fileServiceMock.when(() -> FileService.buildOutputFile(outputPath, "alt38", ".pdf"))
                    .thenReturn(outputPath);

            fileServiceMock.when(() -> FileService.readPathList(transcriptPath)).thenReturn(List.of(transcriptPath));

            service.addOcrToFile(options);

            verify(krakenService).addOcrToFile(options);
            assertEquals(inputPath, options.getInputPath());
            assertEquals(outputPath, options.getOutputPath());
            assertEquals(transcriptPath, options.getTranscriptPath());
        }
    }

    @Test
    public void addOcrToFileWithNoTextTextType() throws Exception {
        Path inputPath = Path.of("src/test/resources/dog-wikipedia.png");
        Path outputPath = tempDir.resolve("dog-wikipedia.pdf");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setTextTypeList(List.of("no text"));
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);

        try (
                MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
                MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)
        ) {
            fileServiceMock.when(() -> FileService.buildOutputFile(outputPath,
                            "dog-wikipedia", ".pdf")).thenReturn(outputPath);

            service.addOcrToFile(options);

            verifyNoInteractions(krakenService);

            commandUtilityMock.verify(() ->
                    CommandUtility.executeCommand(List.of(
                            "gm", "convert", "-auto-orient", inputPath.toString(), outputPath.toString()
                    ))
            );
        }
    }

    @Test
    public void addOcrToFileWithHandwrittenTextTypeNullTranscript() throws Exception {
        Path inputPath = Path.of("src/test/resources/alt38.jpg");
        Path outputPath = tempDir.resolve("alt38.pdf");
        Pdf4uOptions options = new Pdf4uOptions();
        options.setTextTypeList(List.of("handwritten_print"));
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);

        try (MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)) {
            service.addOcrToFile(options);

            verifyNoInteractions(krakenService);

            commandUtilityMock.verify(() ->
                    CommandUtility.executeCommand(List.of(
                            "gm", "convert", "-auto-orient", inputPath.toString(), outputPath.toString()
                    ))
            );
        }
    }

    @Test
    public void addOcrToFileWithHandwrittenTextTypeNoTranscript() throws Exception {
        Path inputPath = Path.of("src/test/resources/alt21.jpg");
        Path outputPath = tempDir.resolve("alt21.pdf");
        Pdf4uOptions options = new Pdf4uOptions();
        options.setTextTypeList(List.of("handwritten_print"));
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(Path.of("src/test/resources/alt21_notranscript.txt"));

        try (MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)) {
            service.addOcrToFile(options);

            verifyNoInteractions(krakenService);

            commandUtilityMock.verify(() ->
                    CommandUtility.executeCommand(List.of(
                            "gm", "convert", "-auto-orient", inputPath.toString(), outputPath.toString()
                    ))
            );
        }
    }

    @Test
    public void addOcrToMultipleFilesWithMixedTextTypesSuccessTest() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path outputPath = tempDir.resolve("combined-output.pdf");

        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Path transcript1 = tempDir.resolve("transcript1.txt");
        Path transcript2 = tempDir.resolve("transcript2.txt");
        Files.write(transcriptListPath, List.of(transcript1.toString(), transcript2.toString()), StandardCharsets.UTF_8);

        Path image1 = tempDir.resolve("image1.tif");
        Path image2 = tempDir.resolve("image2.tif");

        Path intermediatePdf1 = tempDir.resolve("image1.pdf");
        Path intermediatePdf2 = tempDir.resolve("image2.pdf");
        Files.createFile(intermediatePdf1);
        Files.createFile(intermediatePdf2);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setTranscriptPath(transcriptListPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("printed", "handwritten_cursive"));

        try (MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
            MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1, image2));

            fileServiceMock.when(() -> FileService.buildOutputFile(outputPath,
                    FilenameUtils.getBaseName(inputListPath.toString()), ".pdf")).thenReturn(outputPath);

            fileServiceMock.when(() -> FileService.prepareTempPath(image1.toString(), ".pdf"))
                .thenReturn(intermediatePdf1);

            fileServiceMock.when(() -> FileService.prepareTempPath(image2.toString(), ".pdf"))
                .thenReturn(intermediatePdf2);

            Path result = service.addOcrToMultipleFiles(options);

            assertEquals(outputPath, result);

            ArgumentCaptor<Pdf4uOptions> krakenOptionsCaptor = ArgumentCaptor.forClass(Pdf4uOptions.class);
            verify(krakenService, times(2)).addOcrToFile(krakenOptionsCaptor.capture());

            List<Pdf4uOptions> capturedOptions = krakenOptionsCaptor.getAllValues();

            Pdf4uOptions printedOptions = capturedOptions.get(0);
            assertEquals(image1, printedOptions.getInputPath());
            assertEquals(intermediatePdf1, printedOptions.getOutputPath());
            assertEquals(transcript1, printedOptions.getTranscriptPath());
            assertEquals(List.of("printed"), printedOptions.getTextTypeList());

            Pdf4uOptions handwrittenOptions = capturedOptions.get(1);
            assertEquals(image2, handwrittenOptions.getInputPath());
            assertEquals(intermediatePdf2, handwrittenOptions.getOutputPath());
            assertEquals(transcript2, handwrittenOptions.getTranscriptPath());
            assertEquals(List.of("handwritten_cursive"), handwrittenOptions.getTextTypeList());

            commandUtilityMock.verify(() -> CommandUtility.executeCommand(List.of(
                    "pdfunite",
                    intermediatePdf1.toString(),
                    intermediatePdf2.toString(),
                    outputPath.toString()
                ))
            );

            assertFalse(Files.exists(intermediatePdf1));
            assertFalse(Files.exists(intermediatePdf2));
        }
    }

    @Test
    public void addOcrToMultipleFilesWithOnlyOneFileSuccessTest() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Path transcript1 = tempDir.resolve("transcript1.txt");
        Files.write(transcriptListPath, Collections.singleton(transcript1.toString()), StandardCharsets.UTF_8);
        Path outputPath = tempDir.resolve("combined-output.pdf");

        Path image1 = tempDir.resolve("image1.tif");
        Path intermediatePdf1 = tempDir.resolve("image1.pdf");
        Files.createFile(intermediatePdf1);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setTranscriptPath(transcriptListPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("printed"));

        try (MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
             MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1));

            fileServiceMock.when(() -> FileService.buildOutputFile(outputPath,
                    FilenameUtils.getBaseName(inputListPath.toString()), ".pdf")).thenReturn(outputPath);

            fileServiceMock.when(() -> FileService.prepareTempPath(image1.toString(), ".pdf"))
                    .thenReturn(intermediatePdf1);

            Path result = service.addOcrToMultipleFiles(options);

            assertEquals(outputPath, result);

            ArgumentCaptor<Pdf4uOptions> krakenOptionsCaptor = ArgumentCaptor.forClass(Pdf4uOptions.class);
            verify(krakenService, times(1)).addOcrToFile(krakenOptionsCaptor.capture());

            List<Pdf4uOptions> capturedOptions = krakenOptionsCaptor.getAllValues();

            Pdf4uOptions printedOptions = capturedOptions.get(0);
            assertEquals(image1, printedOptions.getInputPath());
            assertEquals(intermediatePdf1, printedOptions.getOutputPath());
            assertEquals(transcript1, printedOptions.getTranscriptPath());
            assertEquals(List.of("printed"), printedOptions.getTextTypeList());

            commandUtilityMock.verify(() -> CommandUtility.executeCommand(List.of(
                            "pdfunite",
                            intermediatePdf1.toString(),
                            outputPath.toString()
                    ))
            );

            assertFalse(Files.exists(intermediatePdf1));
        }
    }

    @Test
    public void addOcrToMultipleFilesWithOnlyOneFileNoTranscriptTest() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Files.write(transcriptListPath, Collections.singleton("no transcript"), StandardCharsets.UTF_8);
        Path outputPath = tempDir.resolve("combined-output.pdf");

        Path image1 = tempDir.resolve("image1.tif");
        Path intermediatePdf1 = tempDir.resolve("image1.pdf");
        Files.createFile(intermediatePdf1);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setTranscriptPath(transcriptListPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("mixed"));

        try (MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
             MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1));

            fileServiceMock.when(() -> FileService.buildOutputFile(outputPath,
                    FilenameUtils.getBaseName(inputListPath.toString()), ".pdf")).thenReturn(outputPath);

            fileServiceMock.when(() -> FileService.prepareTempPath(image1.toString(), ".pdf"))
                    .thenReturn(intermediatePdf1);

            Path result = service.addOcrToMultipleFiles(options);

            assertEquals(outputPath, result);

            verifyNoInteractions(krakenService);

            commandUtilityMock.verify(() ->
                    CommandUtility.executeCommand(List.of(
                            "gm", "convert", "-auto-orient", image1.toString(), intermediatePdf1.toString()
                    ))
            );

            commandUtilityMock.verify(() ->
                    CommandUtility.executeCommand(List.of(
                            "pdfunite",
                            intermediatePdf1.toString(),
                            outputPath.toString()
                    ))
            );

            assertFalse(Files.exists(intermediatePdf1));
        }
    }

    @Test
    public void addOcrToMultipleFilesDoesNotSetNoTranscriptPathTest() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Files.write(transcriptListPath, Collections.singleton("no transcript"), StandardCharsets.UTF_8);
        Path outputPath = tempDir.resolve("output.pdf");

        Path image = tempDir.resolve("image.tif");
        Path intermediatePdf = tempDir.resolve("image.pdf");

        Files.createFile(intermediatePdf);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setTranscriptPath(transcriptListPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("mixed"));

        try (MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
             MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image));

            fileServiceMock.when(() -> FileService.buildOutputFile(outputPath,
                    FilenameUtils.getBaseName(inputListPath.toString()), ".pdf")).thenReturn(outputPath);

            fileServiceMock.when(() -> FileService.prepareTempPath(image.toString(), ".pdf"))
                    .thenReturn(intermediatePdf);

            service.addOcrToMultipleFiles(options);

            verifyNoInteractions(krakenService);

            commandUtilityMock.verify(() ->
                    CommandUtility.executeCommand(List.of(
                            "gm", "convert", "-auto-orient", image.toString(), intermediatePdf.toString()
                    ))
            );
        }
    }

    @Test
    public void addOcrToMultipleFilesDifferentCountsTest() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Path transcript1 = tempDir.resolve("transcript1.txt");
        Files.write(transcriptListPath, Collections.singleton(transcript1.toString()), StandardCharsets.UTF_8);
        Path outputPath = tempDir.resolve("combined-output.pdf");

        Path image1 = tempDir.resolve("image1.tif");
        Path image2 = tempDir.resolve("image2.tif");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setTranscriptPath(transcriptListPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("printed", "handwritten_print"));

        try (MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
            MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1, image2));

            fileServiceMock.when(() -> FileService.buildOutputFile(outputPath,
                    FilenameUtils.getBaseName(inputListPath.toString()), "pdf")).thenReturn(outputPath);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.addOcrToMultipleFiles(options)
            );

            assertEquals(
                "Image list and transcript list must have the same number of entries " +
                        "when transcripts are needed. Images = 2, transcripts = 1",
                exception.getMessage()
            );

            verifyNoInteractions(krakenService);

            fileServiceMock.verify(() -> FileService.prepareTempPath(any(String.class), any(String.class)), never());

            commandUtilityMock.verifyNoInteractions();
        }
    }

    @Test
    public void addOcrToMultipleFilesDeletesIntermediatePdfsEvenWhenPdfUniteFails() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Path transcript1 = tempDir.resolve("transcript1.txt");
        Files.write(transcriptListPath, Collections.singleton(transcript1.toString()), StandardCharsets.UTF_8);
        Path outputPath = tempDir.resolve("combined-output.pdf");

        Path image1 = tempDir.resolve("image1.tif");
        Path intermediatePdf1 = tempDir.resolve("image1.pdf");
        Files.createFile(intermediatePdf1);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setTranscriptPath(transcriptListPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("printed"));

        try (MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
            MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1));

            fileServiceMock.when(() -> FileService.buildOutputFile(outputPath,
                    FilenameUtils.getBaseName(inputListPath.toString()), ".pdf")).thenReturn(outputPath);

            fileServiceMock.when(() -> FileService.prepareTempPath(image1.toString(), ".pdf"))
                .thenReturn(intermediatePdf1);

            commandUtilityMock.when(() -> CommandUtility.executeCommand(List.of(
                    "pdfunite",
                    intermediatePdf1.toString(),
                    outputPath.toString()
                )))
                .thenThrow(new RuntimeException("pdfunite failed"));

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.addOcrToMultipleFiles(options));

            assertEquals("pdfunite failed", exception.getMessage());

            verify(krakenService).addOcrToFile(any(Pdf4uOptions.class));

            assertFalse(Files.exists(intermediatePdf1));
        }
    }

    @Test
    public void addOcrToMultipleFilesPrintedTextTypeIsDifferentCaseTest() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Path transcript1 = tempDir.resolve("transcript1.txt");
        Files.write(transcriptListPath, Collections.singleton(transcript1.toString()), StandardCharsets.UTF_8);
        Path outputPath = tempDir.resolve("combined-output.pdf");

        Path image1 = tempDir.resolve("image1.tif");
        Path intermediatePdf1 = tempDir.resolve("image1.pdf");
        Path outputFile = tempDir.resolve("combined-output.pdf");

        Files.createFile(intermediatePdf1);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setTranscriptPath(transcriptListPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("PRINTED"));

        try (MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
             MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1));

            fileServiceMock.when(() -> FileService.buildOutputFile(outputPath,
                    FilenameUtils.getBaseName(inputListPath.toString()), ".pdf")).thenReturn(outputPath);

            fileServiceMock.when(() ->
                FileService.prepareTempPath(image1.toString(), ".pdf")).thenReturn(intermediatePdf1);

            service.addOcrToMultipleFiles(options);

            verify(krakenService).addOcrToFile(any(Pdf4uOptions.class));

            commandUtilityMock.verify(() ->
                CommandUtility.executeCommand(List.of(
                    "pdfunite",
                    intermediatePdf1.toString(),
                    outputFile.toString()
                ))
            );
        }
    }

    @Test
    public void addOcrToMultipleFilesNoTextTextTypeTest() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Files.write(transcriptListPath, Collections.singleton("no transcript"), StandardCharsets.UTF_8);
        Path outputPath = tempDir.resolve("combined-output.pdf");

        Path image1 = tempDir.resolve("image1.tif");
        Path intermediatePdf1 = tempDir.resolve("image1.pdf");
        Files.createFile(intermediatePdf1);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptListPath);
        options.setTextTypeList(List.of("no text"));

        try (MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
             MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1));

            fileServiceMock.when(() -> FileService.buildOutputFile(outputPath,
                    FilenameUtils.getBaseName(inputListPath.toString()), ".pdf")).thenReturn(outputPath);

            fileServiceMock.when(() -> FileService.prepareTempPath(image1.toString(), ".pdf"))
                    .thenReturn(intermediatePdf1);

            service.addOcrToMultipleFiles(options);

            verifyNoInteractions(krakenService);

            commandUtilityMock.verify(() ->
                    CommandUtility.executeCommand(List.of(
                            "gm", "convert", "-auto-orient", image1.toString(), intermediatePdf1.toString()
                    ))
            );

            commandUtilityMock.verify(() ->
                    CommandUtility.executeCommand(List.of(
                            "pdfunite",
                            intermediatePdf1.toString(),
                            outputPath.toString()
                    ))
            );

            assertFalse(Files.exists(intermediatePdf1));
        }
    }

    @Test
    public void addOcrToMultipleFilesWithAndWithoutTextSuccessTest() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Path transcript1 = tempDir.resolve("transcript1.txt");
        Files.write(transcriptListPath, List.of(transcript1.toString(), "no transcript"), StandardCharsets.UTF_8);
        Path outputPath = tempDir.resolve("combined-output.pdf");

        Path image1 = tempDir.resolve("image1.tif");
        Path image2 = tempDir.resolve("image2.tif");

        Path intermediatePdf1 = tempDir.resolve("image1.pdf");
        Path intermediatePdf2 = tempDir.resolve("image2.pdf");

        Files.createFile(intermediatePdf1);
        Files.createFile(intermediatePdf2);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setTranscriptPath(transcriptListPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("handwritten_print", "no text"));

        try (MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
             MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1, image2));

            fileServiceMock.when(() -> FileService.buildOutputFile(outputPath,
                    FilenameUtils.getBaseName(inputListPath.toString()), ".pdf")).thenReturn(outputPath);

            fileServiceMock.when(() -> FileService.prepareTempPath(image1.toString(), ".pdf"))
                    .thenReturn(intermediatePdf1);

            fileServiceMock.when(() -> FileService.prepareTempPath(image2.toString(), ".pdf"))
                    .thenReturn(intermediatePdf2);

            Path result = service.addOcrToMultipleFiles(options);

            assertEquals(outputPath, result);

            ArgumentCaptor<Pdf4uOptions> krakenOptionsCaptor = ArgumentCaptor.forClass(Pdf4uOptions.class);

            verify(krakenService).addOcrToFile(krakenOptionsCaptor.capture());

            Pdf4uOptions handwrittenOptions = krakenOptionsCaptor.getValue();
            assertEquals(image1, handwrittenOptions.getInputPath());
            assertEquals(intermediatePdf1, handwrittenOptions.getOutputPath());
            assertEquals(List.of("handwritten_print"), handwrittenOptions.getTextTypeList());

            commandUtilityMock.verify(() -> CommandUtility.executeCommand(List.of(
                            "gm", "convert", "-auto-orient", image2.toString(), intermediatePdf2.toString()
                    ))
            );

            commandUtilityMock.verify(() -> CommandUtility.executeCommand(List.of(
                            "pdfunite",
                            intermediatePdf1.toString(),
                            intermediatePdf2.toString(),
                            outputPath.toString()
                    ))
            );

            assertFalse(Files.exists(intermediatePdf1));
            assertFalse(Files.exists(intermediatePdf2));
        }
    }
}