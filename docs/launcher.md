# Launcher
The launcher is an alternative to using `CoreContext` for deployments. 

It adds the following functionality
- a CommandExecutor that parses the commandline and executes an operation.
- deployment of service blocks, ie a block of services configured for a specific host.

##### The shutdown hook 
The shutdown hook will only be used if the launcher is used for deployments.

The stop method in any running `CoreDeployable` will be invoked from the shutdown hook and has a configured timeout. The close operation will be cancelled if the 
stop method takes more than 3s to complete. The shutdown hook timeout can be configured in the system config.