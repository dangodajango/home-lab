# Role Binding

## Theory

We can define as many roles as we want using Role or ClusterRole objects, but by themselves they do not grant any
permissions. A RoleBinding is required to associate those roles with the different users of the cluster.

A RoleBinding is a namespaced resource that assigns a role to a subject. The subject can be a user, a group, or a
service account (technical user). Only after a RoleBinding is created does the subject receive the permissions defined
by the role.

A RoleBinding can reference either a Role or a ClusterRole. When binding a Role, the Role must exist in the same
namespace as the RoleBinding. ClusterRoles are cluster-scoped resources, so they can be referenced directly, but the
permissions granted are still limited to the namespace of the RoleBinding.

## Declarative approach

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: romans-binding
  namespace: new-testament
subjects:
  - kind: ServiceAccount
    name: romans-sa
    namespace: new-testament
  - kind: User
    name: paul
  - kind: Group
    name: students
roleRef:
  kind: Role
  name: apostle
  apiGroup: rbac.authorization.k8s.io
```

A RoleBinding can reference only one roleRef, meaning it can bind to a single Role or a single ClusterRole. To bind a
ClusterRole, we only need to set the kind field in roleRef to ClusterRole; the rest of the configuration remains the
same.

A RoleBinding can have multiple subjects. Supported subject types are ServiceAccount, User, and Group. Users and groups
are not managed by Kubernetes. Instead, Kubernetes receives identity information for authenticated users from an
external authentication system. For example, if identities are managed in Azure, the user first authenticates with
Azure, and the username or group name is then forwarded to Kubernetes. Kubernetes can use these values in RoleBindings
to assign roles.

When a RoleBinding references a Role, the Role must exist in the same namespace as the RoleBinding. It is not possible
to bind a Role from a different namespace.

To dive a bit deeper, it’s important to understand what Kubernetes automatically does with the name property of a
ServiceAccount subject.

In our examples, we only typed the name of the ServiceAccount (e.g., romans-sa), but this is actually a shorthand.
Internally, Kubernetes prepends a string to form the full identity: system:serviceaccounts:new-testament:romans-sa

This only happens for ServiceAccounts, because they are managed by Kubernetes.

system: is a reserved prefix used by Kubernetes, it identifies internal components or logical groups for RBAC.

Examples of system users and groups:

- system:kube-proxy, system:kube-apiserver Internal Kubernetes components
- system:authenticated All authenticated users
- system:unauthenticated All unauthenticated users
- system:masters All cluster administrators
- system:serviceaccounts All ServiceAccounts in all namespaces
- system:serviceaccounts:<namespace>    All ServiceAccounts in a specific namespace
- system:serviceaccounts:<namespace>:<serviceaccount>    A specific ServiceAccount in a namespace

## Imperative approach

Create a RoleBinding for a User:

```shell
kubectl create rolebinding romans-rb \
  --role=apostle \
  --user=paul \
  --namespace=new-testament
```

Create a RoleBinding for a Service Account:

```shell
kubectl create rolebinding cleaner-binding \
  --role=apostle \
  --serviceaccount=new-testament:romans-sa \
  --namespace=new-testament
```