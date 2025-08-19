package com.mathworks.bat.trupload.controller;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.mathworks.bat.trupload.exception.TRWSException;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.exception.HystrixRuntimeException.FailureType;

/**
 * Global exception handler for the controllers.
 */
@ControllerAdvice(value = "com.mathworks.bat.trupload.controller")
public class ExceptionHandlerController {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlerController.class);

    @ExceptionHandler(value = { DataRetrievalFailureException.class, DataIntegrityViolationException.class})
    public ResponseEntity<?> handleNonRetryableQueryException(
    		DataAccessException exception) {
        LOG.error("Data Access Exception", exception);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }

    /**
     * Method to handle non-retryable related exceptions.
     *
     * @param exception
     *            exception
     * @return TRWSException
     */
    @ExceptionHandler(value = { TRWSException.class })
    public ResponseEntity<?> handleNonRetryableApplicationException(
    		TRWSException exception) {
        LOG.error("TRUpload Web Service Exception", exception);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }

    @ExceptionHandler(value = { HystrixRuntimeException.class })
    public ResponseEntity<Throwable> handleHystrixEception(
        HystrixRuntimeException exception) {
        if (FailureType.COMMAND_EXCEPTION.equals(exception.getFailureType())
            || FailureType.BAD_REQUEST_EXCEPTION
                .equals(exception.getFailureType())) {
            LOG.error("Unhandled Exception: ", exception.getCause());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getCause());

        }
        LOG.error("Hystrix Exception", exception);
        return serviceUnavailable(exception);
    }

    /**
     * Method to send HTTP 'Retry-After' header and status code in case of
     * circuit breaker and timeout.
     *
     * @param exception
     *
     * @return ResponseEntity
     */
    private ResponseEntity<Throwable> serviceUnavailable(Throwable exception) {
        DynamicStringProperty retry = DynamicPropertyFactory.getInstance()
            .getStringProperty("retry.after.time.in.seconds", "-1");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .header(HttpHeaders.RETRY_AFTER, retry.get())
            .body(exception);
    }

    /**
     * Method to handle SQL related exceptions.
     *
     * @param exception
     *            exception
     * @return ResponseEntity
     */

    @ExceptionHandler(value = { SQLException.class })
    public ResponseEntity<?> handleSQLException(SQLException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(exception.getMessage());
    }
}