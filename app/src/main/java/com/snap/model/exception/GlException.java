package com.snap.model.exception;

/**
 * Wrapper for native OpenGl errors.
 */
public class GlException extends GlLibException {

    public GlException(String message) {
        super(message);
    }

    public GlException(String message, Throwable cause) {
        super(message, cause);
    }

    public GlException(Throwable cause) {
        super(cause);
    }
}
