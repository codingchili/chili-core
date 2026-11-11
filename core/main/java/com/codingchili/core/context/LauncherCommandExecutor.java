package com.codingchili.core.context;

import io.vertx.core.Future;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

import com.codingchili.core.benchmarking.CoreBenchmarkSuite;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.AuthenticationGenerator;
import com.codingchili.core.security.NodeKey;
import com.codingchili.core.security.SecretBaker;
import org.jline.jansi.Ansi;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Command executor that registers launch commands to a CommandExecutor.
 */
public class LauncherCommandExecutor extends DefaultCommandExecutor {
    private Ansi ansi = Ansi.ansi();

    /**
     * constructs an instance with the default logger
     */
    public LauncherCommandExecutor() {
        this(new StringLogger(LauncherCommandExecutor.class));
    }

    /**
     * @param logger constructs an instance with specified logger
     */
    public LauncherCommandExecutor(Logger logger) {
        super(logger);
        registerCommands();
    }

    private void registerCommands() {
        CoreBenchmarkSuite suite = new CoreBenchmarkSuite();

        super.add((executor) -> LauncherCommandResult.CONTINUE, DEPLOY, getDeployDescription());
        add(Configurations::reset, RECONFIGURE, getReconfigureDescription());

        generator(AuthenticationGenerator::preshare, GENERATE_PRESHARED, getGeneratePresharedDescription());
        generator(AuthenticationGenerator::secrets, GENERATE_SECRETS, getGenerateSecretsDescription());
        generator(AuthenticationGenerator::tokens, GENERATE_TOKENS, getGenerateTokensDescription());
        generator(AuthenticationGenerator::all, GENERATE, getGenerateAllDescription());

        super.addAsync(this::keygen, KEYGEN, getKeygenDescription());
        super.addAsync(this::bake, BAKE, getBakeDescription());

        addAsync(suite::execute, BENCHMARK, getBenchmarkDescription());
        add(this::help, HELP, getCommandExecutorHelpDescription());
    }

    // generate [--rotate]: keeps existing values unless rotating, see AuthenticationGenerator.Mode.
    // completes when the files are written and the context of the generator is closed: a failure fails the
    // command, which makes the launcher exit with a non-zero status.
    private void generator(Function<AuthenticationGenerator, Future<Void>> function, String name, String description) {
        super.addAsync((executor) -> {
            var mode = executor.hasProperty(GENERATE_ROTATE) ?
                    AuthenticationGenerator.Mode.ROTATE : AuthenticationGenerator.Mode.FILL;
            Future<Void> generated;
            CoreContext core = new SystemContext();
            try {
                var generator = new AuthenticationGenerator(core).setMode(mode)
                        .setStore(executor.getProperty(GENERATE_STORE).map(Paths::get).orElse(null))
                        .setNodes(executor.getProperty(GENERATE_NODES).map(Paths::get).orElse(null))
                        .setEncrypt(executor.hasProperty(GENERATE_ENCRYPT));
                generated = function.apply(generator);
            } catch (Throwable e) {
                generated = Future.failedFuture(e);
            }
            return generated.transform(done -> core.close()
                    .transform(closed -> done.succeeded() ?
                            Future.succeededFuture(LauncherCommandResult.SHUTDOWN) :
                            Future.<CommandResult>failedFuture(done.cause())));
        }, name, description);
    }

    // keygen [--force]: a new key for this node, prints its public key.
    private Future<CommandResult> keygen(CommandExecutor executor) {
        try {
            Path path = NodeKey.path();
            NodeKey key = NodeKey.generate();
            key.write(path, executor.hasProperty(KEYGEN_FORCE));
            logger.log(getNodeKeyWritten(path.toString()));
            // alone on its line for scripts and copying.
            System.out.println(key.publicKey());
            return Future.succeededFuture(LauncherCommandResult.SHUTDOWN);
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }

    // bake [--store <dir>] [--out <dir>] [--blocks a,b]: the configuration with the secrets of a store.
    private Future<CommandResult> bake(CommandExecutor executor) {
        try {
            new SecretBaker(Configurations.launcher().getConfigurationDirectory())
                    .setStore(executor.getProperty(GENERATE_STORE).map(Paths::get).orElse(null))
                    .setOut(Paths.get(executor.getProperty(BAKE_OUT).orElse(BAKE_OUT_DEFAULT)))
                    .setBlocks(executor.hasProperty(BAKE_BLOCKS) ? executor.getAllProperties(BAKE_BLOCKS) : null)
                    .bake();
            return Future.succeededFuture(LauncherCommandResult.SHUTDOWN);
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }

    /* helper method to support methods that does not implement Consumer<CommandExecutor> */
    private void add(Runnable runnable, String name, String description) {
        super.add((executor) -> {
            runnable.run();
            return LauncherCommandResult.SHUTDOWN;
        }, name, description);
    }

    @Override
    public Optional<String> getCommand() {
        if (super.getCommand().isPresent()) {
            return super.getCommand();
        } else {
            return Optional.empty();
        }
    }

    private void printCommand(String text, String description, int align) {
        ansi.fgBright(Ansi.Color.CYAN)
                .a(String.format("\t%s", pad(text, align)))
                .reset()
                .a(description)
                .newline();
    }

    private void printExecutorHelpText(int align) {
        ansi.fgBright(Ansi.Color.GREEN)
                .a(" HELP ")
                .newline()
                .reset();

        printCommand("<block-name>", "deploys the services configured in the given block.", align);
        printCommand("<remote-name>", "deploys configured blocks on a remote host.", align);
    }

    private void help() {
        final int DEFAULT_SPACING = 18;
        final int COMMAND_PADDING = 4;

        // find the longest command and add some padding to it.
        int align = commands.values().stream()
                .map(command -> command.getName().length() + COMMAND_PADDING)
                .reduce(Math::max)
                .orElse(DEFAULT_SPACING);

        printExecutorHelpText(align);

        for (Command command : commands.values()) {
            if (command.isVisible()) {
                printCommand(command.getName(), command.getDescription(), align);
            }
        }

        List<BlockRow> blocks = new ArrayList<>();
        ansi.a(String.format("\n\t%s\n\n", CONFIGURED_BLOCKS));

        settings.getBlocks().keySet()
                .forEach(block -> {
                    BlockRow row = new BlockRow(block);
                    settings.getHosts().entrySet().stream()
                            .filter(entry -> entry.getValue().equals(block))
                            .map(Map.Entry::getKey)
                            .forEach(row.remotes::add);
                    blocks.add(row);
                });
        blocks.forEach(block -> block.toAnsi(ansi, align));
        logger.log(ansi.toString());
    }

    private class BlockRow {
        public final String block;
        final List<String> remotes = new ArrayList<>();

        BlockRow(String block) {
            this.block = block;
        }

        public void toAnsi(Ansi ansi, int align) {
            ansi.fgBright(Ansi.Color.CYAN)
                    .a(pad("\t" + block, align))
                    .reset();

            for (int i = 0; i < remotes.size(); i++) {
                if (i == 0)
                    ansi.a("[");

                ansi.fgBright(Ansi.Color.CYAN)
                        .a(remotes.get(i))
                        .reset();

                if (i < remotes.size() - 1)
                    ansi.a(", ");
                if (i == remotes.size() - 1)
                    ansi.a("]");
            }
            ansi.newline();
        }
    }
}
