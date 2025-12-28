# Jobs

## Theory

Jobs are controllers for Pods. Instead of manually creating Pods ourselves, controllers do this for us, and they manage
the creation, restarts, and deletion of Pods. A Job, as a Kubernetes resource, is used when we have a short-lived
process - for example, a batch job - something that starts, executes some logic, and then shuts down. It is not useful
for
long-running applications such as backend web services.

The way a Job works is that when we create it, it contains a Pod template, and Kubernetes schedules a Pod based on that
template. The Pod then runs and either finishes successfully or fails. When it finishes successfully, the Job checks the
number of defined completions (the number of successful Pod runs). If we define this number as 2, the Job will run the
Pod again sequentially until it has two successful runs. If the Pod fails, it will be retried a number of times defined
by the backoffLimit specification property. The default value is 6, so if the Pod fails six times, the entire Job is
marked as failed.

There are two properties that define how a Job behaves: parallelism and completions.

Completions define the number of successful Pod executions. By default, this value is 1 if it is not specified. For
example, if we configure it to 3, the Job will start one Pod; when it succeeds, it will start another, and then another,
sequentially. When a Pod fails, it increments the backoffLimit, and the Job schedules another Pod. If it fails again,
the backoffLimit is incremented again, and the process repeats until either a Pod succeeds or the backoffLimit is
reached, at which point the Job is marked as failed.

A Pod with a restartPolicy of Never fails immediately when the container exits with a non-zero status code. If the
restartPolicy is set to OnFailure, Kubernetes will keep restarting the container within the same Pod. However, these
container restarts do not count toward the backoffLimit; only Pod failures do. In a Job, a Pod must have a restartPolicy
of either Never or OnFailure. It cannot be Always, because the container is expected to exit at some point. If the
policy were Always, the container would be restarted even after exiting with a successful status code.

Parallelism is a property that works in conjunction with completions. It specifies how many Pods should run
simultaneously in order to execute the workload in parallel and reach the desired number of completions faster. The
default value, if not specified, is 1, meaning only one Pod will run at a time. This results in sequential execution
when the number of completions is greater than 1. If parallelism is set to 0, the Job is effectively paused, as no Pods
will be running.

It is important to note that this property works in correlation with the completions setting. For example, if we
configure completions to 2 and parallelism to 5, the Job will start only 2 Pods. Since the completion count is fixed,
there is no need to run 5 Pods when only 2 successful executions are required. Additionally, we must ensure that the
workload being executed can actually run in parallel across multiple Pods.

It is also important to note that as long as the Job is not deleted, the Pods that were executed as part of it continue
to exist, even if they are in a finished or failed state. Only when the Job is deleted are its associated Pods deleted
as well.

Another property to keep in mind is activeDeadlineSeconds. Once the Job starts running, a timer begins. If this timer
reaches the value specified by activeDeadlineSeconds while the Job is still running, the Job is marked as failed and all
running Pods are terminated.

## Declarative approach

## Imperative approach

## Automatic cleanup for finished jobs

Since Kubernetes v1.23, there is a built-in solution for cleaning up Jobs once they have finished. If we do nothing,
Jobs that succeed or fail will remain in the cluster indefinitely until they are manually deleted. This also applies to
the Pods created by the Job - as long as the Job exists, its Pods remain as well. Over time, this can lead to cluster
clutter if more and more Jobs are created without cleanup.

To address this, the Kubernetes team introduced the TTL-after-finished controller, which runs on the cluster and
monitors finished Jobs, whether successful or failed. It checks the ttlSecondsAfterFinished property. If this property
is set, the controller calculates whether the TTL has passed using the Job's completion timestamp. If the TTL has
elapsed, the controller deletes the Job along with all associated resources. Otherwise, it waits until the TTL expires.

The TTL-after-finished controller is enabled automatically on the cluster. The only action required from the user is to
add the ttlSecondsAfterFinished property to the Job manifest. When the Job is created, the property will be present, and
the controller will handle cleanup once the Job completes.

## Resources

https://kubernetes.io/docs/concepts/workloads/controllers/job/
https://kubernetes.io/docs/concepts/workloads/controllers/ttlafterfinished/