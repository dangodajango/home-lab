package com.home.lab.logger;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

public class CommandLineArgumentExtractor {

    private static final int KEY_POSITION = 0;
    private static final int VALUE_POSITION = 1;

    public static CommandLineArguments extractCommandLineArguments(String[] args) {
        CommandLineArguments commandLineArguments = new CommandLineArguments();
        Arrays.stream(args)
                .filter(arg -> !arg.isBlank() || !arg.contains("="))
                .map(arg -> arg.split("="))
                .forEach(keyValuePair -> {
                    if (keyValuePair.length != 2) {
                        throw new IllegalArgumentException();
                    }
                    populateCommandLineArgumentsPojo(commandLineArguments, keyValuePair);
                });
        return commandLineArguments;
    }

    private static void populateCommandLineArgumentsPojo(CommandLineArguments commandLineArguments, String[] keyValuePair) {
        String value = keyValuePair[VALUE_POSITION];
        switch (keyValuePair[KEY_POSITION]) {
            case "nlp" -> extractNumberOfLogsToProduceArgument(commandLineArguments, value);
            case "ltso" -> extractProduceLogsToStandardOutputArgument(commandLineArguments, value);
            case "ltf" -> extractProduceLogsToFileArgument(commandLineArguments, value);
            case "plf" -> extractPathToLogFileArgument(commandLineArguments, value);
        }
    }

    private static void extractNumberOfLogsToProduceArgument(CommandLineArguments commandLineArguments, String value) {
        try {
            int numberOfLogsToProduce = Integer.parseInt(value);
            if (numberOfLogsToProduce < 0) {
                throw new IllegalArgumentException("Invalid input for argument nlp (NumberOfLogsToProduce): it cannot be a negative number");
            }
            commandLineArguments.setNumberOfLogsToProduce(numberOfLogsToProduce);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid input for argument nlp (NumberOfLogsToProduce): the number could not be parsed");
        }
    }

    private static void extractProduceLogsToStandardOutputArgument(CommandLineArguments commandLineArguments, String value) {
        if (value.equals("true") || value.equals("false")) {
            commandLineArguments.setProduceLogsToStandardOutput(Boolean.parseBoolean(value));
        } else {
            throw new IllegalArgumentException("Invalid input for argument ltso (ProduceLogsToStandardOutput): the value must be either 'true' or 'false'");
        }
    }

    private static void extractProduceLogsToFileArgument(CommandLineArguments commandLineArguments, String value) {
        if (value.equals("true") || value.equals("false")) {
            commandLineArguments.setProduceLogsToFile(Boolean.parseBoolean(value));
        } else {
            throw new IllegalArgumentException("Invalid input for argument ltf (ProduceLogsToFile): the value must be either 'true' or 'false'");
        }
    }

    private static void extractPathToLogFileArgument(CommandLineArguments commandLineArguments, String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException("Invalid input for argument plf (PathToLogFile): the value must not be blank");
        }
        try {
            commandLineArguments.setPathToLogFile(Path.of(value));
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new IllegalArgumentException("Invalid input for argument plf (PathToLogFile): the value (%s) must be a valid path".formatted(value));
        }
    }

    public static class CommandLineArguments {
        private Integer numberOfLogsToProduce;
        private Boolean produceLogsToStandardOutput;
        private Boolean produceLogsToFile;
        private Path pathToLogFile;

        public Optional<Integer> getNumberOfLogsToProduce() {
            return Optional.ofNullable(numberOfLogsToProduce);
        }

        public void setNumberOfLogsToProduce(Integer numberOfLogsToProduce) {
            this.numberOfLogsToProduce = numberOfLogsToProduce;
        }

        public Optional<Boolean> getProduceLogsToStandardOutput() {
            return Optional.ofNullable(produceLogsToStandardOutput);
        }

        public void setProduceLogsToStandardOutput(Boolean produceLogsToStandardOutput) {
            this.produceLogsToStandardOutput = produceLogsToStandardOutput;
        }

        public Optional<Boolean> getProduceLogsToFile() {
            return Optional.ofNullable(produceLogsToFile);
        }

        public void setProduceLogsToFile(Boolean produceLogsToFile) {
            this.produceLogsToFile = produceLogsToFile;
        }

        public Optional<Path> getPathToLogFile() {
            return Optional.ofNullable(pathToLogFile);
        }

        public void setPathToLogFile(Path pathToLogFile) {
            this.pathToLogFile = pathToLogFile;
        }
    }
}
