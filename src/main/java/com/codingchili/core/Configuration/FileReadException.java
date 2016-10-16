package com.codingchili.core.Configuration;

/**
 * @author Robin Duda
 */
class FileReadException extends RuntimeException {

    FileReadException(String path) {
        super(Strings.getFileReadError(path));
    }

}
