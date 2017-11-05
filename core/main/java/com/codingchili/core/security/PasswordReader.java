package com.codingchili.core.security;

import java.io.Console;
import java.util.Scanner;

/**
 * Reads passwords.
 */
public class PasswordReader {

    /**
     * Reads a password input from the console. Uses input masking if possible.
     * @param prompt the text to display when requesting for input.
     * @return the read line.
     */
    public static String fromConsole(String prompt) {
        Console console = System.console();
        char[] password;
        if (console == null) {
            System.out.print(prompt);
            System.out.flush();
            password = new Scanner(System.in).nextLine().toCharArray();
        } else {
            password = console.readPassword(prompt);
        }
        return new String(password);
    }

}
