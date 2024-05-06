package org.xxts.reactor.core.publisher;

import org.xxts.reactivestreams.Subscriber;
import org.xxts.reactor.core.CorePublisher;
import org.xxts.reactor.core.publisher.FluxSink.OverflowStrategy;
import org.xxts.reactor.util.context.Context;
import org.xxts.reactor.util.context.ContextView;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Fuseable;
import reactor.core.publisher.*;
import reactor.core.publisher.Hooks;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Flux<T> implements CorePublisher<T> {

    /**
     * Programmatically create a {@link Flux} with the capability of emitting multiple
     * elements in a synchronous or asynchronous manner through the {@link FluxSink} API.
     * This includes emitting elements from multiple threads.
     * <p>
     * <img class="marble" src="doc-files/marbles/createForFlux.svg" alt="">
     * <p>
     * This Flux factory is useful if one wants to adapt some other multi-valued async API
     * and not worry about cancellation and backpressure (which is handled by buffering
     * all signals if the downstream can't keep up).
     * <p>
     * For example:
     *
     * <pre><code>
     * Flux.&lt;String&gt;create(emitter -&gt; {
     *
     *     ActionListener al = e -&gt; {
     *         emitter.next(textField.getText());
     *     };
     *     // without cleanup support:
     *
     *     button.addActionListener(al);
     *
     *     // with cleanup support:
     *
     *     button.addActionListener(al);
     *     emitter.onDispose(() -> {
     *         button.removeListener(al);
     *     });
     * });
     * </code></pre>
     *
     * <p><strong>Discard Support:</strong> The {@link reactor.core.publisher.FluxSink} exposed by this operator buffers in case of
     * overflow. The buffer is discarded when the main sequence is cancelled.
     *
     * @param <T> The type of values in the sequence
     * @param emitter Consume the {@link reactor.core.publisher.FluxSink} provided per-subscriber by Reactor to generate signals.
     * @return a {@link reactor.core.publisher.Flux}
     * @see #push(Consumer)
     */
    public static <T> Flux<T> create(Consumer<? super FluxSink<T>> emitter) {
        return create(emitter, OverflowStrategy.BUFFER);
    }

    /**
     * Programmatically create a {@link reactor.core.publisher.Flux} with the capability of emitting multiple
     * elements in a synchronous or asynchronous manner through the {@link reactor.core.publisher.FluxSink} API.
     * This includes emitting elements from multiple threads.
     * <p>
     * <img class="marble" src="doc-files/marbles/createWithOverflowStrategy.svg" alt="">
     * <p>
     * This Flux factory is useful if one wants to adapt some other multi-valued async API
     * and not worry about cancellation and backpressure (which is handled by buffering
     * all signals if the downstream can't keep up).
     * <p>
     * For example:
     *
     * <pre><code>
     * Flux.&lt;String&gt;create(emitter -&gt; {
     *
     *     ActionListener al = e -&gt; {
     *         emitter.next(textField.getText());
     *     };
     *     // without cleanup support:
     *
     *     button.addActionListener(al);
     *
     *     // with cleanup support:
     *
     *     button.addActionListener(al);
     *     emitter.onDispose(() -> {
     *         button.removeListener(al);
     *     });
     * }, FluxSink.OverflowStrategy.LATEST);
     * </code></pre>
     *
     * <p><strong>Discard Support:</strong> The {@link reactor.core.publisher.FluxSink} exposed by this operator discards elements
     * as relevant to the chosen {@link reactor.core.publisher.FluxSink.OverflowStrategy}. For example, the {@link reactor.core.publisher.FluxSink.OverflowStrategy#DROP}
     * discards each items as they are being dropped, while {@link reactor.core.publisher.FluxSink.OverflowStrategy#BUFFER}
     * will discard the buffer upon cancellation.
     *
     * @param <T> The type of values in the sequence
     * @param backpressure the backpressure mode, see {@link reactor.core.publisher.FluxSink.OverflowStrategy} for the
     * available backpressure modes
     * @param emitter Consume the {@link reactor.core.publisher.FluxSink} provided per-subscriber by Reactor to generate signals.
     * @return a {@link reactor.core.publisher.Flux}
     * @see #push(Consumer, reactor.core.publisher.FluxSink.OverflowStrategy)
     */
    public static <T> Flux<T> create(Consumer<? super FluxSink<T>> emitter, OverflowStrategy backpressure) {
        return onAssembly(new FluxCreate<>(emitter, backpressure, FluxCreate.CreateMode.PUSH_PULL));
    }

    /**
     * Enrich the {@link Context} visible from downstream for the benefit of upstream
     * operators, by making all values from the provided {@link ContextView} visible on top
     * of pairs from downstream.
     * <p>
     * A {@link Context} (and its {@link ContextView}) is tied to a given subscription
     * and is read by querying the downstream {@link Subscriber}. {@link Subscriber} that
     * don't enrich the context instead access their own downstream's context. As a result,
     * this operator conceptually enriches a {@link Context} coming from under it in the chain
     * (downstream, by default an empty one) and makes the new enriched {@link Context}
     * visible to operators above it in the chain.
     *
     * @param contextToAppend the {@link ContextView} to merge with the downstream {@link Context},
     * resulting in a new more complete {@link Context} that will be visible from upstream.
     *
     * @return a contextualized {@link reactor.core.publisher.Flux}
     * @see ContextView
     */
    public final Flux<T> contextWrite(ContextView contextToAppend) {
        return contextWrite(c -> c.putAll(contextToAppend));
    }

    /**
     * Enrich the {@link Context} visible from downstream for the benefit of upstream
     * operators, by applying a {@link Function} to the downstream {@link Context}.
     * <p>
     * The {@link Function} takes a {@link Context} for convenience, allowing to easily
     * call {@link Context#put(Object, Object) write APIs} to return a new {@link Context}.
     * <p>
     * A {@link Context} (and its {@link ContextView}) is tied to a given subscription
     * and is read by querying the downstream {@link Subscriber}. {@link Subscriber} that
     * don't enrich the context instead access their own downstream's context. As a result,
     * this operator conceptually enriches a {@link Context} coming from under it in the chain
     * (downstream, by default an empty one) and makes the new enriched {@link Context}
     * visible to operators above it in the chain.
     *
     * @param contextModifier the {@link Function} to apply to the downstream {@link Context},
     * resulting in a new more complete {@link Context} that will be visible from upstream.
     *
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
     * Programmatically create a {@link reactor.core.publisher.Flux} with the capability of emitting multiple
     * elements from a single-threaded producer through the {@link reactor.core.publisher.FluxSink} API. For
     * a multi-threaded capable alternative, see {@link #create(Consumer)}.
     * <p>
     * <img class="marble" src="doc-files/marbles/push.svg" alt="">
     * <p>
     * This Flux factory is useful if one wants to adapt some other single-threaded
     * multi-valued async API and not worry about cancellation and backpressure (which is
     * handled by buffering all signals if the downstream can't keep up).
     * <p>
     * For example:
     *
     * <pre><code>
     * Flux.&lt;String&gt;push(emitter -&gt; {
     *
     *	 ActionListener al = e -&gt; {
     *		 emitter.next(textField.getText());
     *	 };
     *	 // without cleanup support:
     *
     *	 button.addActionListener(al);
     *
     *	 // with cleanup support:
     *
     *	 button.addActionListener(al);
     *	 emitter.onDispose(() -> {
     *		 button.removeListener(al);
     *	 });
     * });
     * </code></pre>
     *
     * <p><strong>Discard Support:</strong> The {@link reactor.core.publisher.FluxSink} exposed by this operator buffers in case of
     * overflow. The buffer is discarded when the main sequence is cancelled.
     *
     * @param <T> The type of values in the sequence
     * @param emitter Consume the {@link reactor.core.publisher.FluxSink} provided per-subscriber by Reactor to generate signals.
     * @return a {@link reactor.core.publisher.Flux}
     * @see #create(Consumer)
     */
    public static <T> Flux<T> push(Consumer<? super FluxSink<T>> emitter) {
        return push(emitter, OverflowStrategy.BUFFER);
    }

    /**
     * Programmatically create a {@link reactor.core.publisher.Flux} with the capability of emitting multiple
     * elements from a single-threaded producer through the {@link reactor.core.publisher.FluxSink} API. For
     * a multi-threaded capable alternative, see {@link #create(Consumer, reactor.core.publisher.FluxSink.OverflowStrategy)}.
     * <p>
     * <img class="marble" src="doc-files/marbles/pushWithOverflowStrategy.svg" alt="">
     * <p>
     * This Flux factory is useful if one wants to adapt some other single-threaded
     * multi-valued async API and not worry about cancellation and backpressure (which is
     * handled by buffering all signals if the downstream can't keep up).
     * <p>
     * For example:
     *
     * <pre><code>
     * Flux.&lt;String&gt;push(emitter -&gt; {
     *
     *	 ActionListener al = e -&gt; {
     *		 emitter.next(textField.getText());
     *	 };
     *	 // without cleanup support:
     *
     *	 button.addActionListener(al);
     *
     *	 // with cleanup support:
     *
     *	 button.addActionListener(al);
     *	 emitter.onDispose(() -> {
     *		 button.removeListener(al);
     *	 });
     * }, FluxSink.OverflowStrategy.LATEST);
     * </code></pre>
     *
     * <p><strong>Discard Support:</strong> The {@link reactor.core.publisher.FluxSink} exposed by this operator discards elements
     * as relevant to the chosen {@link reactor.core.publisher.FluxSink.OverflowStrategy}. For example, the {@link reactor.core.publisher.FluxSink.OverflowStrategy#DROP}
     * discards each items as they are being dropped, while {@link reactor.core.publisher.FluxSink.OverflowStrategy#BUFFER}
     * will discard the buffer upon cancellation.
     *
     * @param <T> The type of values in the sequence
     * @param backpressure the backpressure mode, see {@link reactor.core.publisher.FluxSink.OverflowStrategy} for the
     * available backpressure modes
     * @param emitter Consume the {@link reactor.core.publisher.FluxSink} provided per-subscriber by Reactor to generate signals.
     * @return a {@link reactor.core.publisher.Flux}
     * @see #create(Consumer, reactor.core.publisher.FluxSink.OverflowStrategy)
     */
    public static <T> Flux<T> push(Consumer<? super FluxSink<T>> emitter, OverflowStrategy backpressure) {
        return onAssembly(new FluxCreate<>(emitter, backpressure, FluxCreate.CreateMode.PUSH_ONLY));
    }

    /**
     * Subscribe to this {@link reactor.core.publisher.Flux} and request unbounded demand.
     * <p>
     * This version doesn't specify any consumption behavior for the events from the
     * chain, especially no error handling, so other variants should usually be preferred.
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/subscribeIgoringAllSignalsForFlux.svg" alt="">
     *
     * @return a new {@link Disposable} that can be used to cancel the underlying {@link Subscription}
     */
    public final Disposable subscribe() {
        return subscribe(null, null, null);
    }

    /**
     * Subscribe a {@link Consumer} to this {@link reactor.core.publisher.Flux} that will consume all the
     * elements in the  sequence. It will request an unbounded demand ({@code Long.MAX_VALUE}).
     * <p>
     * For a passive version that observe and forward incoming data see {@link #doOnNext(java.util.function.Consumer)}.
     * <p>
     * For a version that gives you more control over backpressure and the request, see
     * {@link #subscribe(Subscriber)} with a {@link BaseSubscriber}.
     * <p>
     * Keep in mind that since the sequence can be asynchronous, this will immediately
     * return control to the calling thread. This can give the impression the consumer is
     * not invoked when executing in a main thread or a unit test for instance.
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/subscribeWithOnNextForFlux.svg" alt="">
     *
     * @param consumer the consumer to invoke on each value (onNext signal)
     *
     * @return a new {@link Disposable} that can be used to cancel the underlying {@link Subscription}
     */
    public final Disposable subscribe(Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        return subscribe(consumer, null, null);
    }

    /**
     * Subscribe to this {@link reactor.core.publisher.Flux} with a {@link Consumer} that will consume all the
     * elements in the sequence, as well as a {@link Consumer} that will handle errors.
     * The subscription will request an unbounded demand ({@code Long.MAX_VALUE}).
     * <p>
     * For a passive version that observe and forward incoming data see
     * {@link #doOnNext(java.util.function.Consumer)} and {@link #doOnError(java.util.function.Consumer)}.
     * <p>For a version that gives you more control over backpressure and the request, see
     * {@link #subscribe(Subscriber)} with a {@link BaseSubscriber}.
     * <p>
     * Keep in mind that since the sequence can be asynchronous, this will immediately
     * return control to the calling thread. This can give the impression the consumers are
     * not invoked when executing in a main thread or a unit test for instance.
     *
     * <p>
     * <img class="marble" src="doc-files/marbles/subscribeWithOnNextAndOnErrorForFlux.svg" alt="">
     *
     * @param consumer the consumer to invoke on each next signal
     * @param errorConsumer the consumer to invoke on error signal
     *
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
     * @param consumer the consumer to invoke on each value
     * @param errorConsumer the consumer to invoke on error signal
     * @param completeConsumer the consumer to invoke on complete signal
     *
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
     * @param consumer the consumer to invoke on each value
     * @param errorConsumer the consumer to invoke on error signal
     * @param completeConsumer the consumer to invoke on complete signal
     * @param subscriptionConsumer the consumer to invoke on subscribe signal, to be used
     * for the initial {@link Subscription#request(long) request}, or null for max request
     *
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
     * @param consumer the consumer to invoke on each value
     * @param errorConsumer the consumer to invoke on error signal
     * @param completeConsumer the consumer to invoke on complete signal
     * @param initialContext the base {@link reactor.util.context.Context} tied to the subscription that will
     * be visible to operators upstream
     *
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
        reactor.core.CorePublisher publisher = Operators.onLastAssembly(this);
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
        }
        catch (Throwable e) {
            Operators.reportThrowInSubscribe(subscriber, e);
            return;
        }
    }

    /**
     * An internal {@link Publisher#subscribe(Subscriber)} that will bypass
     * {@link Hooks#onLastOperator(Function)} pointcut.
     * <p>
     * In addition to behave as expected by {@link Publisher#subscribe(Subscriber)}
     * in a controlled manner, it supports direct subscribe-time {@link reactor.util.context.Context} passing.
     *
     * @param actual the {@link Subscriber} interested into the published sequence
     * @see reactor.core.publisher.Flux#subscribe(Subscriber)
     */
    public abstract void subscribe(CoreSubscriber<? super T> actual);

}
