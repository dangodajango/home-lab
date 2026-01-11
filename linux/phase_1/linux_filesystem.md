## Linux filesystem

We start with physical resources such as SSDs, HDDs, or NVMe drives. These devices store data as raw blocks of bytes. At
this level, the storage has no understanding of files, directories, permissions, or filenames. It only knows how to read
and write fixed-size blocks at specific locations. Concepts like “file” or “folder” simply do not exist here.

On top of this raw storage, a filesystem is created. A filesystem is not a purely runtime concept. It is a concrete,
persistent data structure written onto the disk itself. This structure defines how raw blocks are organized into
meaningful entities such as files and directories. It specifies how metadata is stored, how free space is tracked, how
directories reference files, and how consistency is maintained. For example, in filesystems like ext4, this includes
structures such as inode tables, directory entries, allocation maps, and journals. All of this data remains on disk even
when the system is powered off.

When the operating system boots or when a filesystem is mounted, the Linux kernel uses a filesystem driver that
understands the specific on-disk format. At this moment, the kernel reads the filesystem’s metadata from disk and
constructs in-memory representations of it. These runtime structures include cached inodes, directory entries, and
bookkeeping data that allow fast access and consistent behavior. This is the part that exists only while the system is
running and is recreated on each mount.

From the user’s point of view, interaction always happens with this runtime view. When a program opens a file, the
kernel resolves the path using in-memory directory and inode caches, checks permissions, and then reads or writes the
corresponding disk blocks through the filesystem driver. The data itself may be cached in memory for performance
reasons, but it still originates from the on-disk filesystem structure.

At the core of most Linux filesystems, especially ext4, is the idea that filenames are not the primary identity of a
file. Instead, every file is represented by an internal object called an inode. An inode is a data structure stored on
disk that uniquely describes a file or directory. It contains all the metadata needed by the operating system to manage
that file, such as its type, permissions, owner and group, timestamps, size, and most importantly, the information
needed to locate the file’s actual data blocks on disk.

What an inode deliberately does not store is the filename. Filenames exist only inside directories. A directory is
itself a special kind of file whose contents consist of a mapping between human-readable names and inode numbers. When
you see a filename in a directory, what you are really seeing is a reference that points to an inode. This separation is
fundamental to how Linux filesystems work.

When a process accesses a file using a path like /home/alice/file.txt, the kernel resolves that path step by step. It
starts at the root directory’s inode, looks up the entry for home, follows the inode referenced there, then looks up
alice, and finally resolves file.txt to its inode. Once the inode is found, the kernel no longer cares about the
filename; all further operations are done using the inode and its metadata.

The inode also contains references to the data blocks where the file’s contents are stored. For small files, these
references may point directly to blocks. For larger files, the inode uses levels of indirection, meaning it points to
blocks that themselves contain pointers to other blocks. This allows the filesystem to support files of many different
sizes while keeping inode structures reasonably small.

Directories follow the same rules. A directory has an inode just like a regular file, but its data blocks contain
directory entries rather than user data. Each entry maps a filename to an inode number. Because directories are files,
they have permissions too. This is why permissions on a directory control whether you can list its contents, access
files inside it, or create and delete entries.

Another important component of the filesystem is the superblock. The superblock describes the filesystem as a whole. It
contains global information such as the filesystem type, size, block size, inode counts, and feature flags. When a
filesystem is mounted, the kernel reads the superblock first to understand how to interpret the rest of the on-disk
structures. If the superblock is corrupted, the filesystem may become unreadable, which is why filesystems usually keep
backup copies of it.

To keep track of which blocks and inodes are free or in use, the filesystem maintains allocation metadata. In ext4, this
is done using bitmaps or similar structures that record whether a particular inode or data block is allocated. When a
new file is created, the filesystem allocates a free inode and then allocates data blocks as needed, updating these
allocation structures accordingly.

Modern Linux filesystems are also journaled. Journaling is a mechanism used to maintain consistency in the face of
crashes or power loss. Before making changes to critical filesystem metadata, the filesystem writes a description of the
intended changes to a journal. If the system crashes midway through an operation, the filesystem can replay the journal
when it is mounted again and bring itself back to a consistent state. This is why journaled filesystems like ext4
recover quickly after an unclean shutdown.

At runtime, the kernel does not constantly read these structures from disk. Instead, it caches frequently used inodes,
directory entries, and data blocks in memory. These caches dramatically improve performance and also explain some
behaviors that can seem surprising at first, such as deleted files still occupying disk space until all processes using
them are closed.

## Types of files

In a Linux filesystem, every inode has a type. This type tells the kernel how the data associated with that inode should
be interpreted and how operations on it should behave. The most common type is the regular file. A regular file is
exactly what most people think of as a file: it is a sequence of bytes stored in data blocks, with no special meaning
imposed by the filesystem itself. Text files, binaries, images, and logs are all regular files. The filesystem does not
care about the content; it only stores and retrieves bytes.

A directory is also a file, but with a special structure. Instead of arbitrary user data, its contents consist of
directory entries that map filenames to inode numbers. When you list a directory, the kernel reads this structured data
and presents the names it contains. Because directories are files, they have permissions, owners, and timestamps just
like regular files. This is why access to a directory is controlled independently from access to the files inside it.

Symbolic links are another file type. A symbolic link is a small file whose data is simply a path to another file. When
a process tries to access a symlink, the kernel automatically resolves that path and redirects the operation to the
target. The symlink has its own inode and permissions, but those permissions are usually ignored in favor of the
target’s permissions.

Hard links are not a separate file type, but they are an important consequence of how directories and inodes work. A
hard link is simply another directory entry pointing to the same inode. From the filesystem’s point of view, there is no
“original” name; all names are equal references. The inode keeps a link count, and the file’s data is only removed when
that count reaches zero and no process is still using it.

Beyond these familiar file types, Linux defines several special ones that are essential to the system. Character devices
and block devices represent hardware or virtual devices. A block device, such as a disk, works with data in blocks and
supports seeking. A character device, such as a terminal or serial port, works as a stream of bytes. These devices
appear as files under /dev, but reading from or writing to them does not access disk data; instead, the kernel forwards
those operations to device drivers.

Sockets and FIFOs are file types used for inter-process communication. A FIFO, also called a named pipe, allows one
process to write data and another to read it in a stream-like fashion. A socket file represents an endpoint for
communication, often used for local services. Even though they are not backed by disk data in the usual sense, they
still use the same file-based interface: you open them, read from them, write to them, and close them.

This brings us naturally to the idea that “everything is a file” in Linux. What this really means is that the kernel
exposes many different resources through a unified interface based on file operations. Whether a process is reading a
text file, sending data to a network socket, writing to a terminal, or querying kernel information, it uses the same
system calls: open, read, write, and close. The underlying implementation differs, but the interface remains consistent.

## Core filesystem directories

When a Linux system boots and mounts its root filesystem, it presents a directory tree that follows a long-standing
convention known as the Filesystem Hierarchy Standard. This standard exists so that both humans and software can make
reasonable assumptions about where things live, regardless of the specific distribution.

At the very top is the root directory, /. This is not “just another folder”; it is the anchor point of the entire
filesystem. Every file, directory, device, and mounted filesystem ultimately appears somewhere beneath it. Without /,
nothing else is reachable.

Directly under the root, /bin contains essential user-level programs that are required for the system to function in
basic or recovery modes. Commands such as ls, cp, mv, cat, and sh live here. The idea is that even if only the root
filesystem is available, a user can still inspect the system and perform minimal repairs.

Closely related to this is /sbin, which holds essential system binaries intended primarily for administrative tasks.
These include tools for mounting filesystems, configuring networking, and managing system state. Historically, /bin was
for normal users and /sbin for the administrator, though in modern systems this distinction is less strict.

The /etc directory contains system-wide configuration files. These are usually plain text and are meant to be edited by
administrators or managed by configuration tools. Importantly, /etc should contain configuration only, not executable
programs. Files here define how the system and its services behave, from user accounts to network settings to service
configuration.

User data lives under /home. Each regular user typically has a directory here named after their username. This directory
is where personal files, shell configuration, and user-specific application data are stored. Keeping user data separate
from system data makes backups, migrations, and multi-user setups much simpler.

The /root directory is the home directory of the root user. It is kept separate from /home for both security and
reliability reasons, ensuring that the administrator always has a usable home directory even if /home is on a separate
filesystem or temporarily unavailable.

The /usr directory contains the majority of userland software and libraries. Despite its name, it is not primarily about
users’ personal data. Instead, it holds installed applications, shared libraries, documentation, and
architecture-independent data. Subdirectories like /usr/bin, /usr/lib, and /usr/share house most of the programs and
resources that make up the operating system’s software environment.

Variable data is stored under /var. This includes files that change over time, such as logs, caches, mail spools, and
application state. Services that need to write data while running typically do so under /var. Separating variable data
from static system files helps keep the filesystem organized and manageable.

Temporary files are placed in /tmp. This directory is intended for short-lived data that does not need to persist across
reboots. Many systems clean /tmp automatically. Because it is often world-writable, special permission bits are used to
prevent users from interfering with each other’s files.

The /lib directory contains essential shared libraries needed by the binaries in /bin and /sbin, along with kernel
modules in /lib/modules. These libraries must be available early in the boot process, which is why they live outside
/usr on traditional systems.

Device files appear under /dev. This directory exposes hardware and virtual devices as files, allowing programs to
interact with them using standard file operations. Disks, terminals, and pseudo-devices like /dev/null all appear here.
The contents of /dev are typically managed dynamically by the system.

The /proc directory is a virtual filesystem that exposes information about running processes and kernel state. Files
here are generated on the fly by the kernel and do not exist on disk. Reading from these files allows users and programs
to inspect system state, while writing to some of them can influence kernel behavior.

Similarly, /sys provides a structured view of devices, drivers, and kernel subsystems. It is more strictly organized
than /proc and is commonly used by modern tools to query and configure hardware and kernel parameters.

The /run directory holds runtime data for the current system session, such as process IDs and sockets. Its contents are
usually stored in memory and cleared on reboot. It replaces older conventions like /var/run and ensures that runtime
state is always available early during system startup.

Finally, directories like /mnt and /media are intended as mount points for additional filesystems. /mnt is traditionally
used for temporary or manual mounts, while /media is commonly used for removable media such as USB drives.