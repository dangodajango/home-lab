# Role

## Theory

The Role object defines which operations are allowed to be performed on a specific set of resources within a namespace.
The namespace aspect is important because Role objects are namespaced and can only control access to resources in the
namespace where they are defined. They cannot grant access to resources in other namespaces or to cluster-scoped
resources such as nodes.

Roles by themselves do not automatically grant permissions. They only define a set of allowed actions. Permissions take
effect only after a Role is associated with a specific subject (user, group, or service account) through a RoleBinding.
For example, we can define a Role named cleaner that allows listing and deleting Pods, but those permissions will only
apply after the Role is bound to a user.

To work with cluster-scoped resources or to define roles that are not namespace-specific, we can use a ClusterRole.

## Declarative approach

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: romans
  namespace: new-testament
rules:
  - apiGroups: [ "" ]
    resources: [ "pods" ]
    verbs: [ "list", "delete" ]
```

First, the namespace field is mandatory. Since Role objects are namespaced, we must always specify the namespace in
which the role is created. If we want to deploy the role in the default namespace, we still need to explicitly specify
it.

### rules

Defines the permissions that this role can grant to a subject.
Each rule is additive and specifies what actions are allowed on which resources.

### apiGroups

Kubernetes internally exposes resources via REST endpoints on the API server. These endpoints follow this structure:

```text
/apis/<apiGroup>/<version>/<resource>
```

Example:

```text
/apis/apps/v1/deployments
```

Resources are grouped into API groups:

- deployments, replicasets - apps
- ingresses, networkpolicies - networking.k8s.io
- Core resources like pods, secrets, and configmaps do not belong to an API group (they are part of the core API group)

This is reflected in the apiVersion field:

- apps/v1
- networking.k8s.io/v1
- v1 (for core resources)

In RBAC rules, we specify only the API group, without the version.

For core API group resources, we use an empty string:

```yaml
apiGroups: [ "" ]
```

### resources

A list of resources that the permissions apply to.

The listed resources must belong to the specified API group. If a resource does not exist in that API group, the
role will be rejected.

Example:

```yaml
apiGroups: [ "apps" ]
resources: [ "deployments", "replicasets" ]
```

### verbs

The operations that a subject can perform on the specified resources.

Common verbs include:

- get
- list
- watch
- create
- update
- patch
- delete
- deletecollection

### all

For all the fields - apiGroups, resources, and verbs - we can use the * operator, which means “all.”

- apiGroups: ["*"] - applies the rule to all API groups, so we don’t have to list each API group individually when the
  permissions should span multiple groups.
- verbs: ["*"] - allows all operations on the specified resources.
- resources: ["*"] - applies to all resources in the given API group.

This wildcard is especially useful for apiGroups, because it lets us define broad permissions without enumerating every
API group individually.

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: romans
  namespace: new-testament
rules:
  - apiGroups: [ "*" ]
    resources: [ "*" ]
    verbs: [ "*" ]
```

## Imperative approach

Template for the command:

```shell
kubectl create role <role-name> \
  --verb=<verbs> \
  --resource=<resources> \
  --namespace=<namespace>
```

Example:

```shell
kubectl create role romans \
  --verb=list,get \
  --resource=pods,deployments \
  --namespace=* \
  --dry-run=client -o yaml
```