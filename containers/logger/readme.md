# Simple logger

## Overview

A very simple logging application written in Java.
Its main goal is to provide functionality that simulates a meaningful service, which can be used for testing different
features in Kubernetes.

The application only produces logs and can be configured to write them to standard output and/or a file.
It can generate a finite or infinite number of logs and can also be run in a mode where it remains active without
performing any actions.

During the lifetime of the application, a heartbeat periodically runs and updates the last modification date of a file.
The default path for the file is /tmp/heartbeat/heartbeat.

## Setup

We are using Maven primarily as a build tool, which allows us to package the application easily.
Once the application has been packaged, we can run the JAR file and provide command-line arguments that control how it
produces logs.
If no arguments are provided, it will run in an infinite cycle without performing any actions.

Command-line arguments follow the format key=value. Multiple arguments can be provided in any order.

Currently supported arguments:

1. **nlp** – Number of logs to produce. Expects a positive integer or 0. Any invalid value will result in an error. If
   omitted, the application will produce an infinite number of logs.

2. **ltso** – Enable logging to standard output. Expects a boolean value: true or false. Any invalid value will result
   in an error. If omitted, logs will not be written to standard output.

3. **ltf** – Enable logging to a file. Expects a boolean value: true or false. Any invalid value will result in an
   error. If this argument is present, the _plf_ argument must also be provided, otherwise an error will be returned.

4. **plf** – Path to the log file. Expects a valid POSIX path. Any invalid value will result in an error. This argument
   is used only if _ltf_ is set to true.

### Examples

Produce 10 log lines to the standard output:

```shell 
java -jar logger.jar nlp=10 ltso=true
```

Produce an infinite number of logs to a file:

```shell 
java -jar logger.jar ltf=true plf=/tmp/timestamp.log
```

Produce an infinite number of logs to both a file and standard output:

```shell
java -jar logger.jar ltso=true ltf=true plf=/tmp/timestamp.log
```

## Delivery

To run the application in a Kubernetes cluster, it needs to be packaged into a Docker image.
We use a multi-stage build for this; refer to the Dockerfile in the root of the logger project.

Once the image is built, it is published to the following DockerHub registry:
https://hub.docker.com/repository/docker/dangodajango/logger/general

To automate this process, you can use the build.sh script. Note that the version must be manually incremented inside the
script.

The following images can be used by any workload in Kubernetes:

- dangodajango/logger:1.0.1
- dangodajango/logger:1.0.0-distroless