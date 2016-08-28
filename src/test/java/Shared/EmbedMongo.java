package Shared;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.UserTempNaming;

import java.io.IOException;

/**
 * @author Robin Duda
 */
public abstract class EmbedMongo {
    private static MongodProcess process;
    private static MongodExecutable executable;

    public static void start() {
        try {
            Command command = Command.MongoD;
            IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                    .defaults(command)
                    .artifactStore(new ExtractedArtifactStoreBuilder()
                            .defaults(command)
                            .download(new DownloadConfigBuilder()
                                    .defaultsForCommand(command).build())
                            .executableNaming(new UserTempNaming()))
                    .build();

            IMongodConfig config = new MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .net(new Net(27017, false))
                    .build();

            MongodStarter starter = MongodStarter.getInstance(runtimeConfig);
            executable = starter.prepare(config);

            process = executable.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        process.stop();
        executable.stop();
    }
}
