package org.xxts.reactor.core;

import org.xxts.reactivestreams.Subscriber;
import org.xxts.reactivestreams.Subscription;
import org.xxts.reactor.core.scheduler.Scheduler;
import org.xxts.reactor.util.annotation.Nullable;
import org.xxts.reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A Scannable component exposes state in a non strictly memory consistent way and
 * results should be understood as best-effort hint of the underlying state. This is
 * useful to retro-engineer a component graph such as a flux operator chain via
 * {@link Stream} queries from
 * {@link #actuals()}, {@link #parents()} and {@link #inners()}. This allows for
 * visiting patterns and possibly enable serviceability features.
 * <p>
 * Scannable is also a useful tool for the advanced user eager to learn which kind
 * of state we usually manage in the package-scope schedulers or operators
 * implementations.
 * <p>
 * 一个 Scannable 组件以一种不严格要求内存一致的方式公开状态，并且应该被理解为底层状态的最佳提示。
 * 通过 {@link #actuals()}，{@link #parents()} 和 {@link #inners()} 获取 {@link Stream}
 * 对回溯组件图（例如一个 flux operator chain ）非常有用。
 * 允许访问模式，并可能启用可服务特性。
 * <p>
 * Scannable 对于那些渴望了解在 package-scope schedulers 或 operators 的实现中我们通常管理哪种状态的高级用户来说，
 * 也是一个有用的工具。
 */
@FunctionalInterface
public interface Scannable {

    /**
     * This method is used internally by components to define their key-value mappings
     * in a single place. Although it is ignoring the generic type of the {@link Attr} key,
     * implementors should take care to return values of the correct type, and return
     * {@literal null} if no specific value is available.
     * <p>
     *     在一个组件内部使用此方法来定义它的 key-value mappings in a single place。
     *     虽然此方法忽略 {@link Attr} key 的泛型，
     *     但具体实现应该注意返回正确类型的值，以及在指定值不可用时返回 null。
     * </p>
     * <p>
     * For public consumption of attributes, prefer using {@link #scan(Attr)}, which will
     * return a typed value and fall back to the key's default if the component didn't
     * define any mapping.
     * <p>
     *     对于公共使用的属性，推荐使用 {@link #scan(Attr)}，它会返回指定类型的值，并且在组件没有定义 mapping 时返回默认值。
     * </p>
     *
     * @param key a {@link Attr} to resolve for the component.
     * @return the value associated to the key for that specific component, or null if none.
     */
    @Nullable
    Object scanUnsafe(Attr key);

    /**
     * Base class for {@link Scannable} attributes, which all can define a meaningful
     * default.
     * <p>
     * {@link Scannable} 属性的基类，属性都可以定义默认值。
     *
     * @implNote Note that some attributes define an object-to-T converter, which means their
     * private {@link #tryConvert(Object)} method can safely be used by
     * {@link Scannable#scan(Attr)}, making them resilient to class cast exceptions.
     * <br> 注意，一些属性定义了 object-to-T 的转换器，这意味着它们私有的 {@link #tryConvert(Object)} 方法
     * 可以被 {@link Scannable#scan(Attr)} 安全地使用，使它们能够适应类强制转换异常。
     *
     * @param <T> the type of data associated with an attribute
     *
     * @see Scannable#scanUnsafe(Attr)
     * @see Scannable#scan(Attr)
     * @see Scannable#scanOrDefault(Attr, Object)
     */
    class Attr<T> {

        final T defaultValue;
        /**
         * 转换不要抛出异常，可以返回 null
         * @see #tryConvert(Object)
         */
        final Function<Object, ? extends T> safeConverter;

        protected Attr(@Nullable T defaultValue) {
            this(defaultValue, null);
        }

        protected Attr(@Nullable T defaultValue,
                       @Nullable Function<Object, ? extends T> safeConverter) {
            this.defaultValue = defaultValue;
            this.safeConverter = safeConverter;
        }

        /**
         * Meaningful and always applicable default value for the attribute, returned
         * instead of {@literal null} when a specific value hasn't been defined for a
         * component. {@literal null} if no sensible generic default is available.
         *
         * @return the default value applicable to all components or null if none.
         */
        @Nullable
        public T defaultValue() {
            return defaultValue;
        }

        /**
         * Checks if this attribute is capable of safely converting any Object into
         * a {@code T} via {@link #tryConvert(Object)} (potentially returning {@code null}
         * or a Null Object for incompatible raw values).
         *
         * @return true if the attribute can safely convert any object, false if it can
         * throw {@link ClassCastException}
         */
        boolean isConversionSafe() {
            return safeConverter != null;
        }

        /**
         * Attempt to convert any {@link Object} instance o into a {@code T}. By default,
         * unsafe attributes will just try forcing a cast, which can lead to {@link ClassCastException}.
         * However, attributes for which {@link #isConversionSafe()} returns true are
         * required to not throw an exception (but rather return {@code null} or a Null
         * Object).
         * <p>
         * 尝试转换任意的 {@link Object} 实例 o 到类型 {@code T}。
         * 默认情况下，unsafe attributes 会尝试强制转换，有可能导致 {@link ClassCastException} 异常。
         * 但是，要求 {@link #isConversionSafe()} 为 true 的 attributes 不要抛出异常（可以返回 {@code null} 或一个 Null 对象）。
         * </p>
         *
         * @param o the instance to attempt conversion on
         * @return the converted instance
         */
        @Nullable
        T tryConvert(@Nullable Object o) {
            if (o == null) {
                return null;
            }
            if (safeConverter == null) {
                @SuppressWarnings("unchecked") T t = (T) o;
                return t;
            }
            return safeConverter.apply(o);
        }

        /**
         * A constant that represents {@link Scannable} returned via {@link #from(Object)}
         * when the passed non-null reference is not a {@link Scannable}
         */
        static final Scannable UNAVAILABLE_SCAN = new Scannable() {
            @Override
            public Object scanUnsafe(Attr key) {
                return null;
            }

            @Override
            public boolean isScanAvailable() {
                return false;
            }

            @Override
            public String toString() {
                return "UNAVAILABLE_SCAN";
            }

            @Override
            public String stepName() {
                return "UNAVAILABLE_SCAN";
            }
        };

        /**
         * A constant that represents {@link Scannable} returned via {@link #from(Object)}
         * when the passed reference is null
         */
        static final Scannable NULL_SCAN = new Scannable() {
            @Override
            public Object scanUnsafe(Attr key) {
                return null;
            }

            @Override
            public boolean isScanAvailable() {
                return false;
            }

            @Override
            public String toString() {
                return "NULL_SCAN";
            }

            @Override
            public String stepName() {
                return "NULL_SCAN";
            }
        };

        /**
         * The direct dependent component downstream reference if any. Operators in
         * Flux/Mono for instance delegate to a target Subscriber, which is going to be
         * the actual chain navigated with this reference key. Subscribers are not always
         * {@link Scannable}, but this attribute will convert these raw results to an
         * {@link Scannable#isScanAvailable() unavailable scan} object in this case.
         * <p>
         * 组件直接依赖的下游（如果有的话）的 Attr Key。
         * 例如 Flux/Mono 中的 operators 委托给一个 Subscriber，这个 Subscriber 就是
         * 可以使用此 reference key 进行 navigate 的 actual chain。
         * Subscribers 并非总是 {@link Scannable}，这种情况下，这个 Attr 会将这些原始结果
         * 转换为 {@link Scannable#isScanAvailable() unavailable scan} 对象。
         * </p>
         * <p>
         * A reference chain downstream can be navigated via {@link Scannable#actuals()}.
         * <p>
         * {@link Scannable#actuals()} 使用这个 key 递归导航下游链。
         * </p>
         */
        public static final Attr<Scannable> ACTUAL = new Attr<>(null,
                Scannable::from);

        /**
         * Parent key exposes the direct upstream relationship of the scanned component.
         * It can be a Publisher source to an operator, a Subscription to a Subscriber
         * (main flow if ambiguous with inner Subscriptions like flatMap), a Scheduler to
         * a Worker. These types are not always {@link Scannable}, but this attribute
         * will convert such raw results to an {@link Scannable#isScanAvailable() unavailable scan}
         * object in this case.
         * <p>
         * 该 Attr Key 暴露组件的直接上游。
         * 它可以是：
         *  一个 Publisher source 到一个 operator，
         *  一个 Subscription 到一个 Subscriber (main flow if ambiguous with inner Subscriptions like flatMap)，
         *  一个 Scheduler 到一个 Worker。
         * 这些上游并非总是 {@link Scannable}，这种情况下，这个 Attr 会将这些原始结果
         * 转换为 {@link Scannable#isScanAvailable() unavailable scan} 对象。
         * </p>
         * <p>
         * {@link Scannable#parents()} can be used to navigate the parent chain.
         * <p>
         * {@link Scannable#parents()} 使用这个 key 递归导航上游链。
         * </p>
         */
        public static final Attr<Scannable> PARENT = new Attr<>(null,
                Scannable::from);

        /**
         * An arbitrary(任意) name given to the operator component. Defaults to {@literal null}.
         */
        public static final Attr<String> NAME = new Attr<>(null);

        /**
         * LIFTER attribute exposes name of the lifter function. It is calculated as {@link Object#toString} of a function passed to the
         * {@link org.xxts.reactor.core.publisher.Operators#lift} or {@link org.xxts.reactor.core.publisher.Operators#liftPublisher}.
         * Defaults to {@literal null}.
         * <p>
         *     LIFTER 属性暴露 lifter 函数的名字。
         *     他是传递给 {@link org.xxts.reactor.core.publisher.Operators#lift} 或 {@link org.xxts.reactor.core.publisher.Operators#liftPublisher}
         *     的函数，并通过 {@link Object#toString} 计算。
         * </p>
         */
        public static final Attr<String> LIFTER = new Attr<>(null);

        /**
         * Indicate that for some purposes a {@link Scannable} should be used as additional
         * source of information about a contiguous {@link Scannable} in the chain.
         * <p>
         * For example {@link Scannable#steps()} uses this to collate the
         * {@link Scannable#stepName() stepName} of an assembly trace to its
         * wrapped operator (the one before it in the assembly chain).
         * <p>
         *     表明出于某些目的，一个 {@link Scannable} 应该被用于链中一个 contiguous {@link Scannable} 的附加信息源。
         * </p>
         * <p>
         *     例如，{@link Scannable#steps()} 使用这个属性来对照
         *     assembly(装配，组装) trace 和它的 wrapped operator (the one before it in the assembly chain) 的 stepName。
         * </p>
         */
        public static final Attr<Boolean> ACTUAL_METADATA = new Attr<>(false);

        /**
         * A {@link Integer} attribute implemented by components with a backlog
         * capacity. It will expose current queue size or similar related to
         * user-provided held data. Note that some operators and processors CAN keep
         * a backlog larger than {@code Integer.MAX_VALUE}, in which case
         * the {@link Attr#LARGE_BUFFERED Attr} {@literal LARGE_BUFFERED}
         * should be used instead. Such operators will attempt to serve a BUFFERED
         * query but will return {@link Integer#MIN_VALUE} when actual buffer size is
         * oversize for int.
         * <p>
         *     具有积压容量的组件实现的 {@link Integer} 属性。它会公开当前队列的大小或者用户提供的保存数据量。
         *     注意，一些 operators 和 processors 可以保留大于 {@code Integer.MAX_VALUE} 的积压量，这种情况下
         *     可以使用 {@link Attr#LARGE_BUFFERED Attr} {@literal LARGE_BUFFERED}。
         *     带有该属性的 operators 将尝试提供一个 BUFFERED 查询，但当实际 buffer size 超过 Integer 最大值时
         *     返回 {@link Integer#MIN_VALUE}。
         * </p>
         */
        public static final Attr<Integer> BUFFERED = new Attr<>(0);

        /**
         * Similar to {@link Attr#BUFFERED}, but reserved for operators that can hold
         * a backlog of items that can grow beyond {@literal Integer.MAX_VALUE}. These
         * operators will also answer to a {@link Attr#BUFFERED} query up to the point
         * where their buffer is actually too large, at which point they'll return
         * {@literal Integer.MIN_VALUE}, which serves as a signal that this attribute
         * should be used instead. Defaults to {@literal null}.
         * <p>
         *     与 {@link Attr#BUFFERED} 相似，但保留给可以保存可能超出 {@literal Integer.MAX_VALUE} 积压项的 operators。
         *     这些 operators 也将回答 {@link Attr#BUFFERED} 查询，如果它们的缓冲区实际上太大，
         *     此时它们将返回 {@literal Integer.MIN_VALUE}，它作为应该使用此属性的信号。
         * </p>
         * <p>
         * {@code Flux.flatMap}, {@code Flux.filterWhen}
         * and {@code Flux.window} (with overlap) are known to use this attribute.
         */
        public static final Attr<Long> LARGE_BUFFERED = new Attr<>(null);

        /**
         * Return an {@link Integer} capacity when no {@link #PREFETCH} is defined or
         * when an arbitrary maximum limit is applied to the backlog capacity of the
         * scanned component. {@link Integer#MAX_VALUE} signal unlimited capacity.
         * <p>
         *     当 未定义 {@link #PREFETCH} 或 对扫描组件的积压容量应用了任意限制 时，返回 {@link Integer} 容量。
         *     {@link Integer#MAX_VALUE} 意味着无限容量。
         * </p>
         * <p>
         * Note: This attribute usually resolves to a constant value.
         */
        public static final Attr<Integer> CAPACITY = new Attr<>(0);

        /**
         * Prefetch is an {@link Integer} attribute defining the rate of processing in a
         * component which has capacity to request and hold a backlog of data. It
         * usually maps to a component capacity when no arbitrary {@link #CAPACITY} is
         * set. {@link Integer#MAX_VALUE} signal unlimited capacity and therefore
         * unbounded demand.
         * <p>
         *     Prefetch 是一个 {@link Integer} 属性，它定义了一个组件的处理速率，该组件具有请求和保存积压数据的能力。
         *     当没有设置 {@link #CAPACITY} 时，它通常表示组件容量。{@link Integer#MAX_VALUE} 表示无限的容量，因此无限的需求。
         * </p>
         * <p>
         * Note: This attribute usually resolves to a constant value.
         */
        public static final Attr<Integer> PREFETCH = new Attr<>(0);

        /**
         * A {@link Long} attribute exposing the current pending demand of a downstream
         * component. Note that {@link Long#MAX_VALUE} indicates an unbounded (push-style)
         * demand as specified in {@link Subscription#request(long)}.
         * <p>
         *     一个 {@link Long} 属性，暴露了下游组件的当前待处理需求。
         *     注意 {@link Long#MAX_VALUE} 表示在 {@link Subscription#request(long)} 中指定 unbounded (push-style) 需求。
         * </p>
         */
        public static final Attr<Long> REQUESTED_FROM_DOWNSTREAM = new Attr<>(0L);

        /**
         * A {@link Boolean} attribute indicating whether a downstream component
         * has interrupted consuming this scanned component, e.g., a cancelled
         * subscription. Note that it differs from {@link #TERMINATED} which is
         * intended for "normal" shutdown cycles.
         * <p>
         *     一个  {@link Boolean} 属性，指示下游组件是否中断了对该扫描组件的消费。e.g., a cancelled subscription。
         *     注意，它与 {@link #TERMINATED} 不同，后者用于“正常”关闭周期。
         * </p>
         */
        public static final Attr<Boolean> CANCELLED = new Attr<>(false);

        /**
         * A {@link Boolean} attribute indicating whether an upstream component
         * terminated this scanned component. e.g. a post onComplete/onError subscriber.
         * By opposition to {@link #CANCELLED} which determines if a downstream
         * component interrupted this scanned component.
         * <p>
         *     一个 {@link Boolean} 属性，指示上游组件是否终止了被扫描的组件。a post onComplete/onError subscriber。
         *     与 {@link #CANCELLED} 相反，它确定下游组件是否中断了此扫描的组件。
         * </p>
         */
        public static final Attr<Boolean> TERMINATED = new Attr<>(false);

        /**
         * Delay_Error exposes a {@link Boolean} whether the scanned component
         * actively supports error delaying if it manages a backlog instead of fast
         * error-passing which might drop pending backlog.
         * <p>
         *     Delay_Error 暴露了一个 {@link Boolean}，表示如果被扫描的组件管理一个积压
         *     而不是快速的错误传递，那么它是否主动支持错误延迟。
         * </p>
         * <p>
         * Note: This attribute usually resolves to a constant value.
         */
        public static final Attr<Boolean> DELAY_ERROR = new Attr<>(false);

        /**
         * a {@link Throwable} attribute which indicate an error state if the scanned
         * component keeps track of it.
         * <p>
         *     一个 {@link Throwable} 属性，指示错误状态。
         * </p>
         */
        public static final Attr<Throwable> ERROR = new Attr<>(null);

        /**
         * A key that links a {@link Scannable} to another {@link Scannable} it runs on.
         * Usually exposes a link between an operator/subscriber and its {@link Scheduler.Worker} or
         * {@link Scheduler}, provided these are {@link Scannable}. Will return
         * {@link Attr#UNAVAILABLE_SCAN} if the supporting execution is not Scannable or
         * {@link Attr#NULL_SCAN} if the operator doesn't define a specific runtime.
         * <p>
         *     通常公开 operator/subscriber 与其 {@link Scheduler.Worker} 或 {@link Scheduler} 之间的链接，前提是它们都是 {@link Scannable}。
         *     如果支持的执行不是可扫描的，则返回 {@link Attr#UNAVAILABLE_SCAN}；
         *     如果操作符没有定义特定的 runtime，则返回 {@link Attr#NULL_SCAN}。
         * </p>
         */
        public static final Attr<Scannable> RUN_ON = new Attr<>(null, Scannable::from);

        /**
         * A {@link Stream} of {@link Tuple2} representing key/value pairs for tagged components.
         * Defaults to {@literal null}.
         */
        public static final Attr<Stream<Tuple2<String, String>>> TAGS = new Attr<>(null);

        /**
         * An {@link RunStyle} enum attribute indicating whether an operator continues to operate on the same thread.
         * Each value provides a different degree of guarantee from weakest {@link RunStyle#UNKNOWN} to strongest {@link RunStyle#SYNC}.
         * <p>
         *     一个 {@link RunStyle} 枚举属性，指示 operator 是否继续在同一线程上操作。
         *     每个值提供了不同程度的保证，从最弱的 {@link RunStyle#UNKNOWN} 到最强的 {@link RunStyle#SYNC}。
         * </p>
         * <p>
         * Defaults to {@link RunStyle#UNKNOWN}.
         */
        public static final Attr<RunStyle> RUN_STYLE = new Attr<>(RunStyle.UNKNOWN);

        /**
         * An {@link Enum} enumerating the different styles an operator can run :
         * their {@link #ordinal()} reflects the level of confidence in their running mode
         * <p>
         *     枚举了算子可以运行的不同风格：它们的 {@link #ordinal()} 反应了运行模式的置信等级。
         * </p>
         */
        public enum RunStyle {
            /**
             * no guarantees can be given on the running mode (default value, weakest level of guarantee)
             * <p>
             *     默认值，对运行模式不做任何保证，最低的保证水平
             * </p>
             */
            UNKNOWN,

            /**
             * the operator may change threads while running
             * <p>
             *     算子可以在运行时改变线程
             * </p>
             */
            ASYNC,

            /**
             * guarantees the operator doesn't change threads (strongest level of guarantee)
             * <p>
             *     保证算子不会改变线程，最强的保证水平
             * </p>
             */
            SYNC;
        }

        /**
         * 递归 scan
         */
        static Stream<? extends Scannable> recurse(Scannable _s, Attr<Scannable> key) {
            Scannable s = Scannable.from(_s.scan(key));
            if (!s.isScanAvailable()) {
                return Stream.empty();
            }
            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(new Iterator<>() {
                        Scannable c = s;

                        @Override
                        public boolean hasNext() {
                            return c != null && c.isScanAvailable();
                        }

                        @Override
                        public Scannable next() {
                            Scannable _c = c;
                            c = Scannable.from(c.scan(key));
                            return _c;
                        }
                    }, 0),
                    false);
        }
    }

    /**
     * Attempt to cast the Object to a {@link Scannable}. Return {@link Attr#NULL_SCAN} if
     * the value is null, or {@link Attr#UNAVAILABLE_SCAN} if the value is not a {@link Scannable}.
     * Both are constant {@link Scannable} that return false on {@link Scannable#isScanAvailable}.
     * <p>
     *     尝试转换一个 Object 到 {@link Scannable}。
     * </p>
     *
     * @param o a reference to cast
     * @return the cast {@link Scannable}, or one of two default {@link Scannable} instances
     * that aren't actually scannable (one for nulls, one for non-scannable references)
     */
    static Scannable from(@Nullable Object o) {
        if (o == null) {
            return Attr.NULL_SCAN;
        }
        if (o instanceof Scannable) {
            return ((Scannable) o);
        }
        return Attr.UNAVAILABLE_SCAN;
    }

    /**
     * Return true whether the component is available for {@link #scan(Attr)} resolution.
     *
     * @return true whether the component is available for {@link #scan(Attr)} resolution.
     */
    default boolean isScanAvailable() {
        return true;
    }

    /**
     * Introspect a component's specific state {@link Attr attribute}, returning an
     * associated value specific to that component, or the default value associated with
     * the key, or null if the attribute doesn't make sense for that particular component
     * and has no sensible default.
     * <p>
     *     检查当前组件的指定 {@link Attr attribute}，返回相关值，或者 key Attr 的默认值。
     *     如果这个 key Attr 对于当前组件没有意义并且没有合理的默认值，则返回 null。
     * </p>
     *
     * @param key a {@link Attr} to resolve for the component.
     * @return a value associated to the key or null if unmatched or unresolved
     */
    @Nullable
    default <T> T scan(Attr<T> key) {
        // note tryConvert will just plain cast most of the time
        // except e.g. for Attr<Scannable>
        T value = key.tryConvert(scanUnsafe(key));
        if (value == null)
            return key.defaultValue();
        return value;
    }

    /**
     * Introspect a component's specific state {@link Attr attribute}. If there's no
     * specific value in the component for that key, fall back to returning the
     * provided non-null default.
     *
     * @param key          a {@link Attr} to resolve for the component.
     * @param defaultValue a fallback value if key resolve to {@literal null}
     * @return a value associated to the key or the provided default if unmatched or unresolved
     */
    default <T> T scanOrDefault(Attr<T> key, T defaultValue) {
        T v;
        //note tryConvert will just plain cast most of the time
        //except e.g. for Attr<Scannable>
        v = key.tryConvert(scanUnsafe(key));

        if (v == null) {
            return Objects.requireNonNull(defaultValue, "defaultValue");
        }
        return v;
    }

    /**
     * Return a {@link Stream} navigating the {@link Subscriber}
     * chain (downward). The current {@link Scannable} is not included.
     * <p>
     * 返回一个导航 {@link Subscriber} 链（向下）的 {@link Stream}。不包括当前 {@link Scannable}。
     *
     * @return a {@link Stream} navigating the {@link Subscriber}
     * chain (downward, current {@link Scannable} not included).
     */
    default Stream<? extends Scannable> actuals() {
        return Attr.recurse(this, Attr.ACTUAL);
    }

    /**
     * Return a {@link Stream} navigating the {@link Subscription}
     * chain (upward). The current {@link Scannable} is not included.
     * <p>
     * 返回一个导航 {@link Subscription} 链（向上）的 {@link Stream}。不包括当前 {@link Scannable}。
     * </p>
     *
     * @return a {@link Stream} navigating the {@link Subscription}
     * chain (upward, current {@link Scannable} not included).
     */
    default Stream<? extends Scannable> parents() {
        return Attr.recurse(this, Attr.PARENT);
    }

    /**
     * Return a {@link Stream} of referenced inners (flatmap, multicast etc.)
     * <p>
     * 返回一个内部函数(flatmap, multicast etc.) {@link Stream}。
     * </p>
     *
     * @return a {@link Stream} of referenced inners (flatmap, multicast etc.)
     */
    default Stream<? extends Scannable> inners() {
        return Stream.empty();
    }

    /**
     * Check this {@link Scannable} and its {@link #parents()} for a user-defined name and
     * return the first one that is reachable, or default to this {@link Scannable}
     * {@link #stepName()} if none.
     * <p>
     * 检查当前 {@link Scannable} 及其 {@link #parents()} 是否有用户定义的 {@link Attr#NAME}，并返回第一个可访问的名称，
     * 如果没有，则默认为当前 {@link Scannable} 的 {@link #stepName()}。
     * </p>
     *
     * @return the name of the first parent that has one defined (including this scannable)
     */
    default String name() {
        String thisName = this.scan(Attr.NAME);
        if (thisName != null) {
            return thisName;
        }

        return parents()
                .map(s -> s.scan(Attr.NAME))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(stepName());
    }

    /**
     * Return a meaningful {@link String} representation of this {@link Scannable} in
     * its chain of {@link #parents()} and {@link #actuals()}.
     * <p>
     * 返回当前 {@link Scannable} 在 {@link #parents()} 和 {@link #actuals()} 链中的一个有意义的 {@link String} 表示。
     * </p>
     */
    default String stepName() {
        /*
         * Strip an operator name of various prefixes and suffixes.
         * <p>
         * 去除 operator 名称的各种前缀和后缀。
         *
         * @param name the operator name, usually simpleClassName or fully-qualified classname.
         *          通常是simpleClassName或完全限定的类名。
         * @return the stripped operator name
         */
        String name = getClass().getName();
        // 去除内部类名
        int innerClassIndex = name.indexOf('$');
        if (innerClassIndex != -1) {
            name = name.substring(0, innerClassIndex);
        }
        // 去除包名
        int stripPackageIndex = name.lastIndexOf('.');
        if (stripPackageIndex != -1) {
            name = name.substring(stripPackageIndex + 1);
        }
        // 去除算子不相关名
        String stripped = OPERATOR_NAME_UNRELATED_WORDS_PATTERN
                .matcher(name)
                .replaceAll("");

        if (!stripped.isEmpty()) {
            return stripped.substring(0, 1).toLowerCase() + stripped.substring(1);
        }
        return stripped;
    }

    /**
     * List the step names in the chain of {@link Scannable} (including the current element),
     * in their assembly order. This traverses the chain of {@link Scannable} both upstream
     * ({@link #parents()}) and downstream ({@link #actuals()}).
     * <ol>
     *     <li>if the current Scannable is a {@link Subscriber}, the chain can reach down to
     *     the final subscriber, provided it is {@link Scannable} (eg. lambda subscriber)</li>
     *     <li>if it is an operator the chain can reach up to the source, if it is a Reactor
     *     source (that is {@link Scannable}).</li>
     * </ol>
     *
     * @return a {@link Stream} of {@link #stepName()} for each discovered step in the {@link Scannable} chain
     */
    default Stream<String> steps() {
        List<Scannable> chain = new ArrayList<>(parents().toList());
        Collections.reverse(chain);
        chain.add(this);
        chain.addAll(actuals().toList());

        List<String> chainNames = new ArrayList<>(chain.size());
        for (int i = 0; i < chain.size(); i++) {
            Scannable step = chain.get(i);
            Scannable stepAfter = null;
            if (i < chain.size() - 1) {
                stepAfter = chain.get(i + 1);
            }
            //noinspection ConstantConditions
            // if stepAfter is ACTUAL_METADATA: add stepAfter name and skip stepAfter
            // else: add step name
            if (stepAfter != null && Boolean.TRUE.equals(stepAfter.scan(Attr.ACTUAL_METADATA))) {
                chainNames.add(stepAfter.stepName());
                i++;
            } else {
                chainNames.add(step.stepName());
            }
        }

        return chainNames.stream();
    }

    /**
     * Visit this {@link Scannable} and its {@link #parents()}, starting by the furthest reachable parent,
     * and return a {@link Stream} of the tags which includes duplicates and outputs tags in declaration order
     * (grandparent tag(s) &gt; parent tag(s)  &gt; current tag(s)).
     * <p>
     * Tags can only be discovered until no parent can be inspected, which happens either
     * when the source publisher has been reached or when a non-reactor intermediate operator
     * is present in the parent chain (i.e. a stage that is not {@link Scannable} for {@link Attr#PARENT}).
     * <p>
     *     访问当前 {@link Scannable} 和它的 {@link #parents()}，从最远可达的 parent 开始，
     *     返回一个包含重复 tags 的 {@link Stream}，并按声明顺序输出 tags（祖父母标签 &gt; 父母标签 &gt; 当前标签）。
     * </p>
     * <p>
     *     只有没有 parent 可以被检查了，标签才能被发现，这种情况要么发生在 source publisher 已经到达，
     *     要么发生在父节点链中存在 non-reactor 的中间操作符（即 {@link Attr#PARENT} 不可扫描的阶段）。
     * </p>
     *
     * @return the stream of tags for this {@link Scannable} and its reachable parents, including duplicates
     * @see #tagsDeduplicated()
     */
    default Stream<Tuple2<String, String>> tags() {
        List<Scannable> sources = new LinkedList<>();

        Scannable aSource = this;
        while (aSource != null && aSource.isScanAvailable()) {
            sources.add(0, aSource);
            aSource = aSource.scan(Attr.PARENT);
        }

        return sources.stream()
                .flatMap(source -> source.scanOrDefault(Attr.TAGS, Stream.empty()));
    }

    /**
     * Visit this {@link Scannable} and its {@link #parents()}, starting by the furthest reachable parent,
     * deduplicate tags that have a common key by favoring the value declared last (current tag(s) &gt; parent tag(s) &gt; grandparent tag(s))
     * and return a {@link Map} of the deduplicated tags. Note that while the values are the "latest", the key iteration order reflects
     * the tags' declaration order.
     * <p>
     * Tags can only be discovered until no parent can be inspected, which happens either
     * when the source publisher has been reached or when a non-reactor intermediate operator
     * is present in the parent chain (i.e. a stage that is not {@link Scannable} for {@link Attr#PARENT}).
     *
     * @return a {@link Map} of deduplicated tags from this {@link Scannable} and its reachable parents
     * @see #tags()
     */
    default Map<String, String> tagsDeduplicated() {
        return tags().collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2,
                (s1, s2) -> s2, LinkedHashMap::new));
    }

    /**
     * The pattern for matching words unrelated to operator name.
     * Used to strip an operator name of various prefixes and suffixes.
     * <p>
     * 匹配与操作符名称无关的单词。用于去除操作符名称中的各种前缀和后缀。
     * </p>
     */
    Pattern OPERATOR_NAME_UNRELATED_WORDS_PATTERN =
            Pattern.compile("Parallel|Flux|Mono|Publisher|Subscriber|Fuseable|Operator|Conditional");

}
