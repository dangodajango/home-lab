# Linux distributions

## Theory

In its core the Linux OS is build in 2 core parts - the Linux kernel which communicates with the hardware - cpu, disk,
ram and the Linux distribution e.g., Ubuntu, RedHat, Arch.

The Linux kernel is the core of the operating system, it’s like the engine of a car, performing the main work.

Some of the responsibilities of the kernel include:

- Process management: it manages all running processes, schedules CPU time for each process, creates and destroys
  processes, and handles inter-process communication (IPC).

- Memory management: it manages the RAM used by different processes, ensuring each process has its own space and cannot
  interfere with others.

- Hardware management: it handles all interactions with hardware interfaces and drivers. For a process to communicate
  with hardware (e.g., hard disk, keyboard, or screen), it must go through the kernel.

- Networking: it manages network interfaces, sockets, and low-level protocols.

The kernel also performs many other tasks, but for understanding its core concept, this is sufficient.

However, the kernel by itself is not sufficient for a user to interact with the system. While it handles all the
low-level work, process management, memory, hardware, networking, there’s no user interface, no utilities, no way to
run applications.

To make the system usable, we need a Linux distribution, which wraps the kernel with:

- System libraries (e.g., glibc)

- User-space utilities and commands (bash, ls, cp, grep, etc.)

- Desktop environments or terminal interfaces

- Package manager and repositories

- Default configurations and scripts

Together, this forms a complete, functional operating system that users and developers can actually work with.

## Popular distributions

### Debian

The goal of Debian is to be one of the most stable Linux distributions. It often uses older package versions that are
well known for their correct and predictable behavior. Updates are less frequent, but when they do occur, they are
tested extensively to ensure that the system remains stable and free of breaking changes.

The advantage of this approach is that Debian is very reliable and well suited for long-running systems. The downside is
that it rarely includes the newest technologies, and new releases take a long time to become available.

### Ubuntu

Ubuntu is based on Debian, so it inherits much of Debian’s stability. However, Ubuntu keeps itself more current with
frequent software updates and hardware support. It provides a user-friendly graphical interface for interacting with the
system and comes preconfigured for desktops and development environments.

### ARCH BTW

Arch Linux is a very minimalistic distribution. It ships with only a base system, so most user-facing software must be
installed and configured from scratch. Because of this, it is highly extensible and customizable, allowing users to
build a system tailored to their specific needs. Arch is particularly suitable for personal use by those who want to
learn Linux in depth or create a workstation configured exactly the way they want.