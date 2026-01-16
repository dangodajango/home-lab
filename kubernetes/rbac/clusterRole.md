# ClusterRole

## Theory

A ClusterRole serves the same purpose as a Role, it defines a set of permissions for resources, but the difference is
that it is not a namespaced object, meaning it exists at the cluster level.

- When you use a normal Role, it must exist in the same namespace as the RoleBinding that references it. Each namespace
  needs its own Role if you want identical permissions.
- In contrast, a ClusterRole can be referenced by RoleBindings in multiple namespaces, so you don’t have to duplicate
  the same permission definitions in each namespace.

This makes ClusterRoles ideal for permissions that are common across multiple namespaces, or for access to
cluster-scoped resources like nodes or persistent volumes.

## Declarative approach

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: romans
rules:
  - apiGroups: [ "" ]
    resources: [ "pods" ]
    verbs: [ "get", "list", "watch" ]
```

Note: The only differences between a Role manifest and a ClusterRole manifest are:

- The kind field is set to ClusterRole instead of Role.
- ClusterRoles do not have a namespace field, because they are cluster-scoped (global) resources.

All other fields (rules, apiGroups, resources, verbs) are defined in the same way as for a Role.

## Imperative approach

```shell
kubectl create clusterrole <name> \
  --verb=<verb> \
  --resource=<resource> \
  [--resource-name=<specific-resource>] \
  [--api-group=<apiGroup>]
```