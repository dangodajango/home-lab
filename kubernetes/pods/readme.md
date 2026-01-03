# Pods

## Theory

Kubernetes provides functionality to manage containers, so we don’t have to work with them manually.
The Pod resource is the core of Kubernetes. It is a wrapper around one or more containers and configures aspects such as
networking, volumes, and general container properties like environment variables and ports, among other things.
We generally don't work directly with Pods, rather we use other Kubernetes primitives which manage pods for us, but we
still need to know how Pods work.

In general, we don’t work directly with Pods, instead, we use other Kubernetes primitives that manage Pods for us.
However, it is still important to understand how Pods work.

In most use cases, we have a single container per Pod - for example, a microservice. The Pod is scheduled on a worker
node in the cluster that has sufficient resources to meet the Pod’s requirements. If we have multiple containers inside
a Pod, all of them will run on the same node. That’s why it is usually better to use workload controllers, such as
ReplicaSets, to scale the number of application instances, rather than placing multiple containers in a single Pod. If
the node fails, all containers in that Pod will be affected.

The other use case for multiple containers in a Pod is when the containers are tightly coupled. For example, a main
application container might write logs in a specific format, while a secondary container transforms those logs into a
different format. Instead of embedding that logic into the application container, we run it as a separate service in
another container. These containers can be configured to share files, and they automatically share the networking
namespace, allowing them to communicate with each other over localhost.

If a container inside a Pod dies, the kubelet will restart it according to the configured restart policy. If the Pod
itself dies, it will remain dead unless a workload controller is managing it. At its core, the Pod is the glue between
the containers and the Kubernetes system.

## Declarative approach

We can use the kubectl explain command to get more information about the different properties available for
configuration.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: romans
  labels:
    chapter: 10
    verse: 9
spec:
  containers:
    - name: romans
      image: romans:10.9
      imagePullPolicy: IfNotPresent
      restartPolicy: OnFailure
      command: [ "/bin/sh" ]
      args: [ "-c", "preach", "3600" ]
      env:
        - name: BIBLE_CHAPTER
          value: 10
        - name: BIBLE_VERSE
          value: 9
      ports:
        - containerPort: 109
          name: romansPort
          protocol: TCP
      volumeMounts:
        - name: romansVolume
          mountPath: /usr/paul/verses
          readOnly: false
      resources:
        requests:
          cpu: 1
          memory: 250Mi
        limits:
          cpu: 1
          memory: 500Mi
      startupProbe:
        initialDelaySeconds: 60
        periodSeconds: 10
        successThreshold: 1
        failureThreshold: 3
        timeoutSeconds: 3
        exec:
          command: [ "sh", "-c", "./read-verse.sh" ]
      livenessProbe:
        httpGet:
          path: /verses
          port: 109
          scheme: HTTP
      readinessProbe:
        tcpSocket:
          port: 109
  volumes:
    - name: romansVolume
      emptyDir: { }
  restartPolicy: Always
  serviceAccountName: romansSaAccount
  terminationGradePeriodSeconds: 30
  shareProcessNamespace: true
```

## Imperative approach

We can use the kubectl run command to create a Pod in the cluster, and we can also provide arguments to customize the
Pod. Although the command does not provide an argument for every configurable property, it is still useful for quickly
generating a base template on the fly and saving it to a YAML file. This file then serves as a declarative manifest that
we can apply.

```shell
kubectl run romans --image=romans:10.9 --image-pull-policy=Always --restart=OnFailure \
--port=109 --env="BIBLE_CHAPTER=10,BIBLE_VERSE=9" --labels="chapter=10,verse=9"
```

To generate a manifest from the command, we can use the following flags:

```shell
kubectl run romans --image=romans:10.9 --image-pull-policy=Always --restart=OnFailure \
--port=109 --env="BIBLE_CHAPTER=10,BIBLE_VERSE=9" --labels="chapter=10,verse=9" \
--dry-run=client -o yaml > romans.yaml
```

The --dry-run=client argument specifies that the command is executed only on the client side, so it never reaches the
server and no Pod is created. With the addition of the -o yaml output option, the command produces a YAML manifest that
we can save to a file.

Once we have the manifest, we can edit it manually and then apply it to the cluster:

```shell
kubectl apply -f romans.yaml
```

We can also use the -h flag to get details about all available CLI arguments.

## Sidecar containers

Sidecar containers are containers that host logic which helps the main container in some way. They are useful in
situations where we do not want to couple additional functionality to the main application, but instead want it to be
externalized and possibly reused by different processes.

For example, software that transforms or aggregates the logs of the main application so they can be processed by an
external system. If this logic were embedded in the main application, it would need to be copied and pasted across
multiple services. By configuring it as a separate process and running it as a separate container within the same pod,
both containers become tightly coupled in the desired way: the log transformer cannot do anything on its own and depends
entirely on the main application.

For the sake of the CKAD exam, we will discuss sidecar containers purely as a pattern. The implementation will simply be
a second container listed alongside the main container in the pod specification. In recent versions of Kubernetes,
sidecar containers are considered a subtype of init containers. However, unlike init containers, sidecars do not block
the main application from starting and are allowed to be long-running. In this discussion, we will treat them as regular
application containers rather than init containers. Functionally, they work the same way, with the main difference being
that init containers always start before the main application container.

To create a sidecar container, all we need to do is add another container to the container list in the pod manifest.
Technically, nothing prevents us from running any kind of container as a sidecar, even if it is not a supporting
container. However, the downside is that all containers in a pod are scheduled on the same node, so if the node fails,
all containers in the pod fail as well.

One benefit of sidecar containers is that they share the same network namespace. This allows all containers in a pod to
communicate over localhost. For example, if container A is listening on port 9090, container B can access it via
localhost:9090. The downside of this shared network is that if one container binds to port 9090, the other containers
must use different ports.

When it comes to resources, each container in a pod can have its own requests and limits. The effective resource
requests and limits for the pod are the sum of the requests and limits of all containers within the pod. For example, if
a pod has two containers and the first requests 5 GB of memory while the second requests 3 GB, the pod will request a
total of 8 GB of memory, because the containers are expected to run simultaneously.

It is also possible, via configuration, to enable all containers in a pod to share a process namespace. This allows one
container to send signals to processes running in another container, and vice versa.

To share state between containers running in the same pod, we often use volumes. The simplest setup is an emptyDir
volume, which has the same lifecycle as the pod. When the pod is terminated, all data in the volume is deleted. The
volume can be mounted into all containers at a specific directory, allowing them to share its contents. If one container
creates, deletes, or modifies a file, the change is immediately visible to the other containers.

When a pod has multiple containers, and we want to apply a container-specific command using kubectl (for example,
kubectl
logs or kubectl exec), the command is applied by default to the first container listed in the pod specification. We can
use the -c flag to explicitly specify which container the command should target, for example:

```shell
kubectl logs pod-name -c container-2
```

## Init containers

In a pod, we can define multiple containers in the containers spec, but we can also list multiple containers in the
initContainers spec. An init container is a container that runs setup logic before the application containers start.
Init containers are especially useful when the main container image contains minimal tooling for security reasons, but
some environment setup is required before the application runs.

All init containers are executed sequentially, before any application containers. For example, if we have init container
A, init container B, and main container C, Kubernetes will first schedule container A. When container A exits with
status code 0 (success), it triggers container B. Only after container B finishes successfully will Kubernetes start all
application containers.

If an init container fails (exits with a non-zero code), the kubelet takes different actions depending on the pod’s
restartPolicy. If restartPolicy is set to OnFailure or Always, the init container will be restarted until it succeeds.
During this time, the main application containers remain blocked and will not start until the init container succeeds.
If restartPolicy is set to Never and the init container fails, the entire pod’s state is marked as failed.

Another difference between init containers and application containers is that init containers do not support probes.
Probes are intended for long-running containers, whereas init containers are short-lived and primarily used to run
scripts. Other than that, init containers support volumes, resource limits, security settings, and other features in the
same way as application containers.

When it comes to init containers, there is a difference in how the effective resource requests and limits are
calculated. If a pod has multiple init containers, Kubernetes takes the highest request and limit among them, resulting
in a single effective request and limit for all init containers.

For example, if init container 1 requests 3 CPU and has a limit of 4 CPU, and init container 2 requests 2 CPU and has a
limit of 5 CPU, the effective CPU request for init containers will be 3, and the effective CPU limit will be 5.
Kubernetes uses the highest values because only one init container runs at a time; there is no scenario where multiple
init containers run simultaneously. (The only exception is if a sidecar container is used as an init container, but in
that case, it is counted as an application container.)

After calculating the effective requests and limits for init containers, Kubernetes compares them with the effective
resources for the application containers and uses the higher values. For instance, if the effective CPU request and
limit for init containers are 3 and 5, and for the application containers they are 2 and 4, the final effective CPU
request and limit for the entire pod will be 3 and 5.

## Ephemeral containers

If we are following best practices when creating container images for our pods, we should always use scratch or
distroless base images and include only the tools necessary to run the software - for example, the JDK and the
application
JAR. We should not include tools such as curl, netstat, vim, or even a shell. Without a shell, we cannot connect to the
container directly, as commands like kubectl exec -it … -- sh will not work. Such minimal base images are used to reduce
the attack surface of the Kubernetes cluster.

In cases where the main application container lacks tooling or a shell, troubleshooting problems at runtime becomes very
difficult, since we can do little more than run the application itself. For these scenarios, Kubernetes provides
ephemeral containers.

Once a pod is created, it is essentially immutable - we cannot attach a new regular container to it. For a long time (
prior to Kubernetes v1.25), there was no direct solution for debugging distroless containers. Starting with Kubernetes
v1.25, ephemeral containers were introduced. These are dynamically created containers that are attached to an existing
pod. Their specification is not defined under the containers or initContainers fields, but under ephemeralContainers.
Ephemeral containers cannot be defined in a manifest and applied declaratively; instead, they must be created
imperatively using the kubectl debug command.

When creating an ephemeral container, we must target a specific application container in the pod. The ephemeral
container then shares Linux namespaces (such as process and filesystem namespaces) with the target container. The
ephemeral container should be based on an image that includes additional tooling; otherwise, it would be ineffective.
When we connect to the ephemeral container, it has visibility into everything the target container sees. Although they
are different containers, from the ephemeral container we can access the application container’s filesystem, view
running processes, inspect mounted volumes, and perform other debugging tasks.

The following command can be used to create an ephemeral container and attach it to a specified pod, opening an
interactive shell session in the ephemeral container. The --target flag is important, as it specifies which container’s
namespaces the ephemeral container should share.

```shell
kubectl debug pod/pod-name -it --image=busybox --target=container-name
```

Once we are inside the ephemeral container, we can run ps to inspect the running processes. To traverse the filesystem
of the target application container, we need to navigate to /proc/1/root/. From there, it is effectively the root (/) of
the application container’s filesystem.

When we have finished troubleshooting, we can exit the ephemeral container, at which point it will be terminated.

If we run kubectl describe pod <pod-name>, there is a dedicated section for ephemeral containers. Any ephemeral
containers that we have exited will appear in a Terminated state, and we cannot re-run or reattach to them.

Since we cannot reconnect to a terminated ephemeral container, there is usually no need to create a copy of the pod.
However, if we want to be extra safe, we can create a copy of the pod. This copied pod will be detached from any
workload controllers (such as Deployments), removed from any associated Services, and will not receive traffic. Once
troubleshooting is complete, the pod can simply be deleted.

## Sources

https://kubernetes.io/docs/concepts/workloads/pods/
https://kubernetes.io/docs/concepts/workloads/pods/sidecar-containers/
https://www.youtube.com/watch?v=qKb6loAEPV0