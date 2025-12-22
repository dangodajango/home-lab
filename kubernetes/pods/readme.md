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

When a pod has multiple containers, and we want to apply a container-specific command using kubectl (for example, kubectl
logs or kubectl exec), the command is applied by default to the first container listed in the pod specification. We can
use the -c flag to explicitly specify which container the command should target, for example:

```shell
kubectl logs pod-name -c container-2
```

## Init containers

## Ephemeral containers

## Sources

https://kubernetes.io/docs/concepts/workloads/pods/
