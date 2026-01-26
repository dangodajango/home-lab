# Services

## Theory

When working with Pods, we usually don’t interact with them directly. Instead, we use a controller (for example, a
Deployment) which manages the Pods and may create new ones or terminate existing ones at any time.
Each Pod gets its own IP address, but this creates several problems:

- A Pod IP is not stable and can change when the Pod is recreated
- When scaling to multiple Pods, tracking all their IPs becomes impractical
- We need a way to distribute traffic across multiple replicas

To solve this, Kubernetes provides the Service object.

A Service gives us a stable virtual IP address and DNS name through which we can reach a set of Pods. Internally, the
Service forwards traffic to healthy backend Pods that match its label selector. This allows us to scale applications
horizontally without clients needing to know anything about individual Pod IPs.

Services are most commonly used in a microservices architecture, where an application is scaled across multiple Pods and
the Service acts as a simple Layer 4 (TCP/UDP) load balancer in front of them.
Even if an application runs only a single Pod, it is still recommended to use a Service, since Pods themselves do not
have stable IP addresses.

### Service discovery

When a Pod is created, Kubernetes injects information about existing Services into the Pod as environment variables, for
example:

```shell
REDIS_PRIMARY_SERVICE_HOST=10.0.0.11
REDIS_PRIMARY_SERVICE_PORT=6379
REDIS_PRIMARY_PORT=tcp://10.0.0.11:6379
REDIS_PRIMARY_PORT_6379_TCP=tcp://10.0.0.11:6379
REDIS_PRIMARY_PORT_6379_TCP_PROTO=tcp
REDIS_PRIMARY_PORT_6379_TCP_PORT=6379
REDIS_PRIMARY_PORT_6379_TCP_ADDR=10.0.0.11
```

This information can be used at runtime to connect to other Services.
However, this approach has limitations- environment variables are only set at Pod startup; services created later will
not appear in already running Pods

DNS-based service discovery (preferred)
The recommended approach is to use DNS-based service discovery, which is enabled by default in most clusters.
Kubernetes runs an internal DNS service that watches all Services and creates DNS records for them. Each Service can be
reached using its name, for example: http://service-name.namespace:8080

The DNS name is resolved internally to the Service’s virtual IP, which then forwards traffic to the backing Pods.
This allows applications to rely on stable names instead of IP addresses, making scaling and Pod replacement completely
transparent.

### ClusterIP

There are different types of Services, and the default one is ClusterIP.

Kubernetes reserves a range of virtual IP addresses that can be assigned to Services. These IPs exist for as long as
the Service exists and are reachable only from within the cluster.

A ClusterIP Service is typically used for internal communication between microservices, where traffic does not need
to enter the cluster from the outside. Clients inside the cluster reach the Service via its stable IP or DNS name,
and the Service forwards traffic to the backing Pods.

This is the most common Service type.

### NodePort

NodePort is the simplest way to expose a Service outside the cluster. When a Service of type NodePort is created,
Kubernetes allocates a port from a predefined range (by default 30000–32767) and opens that port on every node in the
cluster. Traffic sent to nodeIP:nodePort is forwarded to the Service. Internally, a NodePort Service also creates a
ClusterIP Service, which handles the actual routing of traffic to the backing Pods. As a result, the Service can be
reached using any node’s IP address and the assigned port, and the traffic will be distributed across the matching Pods.

However, NodePort has several limitations. There is no built-in load balancing between different node IP addresses, so
clients must know and manage the list of node endpoints themselves. If traffic is consistently sent to only a single
node, that node can become a single point of failure. Because of these constraints, NodePort is typically used for
development, testing, or simple setups, rather than for production workloads.

### LoadBalancer

The LoadBalancer Service type builds on top of NodePort to solve the problem of distributing traffic across cluster
nodes. When a LoadBalancer Service is created, Kubernetes first creates an underlying NodePort Service. Then, an
external load balancer is provisioned by the underlying infrastructure, typically via a cloud provider integration. This
external load balancer forwards incoming traffic to the NodePorts exposed on the cluster nodes, which then route the
traffic to the backing Pods.

Kubernetes itself does not implement cross-node load balancing. Instead, it integrates with cloud-provider-specific
solutions such as AWS Elastic Load Balancer, Azure Load Balancer, or Google Cloud Load Balancer. The external load
balancer provides a stable external IP address or DNS name, performs health checks, and distributes traffic evenly
across the nodes in the cluster.

## Declarative approach

## Imperative approach

In contrast to the general approach of creating resources imperatively using kubectl create, Services are often created
using kubectl expose.

```shell
kubectl expose deployment deploy-name \
--name=service-name \
--port=80 \
--target-port=8080 \
--dry-run=client -o yaml
```

This command generates a Service of type ClusterIP by default.

## Sources

https://kubernetes.io/docs/concepts/services-networking/service/