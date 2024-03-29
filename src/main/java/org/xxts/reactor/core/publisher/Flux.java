//package org.xxts.reactor.core.publisher;
//
//import org.xxts.reactivestreams.Publisher;
//import org.xxts.reactor.core.CorePublisher;
//
//import java.util.function.Consumer;
//import java.util.function.Function;
//
//public abstract class Flux<T> implements CorePublisher<T> {
//
//
//    /**
//     * Programmatically create a {@link Flux} with the capability of emitting multiple
//     * elements in a synchronous or asynchronous manner through the {@link FluxSink} API.
//     * This includes emitting elements from multiple threads.
//     * <p>
//     * <img class="marble" src="doc-files/marbles/createForFlux.svg" alt="">
//     * <p>
//     * This Flux factory is useful if one wants to adapt some other multi-valued async API
//     * and not worry about cancellation and backpressure (which is handled by buffering
//     * all signals if the downstream can't keep up).
//     * <p>
//     * For example:
//     *
//     * <pre><code>
//     * Flux.&lt;String&gt;create(emitter -&gt; {
//     *
//     *     ActionListener al = e -&gt; {
//     *         emitter.next(textField.getText());
//     *     };
//     *     // without cleanup support:
//     *
//     *     button.addActionListener(al);
//     *
//     *     // with cleanup support:
//     *
//     *     button.addActionListener(al);
//     *     emitter.onDispose(() -> {
//     *         button.removeListener(al);
//     *     });
//     * });
//     * </code></pre>
//     *
//     * <p><strong>Discard Support:</strong> The {@link FluxSink} exposed by this operator buffers in case of
//     * overflow. The buffer is discarded when the main sequence is cancelled.
//     *
//     * @param <T> The type of values in the sequence
//     * @param emitter Consume the {@link FluxSink} provided per-subscriber by Reactor to generate signals.
//     * @return a {@link Flux}
//     * @see #push(Consumer)
//     */
//    public static <T> Flux<T> create(Consumer<? super FluxSink<T>> emitter) {
//        return create(emitter, FluxSink.OverflowStrategy.BUFFER);
//    }
//
//    /**
//     * Programmatically create a {@link Flux} with the capability of emitting multiple
//     * elements in a synchronous or asynchronous manner through the {@link FluxSink} API.
//     * This includes emitting elements from multiple threads.
//     * <p>
//     * <img class="marble" src="doc-files/marbles/createWithOverflowStrategy.svg" alt="">
//     * <p>
//     * This Flux factory is useful if one wants to adapt some other multi-valued async API
//     * and not worry about cancellation and backpressure (which is handled by buffering
//     * all signals if the downstream can't keep up).
//     * <p>
//     * For example:
//     *
//     * <pre><code>
//     * Flux.&lt;String&gt;create(emitter -&gt; {
//     *
//     *     ActionListener al = e -&gt; {
//     *         emitter.next(textField.getText());
//     *     };
//     *     // without cleanup support:
//     *
//     *     button.addActionListener(al);
//     *
//     *     // with cleanup support:
//     *
//     *     button.addActionListener(al);
//     *     emitter.onDispose(() -> {
//     *         button.removeListener(al);
//     *     });
//     * }, FluxSink.OverflowStrategy.LATEST);
//     * </code></pre>
//     *
//     * <p><strong>Discard Support:</strong> The {@link FluxSink} exposed by this operator discards elements
//     * as relevant to the chosen {@link FluxSink.OverflowStrategy}. For example, the {@link FluxSink.OverflowStrategy#DROP}
//     * discards each items as they are being dropped, while {@link FluxSink.OverflowStrategy#BUFFER}
//     * will discard the buffer upon cancellation.
//     *
//     * @param <T> The type of values in the sequence
//     * @param backpressure the backpressure mode, see {@link FluxSink.OverflowStrategy} for the
//     * available backpressure modes
//     * @param emitter Consume the {@link FluxSink} provided per-subscriber by Reactor to generate signals.
//     * @return a {@link Flux}
//     * @see #push(Consumer, FluxSink.OverflowStrategy)
//     */
//    public static <T> Flux<T> create(Consumer<? super FluxSink<T>> emitter, FluxSink.OverflowStrategy backpressure) {
//        return onAssembly(new FluxCreate<>(emitter, backpressure, FluxCreate.CreateMode.PUSH_PULL));
//    }
//
//    /**
//     * To be used by custom operators: invokes assembly {@link Hooks} pointcut given a
//     * {@link Flux}, potentially returning a new {@link Flux}. This is for example useful
//     * to activate cross-cutting concerns at assembly time, e.g. a generalized
//     * {@link #checkpoint()}.
//     *
//     * @param <T> the value type
//     * @param source the source to apply assembly hooks onto
//     *
//     * @return the source, potentially wrapped with assembly time cross-cutting behavior
//     */
//    @SuppressWarnings("unchecked")
//    protected static <T> Flux<T> onAssembly(Flux<T> source) {
//        Function<Publisher, Publisher> hook = Hooks.onEachOperatorHook;
//        if(hook != null) {
//            source = (Flux<T>) hook.apply(source);
//        }
//        if (Hooks.GLOBAL_TRACE) {
//            FluxOnAssembly.AssemblySnapshot stacktrace = new FluxOnAssembly.AssemblySnapshot(null, Traces.callSiteSupplierFactory.get());
//            source = (Flux<T>) Hooks.addAssemblyInfo(source, stacktrace);
//        }
//        return source;
//    }
//
//}
