package com.home.lab.logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.*;

public class LogTransformer {

    private static final String DEFAULT_PATH_TO_LOGS_DIRECTORY = "/tmp/logs";
    private static final String DEFAULT_PATH_TO_TRANSFORMED_LOGS_DIRECTORY = "/tmp/transformed";
    private static final long DEFAULT_TIME_BETWEEN_LOGS_DIRECTORY_SCANS_MILLIS = 10000;

    private static final Map<Path, FileMetadata> processedFiles = new HashMap<>();

    public static void main(String[] args) {
        try {
            HeartbeatMonitor.startHeartbeatMonitor();
            Path pathToLogsDirectory = findPathToLogsDirectory();
            long pauseDurationMillis = findPauseDurationBetweenTransformations();
            while (true) {
                List<Path> logFilesForProcessing = scanLogFilesForTransformation(pathToLogsDirectory);
                processLogFiles(logFilesForProcessing);
                Thread.sleep(pauseDurationMillis);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static Path findPathToLogsDirectory() {
        try {
            Optional<String> overwrittenPathToLogsDirectory = Optional.ofNullable(System.getenv("PATH_TO_LOGS_DIRECTORY"));
            Path pathToLogsDirectory = overwrittenPathToLogsDirectory.map(Path::of).orElseGet(() -> Path.of(DEFAULT_PATH_TO_LOGS_DIRECTORY));
            if (!Files.isDirectory(pathToLogsDirectory)) {
                throw new IllegalStateException("Invalid configuration state: PATH_TO_LOGS_DIRECTORY doesn't point to a directory");
            }
            return pathToLogsDirectory;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new IllegalStateException("Invalid configuration state: PATH_TO_LOGS_DIRECTORY is not configured properly");
        }
    }

    private static long findPauseDurationBetweenTransformations() {
        try {
            long timeBetweenHeartbeatChecksMillis = Optional.ofNullable(System.getenv("TIME_BETWEEN_LOGS_DIRECTORY_SCANS_MILLIS")).map(Long::parseLong)
                    .orElse(DEFAULT_TIME_BETWEEN_LOGS_DIRECTORY_SCANS_MILLIS);
            if (timeBetweenHeartbeatChecksMillis <= 0) {
                throw new IllegalStateException("Invalid configuration state: TIME_BETWEEN_LOGS_DIRECTORY_SCANS_MILLIS cannot be a negative number or zero");
            }
            return timeBetweenHeartbeatChecksMillis;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new IllegalStateException(exception.getMessage());
        }
    }

    private static List<Path> scanLogFilesForTransformation(Path pathToLogsDirectory) {
        try (DirectoryStream<Path> logsDirectoryStream = Files.newDirectoryStream(pathToLogsDirectory)) {
            List<Path> logFilesForProcessing = new ArrayList<>();
            for (Path logFile : logsDirectoryStream) {
                if (processedFiles.containsKey(logFile)) {
                    FileMetadata previouslyProcessedLogFileMetadata = processedFiles.get(logFile);
                    BasicFileAttributes logFileAttributes = Files.readAttributes(logFile, BasicFileAttributes.class);
                    if (previouslyProcessedLogFileMetadata.lastModifiedTime.compareTo(logFileAttributes.lastModifiedTime()) < 0) {
                        logFilesForProcessing.add(logFile);
                    }
                } else {
                    logFilesForProcessing.add(logFile);
                }
            }
            return logFilesForProcessing;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static void processLogFiles(List<Path> logFiles) {
        for (Path logFile : logFiles) {
            try (BufferedReader reader = Files.newBufferedReader(logFile)) {
                long lastLineProcessed = 0;
                if (processedFiles.containsKey(logFile)) {
                    lastLineProcessed = processedFiles.get(logFile).lastLineProcessed;
                    for (long lineNumber = 0; lineNumber < lastLineProcessed; lineNumber++) {
                        if (reader.readLine() == null) {
                            return;
                        }
                    }
                }
                long numberOfLinesProcessed = transformLogFile(logFile, reader, lastLineProcessed);
                BasicFileAttributes logFileAttributes = Files.readAttributes(logFile, BasicFileAttributes.class);
                FileMetadata updatedFileMetadata = new FileMetadata(logFile, logFileAttributes.lastModifiedTime(), numberOfLinesProcessed);
                processedFiles.put(logFile, updatedFileMetadata);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static long transformLogFile(Path logFile, BufferedReader reader, long lastLineProcessed) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(findPathToTransformedLogFile(logFile), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            String logLine;
            long numberOfLinesProcessed = lastLineProcessed;
            while ((logLine = reader.readLine()) != null) {
                writer.write(transformLogLine(logLine));
                writer.newLine();
                numberOfLinesProcessed++;
            }
            return numberOfLinesProcessed;
        }
    }

    private static Path findPathToTransformedLogFile(Path logFileForTransformation) {
        try {
            Optional<String> overwrittenPathToTransformedLogsDirectory = Optional.ofNullable(System.getenv("PATH_TO_TRANSFORMED_LOGS_DIRECTORY"));
            Path pathToTransformedLogsDirectory = overwrittenPathToTransformedLogsDirectory.map(Path::of).orElseGet(() -> Path.of(DEFAULT_PATH_TO_TRANSFORMED_LOGS_DIRECTORY));
            if (!Files.isDirectory(pathToTransformedLogsDirectory)) {
                throw new IllegalStateException("Invalid configuration state: PATH_TO_TRANSFORMED_LOGS_DIRECTORY doesn't point to a directory");
            }
            return pathToTransformedLogsDirectory.resolve(logFileForTransformation.getFileName().toString());
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new IllegalStateException("Invalid configuration state: PATH_TO_TRANSFORMED_LOGS_DIRECTORY is not configured properly");
        }
    }

    private static String transformLogLine(String logLine) {
        return "%s: Transformed at %s".formatted(logLine, LocalDateTime.now());
    }

    record FileMetadata(Path logFile, FileTime lastModifiedTime, long lastLineProcessed) {
    }
}
