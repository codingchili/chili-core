package Configuration;

import java.io.IOException;

/**
 * @author Robin Duda
 *         Generates new tokens and optionally secrets and writes them to the configuration files.
 */
public class GenerateTokens {
    private FileConfiguration configuration = (FileConfiguration) FileConfiguration.instance();

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
