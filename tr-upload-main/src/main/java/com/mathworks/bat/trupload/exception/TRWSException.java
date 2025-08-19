package com.mathworks.bat.trupload.exception;

/**
 * Encapsulates any exception thrown by any API contained by the Service.
 */
public class TRWSException extends Exception {

    private boolean retryable;

    /**
     * Constructor.
     *
     * @param message
     *            message
     */
    public TRWSException(String message) {
        this(message, false);
    }
    
    @Override
    public String toString() {
        return getMessage();
    }

    /**
     * Constructor.
     *
     * @param exc
     *            exception
     */
    public TRWSException(Throwable exc) {
        this(exc, false, null);
    }

    /**
     * Constructor.
     *
     * @param message
     *            message
     * @param cause
     *            cause of the exception
     */
    public TRWSException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     *
     * @param exc
     *            exception
     * @param retryable
     *            retryable flag
     */
    public TRWSException(Throwable exc, boolean retryable) {
        this(exc, retryable, null);
    }

    /**
     * Constructor.
     *
     * @param message
     *            message
     * @param retryable
     *            retryable flag
     */
    public TRWSException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    /**
     * Constructor.
     *
     * @param exc
     *            exception
     * @param retryable
     *            retryable flag
     * @param errorCode
     *            errorCode
     */
    public TRWSException(Throwable exc, boolean retryable, String errorCode) {
        super(exc);
        this.retryable = retryable;
    }

    /**
     * Gets the retryable value for this PerforceException.
     *
     * @return retryable
     */

    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Sets the retryable value for this PerforceException.
     *
     * @param retryable
     */
    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }
}