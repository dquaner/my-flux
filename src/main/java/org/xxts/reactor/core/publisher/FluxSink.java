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
 * <br>
 * 下游 Subscriber 的 Wrapper API，用于发出任意数量的 next 信号，后面跟着 0 或 1 个 onError/onComplete 信号。
 * <p>
 * @param <T> the value type
 */
public interface FluxSink<T> {

    /**
     * Emit a non-null element, generating an {@link Subscriber#onNext(Object) onNext} signal.
     * <br> 发出一个非空元素，生成一个 {@link Subscriber#onNext(Object) onNext} 信号。
     * <p>
     * Might throw an unchecked exception in case of a fatal error downstream which cannot
     * be propagated to any asynchronous handler (aka a bubbling exception).
     * <br> 可能在下游发生致命错误时抛出未检查异常，该错误无法传播到任何异步处理程序（也称为冒泡异常）。
     *
     * @see Subscriber#onNext(Object)
     * @see org.xxts.reactor.core.Exceptions#throwIfFatal(Throwable)
     *
     * @param t the value to emit, not null
     * @return this sink for chaining further signals
     **/
    FluxSink<T> next(T t);

    /**
     * Terminate the sequence successfully, generating an {@link Subscriber#onComplete() onComplete}
     * signal.
     * <br> 成功终止序列，生成一个 {@link Subscriber#onComplete() onComplete} 信号。
     *
     * @see Subscriber#onComplete()
     */
    void complete();

    /**
     * Fail the sequence, generating an {@link Subscriber#onError(Throwable) onError}
     * signal.
     * <br> 失败序列，生成一个 {@link Subscriber#onError(Throwable)} 信号。
     *
     * @param e the exception to signal, not null
     * @see Subscriber#onError(Throwable)
     */
    void error(Throwable e);

    /**
     * Return the current subscriber's context as a {@link ContextView} for inspection.
     * <br> 将当前订阅者的上下文作为 {@link ContextView} 返回以供检查。
     * <p>
     * {@link Context} can be enriched downstream via {@link Flux#contextWrite(Function)}
     * operator or directly by a child subscriber overriding {@link CoreSubscriber#currentContext()}.
     * <br> {@link Context} 可以在下游通过 {@link Flux#contextWrite(Function)} 操作符或
     * 直接通过重写了 {@link CoreSubscriber#currentContext()} 的子订阅者来更新。
     *
     * @return the current subscriber {@link ContextView}.
     */
    ContextView contextView();

    /**
     * The current outstanding request amount.
     * <br> 当前未完成的请求数。
     *
     * @return the current outstanding request amount
     */
    long requestedFromDownstream();

    /**
     * Returns true if the downstream cancelled the sequence.
     * <br> 如果下游取消了该序列，则返回 true。
     *
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
     * <p>
     * 将一个 {@link LongConsumer} 附加到当前 {@link FluxSink}，当有任何请求到当前 sink 时，这个 {@link LongConsumer} 将被通知。
     * <p>
     * For push/pull sinks：<br>
     * （使用 {@link Flux#create(java.util.function.Consumer)} 或 {@link Flux#create(java.util.function.Consumer, FluxSink.OverflowStrategy)} 创建）
     *      <br>每个请求都会调用这个 consumer 来启用 hybrid backpressure-enabled push/pull 模型。
     * <p>
     *      <strong>注意：</strong>
     *      如果此方法同时发生了多次 {@link Subscription#request}，第一个 consumer 调用可能会处理累积的需求，而不是被多次调用。
     * <p>
     *      当与基于异步监听器的 API 进行桥接时，该 {@code onRequest} 回调可用于在需要时从源请求更多的数据，并通过将数据传递给 sink 来管理背压（仅当请求被挂起时）。
     * <p>
     * For push-only sinks：<br>
     * （使用 {@link Flux#push(java.util.function.Consumer)} 或 {@link Flux#push(java.util.function.Consumer, FluxSink.OverflowStrategy)} 创建）
     *      <br>当该方法被调用时，有一个初始的 {@code Long.MAX_VALUE} 请求来调用这个 consumer。
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
     * <p>
     * Attach a {@link Disposable} 作为取消此 {@link FluxSink} 时的回调。
     * 最多可以注册一个回调，后续再次调用此方法的 {@link Disposable} 将立即被处理掉。
     * <p>
     * 该回调仅在下游 {@link Subscription} 被 {@link Subscription#cancel() 取消} 时起作用。
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
     * <br>
     * 注意，这里的 “dispose” 是在 sink 的角度使用的，
     * 不要把它和 {@link Flux#subscribe()} 的 {@link Disposable#dispose()} 方法混淆了
     * （它处置 {@link Subscription}，实际上是 {@link Subscription#cancel()} 信号）。
     *
     * @param d the {@link Disposable} to use as a callback
     * @return the {@link FluxSink} with a callback invoked on any terminal signal or on cancellation
     * @see #onCancel(Disposable) onCancel(Disposable) for a cancellation-only callback
     */
    FluxSink<T> onDispose(Disposable d);

    /**
     * Enumeration for backpressure handling.
     * <br> 用于背压处理枚举类。
     */
    enum OverflowStrategy {
        /**
         * Completely ignore downstream backpressure requests.
         * <br> 完全忽略下游的背压请求。
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
