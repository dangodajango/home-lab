# Environment variables

Environment variables are a runtime key-value structure of data, e.g., PROJECT_DIRECTORY=/app/project. They are
process-specific, meaning each process has its own unique environment table in memory. When a process spawns child
processes, the children inherit a copy of the parent’s environment variables. Changes in one process do not affect
others.

If we independently start two processes, both will start with a minimal environment, unless some mechanism sets
additional variables at startup. For example, when working on Linux through a shell, the shell inherits a set of
environment variables from the login process (such as PATH, HOME, USER, etc.). The shell can then dynamically load
additional variables from configuration files like .bashrc, .bash_profile, or /etc/profile. Any process started from the
shell will inherit these environment variables. Variables can also be modified and exported in the shell session, but
these changes are only in memory and exist for the duration of the session.

Processes started independently of the shell, such as systemd services, start with a minimal environment unless
explicitly configured using directives like Environment= or EnvironmentFile= in the service file. They do not
automatically inherit the shell’s environment variables.

Environment variables created at runtime are temporary, they exist only until the process ends or the system reboots.
That’s why persistent environment variables are configured in startup files (like .bashrc, .profile, /etc/environment)
or via systemd service files.