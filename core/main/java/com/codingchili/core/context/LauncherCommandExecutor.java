package com.codingchili.core.context;

import io.vertx.core.Future;
import org.fusesource.jansi.Ansi;

import java.util.*;
import java.util.function.Function;

import com.codingchili.core.benchmarking.CoreBenchmarkSuite;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.AuthenticationGenerator;

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

        super.add((executor) -> {
            executor.getProperty("deploy").ifPresent(x -> {
                System.out.println("DEPLOY IS === " + x);
            });
            return LauncherCommandResult.CONTINUE;
        }, DEPLOY, getDeployDescription());
        add(Configurations::reset, RECONFIGURE, getReconfigureDescription());

        add(generator(AuthenticationGenerator::preshare), GENERATE_PRESHARED, getGeneratePresharedDescription());
        add(generator(AuthenticationGenerator::secrets), GENERATE_SECRETS, getGenerateSecretsDescription());
        add(generator(AuthenticationGenerator::tokens), GENERATE_TOKENS, getGenerateTokensDescription());
        add(generator(AuthenticationGenerator::all), GENERATE, getGenerateAllDescription());

        add(suite::execute, BENCHMARK, getBenchmarkDescription());
        add(this::help, HELP, getCommandExecutorHelpDescription());
    }

    private Runnable generator(Function<AuthenticationGenerator, Future<Void>> function) {
        return () -> {
            CoreContext core = new SystemContext();
            function.apply(new AuthenticationGenerator(core)).setHandler(done -> {
                core.close();
            });
        };
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
