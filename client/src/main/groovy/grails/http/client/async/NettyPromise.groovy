package grails.http.client.async

import grails.async.Promise
import grails.async.Promises
import groovy.transform.CompileStatic
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener

import java.util.concurrent.TimeUnit

/**
 * Abstraction around Netty promise API
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class NettyPromise<T> implements Promise<T>, java.util.concurrent.Future<T> {

    final io.netty.util.concurrent.Promise<T> nettyPromise

    NettyPromise(io.netty.util.concurrent.Promise<T> nettyPromise) {
        this.nettyPromise = nettyPromise
    }

    @Override
    boolean cancel(boolean mayInterruptIfRunning) {
        return nettyPromise.cancel(mayInterruptIfRunning)
    }

    @Override
    boolean isCancelled() {
        return nettyPromise.isCancelled()
    }

    @Override
    boolean isDone() {
        return nettyPromise.isDone()
    }

    @Override
    T get() throws Throwable {
        nettyPromise.await()
        return nettyPromise.now
    }

    @Override
    T get(long timeout, TimeUnit units) throws Throwable {
        nettyPromise.await(timeout, units)
        return nettyPromise.now
    }

    @Override
    Promise<T> accept(T value) {
        nettyPromise.setSuccess(value)
        return this
    }

    @Override
    Promise<T> onComplete(Closure callable) {
        callable = Promises.promiseFactory.applyDecorators(callable, null)
        nettyPromise.addListener(new GenericFutureListener() {
            void operationComplete(Future future) throws Exception {
                if(future.isSuccess()) {
                    callable.call(future.now)
                }
            }
        })
        return this
    }

    @Override
    Promise<T> onError(Closure callable) {
        callable = Promises.promiseFactory.applyDecorators(callable, null)
        nettyPromise.addListener(new GenericFutureListener<Future<? super T>>() {
            @Override
            void operationComplete(Future<? super T> future) throws Exception {
                if(!future.isSuccess()) {
                    callable.call(future.cause())
                }
            }
        })
        return this
    }

    @Override
    Promise<T> then(Closure callable) {
        return onComplete(callable)
    }
}
