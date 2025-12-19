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

## Init containers

## Ephemeral containers

## Sources

https://kubernetes.io/docs/concepts/workloads/pods/
