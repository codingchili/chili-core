package com.codingchili.core.security;

/**
 * @author Robin Duda
 *         <p>
 *         Compares byte arrays in constant-time.
 */
public abstract class ByteComparator {

    /**
     * Compares two strings in constant time.
     *
     * @param first  the first string.
     * @param second the second string.
     * @return true if both Strings are of equal size and content.
     */
    public static boolean compare(String first, String second) {
        return compare(first.getBytes(), second.getBytes());
    }

    /**
     * Compares two byte arrays in constant time.
     *
     * @param first  the first array.
     * @param second the second array.
     * @return true if both arrays are of equal size and content.
     */
    static boolean compare(byte[] first, byte[] second) {
        int result = 0;

        if (first.length != second.length)
            return false;

        for (int i = 0; i < first.length; i++)
            result |= (first[i] ^ second[i]);

        return result == 0;
    }

}
