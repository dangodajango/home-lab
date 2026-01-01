# Daemon sets

## Theory

In a Kubernetes cluster, we have nodes, which can be either control plane nodes or worker nodes. These nodes, whether
physical or virtual, together make up the cluster. When we tell Kubernetes that we want to run a Pod, the scheduler
checks the nodes based on available resources, affinities, and taints, and selects an appropriate node for the Pod to
run on - usually one of the worker nodes. Control plane nodes are reserved for Kubernetes internal components, such as
the API server, database, and scheduler. However, we also have the option to configure a Pod to run on a specific node.

A DaemonSet solves the problem of running a Pod on every node in the cluster. Instead of manually configuring Pods to
run on every node and handling node additions or removals, the DaemonSet automatically manages this for us.

Essentially, a DaemonSet is a workload controller. It schedules Pods based on a defined template on every eligible node
in the cluster and ensures that Pods are recreated if they fail. We also have the ability to filter nodes where we don’t
want to deploy Pods or specify exactly which nodes should run them.

There are a few important spec properties that are good to know:

### template

A Pod template is used by a DaemonSet to create Pods. It supports the same fields as a standard Pod manifest, with one
exception: the restartPolicy for the Pod or its containers. In the context of a DaemonSet, only Always is allowed; Never
and OnFailure are not permitted.

An additional requirement for the pod template is that it must have labels matching those defined in the selector field,
because these labels are used to associate the pods with the DaemonSet that manages them.

For more details, refer to the Pod resource documentation.

### selector

A standard manifest uses a label selector (for more details, see the notes about labels and selectors).

The labels in the pod template must match the label selector for the DaemonSet; otherwise, Kubernetes will reject the
manifest. If the selector includes an expression that matches existing Pods, those Pods will not be adopted by the
DaemonSet. However, the DaemonSet will still create a new Pod on the node, even if an existing Pod matches the label
criteria (this behaviour differs between the different workload controllers).

A DaemonSet only manages Pods that it creates. For a Pod to be created and managed by a DaemonSet—or any other workload
controller—it must have:

- An internal ownerReference pointing to the controller
- Labels that match the controller’s selector

### revisionHistoryLimit

Whenever we make a change to the pod template, the DaemonSet creates a new revision and saves the old template in the
revision history. This is useful when we want to roll back to a previous version of the pod template.

For example, if we update the container image to a new version but encounter a problem, we can roll back to the previous
revision.

By default, Kubernetes stores the last 10 revisions.

### updateStrategy

Whenever we make a change to the pod template, the DaemonSet needs to know how to update the existing Pods. There are
two update strategies: OnDelete and RollingUpdate.

With OnDelete, the DaemonSet does not stop or update old Pods automatically. It waits for you to manually delete a Pod,
and when a Pod is deleted, the DaemonSet creates a new Pod on that node using the updated pod template.

With RollingUpdate, the DaemonSet gradually replaces existing Pods with new ones based on the updated pod template. The
rollout is controlled by the maxUnavailable setting, which specifies how many Pods (or nodes) can be unavailable during
the update. For example, if there are 4 nodes (and therefore 4 Pods) and maxUnavailable is set to 2, the DaemonSet will
first delete 2 old Pods and create 2 new Pods. Once the new Pods are running, it will update the remaining 2 Pods. This
ensures a controlled, gradual rollout without stopping all Pods at once.

## Declarative approach

```yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: fluentd-elasticsearch
  namespace: kube-system
  labels:
    k8s-app: fluentd-logging
spec:
  revisionHistoryLimit: 4
  updateStrategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
  selector:
    matchLabels:
      name: fluentd-elasticsearch
  template:
    metadata:
      labels:
        name: fluentd-elasticsearch
    spec:
      containers:
        - name: fluentd-elasticsearch
          image: quay.io/fluentd_elasticsearch/fluentd:v5.0.1
      tolerations:
        # these tolerations are to have the daemonset runnable on control plane nodes
        # remove them if your control plane nodes should not run pods
        - key: node-role.kubernetes.io/control-plane
          operator: Exists
          effect: NoSchedule
        - key: node-role.kubernetes.io/master
          operator: Exists
          effect: NoSchedule
```

## Imperative approach

There is no option to create a DaemonSet using the kubectl create command, so we need to create the manifest manually.

For the CKAD exam, the recommended approach is to go to the Kubernetes documentation page for the DaemonSet resource and
copy the example schema into a YAML manifest file, then modify it as needed.

## How to control on which nodes the DaemonSet should create pods

### Node selector

One approach we can use to control where Pods are created is node selection, which allows us to specify on which nodes
Pods should run by using a node selector. A node selector is similar to a label selector, but it operates on nodes
instead of Pods.

Nodes are resources in Kubernetes, just like Pods or CronJobs, so they can also have labels assigned to them:

```shell
kubectl label node node-name label-name=label-value
kubectl get nodes --show-labels
```

If we want a Pod to be scheduled only on nodes that have a label named diskType with a value of ssd, we can use the
following node selector:

```yaml
nodeSelector:
  diskType: ssd
```

Node selectors work the same way as matchLabels selectors used in other resources: the node must have all the specified
labels with the matching values. For more details, see the notes on labels and selectors.

### Taints, tolerations and affinities

Another approach for selecting specific nodes on which to run Pods is through taints, tolerations, and affinities. Since
these mechanisms are not specific to DaemonSets and apply to Pods in general, they will be covered in separate notes
with more detailed explanations.

## Sources

https://kubernetes.io/docs/concepts/workloads/controllers/daemonset/