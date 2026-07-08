package pdf4u.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
    private OcrMyPdfService ocrMyPdfService;

    @BeforeEach
    public void setup() {
        service = new MultipleTextTypesService();

        krakenService = mock(KrakenService.class);
        ocrMyPdfService = mock(OcrMyPdfService.class);

        service.setKrakenService(krakenService);
        service.setOcrMyPdfService(ocrMyPdfService);
    }

    @Test
    public void addOcrToFileWithSinglePrintedTextType() throws Exception {
        Pdf4uOptions options = new Pdf4uOptions();
        options.setTextTypeList(List.of("printed"));

        service.addOcrToFile(options);

        verify(ocrMyPdfService).addOcrToFile(options);
        verifyNoInteractions(krakenService);
    }

    @Test
    public void addOcrToFileWithSingleTypedTextType() throws Exception {
        Pdf4uOptions options = new Pdf4uOptions();
        options.setTextTypeList(List.of("typed"));

        service.addOcrToFile(options);

        verify(ocrMyPdfService).addOcrToFile(options);
        verifyNoInteractions(krakenService);
    }

    @Test
    public void addOcrToFileWithSingleHandwrittenTextType() throws Exception {
        Pdf4uOptions options = new Pdf4uOptions();
        options.setTextTypeList(List.of("handwritten"));

        service.addOcrToFile(options);

        verify(krakenService).addOcrToFile(options);
        verifyNoInteractions(ocrMyPdfService);
    }

    @Test
    public void addOcrToFileWithNoTextTextType() throws Exception {
        Path inputPath = Path.of("src/test/resources/dog-wikipedia.png");
        Path outputPath = tempDir.resolve("dog-wikipedia.pdf");
        Pdf4uOptions options = new Pdf4uOptions();
        options.setTextTypeList(List.of("no text"));
        options.setInputPath(inputPath);
        options.setOutputPath(outputPath);

        try (MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)) {
            service.addOcrToFile(options);

            verifyNoInteractions(krakenService);
            verifyNoInteractions(ocrMyPdfService);

            commandUtilityMock.verify(() ->
                    CommandUtility.executeCommand(List.of(
                            "img2pdf", inputPath.toString(), "--output", outputPath.toString(), "--first-frame-only"
                    ))
            );
        }
    }

    @Test
    public void addOcrToMultipleFilesWithMixedTextTypesSuccessTest() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Path outputPath = tempDir.resolve("combined-output.pdf");

        Path image1 = tempDir.resolve("image1.tif");
        Path image2 = tempDir.resolve("image2.tif");

        Path transcript2 = tempDir.resolve("transcript2.txt");

        Path intermediatePdf1 = tempDir.resolve("image1.pdf");
        Path intermediatePdf2 = tempDir.resolve("image2.pdf");

        Path outputFile = tempDir.resolve("combined-output.pdf");

        Files.createFile(intermediatePdf1);
        Files.createFile(intermediatePdf2);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setTranscriptPath(transcriptListPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("printed", "handwritten"));

        try (
            MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
            MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)
        ) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1, image2));

            fileServiceMock.when(() ->
                FileService.readPathList(transcriptListPath)).thenReturn(List.of("no transcript", transcript2));

            fileServiceMock.when(() -> FileService.prepareTempPath(image1.toString(), ".pdf"))
                .thenReturn(intermediatePdf1);

            fileServiceMock.when(() -> FileService.prepareTempPath(image2.toString(), ".pdf"))
                .thenReturn(intermediatePdf2);

            Path result = service.addOcrToMultipleFiles(options);

            assertEquals(outputFile, result);

            ArgumentCaptor<Pdf4uOptions> ocrOptionsCaptor = ArgumentCaptor.forClass(Pdf4uOptions.class);

            verify(ocrMyPdfService).addOcrToFile(ocrOptionsCaptor.capture());

            Pdf4uOptions printedOptions = ocrOptionsCaptor.getValue();
            assertEquals(image1, printedOptions.getInputPath());
            assertEquals(intermediatePdf1, printedOptions.getOutputPath());
            assertEquals(null, printedOptions.getTranscriptPath());
            assertEquals(List.of("printed"), printedOptions.getTextTypeList());

            ArgumentCaptor<Pdf4uOptions> krakenOptionsCaptor = ArgumentCaptor.forClass(Pdf4uOptions.class);

            verify(krakenService).addOcrToFile(krakenOptionsCaptor.capture());

            Pdf4uOptions handwrittenOptions = krakenOptionsCaptor.getValue();
            assertEquals(image2, handwrittenOptions.getInputPath());
            assertEquals(intermediatePdf2, handwrittenOptions.getOutputPath());
            assertEquals(transcript2, handwrittenOptions.getTranscriptPath());
            assertEquals(List.of("handwritten"), handwrittenOptions.getTextTypeList());

            commandUtilityMock.verify(() -> CommandUtility.executeCommand(List.of(
                    "pdfunite",
                    intermediatePdf1.toString(),
                    intermediatePdf2.toString(),
                    outputFile.toString()
                ))
            );

            assertFalse(Files.exists(intermediatePdf1));
            assertFalse(Files.exists(intermediatePdf2));
        }
    }

    @Test
    public void addOcrToMultipleFilesDifferentCountsTest() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Path outputPath = tempDir.resolve("combined-output");

        Path image1 = tempDir.resolve("image1.tif");
        Path image2 = tempDir.resolve("image2.tif");

        Path transcript1 = tempDir.resolve("transcript1.txt");

        Path outputFile = tempDir.resolve("combined-output.pdf");

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setTranscriptPath(transcriptListPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("printed", "handwritten"));

        try (
            MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
            MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)
        ) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1, image2));

            fileServiceMock.when(() -> FileService.readPathList(transcriptListPath)).thenReturn(List.of(transcript1));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.addOcrToMultipleFiles(options)
            );

            assertEquals(
                "Image list and transcript list must have the same number of entries " +
                        "when transcripts are needed. Images = 2, transcripts = 1",
                exception.getMessage()
            );

            verifyNoInteractions(ocrMyPdfService);
            verifyNoInteractions(krakenService);

            fileServiceMock.verify(() -> FileService.prepareTempPath(any(String.class), any(String.class)), never());

            commandUtilityMock.verifyNoInteractions();
        }
    }

    @Test
    public void addOcrToMultipleFilesDeletesIntermediatePdfsEvenWhenPdfUniteFails() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Path outputPath = tempDir.resolve("combined-output.pdf");

        Path image1 = tempDir.resolve("image1.tif");
        Path transcript1 = tempDir.resolve("transcript1.txt");

        Path intermediatePdf1 = tempDir.resolve("image1.pdf");
        Path outputFile = tempDir.resolve("combined-output.pdf");

        Files.createFile(intermediatePdf1);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setTranscriptPath(transcriptListPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("printed"));

        try (
            MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
            MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)
        ) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1));

            fileServiceMock.when(() -> FileService.readPathList(transcriptListPath)).thenReturn(List.of(transcript1));

            fileServiceMock.when(() -> FileService.prepareTempPath(image1.toString(), ".pdf"))
                .thenReturn(intermediatePdf1);

            commandUtilityMock.when(() -> CommandUtility.executeCommand(List.of(
                    "pdfunite",
                    intermediatePdf1.toString(),
                    outputFile.toString()
                )))
                .thenThrow(new RuntimeException("pdfunite failed"));

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.addOcrToMultipleFiles(options));

            assertEquals("pdfunite failed", exception.getMessage());

            verify(ocrMyPdfService).addOcrToFile(any(Pdf4uOptions.class));

            assertFalse(Files.exists(intermediatePdf1));
        }
    }

    @Test
    public void addOcrToMultipleFilesPrintedTextTypeIsDifferentCaseTest() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Path outputPath = tempDir.resolve("combined-output.pdf");

        Path image1 = tempDir.resolve("image1.tif");
        Path transcript1 = tempDir.resolve("transcript1.txt");

        Path intermediatePdf1 = tempDir.resolve("image1.pdf");
        Path outputFile = tempDir.resolve("combined-output.pdf");

        Files.createFile(intermediatePdf1);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setTranscriptPath(transcriptListPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("PRINTED"));

        try (
            MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
            MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)
        ) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1));

            fileServiceMock.when(() -> FileService.readPathList(transcriptListPath)).thenReturn(List.of(transcript1));

            fileServiceMock.when(() ->
                FileService.prepareTempPath(image1.toString(), ".pdf")).thenReturn(intermediatePdf1);

            service.addOcrToMultipleFiles(options);

            verify(ocrMyPdfService).addOcrToFile(any(Pdf4uOptions.class));
            verifyNoInteractions(krakenService);

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
        Path outputPath = tempDir.resolve("combined-output.pdf");

        Path image1 = tempDir.resolve("image1.tif");
        Path intermediatePdf1 = tempDir.resolve("image1.pdf");
        Path outputFile = tempDir.resolve("combined-output.pdf");

        Files.createFile(intermediatePdf1);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setOutputPath(outputPath);
        options.setTranscriptPath(transcriptListPath);
        options.setTextTypeList(List.of("no text"));

        try (
                MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
                MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)
        ) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1));

            fileServiceMock.when(() -> FileService.readPathList(transcriptListPath)).thenReturn(List.of("no transcript"));

            fileServiceMock.when(() -> FileService.prepareTempPath(image1.toString(), ".pdf"))
                    .thenReturn(intermediatePdf1);

            service.addOcrToMultipleFiles(options);

            verifyNoInteractions(krakenService);
            verifyNoInteractions(ocrMyPdfService);

            commandUtilityMock.verify(() ->
                    CommandUtility.executeCommand(List.of(
                            "img2pdf", image1.toString(), "--output", intermediatePdf1.toString(), "--first-frame-only"
                    ))
            );

            commandUtilityMock.verify(() ->
                    CommandUtility.executeCommand(List.of(
                            "pdfunite",
                            intermediatePdf1.toString(),
                            outputFile.toString()
                    ))
            );

            assertFalse(Files.exists(intermediatePdf1));
        }
    }

    @Test
    public void addOcrToMultipleFilesWithAndWithoutTextSuccessTest() throws Exception {
        Path inputListPath = tempDir.resolve("images.txt");
        Path transcriptListPath = tempDir.resolve("transcripts.txt");
        Path outputPath = tempDir.resolve("combined-output.pdf");

        Path image1 = tempDir.resolve("image1.tif");
        Path image2 = tempDir.resolve("image2.tif");

        Path transcript1 = tempDir.resolve("transcript1.txt");
        Path noTranscript = tempDir.resolve("no-transcript.txt");

        Path intermediatePdf1 = tempDir.resolve("image1.pdf");
        Path intermediatePdf2 = tempDir.resolve("image2.pdf");

        Files.createFile(intermediatePdf1);
        Files.createFile(intermediatePdf2);

        Pdf4uOptions options = new Pdf4uOptions();
        options.setInputPath(inputListPath);
        options.setTranscriptPath(transcriptListPath);
        options.setOutputPath(outputPath);
        options.setTextTypeList(List.of("handwritten", "no text"));

        try (
                MockedStatic<FileService> fileServiceMock = mockStatic(FileService.class);
                MockedStatic<CommandUtility> commandUtilityMock = mockStatic(CommandUtility.class)
        ) {
            fileServiceMock.when(() -> FileService.readPathList(inputListPath)).thenReturn(List.of(image1, image2));

            fileServiceMock.when(() ->
                    FileService.readPathList(transcriptListPath)).thenReturn(List.of(transcript1, noTranscript));

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
            assertEquals(List.of("handwritten"), handwrittenOptions.getTextTypeList());

            commandUtilityMock.verify(() -> CommandUtility.executeCommand(List.of(
                            "img2pdf", image2.toString(), "--output", intermediatePdf2.toString(), "--first-frame-only"
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