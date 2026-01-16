# Service accounts

## Theory

In a Kubernetes system, we have user accounts, which are generally provided externally (e.g., through Active Directory),
and they are used by people to authenticate with the cluster and perform operations on it based on the access they have.
Now, if we want a pod to perform some kind of automated action, for example, querying the Kubernetes API, and it needs
to authenticate first, we could set up a user account for it, but that is not best practice. Instead, we would like to
use something like a technical or machine user, and for that we can utilize ServiceAccounts.

ServiceAccounts are essentially technical users managed by Kubernetes. They exist only inside the cluster and are
namespaced. We can configure their permissions through RBAC objects, and for authentication, Kubernetes provides either
an automatically rotatable JWT token, which can be mounted into pods in the namespace, or a permanent, long-lived token
configured as a Secret associated with the ServiceAccount.

A ServiceAccount can be attached to pods; the credentials will then be mounted by default (this behavior can be
disabled), and the pod can authenticate with the Kubernetes API server. Another use case is allowing external services,
such as Azure CI/CD, to authenticate with the cluster so that we can deploy services as part of our automated pipelines
by providing them with the long-lived token stored in the Secret.

Note that Kubernetes automatically creates a default ServiceAccount for every namespace. This ServiceAccount is attached
to pods running in the namespace if no custom ServiceAccount is specified. By default, it has minimal permissions.

## Declarative approach

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: romans
  namespace: new-testament
imagePullSecrets:
  - name: pauls-secret
automountServiceAccountToken: false 
```

A ServiceAccount is a very simple object; its manifest is only as complex as shown in the example. A namespace must be
specified, and the automountServiceAccountToken property defaults to true, meaning the token is automatically mounted
into the pod when it starts. This behavior can be disabled if needed.

The imagePullSecrets field is particularly interesting. If the image used by the container running in the pod is stored
in a private registry (for example, Azure Container Registry) and the kubelet needs to authenticate in order to pull it,
we can store the registry credentials in a Secret referenced by this field. When the ServiceAccount is attached to a
pod, the kubelet will use these credentials to authenticate with the registry hosting the image.

## Imperative approach

```yaml
kubectl -n new-testament create sa romans --dry-run=client -o yaml
```

## Source

https://kubernetes.io/docs/concepts/security/service-accounts/