package com.home.lab.logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class Heartbeat {

    private static final String DEFAULT_PATH_TO_HEARTBEAT_FILE = "/tmp/heartbeat";
    private static final long TIMEOUT_BETWEEN_HEARTBEATS = 10000;

    public static void startHeartbeatThread() {
        new Thread(Heartbeat::performHeartbeat).start();
    }

    private static void performHeartbeat() {
        try {
            Path pathToHeartbeatFile = Path.of(DEFAULT_PATH_TO_HEARTBEAT_FILE);
            if (Files.notExists(pathToHeartbeatFile)) {
                Files.createFile(pathToHeartbeatFile);
            }
            while (true) {
                Files.setLastModifiedTime(pathToHeartbeatFile, FileTime.from(Instant.now()));
                Thread.sleep(TIMEOUT_BETWEEN_HEARTBEATS);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            System.err.println("A problem occurred while performing a heartbeat. Shutting down the heartbeat thread.");
        }
    }
}
