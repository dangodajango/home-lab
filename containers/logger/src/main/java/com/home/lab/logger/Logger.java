package com.home.lab.logger;

import com.home.lab.logger.util.CommandLineArgumentExtractor;
import com.home.lab.logger.util.CommandLineArgumentExtractor.CommandLineArguments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Logger {

    public static void main(String[] args) {
        try {
            CommandLineArguments commandLineArguments = CommandLineArgumentExtractor.extractCommandLineArguments(args);
            commandLineArguments.getNumberOfLogsToProduce()
                    .ifPresentOrElse(
                            numberOfLogsToProduce -> produceFixedNumberOfLogs(numberOfLogsToProduce, commandLineArguments),
                            () -> produceInfiniteNumberOfLogs(commandLineArguments)
                    );
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            System.exit(1);
        }
    }

    private static void produceFixedNumberOfLogs(Integer numberOfLogsToProduce, CommandLineArguments commandLineArguments) {
        for (int i = 0; i < numberOfLogsToProduce; i++) {
            produceLog(commandLineArguments);
        }
    }

    private static void produceInfiniteNumberOfLogs(CommandLineArguments commandLineArguments) {
        while (true) {
            produceLog(commandLineArguments);
        }
    }

    private static void produceLog(CommandLineArguments commandLineArguments) {
        String logMessage = LocalDateTime.now().toString();
        commandLineArguments.getProduceLogsToStandardOutput()
                .ifPresent(produceLogsToStandardOutput -> {
                    if (produceLogsToStandardOutput) writeToStandardOutput(logMessage);
                });
        commandLineArguments.getProduceLogsToFile()
                .ifPresent(produceLogsToFile -> {
                    if (produceLogsToFile) {
                        commandLineArguments.getPathToLogFile()
                                .ifPresentOrElse(
                                        pathToFile -> writeToFile(logMessage, pathToFile),
                                        () -> {
                                            throw new IllegalArgumentException("Invalid configuration state: to produce logs to a file, a path to a file must be specified");
                                        });
                    }
                });
    }

    private static void writeToStandardOutput(String logMessage) {
        System.out.println(logMessage);
    }

    private static void writeToFile(String logMessage, Path pathToFile) {
        try {
            Files.writeString(pathToFile, logMessage);
        } catch (IOException exception) {
            System.err.printf("Could not produce a log to the file %s%n", pathToFile.getFileName());
            throw new IllegalStateException(exception.getMessage());
        }
    }
}

