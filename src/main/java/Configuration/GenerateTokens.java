package Configuration;

import java.io.IOException;

/**
 * Created by Robin on 2016-04-28.
 */
public class GenerateTokens {
    private Config configuration = Config.instance();

    public static void main(String[] args) throws IOException {

        if (args.length >= 1 && args[0].equals("regenerate")) {
            new GenerateTokens().regenerate().sign();
        } else {
            new GenerateTokens().sign();
        }
    }

    private GenerateTokens regenerate() {
        System.out.print("Generating secrets..");
        configuration.generateAuthSecret();
        configuration.generateLoggingSecret();
        System.out.println(" Done.");
        return this;
    }

    private GenerateTokens sign() throws IOException {
        System.out.print("Generating tokens..");
        configuration.generateRealmTokens();
        configuration.generateLoggingTokens();
        System.out.println(" Done.");
        return this;
    }
}
