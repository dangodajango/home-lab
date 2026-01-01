# Namespaces

## Theory

Kubernetes clusters can have very different setups. For example, a cluster might be dedicated to a single team or
environment, or it might be shared by multiple teams and environments within a specific availability zone. This varies
from one organization to another. One thing is certain, though: when a cluster is shared, name collisions can occur
between resources deployed by different teams or workloads.

To address this issue, Kubernetes provides namespaces. Namespaces allow us to logically separate and group resources
within the same cluster. This is a virtual separation, similar to how packages in Java prevent class name conflicts
across an application. In the same way, namespaces in Kubernetes help avoid naming conflicts between resources.

Each resource must have a unique name within a namespace, but the same resource name can be reused in a different
namespace. Additionally, resources of different kinds can share the same name within a namespace. For example, a Pod
named romans and a Deployment named romans can coexist in the same namespace. However, you cannot have two Pods with the
same name in the same namespace.

Namespaces can also be used to divide and control cluster resources. For example, we may have a group of workloads that
require higher physical resources than others. This can be achieved by combining namespaces with ResourceQuotas
and LimitRanges (see the notes on resource quotas for more details).

When we create a new Kubernetes cluster, the following namespaces are created out of the box:

- default - If we do not specify a namespace, Kubernetes will use the default namespace. It exists so we can immediately
  start creating resources in a fresh cluster. However, in practice, it is usually better to create and use custom
  namespaces instead of relying on default.

- kube-node-lease - An internal namespace used for objects that manage node lifecycles and heartbeats. We should not use
  this namespace directly.

- kube-public - This namespace always exists and is readable by all users (including unauthenticated users). It can be
  used to store resources that should be visible across the entire cluster, although it is optional and not commonly
  used by most workloads.

- kube-system - A namespace where Kubernetes creates and manages its internal components and system-level resources. We
  should not deploy our own workloads here.

## Declarative approach

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: romans
```

This is the only manifest content required to create a Namespace; there is no spec section for it. Once we apply the
manifest, the Namespace will be created.

For the CKAD exam, if we need to create a Namespace, it is usually better to use the imperative command, since it is
faster.

When working with Namespaces, we can generally specify the target Namespace for any namespaced resource in the metadata
section:

```yaml
kind: Pod
apiVersion: v1
metadata:
  name: romans
  namespace: NewTestament
```

When this resource is created, it will be placed inside the NewTestament Namespace.

## Imperative approach

To create a Namespace imperatively, we can use the following command:

```shell
kubectl create namespace romans
```

To list all available Namespaces:

```shell
kubectl get ns 
kubectl get namespace
```

To list resources within a specific Namespace:

```shell
kubectl -n romans get pods
```

If we do not specify a Namespace in a kubectl command, it will default to the default Namespace:

```shell
kubectl get pods  # Lists pods in the default namespace
```

## Sources

https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/