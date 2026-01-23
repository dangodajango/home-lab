# Secrets

## Theory

Secrets are similar to ConfigMaps in the sense that they are intended to separate configuration data from the
application or container image itself and inject that data at runtime, either as environment variables or as files
mounted into the container running in a Pod.

The main difference between the two resources is that Secrets are intended to store sensitive information, such as
passwords, certificates, and cryptographic keys. Unlike ConfigMaps, Secrets can be encrypted at rest, but this is not
enabled by default and must be explicitly configured at the cluster level. Internally, Secrets are handled slightly
differently from ConfigMaps, but from a user perspective, interaction with them is largely the same: their contents can
be mounted as files or exposed as environment variables inside a container.

Secrets are key–value pairs. The data can be provided either as base64-encoded values under the data field or as raw
strings using the stringData field. When stringData is used, Kubernetes automatically base64-encodes the values and
stores them in the data field. The stringData field itself is write-only and is not persisted.

There are different types of Secrets, including:

- Opaque
- kubernetes.io/service-account-token
- kubernetes.io/basic-auth
- kubernetes.io/tls

Other types also exist, and custom Secret types can be defined as well.

The Opaque type is the default if no type is specified. It is a generic Secret intended to store arbitrary user-defined
secret data and does not carry any specific semantic meaning.

Secrets of type kubernetes.io/service-account-token were historically used to store long-lived JWT tokens associated
with a ServiceAccount. In modern Kubernetes versions (1.24+), this mechanism is considered legacy. ServiceAccount tokens
are now typically provided via bound, short-lived, automatically rotated tokens projected directly into Pods, rather
than being stored as persistent Secret objects.

Secrets of type kubernetes.io/basic-auth and kubernetes.io/tls are primarily used to express intent and follow
well-known conventions (such as expected key names), rather than for strict technical enforcement. In most cases, the
same data could also be stored using an Opaque Secret, although some tooling and controllers may expect these specific
types.

## Declarative approach

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: my-secrets
  namespace: kube-system
type: Opaque
data:
  username: dHJ1ZQ==
  password: dHJ1ZQ==
```

Here we use the data field, which requires each value in the key-value pairs to be base64-encoded. Alternatively, we
could use stringData and provide the values as raw strings, letting Kubernetes handle the encoding automatically.

### Injecting specific environment variables

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: configmap-demo-pod
spec:
  containers:
    - name: demo
      image: alpine
      command: [ "sleep", "3600" ]
      env:
        - name: USERNAME
          valueFrom:
            secretKeyRef:
              name: my-secrets
              key: username
        - name: PASSWORD
          valueFrom:
            secretKeyRef:
              name: my-secrets
              key: password
```

### Injecting all key-value pairs from a ConfigMap as environment variables

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod
spec:
  containers:
    - name: app
      command: [ "/bin/sh", "-c", "printenv" ]
      image: busybox:latest
      envFrom:
        - secretRef:
            name: my=secret
```

### Mounting the Secret pairs as files

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: secret-demo-pod
spec:
  containers:
    - name: demo
      image: alpine
      command: [ "sleep", "3600" ]
      volumeMounts:
        - name: config
          mountPath: "/config"
          readOnly: true
  volumes:
    - name: config
      configMap:
        name: secret-with-credentials
        items:
          - key: "credentials.conf"
```

This will create a file named credentials.conf in the mounted directory and write the value to it. If the value is from
a Secret, it will be base64-decoded before writing. If we omit the items array, Kubernetes will create a separate file
for each key-value pair in the ConfigMap or Secret, even if the value is a single line.

## Imperative approach

## Source

https://kubernetes.io/docs/concepts/configuration/secret/