package com.codingchili.core.context;

import com.codingchili.core.Launcher;
import com.codingchili.core.configuration.Environment;
import com.codingchili.core.configuration.exception.BlockNotConfiguredException;
import com.codingchili.core.configuration.exception.NoServicesConfiguredForBlock;
import com.codingchili.core.configuration.exception.RemoteBlockNotConfiguredException;
import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Logger;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.*;

import static com.codingchili.core.configuration.CoreStrings.ID_DEFAULT;

/**
 * Provides context for the Launcher system, passed to the launcher when starting an application
 * using the {@link Launcher} subsystem.
 */
public class LaunchContext {
    private static final String BLOCK_DEFAULT = "default";
    private static final String LOCALHOST = "localhost";
    private CommandExecutor executor = new LauncherCommandExecutor();
    private Logger console = new ConsoleLogger(getClass());
    private String[] args;

    /**
     * @param args process arguments to create a launcher for.
     */
    public LaunchContext(String... args) {
        this.args = args;
    }

    /**
     * @return the launcher settings, must be modified before starting the launcher.
     */
    public LauncherSettings settings() {
        return Configurations.launcher();
    }

    private boolean isRemoteBlock(String remote) {
        return settings().getHosts().containsKey(remote);
    }

    private boolean isBlockConfigured(String block) {
        return settings().getBlocks().containsKey(block);
    }

    private List<String> getBlockForRemote(String remote) throws RemoteBlockNotConfiguredException,
            BlockNotConfiguredException {
        String block = settings().getHosts().get(remote);

        if (isBlockConfigured(block)) {
            return getBlock(block);
        } else {
            throw new RemoteBlockNotConfiguredException(remote, block);
        }
    }

    private List<String> getBlock(String block) throws BlockNotConfiguredException {
        if (isBlockConfigured(block)) {
            return settings().getBlocks().get(block);
        } else {
            if (isBlockConfigured(ID_DEFAULT)) {
                return settings().getBlocks().get(ID_DEFAULT);
            } else {
                throw new BlockNotConfiguredException(ID_DEFAULT);
            }
        }
    }

    /**
     * see #{@link LaunchContext#services(List)}
     */
    protected List<String> service(String block) throws CoreException {
        return services(List.of(block));
    }

    /**
     * Get the configured services for the given blocks or remote identifier.
     *
     * @param blocks the handler of the configured block or the hostname.
     * @return a list of configured services for the given block or host.
     * @throws RemoteBlockNotConfiguredException when no block is configured for given host.
     * @throws BlockNotConfiguredException       when no block is configured for given block-handler.
     */
    protected List<String> services(List<String> blocks) throws CoreException {
        Set<String> services = new HashSet<>();

        for (String block : blocks) {
            List<String> list;
            if (isRemoteBlock(block)) {
                list = getBlockForRemote(block);
            } else {
                list = getBlock(block);
            }
            if (list.isEmpty()) {
                throw new NoServicesConfiguredForBlock(String.join(",", blocks));
            } else {
                services.addAll(list);
            }
        }
        return new ArrayList<>(services);
    }

    /**
     * @return the commandline arguments that was passed to the constructor.
     */
    public String[] args() {
        return args;
    }

    /**
     * Retrieves a block of services that should be deployed. If no block is explicitly set as the second
     * argument then the block is inferred from the host-to-block mappings. if there is no such mapping
     * the 'default' block is deployed, if one is specified.
     *
     * @return a list of services to be deployed for the given arguments.
     * @throws CoreException when the specified block does not exist or when no blocks are specified
     *                       that match the host and the 'default' block is missing.
     */
    public List<String> services() throws CoreException {
        return services(blockNames());
    }

    /**
     * @return the name of the block being deployed.
     */
    public List<String> blockNames() {
        List<String> blocks = executor.getAllProperties("deploy");
        if (blocks.size() == 0) {
            return List.of(findBlockByEnvironment());
        } else {
            return blocks;
        }
    }

    /**
     * Attempts to identify a remote block to use by looking at the hostname and the
     * ip addresses available on the network interfaces.
     *
     * @return a chosen remote or local block.
     */
    private String findBlockByEnvironment() {
        Optional<String> hostname = Environment.hostname();

        // prefer hostname match as network interface enumeration is slow. (~200ms)
        if (hostname.isPresent() && isRemoteBlock(hostname.get())) {
            return hostname.get();
        } else {
            // guard against checking network interfaces if there are no remotes configured.
            if (settings().getHosts().size() > 0) {
                var interfaces = Environment.addresses();

                // add last as a default.
                interfaces.add(LOCALHOST);

                for (String address : interfaces) {
                    if (isRemoteBlock(address)) {
                        return address;
                    }
                }
            }
        }
        return BLOCK_DEFAULT;
    }

    /**
     * @return the command that was passed to the launcher if existing.
     */
    public Optional<String> getCommand() {
        return executor.getCommand();
    }

    /**
     * @return the active logger for the current context.
     */
    public Logger logger() {
        return console;
    }

    /**
     * adds a new command to the command executor, is used to handle command line arguments.
     *
     * @param command the command to add.
     * @return fluent.
     */
    public LaunchContext getCommand(Command command) {
        executor.add(command);
        return this;
    }

    /**
     * Set the command handler used to parse and execute command line arguments.
     *
     * @param executor the executor to execute commands with.
     * @return fluent.
     */
    public LaunchContext setCommandExecutor(CommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * @return future resolved when the command executor for the launch context has executed.
     */
    public Future<CommandResult> execute() {
        var promise = Promise.<CommandResult>promise();
        executor.execute(promise.future(), args);
        return promise.future();
    }

    /**
     * Get the CommandExecutor attached to the launch context.
     *
     * @return fluent
     */
    public CommandExecutor getExecutor() {
        return executor;
    }

    /**
     * Starts the launcher using this context.
     */
    public void start() {
        Launcher.start(this);
    }
}