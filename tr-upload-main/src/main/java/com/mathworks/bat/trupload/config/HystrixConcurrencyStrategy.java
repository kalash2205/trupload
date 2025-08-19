package com.mathworks.bat.trupload.config;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.ThreadContext;

import com.mathworks.bat.common.reqctx.RequestContext;

public class HystrixConcurrencyStrategy extends com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy {

    @Override
    public <K> Callable<K> wrapCallable(Callable<K> callable) {
        return new TRWSHystrixContextCallable<K>(callable);
    }
}

/**
 * This class is used to copy parent's context data to the child's in-order to pass thread specific
 * information while executing in Hystrix thread pool.
 * @param <K> Generic object type
 */
class TRWSHystrixContextCallable<K> implements Callable<K> {

    private final Callable<K> actual;
    private final RequestContext parentContext;

    /**
     * Constructor
     *
     * @param actual Callback
     */
    public TRWSHystrixContextCallable(Callable<K> actual) {
        this.actual = actual;
        this.parentContext = RequestContext.get();
    }

    @Override
    public K call() throws Exception {

        // Save current context - Hystrix thread pool context.
        RequestContext childContext = RequestContext.get();
        try {
            // Copy parent's context to Hystrix's threadpool context.
            RequestContext.set(parentContext);
            ThreadContext.put(
                RequestContext.REQUEST_ID, RequestContext.get().getRequestId()
            );

            return this.actual.call();
        } finally {
            // Set Hystrix thread pool context back.
            RequestContext.set(childContext);
        }
    }
}
