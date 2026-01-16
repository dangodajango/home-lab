# Role based access control

## Theory

RBAC (Role-Based Access Control) is not a concept specific to Kubernetes; it is a common access-control model used in
large organizations. It is applied during the authorization step, which determines whether a user has sufficient
permissions to perform a requested operation.

Authorization is typically implemented by assigning permissions to identities. For example, in Linux filesystems,
permissions such as read, write, and execute are assigned to a file owner, a group, and others. The core idea behind
RBAC is to decouple identity (user or group) from permissions by grouping permissions into roles and binding those roles
to identities. This approach simplifies permission management and makes it easier to reuse, modify, and assign
permissions for specific use cases.

In Kubernetes, RBAC is the primary mechanism used to control access to cluster resources. The general flow is as
follows: a user or service account is first authenticated, then attempts to perform an operation on a resource (for
example, deleting a Pod). Kubernetes evaluates the applicable RoleBindings and ClusterRoleBindings to determine whether
any of the bound roles grant the required permissions. If the permissions are sufficient, the request is allowed;
otherwise, Kubernetes returns a 403 Forbidden response.

Kubernetes RBAC consists of subjects (users, groups, and service accounts) and four core RBAC objects: Role,
ClusterRole, RoleBinding, and ClusterRoleBinding.

Note that Kubernetes follows the principle of least privilege. By default, if no Role is assigned, a user cannot perform
any actions. Therefore, all rules (permissions) added to Roles are additive: they only grant permissions. There are no
rules that explicitly deny a user from performing an action; denial is the default behavior when a permission is
missing.

## Sources

https://www.youtube.com/watch?v=iE9Qb8dHqWI
https://kubernetes.io/docs/reference/access-authn-authz/rbac/