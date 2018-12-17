package com.codingchili.core.context;

import io.vertx.core.Future;

import com.codingchili.core.benchmarking.CoreBenchmarkSuite;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.AuthenticationGenerator;

import java.util.*;
import java.util.function.Function;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * Command executor that registers launch commands to a CommandExecutor.
 */
public class LauncherCommandExecutor extends DefaultCommandExecutor {

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

        super.add((executor) -> CommandResult.CONTINUE, DEPLOY, getDeployDescription());
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
            return CommandResult.SHUTDOWN;
        }, name, description);
    }

    @Override
    public Optional<String> getCommand() {
        if (super.getCommand().isPresent()) {
            return super.getCommand();
        } else {
            return Optional.of(ID_DEFAULT);
        }
    }

    private void help() {
        int align = 24;

        for (String line : getCommandExecutorText()) {
            logger.log(line, Level.STARTUP);
        }

        for (Command command : commands.values()) {
            int space = align - command.getName().length();

            if (command.isVisible()) {
                logger.log("\t\t" + command.getName() +
                        String.join("", Collections.nCopies(space, " ")) +
                        command.getDescription(), Level.STARTUP);
            }
        }

        List<BlockRow> blocks = new ArrayList<>();
        logger.log("\n\t\t" + CoreStrings.CONFIGURED_BLOCKS + "\t\t" + getRemotesAvailable(), Level.WARNING);
        settings.getBlocks().keySet()
                .forEach(block -> {
                    BlockRow row = new BlockRow(block);
                    settings.getHosts().entrySet().stream()
                            .filter(entry -> entry.getValue().equals(block))
                            .map(Map.Entry::getKey)
                            .forEach(row.remotes::add);
                    blocks.add(row);
                });
        blocks.forEach(block -> logger.log(block.toString(), Level.SPECIAL));
    }

    private class BlockRow {
        public final String block;
        final List<String> remotes = new ArrayList<>();

        BlockRow(String block) {
            this.block = block;
        }

        @Override
        public String toString() {
            int align = 36;
            String string = "\t\t" + block + String.join("",
                    Collections.nCopies(align - block.length(), " "));

            for (int i = 0; i < remotes.size(); i++) {
                if (i == 0)
                    string += "[";

                string += remotes.get(i);

                if (i < remotes.size() - 1)
                    string += ", ";
                if (i == remotes.size() - 1)
                    string += "]";
            }

            return string;
        }
    }
}
