package com.mathworks.bat.trupload.util;

/** Exception thrown by the BrcReader if there were any problems accessing the BRC values */
public class BrcReaderException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BrcReaderException(final Throwable cause) {
        super(cause);
    }
}