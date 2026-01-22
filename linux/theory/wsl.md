# WSL (Windows Subsystem for Linux)

## Before WSL

Before WSL1/2, if we wanted to work in a Linux environment while having Windows installed locally, we had three main
choices:

1. Dual boot – Set up both Windows and Linux on the same physical machine and choose which OS to run at boot time.
2. Virtual machine – Run Linux inside a VM using tools like VMware or VirtualBox.
3. Remote Linux – SSH into another machine running Linux.

All of these options are valid, but they introduce inconvenience when we want to switch quickly between environments or
tightly integrate Linux tooling with Windows. Because of this, the idea of WSL (Windows Subsystem for Linux) was born.

## WSL 1

WSL 1 was introduced with the goal of allowing Linux binaries to be executed directly on the Windows OS. Linux binaries
normally expect to run on a Linux kernel, rely on Linux system calls, and use Linux filesystem semantics, none of which
exist natively on Windows.

The goal of WSL 1 was therefore to translate these Linux-specific dependencies into Windows-native ones. Linux system
calls are intercepted at runtime and translated into equivalent Windows system calls, effectively acting as a
compatibility layer between Linux user-space programs and the Windows kernel.

However, WSL 1 has fundamental limitations. Some Linux kernel features cannot be implemented purely through system call
translation on top of the Windows kernel. As a result, certain tools and technologies that depend on these kernel
features, such as cgroups, namespaces, iptables, Docker are not supported on WSL 1.

Note that WSL does not include a real Linux kernel; Linux system calls are translated into their Windows equivalents.

## WSL 2

The problem with WSL 1 is that some Linux features are not exposed purely through system calls, but are embedded in the
kernel itself. Because of this, the WSL 1 architecture could not provide a 100% compatible Linux environment.

WSL 2 is a redesigned version of WSL 1 that uses a different approach to allow Linux to run on Windows. WSL 2 relies on
a real Linux kernel, maintained by Microsoft and based on the official Linux kernel. This kernel is started inside a
very lightweight virtual machine that is completely managed by WSL, with no direct interaction from the user.

The virtual machine is minimal and exists solely to host a Linux distribution. There is no VM UI, no manual
configuration, and although it is technically a virtual machine, it is presented to the user as a subsystem rather than
a traditional VM.

Instead of translating Linux system calls into Windows-compatible ones, WSL 2 runs Linux binaries directly against the
Linux kernel. Because the binaries target a real Linux environment, compatibility issues present in WSL 1 are
eliminated.

As a result, Linux-based software that was not compatible with WSL 1 can now run correctly on WSL 2.

One drawback of WSL 2 compared to WSL 1 is filesystem I/O performance when accessing Windows files. In WSL 2, the
Windows filesystem is mounted into the Linux virtual machine, whereas in WSL 1 Linux processes accessed the Windows
filesystem directly through system call translation.

## WSL Distributions

WSL provides a Microsoft-maintained Linux kernel, while allowing users to choose from different Linux distributions that
run on top of it. Each distribution supplies its own user-space components such as the package manager, binaries,
libraries, and default tooling.

Users can install common Linux distributions such as Ubuntu, Debian, or Kali, similar to what they would use in a
traditional virtual machine. Multiple distributions can be installed and run simultaneously, each with its own isolated
filesystem and configuration.

The state of each distribution is persistent across sessions. Any changes made to its filesystem or installed packages
are preserved and restored when the distribution is started again.

WSL also allows exporting a distribution’s state to a tar archive, which can later be imported on the same machine or a
different one, enabling easy replication of the same Linux environment across systems.

## Filesystem

A filesystem is a way to organize data on storage. It structures data into files and directories and defines access
permissions and metadata. Different operating systems typically use different filesystems, such as NTFS on Windows and
ext4 on Linux. Other common filesystems include FAT32 and exFAT. Each filesystem works differently internally, so they
are not directly compatible without appropriate drivers.

One important concept related to filesystems is mounting. On Linux, the entire filesystem is exposed as a single
directory tree rooted at /. Although there is only one directory tree, multiple filesystems can be attached to it at the
same time.

When an additional filesystem exists, such as one on an external drive or USB stick, it has its own root. Mounting is
the
process of attaching that filesystem to a specific directory, called a mount point. From that point onward, the mounted
filesystem’s root appears at that directory, effectively injecting it into the existing tree rather than creating a
second root.

Linux can mount many different types of filesystems, including virtual and non-native ones such as NTFS, by using
appropriate filesystem drivers. This allows files from other operating systems or virtual sources to appear as part of
the Linux filesystem hierarchy.

Once a filesystem is mounted, its files appear as if they are part of the Linux filesystem. Performance characteristics
depend on the filesystem type and how it is accessed. Native Linux filesystems typically offer the best performance,
while mounted filesystems that require translation or cross system boundaries may be slower.

In WSL 2, Windows drives and directories are mounted into the Linux environment because the Linux kernel runs inside a
virtual machine. Accessing Windows files therefore involves crossing the VM boundary, which results in slower filesystem
performance compared to native Linux filesystem access.

In contrast, WSL 1 did not use a virtual machine. Instead, Linux system calls were translated directly into Windows
system calls, allowing Linux processes to access the Windows filesystem more directly. Because of this tighter
integration, filesystem access to Windows files was generally faster in WSL 1 than in WSL 2.

## Resource management

Compared to most traditional virtual machines, WSL 2 does not allocate a fixed amount of resources upfront. Instead,
resources are managed dynamically. Initially, WSL 2 starts with a bare minimum of CPU, memory, and disk usage. As the
Linux environment is used more heavily, additional resources are requested and allocated as needed.

Resource limits for WSL 2 can be configured by creating a .wslconfig file under - C:\Users\<username>\.wslconfig

Example configuration:

[wsl2]
memory=8GB
processors=4
swap=4GB

These configuration settings apply globally to all WSL 2 distributions.

## Commands

List installed distributions:

```shell
wsl --list --verbose
```

Start specific distribution:

```shell
wsl -d Ubuntu
```

Set default distribution:

```shell
wsl --set-default Ununtu
```

Start default distribution:

```shell
wsl
```

Start default distribution at a specific directory:

```shell
wsl ~
wsl /mnt/c/Projects
```

Shutdown WSL:

```shell
wsl --shutdown
```

Export a distribution - creates a snapshot of the filesystem, which can be transfered and applied in a different system,
to replicate the current state of the linux environment.

```shell
wsl --export Ubuntu ubuntu-backup.tar
```

Import a distribution:

```shell
wsl --import UbuntuClone C:\WSL\UbuntuClone ubuntu-backup.tar
```

Update WSL:

```shell
wsl --update
```

Find available distributions:

```shell
wsl --list --online
```

Install a distribution:

```shell
wsl --install Ubuntu
```

Delete a distribution:

```shell
wsl --unregister Ubuntu
```

Windows directories are automatically mounted under /mnt/c or /mnt/d depending on what disks we have.

## Sources

- https://learn.microsoft.com/en-us/windows/wsl/setup/environment
- https://learn.microsoft.com/en-us/windows/wsl/about
- https://learn.microsoft.com/en-us/windows/wsl/compare-versions
- https://learn.microsoft.com/en-us/windows/wsl/basic-commands