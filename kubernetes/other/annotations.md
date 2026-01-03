# Annotations

To attach metadata to an object we can use either labels or annotations, the main difference is that labels are used for
when we want to select objects matching a criteria, they are intended for grouping of resources, while annotations are
just plain metadata which is intended to be used by Kubernetes itself or other tools or applications.

Annotations are not necessarily used to identify an object, meaning we have multiple objects with the same value for an
annotation, even though we could create a custom annotation which is uniquely identifying an object.

Some examples for when we could find annotations to be useful are - git properties e.g., commit hash, branch, author,
etc..., library information, logging tool metadata and so on.

## Syntax rules

Annotations have the same structure as labels: they are key–value pairs.

Keys consist of two segments: an optional prefix and the annotation name itself. The idea behind prefixes for
annotations differs from that of labels. For labels, prefixes are often needed to avoid collisions during selection.
Annotations, however, do not participate in any kind of selection. Instead, annotation prefixes are useful when we want
to show ownership of a resource or associate metadata with a specific tool, for example: prometheus.io/scrape: "true" or
nginx.ingress.kubernetes.io/proxy-read-timeout: "30". The same reserved annotation prefixes exist as with labels:
kubernetes.io/ and k8s.io/.

When it comes to syntax, the rules for prefixes and names are the same as for labels. Prefixes must be at most 253
characters long and may contain only lowercase alphanumeric characters, dots (.), and dashes (-). The name must be at
most 63 characters long, start and end with a letter or number, and may contain letters, numbers, dots (.), dashes (-),
and underscores (_).
The rules for values, however, differ significantly from labels: annotation values have no strict constraints and can
contain arbitrary data.

## Manifest

We can add annotations through the manifest, and when we apply the manifest and the object is created, the system or any
external tools we use may add their own annotations as well.

```yaml
kind: Pod
apiVersion: v1
metadata:
  annotations:
    romans: 3:16
```

## Source

https://kubernetes.io/docs/concepts/overview/working-with-objects/annotations/