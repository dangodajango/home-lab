# Labels and Selectors

## Labels

Labels are key-value pairs assigned to Kubernetes resources such as Pods, Deployments, ReplicaSets, CronJobs, Nodes,
etc. They are used to organize and group resources.

By tagging resources with labels, we can later select and retrieve only the resources that match those labels.
For example, if a ReplicaSet creates 4 Pods, we can assign all of them a label like:

```yaml
app: my-app
```

Then, when querying the Kubernetes API (via kubectl), we can retrieve only the Pods running that application:

```shell
kubectl get pods -l app=my-app
```

Without labels, Kubernetes would have no native way to group related resources, and we would have to rely on external
filtering tools like grep.

The labels are assigned to a resource in its metadata section:

```yaml
kind: Pod
version: v1
metadata:
  name: romans-pod
  labels:
    app: romans
spec:
  ...
```

Note that name is not a label, but a separate metadata field which uniquely identifies the resource. Labels are not used
for achieving uniqueness across the resources, but the opposite, it is expected that multiple resources will have the
same label.

When listing a specific resource, we can add the --show-labels flag to include the labels for each item:

```shell
kubectl get po --show-labels
```

### Syntax rules

There are some rules regarding the content of Keys and Values.

Keys consist of two segments: an optional prefix and the label name itself. In a large environment, there may be
conflicts between different label names. For example, the label app may be used by multiple resources, and if the values
happen to match, it can result in collisions. To avoid this, prefixes are used, e.g., app.kubernetes.io/name, where the
prefix is app.kubernetes.io. Custom prefixes can also be created, e.g., example.com/environment. These custom prefixes
have no special meaning; we just need to know that there are two prefixes reserved for Kubernetes: kubernetes.io/ and
k8s.io/.

The rules for prefixes are as follows: they must be at most 253 characters long and may contain only letters. Different
words must be separated by dots (.), no other special characters are allowed, and the prefix must end with a /.

The name segment must be at most 63 characters long. It should begin and end with a letter, but can also contain
numbers. The only allowed special characters are dashes (-), underscores (_), and dots (.). Values must follow the same
rules as the name segment.

## Selectors

Labels are only useful in combination with a selector. A selector queries the Kubernetes cluster and searches for any
resources matching the given key-value labels. For example, we may want to find all pods with a label
app.kubernetes.io/name and a value of romans; we can use a selector for that.

In an SQL context, selectors are like the WHERE clause in a query, and the elements in the WHERE clause correspond to
the labels. Essentially, we are saying: "Give me all pods that have this label with this value," or "Give me all pods
that do not have this label."

In general, there are two subcategories of selectors:

1. Manifest selectors – used in resource manifests. For example, a ReplicaSet requires a label selector to determine
   which
   pods it manages.

2. CLI selectors – used when executing commands through kubectl, e.g., listing all pods that match a specific label.

### Manifest selectors

Manifest selectors are defined in YAML for resources that manage other resources and need to identify them, such as a
DaemonSet or ReplicaSet selecting the pods they manage.

You can have both types of selectors at the same time. Kubernetes applies a logical AND, meaning a resource must satisfy
the requirements defined by both the matchExpressions selector and the matchLabels selector in order to be selected.

#### MatchLabels

```yaml
selector:
  matchLabels:
    app.kubernetes.io/name: romans
    verse: 1
```

The matchLabels selector is very simple: you just list the labels and their values, and the resource must have them. If
the selector contains multiple labels, the resource must have all of them. This works like a logical AND. For example,
the resource must have app.kubernetes.io/name=romans AND verse=1.

#### MatchExpressions

```yaml
selector:
  matchExpressions:
    - key: app.kubernetes.io/name
      operator: In
      values:
        - romans
        - acts
    - key: verses
      operator: NotIn
      values:
        - 12
        - 22
    - key: chapter
      operator: Exists
    - key: sad
      operator: DoesNotExist
```

With the matchExpressions selector, we can create more complex queries. We can simulate a logical OR or NOR using the In
and NotIn operators:

- In checks if a label’s value is one of the specified values.

- NotIn checks if a label’s value is not one of the specified values.

We can also use Exists or DoesNotExist to check whether a label exists or doesn’t exist, without caring about its value.

If multiple expressions are defined, as in the example, all of them are combined using a logical AND. That means a
resource must satisfy all expressions to be selected.

### CLI selectors

CLI selectors behave the same way as the manifests one, but the syntax is different because we are using the selector in
the command itself.

It is used when we want to list resources matching a selector, much more useful than tools like grep, because the
selector can be used to apply changes to the resources as well.

Just as we can use both matchExpressions and matchLabels together, we can also combine equality-based and set-based
selectors in the CLI. We simply separate them with commas, and a logical AND is applied.

#### Equality based selectors

Equality-based CLI selectors are similar to the matchLabels selector in a manifest, but they also support inequality
using !=. For equality, you can use either = or ==.

You can specify multiple label constraints separated by commas (,), and they are combined using a logical AND.

```shell
kubectl get pods -l app.kubernetes.io/name=romans,verse=13
```

```shell
kubectl get pods -l app.kubernetes.io/version!=1.20.3
```

#### Set based selectors

CLI label selectors support the same features as the matchExpressions selector in manifests. We can check whether a
label’s value is in a set of values, not in a set of values, whether a label exists, or whether it does not exist.

We can list multiple such criteria by separating them with commas (,), and they will be combined using a logical AND.

List all pods that have a label with a value of either 13, 23, or 5:

```shell
kubectl get pods -l 'verse in (13, 23, 5)'
```

List all pods that do not have a label with a value of either 4, 55, or 2:

```shell
kubctl get pods -l 'verse notin (4, 55, 2)'
```

List all pods that have a label named verse:

```shell
kubectl get pods -l verse
```

List all pods that do not have a label named verse:

```shell
kubectl get pods -l '!verse'
```

Note that when using the in and notin operators, we must wrap the entire expression in single quotes ('). This is
necessary because the expression contains spaces and special characters, and without quotes the shell would parse it
incorrectly.

If we want to combine equality-based and set-based selectors, we can do so as follows:

```shell
kubectl get pods -l 'environment=heaven,verse in (3, 4, 5)'
```

## Sources

https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/
