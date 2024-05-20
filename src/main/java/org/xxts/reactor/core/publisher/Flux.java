package org.xxts.reactor.core.publisher;

import org.xxts.reactivestreams.Publisher;
import org.xxts.reactivestreams.Subscriber;
import org.xxts.reactivestreams.Subscription;
import org.xxts.reactor.core.CorePublisher;
import org.xxts.reactor.core.CoreSubscriber;
import org.xxts.reactor.core.Disposable;
import org.xxts.reactor.core.publisher.FluxSink.OverflowStrategy;
import org.xxts.reactor.util.annotation.Nullable;
import org.xxts.reactor.util.context.Context;
import org.xxts.reactor.util.context.ContextView;
import org.xxts.reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Objects;
import java.util.function.*;

public abstract class Flux<T> implements CorePublisher<T> {

    /**
     * Programmatically create a {@link Flux} with the capability of emitting multiple
     * elements in a synchronous or asynchronous manner through the {@link FluxSink} API.
     * This includes emitting elements from multiple threads.
     * <br>
     * 通过编程方式创建一个 {@link Flux}，它可以通过 {@link FluxSink} API 以同步或异步方式发送多个元素，包括从多个线程发出元素。
     * <p>
     * <img class="marble" src="doc-files/marbles/createForFlux.svg" alt="">
     * <p>
     * This Flux factory is useful if one wants to adapt some other multivalued async API
     * and not worry about cancellation and backpressure (which is handled by buffering
     * all signals if the downstream can't keep up).
     * <br>
     * 此 Flux 工厂适用于以下情景：如果你想使用多值的异步的 API，而不担心取消和背压（如果下游无法及时处理，则缓冲所有无法处理的信号）。
     * <p>
     * For example:
     *
     * <pre><code>
     * Flux.&lt;String&gt;create(emitter -&gt; {
     *
     *     ActionListener al = e -&gt; {
     *         emitter.next(textField.getText());
     *     };
     *
     *     // without cleanup support:
     *     button.addActionListener(al);
     *
     *     // with cleanup support:
     *     button.addActionListener(al);
     *     emitter.onDispose(() -> {
     *         button.removeListener(al);
     *     });
     *
     * });
     * </code></pre>
     *
     * <p><strong>Discard Support:</strong> The {@link FluxSink} exposed by this operator buffers in case of
     * overflow. The buffer is discarded when the main sequence is cancelled.
     * <br>
     * <strong>丢弃支持：</strong>此算子暴露的 {@link FluxSink} 在溢出的情况下进行缓冲。当主序列被取消时，缓冲区将被丢弃。
     *
     * @param <T>     The type of values in the sequence
     * @param emitter Consume the {@link FluxSink} provided per-subscriber by Reactor to generate signals.
     * @return a {@link Flux}
     * @see #push(Consumer)
     */
    public static <T> Flux<T> create(Consumer<? super FluxSink<T>> emitter) {
        return create(emitter, OverflowStrategy.BUFFER);
    }

    /**
     * Programmatically create a {@link Flux} with the capability of emitting multiple
     * elements in a synchronous or asynchronous manner through the {@link FluxSink} API.
     * This includes emitting elements from multiple threads.
     * <br>
     * 通过编程的方式创建一个 {@link Flux}，它可以通过 {@link FluxSink} API 以同步或异步的方式发出多个元素，包括从多个线程发出元素。
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/createWithOverflowStrategy.svg" alt="">
     * <p>
     * This Flux factory is useful if one wants to adapt some other multivalued async API
     * and not worry about cancellation and backpressure (which is handled by buffering
     * all signals if the downstream can't keep up).
     * <br>
     * 此 Flux 工厂适用于以下情景：
     * 如果你想使用多值的异步的 API，而不担心取消和背压（如果下游无法及时处理，则缓冲所有无法处理的信号）。
     * <p>
     * For example:
     *
     * <pre><code>
     * Flux.&lt;String&gt;create(emitter -&gt; {
     *
     *     ActionListener al = e -&gt; {
     *         emitter.next(textField.getText());
     *     };
     *
     *     // without cleanup support:
     *     button.addActionListener(al);
     *
     *     // with cleanup support:
     *     button.addActionListener(al);
     *     emitter.onDispose(() -> {
     *         button.removeListener(al);
     *     });
     *
     * }, FluxSink.OverflowStrategy.LATEST);
     * </code></pre>
     *
     * <p><strong>Discard Support:</strong> The {@link FluxSink} exposed by this operator discards elements
     * as relevant to the chosen {@link FluxSink.OverflowStrategy}. For example, the {@link FluxSink.OverflowStrategy#DROP}
     * discards each item as they are being dropped, while {@link FluxSink.OverflowStrategy#BUFFER}
     * will discard the buffer upon cancellation.
     * <br><strong>丢弃支持：</strong> 此算子暴露的 {@link FluxSink} 按照给定的的 {@link FluxSink.OverflowStrategy} 模式
     * 丢弃元素。例如，{@link FluxSink.OverflowStrategy#DROP} 模式丢弃每一个被 dropped 的元素，
     * {@link FluxSink.OverflowStrategy#BUFFER} 模式在主序列取消时丢弃缓冲区中的元素。
     *
     * @param <T>          The type of values in the sequence
     * @param backpressure the backpressure mode, see {@link FluxSink.OverflowStrategy} for the
     *                     available backpressure modes
     * @param emitter      Consume the {@link FluxSink} provided per-subscriber by Reactor to generate signals.
     * @return a {@link Flux}
     * @see #push(Consumer, FluxSink.OverflowStrategy)
     */
    public static <T> Flux<T> create(Consumer<? super FluxSink<T>> emitter, OverflowStrategy backpressure) {
        return onAssembly(new FluxCreate<>(emitter, backpressure, FluxCreate.CreateMode.PUSH_PULL));
    }


    /**
     * Add behavior (side-effect) triggered when the {@link Flux} completes with an error.
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/doOnErrorForFlux.svg" alt="">
     *
     * <p>
     * The {@link Consumer} is executed first, then the onError signal is propagated
     * downstream.
     * <br>
     * 先执行 {@link Consumer}，然后向下游传播 onError 信号。
     *
     * @param onError the callback to call on {@link Subscriber#onError}
     *
     * @return an observed {@link Flux}
     */
    public final Flux<T> doOnError(Consumer<? super Throwable> onError) {
        Objects.requireNonNull(onError, "onError");
        return doOnSignal(this, null, null, onError, null, null, null, null);
    }

    /**
     * Add behavior (side-effect) triggered when the {@link Flux} completes with an error matching the given exception type.
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/doOnErrorWithClassPredicateForFlux.svg" alt="">
     *
     * <p>
     * The {@link Consumer} is executed first, then the onError signal is propagated
     * downstream.
     * <br>
     * 先执行 {@link Consumer}，然后向下游传播 onError 信号。
     *
     *
     * @param exceptionType the type of exceptions to handle
     * @param onError the error handler for each error
     * @param <E> type of the error to handle
     *
     * @return an observed {@link Flux}
     *
     */
    public final <E extends Throwable> Flux<T> doOnError(Class<E> exceptionType,
                                                         final Consumer<? super E> onError) {
        Objects.requireNonNull(exceptionType, "type");
        @SuppressWarnings("unchecked")
        Consumer<Throwable> handler = (Consumer<Throwable>)onError;
        return doOnError(exceptionType::isInstance, (handler));
    }

    /**
     * Add behavior (side-effect) triggered when the {@link Flux} completes with an error matching the given exception.
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/doOnErrorWithPredicateForFlux.svg" alt="">
     *
     * <p>
     * The {@link Consumer} is executed first, then the onError signal is propagated
     * downstream.
     * <br>
     * 先执行 {@link Consumer}，然后向下游传播 onError 信号。
     *
     *
     * @param predicate the matcher for exceptions to handle
     * @param onError the error handler for each error
     *
     * @return an observed {@link Flux}
     *
     */
    public final Flux<T> doOnError(Predicate<? super Throwable> predicate,
                                   final Consumer<? super Throwable> onError) {
        Objects.requireNonNull(predicate, "predicate");
        return doOnError(t -> {
            if (predicate.test(t)) {
                onError.accept(t);
            }
        });
    }

    /**
     * Add behavior (side-effect) triggered when the {@link Flux} emits an item.
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/doOnNextForFlux.svg" alt="">
     *
     * <p>
     * The {@link Consumer} is executed first, then the onNext signal is propagated downstream.
     * <br>
     * 先执行 {@link Consumer}，然后向下游传播 onNext 信号。
     *
     * <p><strong>Error Mode Support:</strong>
     * This operator supports {@link #onErrorContinue(BiConsumer) resuming on errors}
     * (including when fusion is enabled). Exceptions thrown by the consumer are passed to
     * the {@link #onErrorContinue(BiConsumer)} error consumer (the value consumer
     * is not invoked, as the source element will be part of the sequence). The onNext
     * signal is then propagated as normal.
     *
     * @param onNext the callback to call on {@link Subscriber#onNext}
     * @return an observed {@link Flux}
     */
    public final Flux<T> doOnNext(Consumer<? super T> onNext) {
        Objects.requireNonNull(onNext, "onNext");
        return doOnSignal(this, null, onNext, null, null, null, null, null);
    }

    static <T> Flux<T> doOnSignal(Flux<T> source,
                                  @Nullable Consumer<? super Subscription> onSubscribe,
                                  @Nullable Consumer<? super T> onNext,
                                  @Nullable Consumer<? super Throwable> onError,
                                  @Nullable Runnable onComplete,
                                  @Nullable Runnable onAfterTerminate,
                                  @Nullable LongConsumer onRequest,
                                  @Nullable Runnable onCancel) {
        if (source instanceof Fuseable) {
            return onAssembly(new FluxPeekFuseable<>(source,
                    onSubscribe,
                    onNext,
                    onError,
                    onComplete,
                    onAfterTerminate,
                    onRequest,
                    onCancel));
        }
        return onAssembly(new FluxPeek<>(source,
                onSubscribe,
                onNext,
                onError,
                onComplete,
                onAfterTerminate,
                onRequest,
                onCancel));
    }

    /**
     * Enrich the {@link Context} visible from downstream for the benefit of upstream
     * operators, by making all values from the provided {@link ContextView} visible on top
     * of pairs from downstream.
     * <br>
     * 丰富下游传来的 {@link Context} 以备上游算子使用：将给定 {@link ContextView} 提供的所有值加到下游的 {@link Context} 中。
     *
     * <p>
     * A {@link Context} (and its {@link ContextView}) is tied to a given subscription
     * and is read by querying the downstream {@link Subscriber}. {@link Subscriber} that
     * don't enrich the context instead access their own downstream's context. As a result,
     * this operator conceptually enriches a {@link Context} coming from under it in the chain
     * (downstream, by default an empty one) and makes the new enriched {@link Context}
     * visible to operators above it in the chain.
     * <br>
     * {@link Context}（及其 {@link ContextView}）被绑定到一个给定的 subscription 上，并可以通过查询下游 {@link Subscriber} 来读取。
     * 没有 enrich context 的 {@link Subscriber} 会访问自己下游的 context。因此，从概念上说，这个算子 enrich 了链中
     * 位于它下面的 {@link Context}（下游，默认情况下是空的），并使新的 enrich 过的 {@link Context} 对链中位于它上面的算子可见。
     *
     * @param contextToAppend the {@link ContextView} to merge with the downstream {@link Context},
     *                        resulting in a new more complete {@link Context} that will be visible from upstream.
     * @return a contextualized {@link Flux}
     * @see ContextView
     */
    public final Flux<T> contextWrite(ContextView contextToAppend) {
        return contextWrite(c -> c.putAll(contextToAppend));
    }

    /**
     * Enrich the {@link Context} visible from downstream for the benefit of upstream
     * operators, by applying a {@link Function} to the downstream {@link Context}.
     * <br>
     * 丰富下游传来的 {@link Context} 以备上游算子使用：对下游 {@link Context} 应用 {@link Function}。
     *
     * <p>
     * The {@link Function} takes a {@link Context} for convenience, allowing to easily
     * call {@link Context#put(Object, Object) write APIs} to return a new {@link Context}.
     * <br>
     * 为方便起见，该 {@link Function} 接受一个 {@link Context}，允许轻松调用 {@link Context#put(Object, Object) write APIs}
     * 来返回一个新的 {@link Context}。
     *
     * <p>
     * A {@link Context} (and its {@link ContextView}) is tied to a given subscription
     * and is read by querying the downstream {@link Subscriber}. {@link Subscriber} that
     * don't enrich the context instead access their own downstream's context. As a result,
     * this operator conceptually enriches a {@link Context} coming from under it in the chain
     * (downstream, by default an empty one) and makes the new enriched {@link Context}
     * visible to operators above it in the chain.
     * <br>
     * {@link Context}（及其 {@link ContextView}）被绑定到一个给定的 subscription 上，并可以通过查询下游 {@link Subscriber} 来读取。
     * 没有 enrich context 的 {@link Subscriber} 会访问自己下游的 context。因此，从概念上说，这个算子 enrich 了链中
     * 位于它下面的 {@link Context}（下游，默认情况下是空的），并使新的 enrich 过的 {@link Context} 对链中位于它上面的算子可见。
     *
     * @param contextModifier the {@link Function} to apply to the downstream {@link Context},
     *                        resulting in a new more complete {@link Context} that will be visible from upstream.
     * @return a contextualized {@link reactor.core.publisher.Flux}
     * @see Context
     */
    public final Flux<T> contextWrite(Function<Context, Context> contextModifier) {
        if (ContextPropagationSupport.shouldPropagateContextToThreadLocals()) {
            return onAssembly(new FluxContextWriteRestoringThreadLocals<>(
                    this, contextModifier
            ));
        }
        return onAssembly(new FluxContextWrite<>(this, contextModifier));
    }

    /**
     * Programmatically create a {@link Flux} with the capability of emitting multiple
     * elements from a single-threaded producer through the {@link FluxSink} API. For
     * a multi-threaded capable alternative, see {@link #create(Consumer)}.
     * <br>
     * 通过编程的方式创建一个 {@link Flux}，它可以通过 {@link FluxSink} API 从单线程的 producer 发出多个元素。
     * 支持多线程的方法，见 {@link #create(Consumer)}。
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/push.svg" alt="">
     *
     * <p>
     * This Flux factory is useful if one wants to adapt some other single-threaded
     * multivalued async API and not worry about cancellation and backpressure (which is
     * handled by buffering all signals if the downstream can't keep up).
     * <br>
     * 此 Flux 工厂适用于以下情景：如果你想使用单线程多值异步 API，而不担心取消和背压（如果下游无法及时处理，则缓冲所有无法处理的信号）。
     *
     * <p>
     * For example:
     *
     * <pre><code>
     * Flux.&lt;String&gt;push(emitter -&gt; {
     *
     * 	 ActionListener al = e -&gt; {
     * 		 emitter.next(textField.getText());
     *     };
     *
     * 	 // without cleanup support:
     * 	 button.addActionListener(al);
     *
     * 	 // with cleanup support:
     * 	 button.addActionListener(al);
     * 	 emitter.onDispose(() -> {
     * 		 button.removeListener(al);
     *     });
     *
     * });
     * </code></pre>
     *
     * <p><strong>Discard Support:</strong> The {@link FluxSink} exposed by this operator buffers in case of
     * overflow. The buffer is discarded when the main sequence is cancelled.
     * <br>
     * <strong>丢弃支持：</strong>此算子暴露的 {@link FluxSink} 在溢出的情况下进行缓冲。当主序列被取消时，缓冲区将被丢弃。
     *
     * @param <T>     The type of values in the sequence
     * @param emitter Consume the {@link FluxSink} provided per-subscriber by Reactor to generate signals.
     * @return a {@link Flux}
     * @see #create(Consumer)
     */
    public static <T> Flux<T> push(Consumer<? super FluxSink<T>> emitter) {
        return push(emitter, OverflowStrategy.BUFFER);
    }

    /**
     * Programmatically create a {@link Flux} with the capability of emitting multiple
     * elements from a single-threaded producer through the {@link FluxSink} API. For
     * a multi-threaded capable alternative, see {@link #create(Consumer, FluxSink.OverflowStrategy)}.
     * <br>
     * 通过编程的方式创建一个 Flux，它可以通过 FluxSink API 从单线程的 producer 发出多个元素。
     * 支持多线程的方法，见 {@link #create(Consumer, FluxSink.OverflowStrategy)}。
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/pushWithOverflowStrategy.svg" alt="">
     *
     * <p>
     * This Flux factory is useful if one wants to adapt some other single-threaded
     * multivalued async API and not worry about cancellation and backpressure (which is
     * handled by buffering all signals if the downstream can't keep up).
     * <br>
     * 此 Flux 工厂适用于以下情景：如果你想使用单线程多值异步 API，而不担心取消和背压（如果下游无法及时处理，则缓冲所有无法处理的信号）。
     *
     * <p>
     * For example:
     *
     * <pre><code>
     * Flux.&lt;String&gt;push(emitter -&gt; {
     *
     * 	 ActionListener al = e -&gt; {
     * 		 emitter.next(textField.getText());
     *     };
     *
     * 	 // without cleanup support:
     * 	 button.addActionListener(al);
     *
     * 	 // with cleanup support:
     * 	 button.addActionListener(al);
     * 	 emitter.onDispose(() -> {
     * 		 button.removeListener(al);
     *     });
     *
     * }, FluxSink.OverflowStrategy.LATEST);
     * </code></pre>
     *
     * <p><strong>Discard Support:</strong> The {@link FluxSink} exposed by this operator discards elements
     * as relevant to the chosen {@link FluxSink.OverflowStrategy}. For example, the {@link FluxSink.OverflowStrategy#DROP}
     * discards each item as they are being dropped, while {@link FluxSink.OverflowStrategy#BUFFER}
     * will discard the buffer upon cancellation.
     * <br><strong>丢弃支持：</strong> 此算子暴露的 {@link FluxSink} 按照给定的的 {@link FluxSink.OverflowStrategy} 模式
     * 丢弃元素。例如，{@link FluxSink.OverflowStrategy#DROP} 模式丢弃每一个被 dropped 的元素，
     * {@link FluxSink.OverflowStrategy#BUFFER} 模式在主序列取消时丢弃缓冲区中的元素。
     *
     * @param <T>          The type of values in the sequence
     * @param backpressure the backpressure mode, see {@link FluxSink.OverflowStrategy} for the available backpressure modes
     * @param emitter      Consume the {@link FluxSink} provided per-subscriber by Reactor to generate signals.
     * @return a {@link Flux}
     * @see #create(Consumer, FluxSink.OverflowStrategy)
     */
    public static <T> Flux<T> push(Consumer<? super FluxSink<T>> emitter, OverflowStrategy backpressure) {
        return onAssembly(new FluxCreate<>(emitter, backpressure, FluxCreate.CreateMode.PUSH_ONLY));
    }


    /**
     * Re-subscribes to this {@link Flux} sequence if it signals any error, indefinitely.
     * <p>
     * <img class="marble" src="doc-files/marbles/retryForFlux.svg" alt="">
     *
     * @return a {@link Flux} that retries on onError
     */
    public final Flux<T> retry() {
        return retry(Long.MAX_VALUE);
    }

    /**
     * Re-subscribes to this {@link Flux} sequence if it signals any error, for a fixed
     * number of times.
     * <p>
     * Note that passing {@literal Long.MAX_VALUE} is treated as infinite retry.
     * <p>
     * <img class="marble" src="doc-files/marbles/retryWithAttemptsForFlux.svg" alt="">
     *
     * @param numRetries the number of times to tolerate an error
     *
     * @return a {@link Flux} that retries on onError up to the specified number of retry attempts.
     *
     */
    public final Flux<T> retry(long numRetries) {
        return onAssembly(new FluxRetry<>(this, numRetries));
    }

    /**
     * Retries this {@link Flux} in response to signals emitted by a companion {@link Publisher}.
     * The companion is generated by the provided {@link Retry} instance, see {@link Retry#max(long)}, {@link Retry#maxInARow(long)}
     * and {@link Retry#backoff(long, Duration)} for readily available strategy builders.
     * <p>
     * The operator generates a base for the companion, a {@link Flux} of {@link Retry.RetrySignal}
     * which each give metadata about each retryable failure whenever this {@link Flux} signals an error. The final companion
     * should be derived from that base companion and emit data in response to incoming onNext (although it can emit fewer
     * elements, or delay the emissions).
     * <p>
     * Terminal signals in the companion terminate the sequence with the same signal, so emitting an {@link Subscriber#onError(Throwable)}
     * will fail the resulting {@link Flux} with that same error.
     * <p>
     * <img class="marble" src="doc-files/marbles/retryWhenSpecForFlux.svg" alt="">
     * <p>
     * Note that the {@link Retry.RetrySignal} state can be
     * transient and change between each source
     * {@link Subscriber#onError(Throwable) onError} or
     * {@link Subscriber#onNext(Object) onNext}. If processed with a delay,
     * this could lead to the represented state being out of sync with the state at which the retry
     * was evaluated. Map it to {@link Retry.RetrySignal#copy()}
     * right away to mediate this.
     * <p>
     * Note that if the companion {@link Publisher} created by the {@code whenFactory}
     * emits {@link Context} as trigger objects, these {@link Context} will be merged with
     * the previous Context:
     * <blockquote><pre>
     * {@code
     * Retry customStrategy = Retry.from(companion -> companion.handle((retrySignal, sink) -> {
     * 	    ContextView ctx = sink.contextView();
     * 	    int rl = ctx.getOrDefault("retriesLeft", 0);
     * 	    if (rl > 0) {
     *		    sink.next(Context.of(
     *		        "retriesLeft", rl - 1,
     *		        "lastError", retrySignal.failure()
     *		    ));
     * 	    } else {
     * 	        sink.error(Exceptions.retryExhausted("retries exhausted", retrySignal.failure()));
     * 	    }
     * }));
     * Flux<T> retried = originalFlux.retryWhen(customStrategy);
     * }</pre>
     * </blockquote>
     *
     * @param retrySpec the {@link Retry} strategy that will generate the companion {@link Publisher},
     * given a {@link Flux} that signals each onError as a {@link Retry.RetrySignal}.
     *
     * @return a {@link Flux} that retries on onError when a companion {@link Publisher} produces an onNext signal
     * @see Retry#max(long)
     * @see Retry#maxInARow(long)
     * @see Retry#backoff(long, Duration)
     */
    public final Flux<T> retryWhen(Retry retrySpec) {
        return onAssembly(new FluxRetryWhen<>(this, retrySpec));
    }

    /**
     * Subscribe to this {@link Flux} and request unbounded demand.
     * <br>
     * 订阅当前 {@link Flux} 并请求无限元素。
     *
     * <p>
     * This version doesn't specify any consumption behavior for the events from the
     * chain, especially no error handling, so other variants should usually be preferred.
     * <br>
     * 该版本没有为来自链的事件指定任何消费行为，特别是没有错误处理，因此通常应该首选其他重载方法。
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/subscribeIgnoringAllSignalsForFlux.svg" alt="">
     *
     * @return a new {@link Disposable} that can be used to cancel the underlying {@link Subscription}
     * <br/> 一个新的 {@link Disposable}，可用于取消底层的 {@link Subscription}
     */
    public final Disposable subscribe() {
        return subscribe(null, null, null);
    }

    /**
     * Subscribe a {@link Consumer} to this {@link Flux} that will consume all the
     * elements in the sequence. It will request an unbounded demand ({@code Long.MAX_VALUE}).
     * <p>
     * For a passive(被动的) version that observe and forward incoming data see {@link #doOnNext(java.util.function.Consumer)}.
     * <p>
     * For a version that gives you more control over backpressure and the request, see
     * {@link #subscribe(Subscriber)} with a {@link BaseSubscriber}.
     *
     * <p>
     * Keep in mind that since the sequence can be asynchronous, this will immediately
     * return control to the calling thread. This can give the impression the consumer is
     * not invoked when executing in a main thread or a unit test for instance.
     * <br>
     * 注意，由于序列可以是异步的，所以会立即将控制权返回给调用线程。这可能会给人一种印象，即在主线程或单元测试中执行时不会调用消费者。
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/subscribeWithOnNextForFlux.svg" alt="">
     *
     * @param consumer the consumer to invoke on each value (onNext signal)
     * @return a new {@link Disposable} that can be used to cancel the underlying {@link Subscription}
     */
    public final Disposable subscribe(Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        return subscribe(consumer, null, null);
    }

    /**
     * Subscribe to this {@link Flux} with a {@link Consumer} that will consume all the
     * elements in the sequence, as well as a {@link Consumer} that will handle errors.
     * The subscription will request an unbounded demand ({@code Long.MAX_VALUE}).
     * <p>
     * For a passive(被动的) version that observe and forward incoming data see
     * {@link #doOnNext(java.util.function.Consumer)} and {@link #doOnError(java.util.function.Consumer)}.
     * <p>For a version that gives you more control over backpressure and the request, see
     * {@link #subscribe(Subscriber)} with a {@link BaseSubscriber}.
     * <p>
     * Keep in mind that since the sequence can be asynchronous, this will immediately
     * return control to the calling thread. This can give the impression the consumers are
     * not invoked when executing in a main thread or a unit test for instance.
     * <br>
     * 注意，由于序列可以是异步的，所以会立即将控制权返回给调用线程。这可能会给人一种印象，即在主线程或单元测试中执行时不会调用消费者。
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/subscribeWithOnNextAndOnErrorForFlux.svg" alt="">
     *
     * @param consumer      the consumer to invoke on each next signal
     * @param errorConsumer the consumer to invoke on error signal
     * @return a new {@link Disposable} that can be used to cancel the underlying {@link Subscription}
     */
    public final Disposable subscribe(@Nullable Consumer<? super T> consumer, Consumer<? super Throwable> errorConsumer) {
        Objects.requireNonNull(errorConsumer, "errorConsumer");
        return subscribe(consumer, errorConsumer, null);
    }

    /**
     * Subscribe {@link Consumer} to this {@link reactor.core.publisher.Flux} that will respectively consume all the
     * elements in the sequence, handle errors and react to completion. The subscription
     * will request unbounded demand ({@code Long.MAX_VALUE}).
     * <p>
     * For a passive version that observe and forward incoming data see {@link #doOnNext(java.util.function.Consumer)},
     * {@link #doOnError(java.util.function.Consumer)} and {@link #doOnComplete(Runnable)}.
     * <p>For a version that gives you more control over backpressure and the request, see
     * {@link #subscribe(Subscriber)} with a {@link BaseSubscriber}.
     * <p>
     * Keep in mind that since the sequence can be asynchronous, this will immediately
     * return control to the calling thread. This can give the impression the consumer is
     * not invoked when executing in a main thread or a unit test for instance.
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/subscribeWithOnNextAndOnErrorAndOnCompleteForFlux.svg" alt="">
     *
     * @param consumer         the consumer to invoke on each value
     * @param errorConsumer    the consumer to invoke on error signal
     * @param completeConsumer the consumer to invoke on complete signal
     * @return a new {@link Disposable} that can be used to cancel the underlying {@link Subscription}
     */
    public final Disposable subscribe(
            @Nullable Consumer<? super T> consumer,
            @Nullable Consumer<? super Throwable> errorConsumer,
            @Nullable Runnable completeConsumer) {
        return subscribe(consumer, errorConsumer, completeConsumer, (Context) null);
    }

    /**
     * Subscribe {@link Consumer} to this {@link reactor.core.publisher.Flux} that will respectively consume all the
     * elements in the sequence, handle errors, react to completion, and request upon subscription.
     * It will let the provided {@link Subscription subscriptionConsumer}
     * request the adequate amount of data, or request unbounded demand
     * {@code Long.MAX_VALUE} if no such consumer is provided.
     * <p>
     * For a passive version that observe and forward incoming data see {@link #doOnNext(java.util.function.Consumer)},
     * {@link #doOnError(java.util.function.Consumer)}, {@link #doOnComplete(Runnable)}
     * and {@link #doOnSubscribe(Consumer)}.
     * <p>For a version that gives you more control over backpressure and the request, see
     * {@link #subscribe(Subscriber)} with a {@link BaseSubscriber}.
     * <p>
     * Keep in mind that since the sequence can be asynchronous, this will immediately
     * return control to the calling thread. This can give the impression the consumer is
     * not invoked when executing in a main thread or a unit test for instance.
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/subscribeForFlux.svg" alt="">
     *
     * @param consumer             the consumer to invoke on each value
     * @param errorConsumer        the consumer to invoke on error signal
     * @param completeConsumer     the consumer to invoke on complete signal
     * @param subscriptionConsumer the consumer to invoke on subscribe signal, to be used
     *                             for the initial {@link Subscription#request(long) request}, or null for max request
     * @return a new {@link Disposable} that can be used to cancel the underlying {@link Subscription}
     * @deprecated Because users tend to forget to {@link Subscription#request(long) request} the subsciption. If
     * the behavior is really needed, consider using {@link #subscribeWith(Subscriber)}. To be removed in 3.5.
     */
    @Deprecated
    public final Disposable subscribe(
            @Nullable Consumer<? super T> consumer,
            @Nullable Consumer<? super Throwable> errorConsumer,
            @Nullable Runnable completeConsumer,
            @Nullable Consumer<? super Subscription> subscriptionConsumer) {
        return subscribeWith(new LambdaSubscriber<>(consumer, errorConsumer,
                completeConsumer,
                subscriptionConsumer,
                null));
    }

    /**
     * Subscribe {@link Consumer} to this {@link reactor.core.publisher.Flux} that will respectively consume all the
     * elements in the sequence, handle errors and react to completion. Additionally, a {@link reactor.util.context.Context}
     * is tied to the subscription. At subscription, an unbounded request is implicitly made.
     * <p>
     * For a passive version that observe and forward incoming data see {@link #doOnNext(java.util.function.Consumer)},
     * {@link #doOnError(java.util.function.Consumer)}, {@link #doOnComplete(Runnable)}
     * and {@link #doOnSubscribe(Consumer)}.
     * <p>For a version that gives you more control over backpressure and the request, see
     * {@link #subscribe(Subscriber)} with a {@link BaseSubscriber}.
     * <p>
     * Keep in mind that since the sequence can be asynchronous, this will immediately
     * return control to the calling thread. This can give the impression the consumer is
     * not invoked when executing in a main thread or a unit test for instance.
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/subscribeForFlux.svg" alt="">
     *
     * @param consumer         the consumer to invoke on each value
     * @param errorConsumer    the consumer to invoke on error signal
     * @param completeConsumer the consumer to invoke on complete signal
     * @param initialContext   the base {@link reactor.util.context.Context} tied to the subscription that will
     *                         be visible to operators upstream
     * @return a new {@link Disposable} that can be used to cancel the underlying {@link Subscription}
     */
    public final Disposable subscribe(
            @Nullable Consumer<? super T> consumer,
            @Nullable Consumer<? super Throwable> errorConsumer,
            @Nullable Runnable completeConsumer,
            @Nullable Context initialContext) {
        return subscribeWith(new LambdaSubscriber<>(consumer, errorConsumer,
                completeConsumer,
                null,
                initialContext));
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void subscribe(Subscriber<? super T> actual) {
        CorePublisher publisher = Operators.onLastAssembly(this);
        CoreSubscriber subscriber = Operators.toCoreSubscriber(actual);

        if (subscriber instanceof Fuseable.QueueSubscription && this != publisher && this instanceof Fuseable && !(publisher instanceof Fuseable)) {
            subscriber = new FluxHide.SuppressFuseableSubscriber<>(subscriber);
        }

        try {
            if (publisher instanceof OptimizableOperator) {
                OptimizableOperator operator = (OptimizableOperator) publisher;
                while (true) {
                    subscriber = operator.subscribeOrReturn(subscriber);
                    if (subscriber == null) {
                        // null means "I will subscribe myself", returning...
                        return;
                    }
                    OptimizableOperator newSource = operator.nextOptimizableSource();
                    if (newSource == null) {
                        publisher = operator.source();
                        break;
                    }
                    operator = newSource;
                }
            }

            subscriber = Operators.restoreContextOnSubscriberIfPublisherNonInternal(publisher, subscriber);
            publisher.subscribe(subscriber);
        } catch (Throwable e) {
            Operators.reportThrowInSubscribe(subscriber, e);
            return;
        }
    }

    /**
     * An internal {@link Publisher#subscribe(Subscriber)} that will bypass
     * {@link Hooks#onLastOperator(Function)} pointcut.
     * <br>
     * 一个内部的 {@link Publisher#subscribe(Subscriber)}，它将绕过 {@link Hooks#onLastOperator(Function)} 切点。
     *
     * <p>
     * In addition to behave as expected by {@link Publisher#subscribe(Subscriber)}
     * in a controlled manner, it supports direct subscribe-time {@link Context} passing.
     * <br>
     * 除了按照 {@link Publisher#subscribe(Subscriber)} 的期望以受控的方式行事外，它还支持直接订阅时 {@link Context} 传递。
     *
     * @param actual the {@link Subscriber} interested into the published sequence
     * @see Flux#subscribe(Subscriber)
     */
    public abstract void subscribe(CoreSubscriber<? super T> actual);

}
