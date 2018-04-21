package com.codingchili.core.security;

import com.codingchili.core.files.Configurations;

/**
 * @author Robin Duda
 * <p>
 * Contains default argon2 parameters for password hashing.
 * These can be configured in the {@link Configurations#security()}.
 * <p>
 * Please note that these settings will only be used when creating
 * new hashes, for verifying old hashes the parameters encoded in the
 * hash will be used instead.
 * <p>
 * These defaults are tuned to hash/verify in approximately 10ms on a
 * recent desktop CPU. The parameters must be tuned to incur the most
 * affordable time penalty for maximum security.
 */
public class ArgonSettings {
    private int iterations = 1;
    private int memory = 8192;
    private int parallelism = 4;
    private int hashLength = 32;
    private int saltLength = 16;

    /**
     * @return the number of iterations to perform when hashing.
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * @param iterations the number of iterations to perform when hashing.
     *                   More iterations requires more processing power to verify.
     */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    /**
     * @return the memory requirements for hashing in kibibytes.
     */
    public int getMemory() {
        return memory;
    }

    /**
     * @param memory the memory requirements for hashing in kibibytes, more memory
     *               means that ASICs will perform worse. At least while
     *               memory is expensive.
     */
    public void setMemory(int memory) {
        this.memory = memory;
    }

    /**
     * @return the number of threads to use for computation.
     */
    public int getParallelism() {
        return parallelism;
    }

    /**
     * @param parallelism the number of threads for computation.
     */
    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    /**
     * @return the total length of the generated hash in bytes.
     */
    public int getHashLength() {
        return hashLength;
    }

    /**
     * @param hashLength set the length of the hash in bytes.
     */
    public void setHashLength(int hashLength) {
        this.hashLength = hashLength;
    }

    /**
     * @return the length of the generated salt in bytes.
     */
    public int getSaltLength() {
        return saltLength;
    }

    /**
     * @param saltLength the length of the generated salt in bytes.
     */
    public void setSaltLength(int saltLength) {
        this.saltLength = saltLength;
    }
}
