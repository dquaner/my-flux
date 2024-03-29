package org.xxts.reactor.core.publisher;


import org.xxts.reactivestreams.Subscriber;
import org.xxts.reactivestreams.Subscription;
import org.xxts.reactor.core.CoreSubscriber;
import org.xxts.reactor.core.Disposable;
import org.xxts.reactor.util.context.*;

import java.util.function.Function;
import java.util.function.LongConsumer;

/**
 * Wrapper API around a downstream Subscriber for emitting any number of
 * next signals followed by zero or one onError/onComplete.
 * <p>
 * @param <T> the value type
 */
public interface FluxSink<T> {

    /**
     * Emit a non-null element, generating an {@link Subscriber#onNext(Object) onNext} signal.
     * <p>
     * Might throw an unchecked exception in case of a fatal error downstream which cannot
     * be propagated to any asynchronous handler (aka a bubbling exception).
     *
     * @param t the value to emit, not null
     * @return this sink for chaining further signals
     **/
    FluxSink<T> next(T t);

    /**
     * Terminate the sequence successfully, generating an {@link Subscriber#onComplete() onComplete}
     * signal.
     *
     * @see Subscriber#onComplete()
     */
    void complete();

    /**
     * Fail the sequence, generating an {@link Subscriber#onError(Throwable) onError}
     * signal.
     *
     * @param e the exception to signal, not null
     * @see Subscriber#onError(Throwable)
     */
    void error(Throwable e);

    /**
     * Return the current subscriber {@link Context}.
     * <p>
     *   {@link Context} can be enriched via {@link Flux#contextWrite(Function)}
     *   operator or directly by a child subscriber overriding
     *   {@link CoreSubscriber#currentContext()}
     *
     * @deprecated To be removed in 3.6.0 at the earliest. Prefer using #contextView() instead.
     */
    @Deprecated
    Context currentContext();

    /**
     * Return the current subscriber's context as a {@link ContextView} for inspection.
     * <p>
     * {@link Context} can be enriched downstream via {@link Flux#contextWrite(Function)}
     * operator or directly by a child subscriber overriding {@link CoreSubscriber#currentContext()}.
     *
     * @return the current subscriber {@link ContextView}.
     */
    default ContextView contextView() {
        return currentContext();
    }


    /**
     * The current outstanding request amount.
     * @return the current outstanding request amount
     */
    long requestedFromDownstream();

    /**
     * Returns true if the downstream cancelled the sequence.
     * @return true if the downstream cancelled the sequence
     */
    boolean isCancelled();

    /**
     * Attaches a {@link LongConsumer} to this {@link FluxSink} that will be notified of
     * any request to this sink.
     * <p>
     * For push/pull sinks created using {@link Flux#create(java.util.function.Consumer)}
     * or {@link Flux#create(java.util.function.Consumer, FluxSink.OverflowStrategy)},
     * the consumer
     * is invoked for every request to enable a hybrid backpressure-enabled push/pull model.
     * <p>
     * <strong>Note:</strong> in case of multiple {@link Subscription#request} happening
     * concurrently to this method, the first consumer invocation may process
     * accumulated demand instead of being called multiple times.
     * <p>
     * When bridging with asynchronous listener-based APIs, the {@code onRequest} callback
     * may be used to request more data from source if required and to manage backpressure
     * by delivering data to sink only when requests are pending.
     * <p>
     * For push-only sinks created using {@link Flux#push(java.util.function.Consumer)}
     * or {@link Flux#push(java.util.function.Consumer, FluxSink.OverflowStrategy)},
     * the consumer is invoked with an initial request of {@code Long.MAX_VALUE} when this method
     * is invoked.
     *
     * @param consumer the consumer to invoke on each request
     * @return {@link FluxSink} with a consumer that is notified of requests
     */
    FluxSink<T> onRequest(LongConsumer consumer);

    /**
     * Attach a {@link Disposable} as a callback for when this {@link FluxSink} is
     * cancelled. At most one callback can be registered, and subsequent calls to this method
     * will result in the immediate disposal of the extraneous {@link Disposable}.
     * <p>
     * The callback is only relevant when the downstream {@link Subscription} is {@link Subscription#cancel() cancelled}.
     *
     * @param d the {@link Disposable} to use as a callback
     * @return the {@link FluxSink} with a cancellation callback
     * @see #onCancel(Disposable) onDispose(Disposable) for a callback that covers cancellation AND terminal signals
     */
    FluxSink<T> onCancel(Disposable d);

    /**
     * Attach a {@link Disposable} as a callback for when this {@link FluxSink} is effectively
     * disposed, that is it cannot be used anymore. This includes both having played terminal
     * signals (onComplete, onError) and having been cancelled (see {@link #onCancel(Disposable)}).
     * At most one callback can be registered, and subsequent calls to this method will result in
     * the immediate disposal of the extraneous {@link Disposable}.
     * <p>
     * Note that the "dispose" term is used from the perspective of the sink. Not to
     * be confused with {@link Flux#subscribe()}'s {@link Disposable#dispose()} method, which
     * maps to disposing the {@link Subscription} (effectively, a {@link Subscription#cancel()}
     * signal).
     *
     * @param d the {@link Disposable} to use as a callback
     * @return the {@link FluxSink} with a callback invoked on any terminal signal or on cancellation
     * @see #onCancel(Disposable) onCancel(Disposable) for a cancellation-only callback
     */
    FluxSink<T> onDispose(Disposable d);

    /**
     * Enumeration for backpressure handling.
     */
    enum OverflowStrategy {
        /**
         * Completely ignore downstream backpressure requests.
         * <p>
         * This may yield {@link IllegalStateException} when queues get full downstream.
         */
        IGNORE,
        /**
         * Signal an {@link IllegalStateException} when the downstream can't keep up
         */
        ERROR,
        /**
         * Drop the incoming signal if the downstream is not ready to receive it.
         */
        DROP,
        /**
         * Downstream will get only the latest signals from upstream.
         */
        LATEST,
        /**
         * Buffer all signals if the downstream can't keep up.
         * <p>
         * Warning! This does unbounded buffering and may lead to {@link OutOfMemoryError}.
         */
        BUFFER
    }

}
