# ReplicaSets

## Theory

When it comes to scaling our applications, we can either scale them vertically by increasing the resources available to
them, or horizontally by increasing the number of replicas. The ReplicaSet controller is used to run and manage multiple
pods based on a template. If a pod dies, it will be restarted; when we increase the desired number of replicas, new pods
are created; and when we decrease the count, excess pods are removed.

In practice, we usually avoid creating pods manually. Even if we only need a single instance of a pod, it is better to
rely on a ReplicaSet with a replica count of 1. This is because the ReplicaSet also handles pod restarts and node
failures by automatically rescheduling the pod onto a different node. Without it, we would need to manually track the
pod and handle any failures ourselves.

A ReplicaSet controller uses a few key components: a pod template, a label selector, and the desired number of replicas.

When a ReplicaSet is created, it searches for any existing pods that match the defined label selector. If such pods
exist, the ReplicaSet will adopt them. If no matching pods exist, it will create a number of pods equal to the replica
count based on the pod template. Each pod will have an ownerReferences field containing information about the ReplicaSet
that owns it.

It is also required that the pod template’s labels match the ReplicaSet’s selector; otherwise, Kubernetes will reject
the manifest. Additionally, if there are label conflicts - meaning other pods already match the same selector - they
will be adopted by the ReplicaSet. If the total number of matching pods exceeds the desired replica count, some of the
existing pods may be deleted.

When we update the pod template, the ReplicaSet does not automatically apply those changes to existing pods. The updated
template is only used when new pods are created. To address this limitation, we typically use Deployments instead of
managing ReplicaSets directly. Deployments are higher-level workload controllers that manage ReplicaSets and
automatically roll out changes to the pod template.

## Declarative approach

```yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: frontend
  labels:
    app: guestbook
    tier: frontend
spec:
  replicas: 3
  selector:
    matchLabels:
      tier: frontend
  template:
    metadata:
      labels:
        tier: frontend
    spec:
      containers:
        - name: php-redis
          image: us-docker.pkg.dev/google-samples/containers/gke/gb-frontend:v5
```

From the example, we can see that when we apply the ReplicaSet, it creates three pods running the php-redis container.
Each pod has the label tier=frontend, and the selector is configured to match pods with the same label.

For more details about selectors, refer to the notes on labels and selectors.
For more details about the pod specification, refer to the notes on pods.

## Imperative approach

Just like with a DaemonSet, we cannot create a ReplicaSet using the kubectl create command.

For the CKAD exam, the recommended approach is to refer to the documentation, copy an existing YAML manifest, and edit
it as needed on the fly.

## Sources

https://kubernetes.io/docs/concepts/workloads/controllers/replicaset/