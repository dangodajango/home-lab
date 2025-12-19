package com.home.lab.logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Optional;

public class HeartbeatMonitor {

    private static final String DEFAULT_PATH_TO_HEARTBEAT_FILE = "/tmp/heartbeat";
    private static final long DEFAULT_TIME_BETWEEN_HEARTBEAT_CHECKS_MILLIS = 30000;
    private static final int FAILED_HEARTBEAT_THRESHOLD = 3;

    public static void startHeartbeatMonitor() {
        Thread heartbeatMonitorThread = new Thread(HeartbeatMonitor::performHeartbeatCheck);
        heartbeatMonitorThread.setName("Heartbeat Monitor Thread");
        heartbeatMonitorThread.start();
    }

    private static void performHeartbeatCheck() {
        Path heartbeatFile = findHeartbeatFile();
        long timeBetweenHeartbeatChecksMillis = findTimeBetweenHeartbeatChecksMillis();
        FileTime previousModifiedTime = null;
        int failedHeartbeatChecks = 0;
        while (true) {
            try {
                BasicFileAttributes heartbeatFileAttributes = Files.readAttributes(heartbeatFile, BasicFileAttributes.class);
                if (previousModifiedTime == null) {
                    previousModifiedTime = heartbeatFileAttributes.lastModifiedTime();
                } else {
                    if (isHeartbeatFileUpdated(previousModifiedTime, heartbeatFileAttributes.lastModifiedTime())) {
                        previousModifiedTime = heartbeatFileAttributes.lastModifiedTime();
                        failedHeartbeatChecks = 0;
                    } else {
                        if (failedHeartbeatChecks >= FAILED_HEARTBEAT_THRESHOLD) {
                            System.err.println("System shutdown: Reached the threshold for heartbeat retries");
                            System.exit(0);
                        }
                        failedHeartbeatChecks++;
                    }
                }
                Thread.sleep(timeBetweenHeartbeatChecksMillis);
            } catch (Exception exception) {
                exception.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static Path findHeartbeatFile() {
        try {
            Path heartbeatFile = Optional.ofNullable(System.getenv("PATH_TO_HEARTBEAT_FILE")).map(Path::of)
                    .orElse(Path.of(DEFAULT_PATH_TO_HEARTBEAT_FILE));
            if (!heartbeatFile.toFile().isFile()) {
                throw new IllegalStateException("Invalid configuration state: PATH_TO_HEARTBEAT_FILE doesn't point to a file");
            }
            return heartbeatFile;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new IllegalStateException(exception.getMessage());
        }
    }

    private static long findTimeBetweenHeartbeatChecksMillis() {
        try {
            long timeBetweenHeartbeatChecksMillis = Optional.ofNullable(System.getenv("TIME_BETWEEN_HEARTBEAT_CHECKS_MILLIS")).map(Long::parseLong)
                    .orElse(DEFAULT_TIME_BETWEEN_HEARTBEAT_CHECKS_MILLIS);
            if (timeBetweenHeartbeatChecksMillis <= 0) {
                throw new IllegalStateException("Invalid configuration state: TIME_BETWEEN_HEARTBEAT_CHECKS_MILLIS cannot be a negative number or zero");
            }
            return timeBetweenHeartbeatChecksMillis;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new IllegalStateException(exception.getMessage());
        }
    }

    private static boolean isHeartbeatFileUpdated(FileTime previousModifiedTime, FileTime currentModifiedTime) {
        return previousModifiedTime.compareTo(currentModifiedTime) < 0;
    }
}
