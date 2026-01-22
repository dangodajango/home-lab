# Package management

## Overview

There are conventions in the filesystem for where different types of files should be placed, e.g., configuration files
under /etc, libraries under /usr/lib, binaries under /bin, etc. When software is installed, its creators try to adhere
as much as possible to these conventions, so the different files that are part of the installed software are scattered
across the filesystem. This is in contrast to Windows, where all files related to a program are usually deployed in a
single folder. On Linux, this is not the case. Since we don’t want to handle the burden of figuring out where to place
the files and their paths, these locations are defined in a package, which the package manager applies when installing
the software.

Another core problem that the package manager resolves is where to find the actual software. Each distribution generally
hosts its own repository, and the respective package manager downloads packages from there. After a package is
downloaded, the package manager verifies it using a digital signature to ensure it has not been tampered with or
compromised. Then it resolves the dependencies. This is similar to how Maven works for a Java project: where multiple
dependencies are required, the package manager downloads all necessary packages recursively down the dependency tree.

As dependencies are installed, their files are extracted and placed in the configured directories on the filesystem.

## Types of package managers

### APT (Advanced Packaging Tool)

APT is not just a single binary, but rather a whole set of tools and libraries. There are two main parts of the system:
dpkg, the software that installs packages. The packages are in the .deb format; and the higher-level APT system,
which communicates with repositories, downloads packages and their dependencies, and invokes dpkg when it needs to
install a package.

APT only downloads software from a predefined set of trusted repositories. The list of these can be found in
/etc/apt/sources.list.

It is used by Debian-based distributions such as Ubuntu, Mint, and others.

#### Commands

Before we download or search for any software that we want to install, it is good practice to update the local APT
metadata so it contains the latest information about available packages. This step does not install any software.

```shell
apt update
```

If we want to install software, we can use the install command. There are some useful flags:
-y (accepts all interactive prompts so the command does not block and wait for user input) and
--no-install-recommends (some packages have recommended dependencies that are installed automatically; with this flag,
only the strictly required dependencies are installed).

```shell
apt install -y --no-install-recommends vim curl nginx
```

Remove an installed package, but keep its configuration files under /etc:

```shell
apt remove nginx
```

Completely remove a package from the system, including its configuration files:

```shell
apt purge nginx
```

Upgrade only the installed packages that do not require new versions of their dependencies to be installed:

```shell
apt upgrade 
```

Upgrade all packages; this may remove packages and install newer versions of dependencies:

```shell
apt full-ugprade
```

List all installed packages:

```shell
apt list --installed
```

List all packages that can be upgraded:

```shell
apt list --upgradable
```

Check whether a piece of software is available in the repositories. This command scans the local metadata, so it is
important to run apt update beforehand:

```shell
apt search nginx
```

### DNF (Dandified YUM)

DNF follows the same principles as APT, but it is used by a different set of distributions - RPM-based ones such as
Fedora, RHEL, CentOS, and others.

Like APT, DNF is not a single tool but a complete system of binaries. Just as dpkg is used by APT, DNF relies on rpm,
which actually installs packages in the .rpm format. On top of that, there is the higher-level DNF software, which
downloads packages, resolves dependencies, and keeps track of package metadata, similarly to how APT works on
Debian-based
systems.

The repository configuration files are located in /etc/yum.repos.d/

#### Commands

Refresh the local metadata cache for available packages:

```shell
dnf makecache
```

Install packages. The -y flag can be used to automatically accept all interactive prompts:

```shell
dnf install -y vim curl nginx
```

Upgrade all installed packages to their latest available versions:

```shell
dnf upgrade
```

List all installed packages:

```shell
dnf list installed
```

List all packages that can be upgraded:

```shell
dnf list upgrades
```

Search for a package in the local repository metadata. It is good practice to update the cache before searching for a
package:

```shell
dnf search nginx
```