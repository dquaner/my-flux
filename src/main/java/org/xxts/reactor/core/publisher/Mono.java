package org.xxts.reactor.core.publisher;

import org.xxts.reactivestreams.Publisher;
import org.xxts.reactivestreams.Subscriber;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class Mono<T> {


    /**
     * Unchecked wrap of {@link Publisher} as {@link Mono}, supporting {@link Fuseable} sources.
     * When converting a {@link Mono} or {@link Mono Monos} that have been converted to a {@link Flux} and back,
     * the original {@link Mono} is returned unwrapped.
     * Note that this bypasses {@link Hooks#onEachOperator(String, Function) assembly hooks}.
     *
     * @param source the {@link Publisher} to wrap
     * @param enforceMonoContract {@code true} to wrap publishers without assumption about their cardinality
     * (first {@link Subscriber#onNext(Object)} will cancel the source), {@code false} to behave like {@link #fromDirect(Publisher)}.
     * @param <T> input upstream type
     * @return a wrapped {@link Mono}
     */
    static <T> Mono<T> wrap(Publisher<T> source, boolean enforceMonoContract) {
        //some sources can be considered already assembled monos
        //all conversion methods (from, fromDirect, wrap) must accommodate for this
        boolean shouldWrap = ContextPropagationSupport.shouldWrapPublisher(source);
        if (source instanceof Mono) {
            if (!shouldWrap) {
                return (Mono<T>) source;
            }
            return ContextPropagation.monoRestoreThreadLocals((Mono<? extends T>) source);
        }

        if (source instanceof FluxSourceMono || source instanceof FluxSourceMonoFuseable) {
            @SuppressWarnings("unchecked") Mono<T> extracted =
                    (Mono<T>) ((FluxFromMonoOperator<T, T>) source).source;
            boolean shouldWrapExtracted =
                    ContextPropagationSupport.shouldWrapPublisher(extracted);
            if (!shouldWrapExtracted) {
                return extracted;
            }
            return ContextPropagation.monoRestoreThreadLocals(extracted);
        }

        if (source instanceof Flux && source instanceof Callable) {
            @SuppressWarnings("unchecked") Callable<T> m = (Callable<T>) source;
            return Flux.wrapToMono(m);
        }

        Mono<T> target;

        //equivalent to what from used to be, without assembly hooks
        if (enforceMonoContract) {
            if (source instanceof Flux) {
                target = new MonoNext<>((Flux<T>) source);
            } else {
                target = new MonoFromPublisher<>(source);
            }
            //equivalent to what fromDirect used to be without onAssembly
        } else if (source instanceof Flux && source instanceof Fuseable) {
            target = new MonoSourceFluxFuseable<>((Flux<T>) source);
        } else if (source instanceof Flux) {
            target = new MonoSourceFlux<>((Flux<T>) source);
        } else if (source instanceof Fuseable) {
            target = new MonoSourceFuseable<>(source);
        } else {
            target = new MonoSource<>(source);
        }

        if (shouldWrap) {
            return ContextPropagation.monoRestoreThreadLocals(target);
        }
        return target;
    }


    /**
     * Convert a {@link Publisher} to a {@link Mono} without any cardinality check
     * (ie this method doesn't cancel the source past the first element).
     * Conversion transparently returns {@link Mono} sources without wrapping and otherwise
     * supports {@link Fuseable} sources.
     * Note this is an advanced interoperability operator that implies you know the
     * {@link Publisher} you are converting follows the {@link Mono} semantics and only
     * ever emits one element.
     * <p>
     * {@link Hooks#onEachOperator(String, Function)} and similar assembly hooks are applied
     * unless the source is already a {@link Mono}.
     *
     * @param source the Mono-compatible {@link Publisher} to wrap
     * @param <I> type of the value emitted by the publisher
     * @return a wrapped {@link Mono}
     */
    public static <I> Mono<I> fromDirect(Publisher<? extends I> source){
        //some sources can be considered already assembled monos
        //all conversion methods (from, fromDirect, wrap) must accommodate for this
        boolean shouldWrap = ContextPropagationSupport.shouldWrapPublisher(source);
        if (source instanceof Mono && !shouldWrap) {
            @SuppressWarnings("unchecked")
            Mono<I> m = (Mono<I>)source;
            return m;
        }
        if (source instanceof FluxSourceMono
                || source instanceof FluxSourceMonoFuseable) {
            @SuppressWarnings("unchecked")
            FluxFromMonoOperator<I, I> wrapper = (FluxFromMonoOperator<I,I>) source;
            @SuppressWarnings("unchecked")
            Mono<I> extracted = (Mono<I>) wrapper.source;
            boolean shouldWrapExtracted =
                    ContextPropagationSupport.shouldWrapPublisher(extracted);
            if (!shouldWrapExtracted) {
                return extracted;
            } else {
                // Skip assembly hook
                return wrap(extracted, false);
            }
        }

        //we delegate to `wrap` and apply assembly hooks
        @SuppressWarnings("unchecked") Publisher<I> downcasted = (Publisher<I>) source;
        return onAssembly(wrap(downcasted, false));
    }
}
