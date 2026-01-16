# ClusterRoleBinding

## Theory

A ClusterRoleBinding serves the same purpose as a regular RoleBinding — it attaches a ClusterRole to one or more
subjects. The key differences are:

- It is a cluster-scoped resource, not namespaced.
- It can only bind a ClusterRole, not a regular Role (since Roles are namespaced).
- The subjects can be any of the three types: User, Group, or ServiceAccount.
    - Even though ServiceAccounts are namespaced, a ClusterRoleBinding can grant them cluster-wide permissions.
- You can also use system-defined identities here, such as system:authenticated, system:masters, or system:
  serviceaccounts.
    - For example, system:serviceaccounts targets all ServiceAccounts in all namespaces, which is not allowed in a
      namespaced
      RoleBinding.

## Declarative approach

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: romans-global
subjects:
  - kind: ServiceAccount
    name: romans-sa
    namespace: new-testament
  - kind: User
    name: paul
  - kind: Group
    name: apostles
roleRef:
  kind: ClusterRole
  name: apostles-global-role
  apiGroup: rbac.authorization.k8s.io
```

## Imperative approach

```shell
kubectl create clusterrolebinding <binding-name> \
  --clusterrole=<clusterrole-name> \
  --user=<username> \
  [--group=<groupname>] \
  [--serviceaccount=<namespace>:<sa-name>]
```