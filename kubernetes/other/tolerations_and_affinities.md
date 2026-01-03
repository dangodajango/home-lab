## Affinities

One way to control which node a pod should be scheduled on is by using a nodeSelector, which is essentially a
matchLabels selector applied to node labels. If a node has the required labels, it becomes part of the pool of nodes on
which the pod can be scheduled. The advantage of this approach is its simplicity: if we do not need complex logic to
decide where to deploy the pod, a nodeSelector is sufficient. However, if more sophisticated scheduling logic is
required, we can use node affinities instead.

There are two types of node affinity. Both are used to attract a pod to a specific node, but they work in different
ways:

### Regular Node affinity

If a nodeSelector is analogous to a matchLabels selector, then node affinity is analogous to a matchExpressions
selector. It uses the same syntax, operating on sets of labels belonging to a node, but it also supports
preference-based rules rather than only hard requirements.

```yaml
kind: Pod
apiVersion: v1
metadata:
  name: romans
spec:
  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
          - matchExpressions:
              - key: verses
                operator: In
                values:
                  - 1
                  - 3
                  - 5
      preferredDuringSchedulingIgnoredDuringExecution:
        - weight: 1
          preference:
            matchExpressions:
              - key: chapters
                operator: In
                values:
                  - 2
                  - 5
  containers:
    - name: romans
      image: newTestament:romans
```

There are two types of node affinity: required and preferred during scheduling.
The required type means that the pod will be scheduled only on nodes that match the defined rules. If no nodes match,
the pod will not be scheduled and will remain in the Pending state.
The preferred type is more flexible: the scheduler will try to place the pod on nodes that match the rules and can apply
different weights to prioritize certain nodes. However, if no nodes satisfy the preferences, the pod can still be
scheduled on any suitable node.

The IgnoredDuringExecution part exists because node affinity relies on node labels. If a pod has already been scheduled
and the node’s labels change while the pod is running, the pod will not be evicted and will continue running. However,
subsequent pods using the same specification will not be scheduled on that node if it no longer matches the rules.

For the required type, multiple selector terms can be defined under the nodeSelectorTerms field, and they are ORed
together. This means a node only needs to satisfy one of the terms to be considered. Within a single matchExpressions
block, however, multiple criteria are ANDed together, so all of them must match.

For more details about the matchExpressions selector, refer to the notes on labels and selectors.

When it comes to the preferred type, it is important to pay attention to the weight field. If we define multiple sets of
preferred nodes, for example:

```yaml
      preferredDuringSchedulingIgnoredDuringExecution:
        - weight: 1
          preference:
            matchExpressions:
              - key: chapters
                operator: In
                values:
                  - 2
                  - 5
        - weight: 5
          preference:
            matchExpressions:
              - key: verses
                operator: In
                values:
                  - 1
```

If there is a node that matches the first preference (a chapters label with a value of either 2 or 5), and another node
that matches the second preference (a verses label with a value of 1), the scheduler will choose the second node because
it has a higher weight. This is where the notion of preference comes into play. The weight field can range from 1 to
100, where 100 represents the highest preference.

### Inter-pod affinity

Another type of affinity works in a different manner: instead of looking at node labels, it looks at the labels of the
pods already scheduled on the nodes. This allows us to schedule pods on the same node as other pods.

## Taints and tolerations

While affinities are used from the pod’s perspective to find nodes that match the criteria defined in the pod
specification, taints and tolerations work from the node’s perspective to repel pods that do not match the criteria
defined by the node.

Taints can be compared to things that cause allergies in humans. For example, a node may have a taint for nuts, meaning
that only pods that are “resistant” to nut allergies can be deployed there. This resistance is called a toleration.
Another example could be a node with a taint representing a bad smell, where only pods that tolerate the smell are
allowed to run on it.

Taints are applied to nodes, and only pods that have a corresponding toleration can be scheduled onto those nodes. If a
pod does not tolerate a taint, it will not be scheduled on the node.

To add a taint to a node, we can run the following command:

```shell
kubectl taint nodes node1 key=value:NoSchedule
```

The structure of a taint consists of three parts: a key, which follows the same rules as label names; a value, which is
an optional string; and an effect, which is required and very important. The effect specifies what happens to pods that
do not tolerate the taint.

There are three types of effects:

1. NoSchedule – If a pod does not tolerate the taint, it will not be scheduled on the node.

2. PreferNoSchedule – Kubernetes will try to avoid scheduling pods on this node if they do not tolerate the taint, but
   if there are no other viable options, it may still schedule the pod on the node.

3. NoExecute – If a pod does not tolerate the taint, it will not be scheduled on the node (similar to NoSchedule), and
   it will also evict any already running pods that do not tolerate the taint. This typically occurs when a new taint is
   added to a node that already has running pods, and we want to clear the node.

Tolerations are defined in the pod specification:

```yaml
kind: Pod
apiVersion: v1
spec:
  tolerations:
    - key: very-bad-smell
      operator: Equal
      value: socks
      effect: NoSchedule
```

The most important properties are the four shown in the example:

1. key – Specifies the taint key.

2. operator – Either Equal, which matches the taint’s value with the value defined in the toleration, or Exists, which
   checks only for the existence of the key and ignores the value.

3. value – Required only when the operator is Equal; it must match the value of the taint with the same key.

4. effect – Optional. It is used to match the toleration to a specific taint effect. If omitted, the toleration matches
   all taints with the same key/value pair (since multiple taints with different effects can exist for the same key).

## Source

https://kubernetes.io/docs/concepts/scheduling-eviction/taint-and-toleration/
https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#affinity-and-anti-affinity