# Cron jobs

## Theory

CronJobs are a special workload controller for Jobs. We define a schedule using a cron expression (e.g., every Sunday at
9:00). When the scheduled time is reached, the CronJob creates a Job based on a template, and the Job then creates the
necessary Pods.

CronJobs are useful when we want to execute tasks that are not long-running processes on a specific, recurring schedule.
If the task does not need to be repeated, a regular Job should be used instead.

There are a few important spec properties that are good to know:

### jobTemplate

This is the template used for creating Jobs. It has exactly the same structure as a Job manifest, except for the kind
and apiVersion fields. Refer to the notes for the Job resource for more details.

### startingDeadlineSeconds

When the time to create a Job is reached based on the cron expression, the CronJob is
expected to create a new Job. If, for some reason, the Job creation is delayed, we need a way to indicate to the
CronJob that if the Job has not been created after a certain amount of time, it is no longer useful.

For example, consider a CronJob for database backups that runs every 3 hours. It is time to run the next iteration,
but suppose the cluster is temporarily out of resources and the Job cannot be scheduled. Two hours and thirty minutes
pass, and only then the Job is scheduled. At that point, the backup is no longer useful; it would be better to wait
another 30 minutes for the next scheduled run.

This interval can be defined using startingDeadlineSeconds. If the Job creation is delayed longer than the value
specified by this property, the iteration is skipped.

### concurrencyPolicy

This property defines the behavior of a CronJob when the currently running Job takes longer to complete and the next
scheduled iteration is reached.

It supports the following values:

- Allow (default) - If a new Job needs to be scheduled while the previous one is still running, the new Job is
  created and both Jobs run in parallel.
- Forbid - If it is time for a new Job but the previous one is still running, the next iteration is skipped and only
  the Job from the previous iteration continues to run.
  Note that if startingDeadlineSeconds is defined, it is still taken into account. For example, the new iteration
  may initially be skipped, but if the old Job terminates and the deadline has not yet been exceeded, the new Job
  will be scheduled immediately.
- Replace - If it is time for a new Job while the previous one is still running, the running Job is terminated and
  the new Job is scheduled.

### successfulJobHistoryLimit and failedJobHistoryLimit

These fields specify the number of successful or failed Jobs that should be retained.
successfulJobHistoryLimit defaults to 3, and failedJobHistoryLimit defaults to 1. If either of these values is set to 0,
no Job history will be kept for the respective type of Job termination.

Note that automatic Job cleanup can also be handled by the TTL-after-finished controller. For more details, see the
documentation for the Job resource.

### suspend

A CronJob can be disabled by setting the suspend property to true; by default, it is set to false. When a CronJob is
suspended, all Jobs that would normally be scheduled are skipped until the CronJob is unsuspended.

Skipped Jobs are counted as missed Jobs. Depending on startingDeadlineSeconds and concurrencyPolicy, the behavior when
the CronJob is unsuspended may differ. For example, if startingDeadlineSeconds is not defined, the controller will
attempt to schedule all missed Jobs immediately, and concurrencyPolicy will determine whether this is allowed.

## Declarative approach

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: cron-job-1
spec:
  startingDeadlineSeconds: 30
  concurrencyPolicy: Replace
  failedJobsHistoryLimit: 3
  jobTemplate:
    metadata:
      name: cron-job-1
    spec:
      template:
        spec:
          restartPolicy: OnFailure
          containers:
            - image: busybox
              name: cron-job-1
              command:
                - /bin/sh
                - -c
                - date; echo "John 3:16"
  schedule: '* * * * *'

```

## Imperative approach

If we are trying to be quick in the CKAD exam, we should start with an imperative command using --dry-run=client and -o
yaml, and save the generated manifest to a file. This gives us the basic structure of the manifest, after which we can
customise the cronjob fields.

The basic create command only allows for the customisation of the base image and the schedule (cron expression):

```shell
kubectl create cronjob cron-job-name --image=busybox:latest --schedule="* * * * *" --dry-run=client -o yaml > cronjob.yaml
```

For more details and examples we can use the help command:

```shell
kubectl create cj --help
```

## Cron syntax

```text
# ┌───────────── minute (0 - 59)
# │ ┌───────────── hour (0 - 23)
# │ │ ┌───────────── day of the month (1 - 31)
# │ │ │ ┌───────────── month (1 - 12)
# │ │ │ │ ┌───────────── day of the week (0 - 6) (Sunday to Saturday)
# │ │ │ │ │                                   OR sun, mon, tue, wed, thu, fri, sat
# │ │ │ │ │
# │ │ │ │ │
# * * * * *
```

Available macros:

@yearly or @annually - 0 0 1 1 * - Runs once a year at midnight at 1st of January

@monthly - 0 0 1 * * - Run once a month at midnight of the first day of the month

@weekly - 0 0 * * 0 - Run once a week at midnight on Sunday morning

@daily or @midnight - 0 0 * * * - Run once a day at midnight

@hourly - 0 * * * * - Run once an hour at the beginning of the hour

## Sources

https://kubernetes.io/docs/concepts/workloads/controllers/cron-jobs/