# Environment variables

Environment variables are a runtime key-value pair structure of data, e.g., PROJECT_DIRECTORY=/app/project. The
environemtn variables are specific to a process, meaning that each process has it's own unique pool of them and if the
process spawns child processes they will inherit the environemtn variables from their parent.

So if we independently start 2 processes both of them will have a clean state with no environment variables, unless
there is some mechanism to add them on boot. I say this because when I work on Linux i often run processes through the
shell, so the shell has a behaviour where we can define environment variables in the shell configuration files and when
a session is started it will automatically load all environment variables, so we awalys have them available, then if we
run an application through th shell session, the child process will have a copy of the environemnt variables avialble in
the shell. But in it's core the shell starts with no environment variables, then dynmically loads them in the sessoin,
and from that point all child processes also have them, they can modify and export them, but it's only in memory, if the
session restarts it will start with a clean state. But theses environment variables are only available when the process
is started through the shell, if we use for example systemd and configure a few services, all of them will be started
without any environemnt variables, unless we configure the service file with such. It's not a given that all processes
will have the same evnrinment variables as the ones which are started through the shell.

Note, environment variables that are created at runtime, are only available until the system is rebooted. That's why
shell uses files like .bashrc or .profile where we can define the environment variables sto be loaded when the shell
session is created, and that's why we need to modify the files if we want to update the environemnt variables. 
The same applies for any other process, which relies on environemnt variables.