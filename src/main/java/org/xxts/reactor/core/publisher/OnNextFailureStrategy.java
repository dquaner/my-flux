package org.xxts.reactor.core.publisher;

import org.xxts.reactor.util.annotation.Nullable;
import org.xxts.reactor.util.context.Context;

import java.util.function.*;

/**
 * A strategy to evaluate if errors that happens during an operator's onNext call can
 * be recovered from, allowing the sequence to continue. This opt-in strategy is
 * applied by compatible operators through the {@link Operators#onNextError(Object, Throwable, Context, Subscription)}
 * and {@link Operators#onNextPollError(Object, Throwable, Context)} methods.
 * See {@link #stop()}, {@link #resumeDrop()}, {@link #resumeDropIf(Predicate)},
 * {@link #resume(BiConsumer)} and {@link #resumeIf(Predicate, BiConsumer)}
 * for the possible strategies.
 *
 * @author Simon Baslé
 */
interface OnNextFailureStrategy extends BiFunction<Throwable, Object, Throwable>,
        BiPredicate<Throwable, Object> {

    /**
     * The key that can be used to store an {@link OnNextFailureStrategy} in a {@link Context}.
     */
    String KEY_ON_NEXT_ERROR_STRATEGY = "reactor.onNextError.localStrategy";

    @Override
    @Nullable
    default Throwable apply(Throwable throwable, @Nullable Object o) {
        return process(throwable, o, Context.empty());
    }

    @Override
    boolean test(Throwable throwable, @Nullable Object o);

    /**
     * Process an error and the optional value that caused it (when applicable) in
     * preparation for sequence resume, so that the error is not completely swallowed.
     * <p>
     * If the strategy cannot resume this kind of error (ie. {@link #test(Throwable, Object)}
     * returns false), return the original error. Any exception in the processing will be
     * caught and returned. If the strategy was able to process the error correctly,
     * returns null.
     *
     * @param error   the error being recovered from.
     * @param value   the value causing the error, null if not applicable.
     * @param context the {@link Context} associated with the recovering sequence.
     * @return null if the error was processed for resume, a {@link Throwable} to propagate
     * otherwise.
     * @see #test(Throwable, Object)
     */
    @Nullable
    Throwable process(Throwable error, @Nullable Object value, Context context);

    /**
     * A strategy that never let any error resume.
     */
//    static OnNextFailureStrategy stop() {
//        return STOP;
//    }

    /**
     * A strategy that let all error resume. When processing, the error is passed to the
     * {@link Operators#onErrorDropped(Throwable, Context)} hook and the incriminating
     * source value (if available) is passed to the {@link Operators#onNextDropped(Object, Context)}
     * hook, allowing the sequence to continue with further values.
     */
    static OnNextFailureStrategy resumeDrop() {
        return RESUME_DROP;
    }

    /**
     * A strategy that let some error resume. When processing, the error is passed to the
     * {@link Operators#onErrorDropped(Throwable, Context)} hook and the incriminating
     * source value (if available) is passed to the {@link Operators#onNextDropped(Object, Context)}
     * hook, allowing the sequence to continue with further values.
     * <p>
     * Any exception thrown by the predicate is thrown as is.
     *
     * @param causePredicate the predicate to match in order to resume from an error.
     */
    static OnNextFailureStrategy resumeDropIf(Predicate<Throwable> causePredicate) {
        return new OnNextFailureStrategy.ResumeDropStrategy(causePredicate);
    }

    /**
     * A strategy that let all errors resume. When processing, the error and the
     * incriminating source value are passed to custom {@link Consumer Consumers}.
     * <p>
     * Any exception thrown by the consumers will suppress the original error and be
     * returned for propagation. If the original error is fatal then it is thrown
     * upon processing it (see {@link Exceptions#throwIfFatal(Throwable)}).
     *
     * @param errorConsumer the {@link BiConsumer<Throwable, Object>} to process the recovered errors with.
     *                      It must deal with potential {@code null}s.
     * @return a new {@link OnNextFailureStrategy} that allows resuming the sequence.
     */
    static OnNextFailureStrategy resume(BiConsumer<Throwable, Object> errorConsumer) {
        return new OnNextFailureStrategy.ResumeStrategy(null, errorConsumer);
    }

    /**
     * A strategy that let some errors resume. When processing, the error and the
     * incriminating source value are passed to custom {@link Consumer Consumers}.
     * <p>
     * Any exception thrown by the predicate is thrown as is. Any exception thrown by
     * the consumers will suppress the original error and be returned for propagation.
     * Even if the original error is fatal, if it passes the predicate then it can
     * be processed and recovered from (see {@link Exceptions#throwIfFatal(Throwable)}).
     *
     * @param causePredicate the {@link Predicate} to use to determine if a failure
     *                       should be recovered from.
     * @param errorConsumer  the {@link BiConsumer<Throwable, Object>} to process the recovered errors with.
     *                       It must deal with potential {@code null}s.
     * @return a new {@link OnNextFailureStrategy} that allows resuming the sequence.
     */
    static OnNextFailureStrategy resumeIf(
            Predicate<Throwable> causePredicate,
            BiConsumer<Throwable, Object> errorConsumer) {
        return new OnNextFailureStrategy.ResumeStrategy(causePredicate, errorConsumer);
    }

    //==== IMPLEMENTATIONS ====
//    OnNextFailureStrategy STOP = new OnNextFailureStrategy() {
//
//        @Override
//        public boolean test(Throwable error, @Nullable Object value) {
//            return false;
//        }
//
//        @Override
//        public Throwable process(Throwable error, @Nullable Object value, Context context) {
//            Exceptions.throwIfFatal(error);
//            Throwable iee = new IllegalStateException("STOP strategy cannot process errors");
//            iee.addSuppressed(error);
//            return iee;
//        }
//    };

    OnNextFailureStrategy RESUME_DROP = new OnNextFailureStrategy.ResumeDropStrategy(null);

    final class ResumeStrategy implements OnNextFailureStrategy {

        final Predicate<Throwable> errorPredicate;
        final BiConsumer<Throwable, Object> errorConsumer;

        ResumeStrategy(@Nullable Predicate<Throwable> errorPredicate,
                       BiConsumer<Throwable, Object> errorConsumer) {
            this.errorPredicate = errorPredicate;
            this.errorConsumer = errorConsumer;
        }

        @Override
        public boolean test(Throwable error, @Nullable Object value) {
            return errorPredicate == null || errorPredicate.test(error);
        }

        @Override
        @Nullable
        public Throwable process(Throwable error, @Nullable Object value, Context context) {
//            if (errorPredicate == null) {
//                Exceptions.throwIfFatal(error);
//            } else if (!errorPredicate.test(error)) {
//                Exceptions.throwIfFatal(error);
//                return error;
//            }
//            try {
//                errorConsumer.accept(error, value);
//                return null;
//            } catch (Throwable e) {
//                return Exceptions.addSuppressed(e, error);
//            }
            return null;
        }
    }

    final class ResumeDropStrategy implements OnNextFailureStrategy {

        final Predicate<Throwable> errorPredicate;

        ResumeDropStrategy(@Nullable Predicate<Throwable> errorPredicate) {
            this.errorPredicate = errorPredicate;
        }

        @Override
        public boolean test(Throwable error, @Nullable Object value) {
            return errorPredicate == null || errorPredicate.test(error);
        }

        @Override
        @Nullable
        public Throwable process(Throwable error, @Nullable Object value, Context context) {
//            if (errorPredicate == null) {
//                Exceptions.throwIfFatal(error);
//            } else if (!errorPredicate.test(error)) {
//                Exceptions.throwIfFatal(error);
//                return error;
//            }
//            try {
//                if (value != null) {
//                    Operators.onNextDropped(value, context);
//                }
//                Operators.onErrorDropped(error, context);
//                return null;
//            } catch (Throwable e) {
//                return Exceptions.addSuppressed(e, error);
//            }
            return null;
        }
    }

    final class LambdaOnNextErrorStrategy implements OnNextFailureStrategy {

        private final BiFunction<? super Throwable, Object, ? extends Throwable> delegateProcessor;
        private final BiPredicate<? super Throwable, Object> delegatePredicate;

        @SuppressWarnings("unchecked")
        public LambdaOnNextErrorStrategy(
                BiFunction<? super Throwable, Object, ? extends Throwable> delegateProcessor) {
            this.delegateProcessor = delegateProcessor;
            if (delegateProcessor instanceof BiPredicate) {
                this.delegatePredicate = (BiPredicate<? super Throwable, Object>) delegateProcessor;
            } else {
                this.delegatePredicate = (e, v) -> true;
            }
        }

        @Override
        public boolean test(Throwable error, @Nullable Object value) {
            return delegatePredicate.test(error, value);
        }

        @Override
        @Nullable
        public Throwable process(Throwable error, @Nullable Object value, Context ignored) {
            return delegateProcessor.apply(error, value);
        }
    }
}

