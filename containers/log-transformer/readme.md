# Log transformer

## Overview

This application is a simple log transformer. It takes all log files from a specific directory, appends metadata to each
line, and then stores the transformed log files in a different, configured location on the file system.

The main idea is to have logic that is decoupled from the main application, allowing it to be used to test the
Kubernetes sidecar Pod pattern.

This is a long-running process that scans all files in a specific directory at fixed time intervals (for example, every
30 seconds). Whenever a new file appears, it is picked up during the next scan and queued for processing. Once a file is
processed, a new file with the updated contents is created in a different directory, using the same file name.

For files that already exist but have new data appended, processing resumes from the last line that was previously
transformed. Any log entries that were already processed are not reprocessed - only newly added log lines are handled.
If there are no changes to existing files, they are simply skipped during the next scan.

Since this process is not meant to run standalone, it depends on another application. If the main application is no
longer running, there is no need to keep this process alive, as it would only keep the Pod running without doing any
meaningful work - especially if the main application has a fixed lifetime.

To address this, it uses a heartbeat mechanism that the main application must implement. The process checks the last
modification time of a configured file; if the file has not been updated since the previous health check, a retry
mechanism is triggered. After three consecutive failed heartbeat checks, it assumes that the main container is no longer
running and shuts itself down.

## Setup

We are using Maven primarily as a build tool, which allows us to package the application easily. Once the application is
packaged, we can run the JAR file, configure the environment, and start an application that produces logs, which can
then be transformed and handled by this process.

If no other process is running, the log transformer will fail its heartbeat checks and shut down on its own.

The following environment variables can be provided to customize the behavior of the service:

- **PATH_TO_LOGS_DIRECTORY** – The path to the directory where the log files are located. Expects a valid POSIX path. Any
invalid value will result in an error. The default location is **/tmp/logs**.

- **PATH_TO_TRANSFORMED_LOGS_DIRECTORY** – The path to the directory where the transformed log files will be created. Expects
a valid POSIX path. Any invalid value will result in an error. The default location is **/tmp/transformed**.

- **PATH_TO_HEARTBEAT_FILE** – The path to the heartbeat file, used to determine whether the main application is still
running. Expects a valid POSIX path. Any invalid value will result in an error. The default location is **/tmp/heartbeat**.

- **TIME_BETWEEN_HEARTBEAT_CHECKS_MILLIS** – The interval between heartbeat checks, in milliseconds. Expects an integer or
long value. Any invalid value will result in an error. The default interval is **30000** milliseconds (30 seconds).

- **TIME_BETWEEN_LOGS_DIRECTORY_SCANS_MILLIS** – The interval between scans of the logs directory, controlling how often logs
are transformed. Expects an integer or long value. Any invalid value will result in an error. The default interval is
**10000** milliseconds (10 seconds).

## Delivery

To run the application in a Kubernetes cluster, it needs to be packaged into a Docker image.
We use a multi-stage build for this; refer to the Dockerfile in the root of the logger project.

Once the image is built, it is published to the following DockerHub registry:
https://hub.docker.com/repository/docker/dangodajango/log-transformer/general

To automate this process, you can use the build.sh script. Note that the version must be manually incremented inside the
script.

The following images can be used by any workload in Kubernetes:

- dangodajango/log-transformer:1.0.0