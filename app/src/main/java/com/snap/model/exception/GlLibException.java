package com.snap.model.exception;

/**
 * Root for all checked errors from GL lib.
 */
public class GlLibException extends Exception {

    public GlLibException(String message) {
        super(message);
    }

    public GlLibException(String message, Throwable cause) {
        super(message, cause);
    }

    public GlLibException(Throwable cause) {
        super(cause);
    }
}
