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

These are the essential YAML configurations specific to Job resources. For the Pod template, refer to the notes for Pods
themselves.

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: job-name
spec:
  completions: 3
  parallelism: 5
  backoffLimit: 4
  activeDeadlineSeconds: 3600
  template:
    spec:
      containers:
        - name: pi
          image: perl:5.34.0
          command: [ "perl", "-Mbignum=bpi", "-wle", "print bpi(2000)" ]
      restartPolicy: Never
```

In this example, we use a simple Perl script to calculate Pi. We can customize the Pod specification as we normally
would for a Pod, but here we focus on the Job configuration.

We can see that completions is set to 3, which means we want three Pods to be executed and finish successfully.
parallelism is set to 5, but since completions is limited to 3, the Job will start only three Pods. This is because the
completion count is explicit, and there is no need to run more Pods than the number of required successful executions.

## Imperative approach

If we are trying to be quick in the CKAD exam, we should start with an imperative command using --dry-run=client and -o
yaml, and save the generated manifest to a file. This gives us the basic structure of the manifest, after which we can
add properties such as completions, parallelism, and others.

```shell
kubectl create job job-name --image=busybox:latest --dry-run=client -o yaml > job.yaml
```

We cannot configure most Job properties directly from the kubectl create command, so they need to be defined manually in
the manifest.

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

Note that we can also edit terminated Jobs and add this property. However, keep in mind that if we set
ttlSecondsAfterFinished to, for example, 30 seconds, the countdown starts from the time the Job finished, not from the
time the property was applied. As a result, the Job may be deleted immediately if more than 30 seconds have already
passed since it terminated.

## Completion Mode

When a Job has a fixed completions count (that is, when completions is specified and not null), we can customize the
completionMode. The completion mode defines how the successful termination of Pods created by the Job is counted toward
the specified completions value.

There are two completion modes: NonIndexed and Indexed. If no mode is specified, the default is NonIndexed.

### NonIndexed jobs

NonIndexed is the default behavior for a Job. In this mode, the only requirement is that the number of successfully
completed Pods equals the completions count. All Pods are treated identically; it does not matter which Pod succeeds, as
long as the required number of successful completions is reached.

Even if parallelism is greater than 1 and some Pods fail, new Pods may be created to replace them. As long as enough
Pods eventually succeed, the Job will be considered complete.

### Indexed jobs

Indexed Jobs work differently from non-indexed Jobs. Each Pod is assigned a unique index with a value from 0 to
completions - 1 (similar to array indexing). Each completion is tied to a specific index, and in order for the Job to be
considered complete, all indices must complete successfully.

For example, if we configure completions: 3, the Job will create three Pods with indices 0, 1, and 2. When the Pod with
index 0 terminates successfully, index 0 is considered complete, and the same applies to the other indices. If a Pod
associated with a given index fails, a new Pod is created with the same index and retried until it succeeds or the
failure limit is reached.

Indexed Jobs are useful when each Pod must execute a specific part of a task. For example, each Pod in the Job can
process a distinct chunk of data or a specific set of files, and we want to avoid clashes between Pods. With indexing,
we know that the Pod with index 0 will process a specific chunk, and the same applies to the other Pods.

This feature is not strictly tied to the parallelism property, but it is most useful when used in combination with it.
If we set parallelism to 1 and completions to 3 in indexed mode, Kubernetes will still assign a unique index to each
Pod, but the Pods will run sequentially. In this case, the benefit of indexing is limited. Indexed Jobs are most
effective when parallelism is greater than 1. For example, with parallelism: 3, Kubernetes will start three Pods
simultaneously, each with its own index. If one Pod fails, a new Pod is created with the same index and retried.

To determine the index assigned to a Pod, Kubernetes automatically adds a label and an annotation named
batch.kubernetes.io/job-completion-index. Additionally, an environment variable named JOB_COMPLETION_INDEX is made
available inside the Pod, allowing the workload to split and process tasks programmatically based on the index.

To handle Pod failures, in addition to the backoffLimit property, which behaves the same way as in non-indexed
Jobs, indexed Jobs can also use backoffLimitPerIndex. This property tracks failures at the index level rather than
globally.

For example, if we have three indices and set backoffLimitPerIndex to 3, the Pod with index 0 may fail twice and then
succeed, and the Pod with index 1 may also fail twice and then succeed. Neither will exceed the retry limit because
failures are tracked separately for each index.

When backoffLimitPerIndex is specified, the backoffLimit property is ignored.

## Non-fixed completion count jobs

This behavior occurs when we set completions to null and set parallelism to a value greater than 1. In this
configuration, the Job will create a number of Pods equal to the parallelism value. It is then up to the applications
running inside the Pods to coordinate their work. Once any Pod terminates successfully, the entire Job is marked as
successful, and all remaining Pods are terminated, respecting their graceful shutdown.

When a Pod fails, a new one is created, and retries respect the backoffLimit. This works the same way as with Jobs that
have a fixed completion count.

For Jobs with a non-fixed completion count, we cannot use completionMode or backoffLimitPerIndex.

## Resources

https://kubernetes.io/docs/concepts/workloads/controllers/job/
https://kubernetes.io/docs/concepts/workloads/controllers/ttlafterfinished/