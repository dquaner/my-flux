package org.xxts.reactor.util.context;

import org.xxts.reactor.util.annotation.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * A key/value store that is propagated between components such as operators via the
 *  context protocol. Contexts are ideal to transport orthogonal information such as
 *  tracing or security tokens.
 * <br>
 * 通过上下文协议在操作符等组件之间传播的键/值对集合的只读视图。
 * 上下文是传输正交信息(指两个或多个序列信号之间存在的特定的关系，如追踪或安全令牌)的理想选择。
 * <p>
 *
 * {@link Context} implementations are thread-safe and immutable: mutative operations like
 * {@link #put(Object, Object)} will in fact return a new {@link Context} instance.
 * <br>
 * {@link Context} 实现是线程安全且不可变的：像 {@link #put(Object, Object)} 这样的可变操作
 * 实际上会返回一个新的 {@link Context} 实例。
 * <p>
 *
 * Note that contexts are optimized for low cardinality key/value storage, and a user
 * might want to associate a dedicated mutable structure to a single key to represent his
 * own context instead of using multiple {@link #put}, which could be more costly.
 * Past five user key/value pair, the {@link Context} will use a copy-on-write
 * implementation backed by a new {@link java.util.Map} on each {@link #put}.
 * <br>
 * 注意：上下文针对的低基数键/值存储进行了优化，
 * 用户可能希望将一个专用的可变结构关联到单个键上以表示自己的上下文，而不是使用多次 {@link #put} 方法，但这种操作可能更昂贵。
 * 超过5个用户键/值对后，{@link Context} 将使用写时复制的实现方式，在每次 {@link #put} 时创建一个新的 {@link java.util.Map}。
 * <br>
 * 解释：
 * 1. 低基数意思是存储的键/值对的种类或数量相对较少；
 * 2. Context{0-5} 直接持有键值对，ContextN 继承自 LinkedHashMap；
 * 3. 更新频繁或数据结构较庞大时，多次 {@link #put} 操作成本较高。
 * <br>
 * 然后呢？
 * 不知道理解是否正确：因为用户可能希望将一个专用的可变结构关联到单个键上以表示自己的上下文，就代表着低基数的上下文用例比较多，且每次
 * 更新的数据结构庞大，如果统一使用 Map 结构，性能比较低。
 * 但不论怎样，每次 put 返回的都是新建的实例。
 */
public interface Context extends ContextView {

    /**
     * Return an empty {@link Context}
     *
     * @return an empty {@link Context}
     */
    static Context empty() {
        return Context0.INSTANCE;
    }

    /**
     * Create a {@link Context} pre-initialized with one key-value pair.
     *
     * @param key the key to initialize.
     * @param value the value for the key.
     * @return a {@link Context} with a single entry.
     * @throws NullPointerException if either key or value are null
     */
    static Context of(Object key, Object value) {
        return new Context1(key, value);
    }

    /**
     * Create a {@link Context} pre-initialized with two key-value pairs.
     *
     * @param key1 the first key to initialize.
     * @param value1 the value for the first key.
     * @param key2 the second key to initialize.
     * @param value2 the value for the second key.
     * @return a {@link Context} with two entries.
     * @throws NullPointerException if any key or value is null
     */
    static Context of(Object key1, Object value1,
                                           Object key2, Object value2) {
        return new Context2(key1, value1, key2, value2);
    }

    /**
     * Create a {@link Context} pre-initialized with three key-value pairs.
     *
     * @param key1 the first key to initialize.
     * @param value1 the value for the first key.
     * @param key2 the second key to initialize.
     * @param value2 the value for the second key.
     * @param key3 the third key to initialize.
     * @param value3 the value for the third key.
     * @return a {@link Context} with three entries.
     * @throws NullPointerException if any key or value is null
     */
    static Context of(Object key1, Object value1,
                                           Object key2, Object value2,
                                           Object key3, Object value3) {
        return new Context3(key1, value1, key2, value2, key3, value3);
    }

    /**
     * Create a {@link Context} pre-initialized with four key-value pairs.
     *
     * @param key1 the first key to initialize.
     * @param value1 the value for the first key.
     * @param key2 the second key to initialize.
     * @param value2 the value for the second key.
     * @param key3 the third key to initialize.
     * @param value3 the value for the third key.
     * @param key4 the fourth key to initialize.
     * @param value4 the value for the fourth key.
     * @return a {@link Context} with four entries.
     * @throws NullPointerException if any key or value is null
     */
    static Context of(Object key1, Object value1,
                                           Object key2, Object value2,
                                           Object key3, Object value3,
                                           Object key4, Object value4) {
        return new Context4(key1, value1, key2, value2, key3, value3, key4, value4);
    }

    /**
     * Create a {@link Context} pre-initialized with five key-value pairs.
     *
     * @param key1 the first key to initialize.
     * @param value1 the value for the first key.
     * @param key2 the second key to initialize.
     * @param value2 the value for the second key.
     * @param key3 the third key to initialize.
     * @param value3 the value for the third key.
     * @param key4 the fourth key to initialize.
     * @param value4 the value for the fourth key.
     * @param key5 the fifth key to initialize.
     * @param value5 the value for the fifth key.
     * @return a {@link Context} with five entries.
     * @throws NullPointerException if any key or value is null
     */
    static Context of(Object key1, Object value1,
                                           Object key2, Object value2,
                                           Object key3, Object value3,
                                           Object key4, Object value4,
                                           Object key5, Object value5) {
        return new Context5(key1, value1, key2, value2, key3, value3, key4, value4, key5, value5);
    }

    /**
     * Create a {@link Context} out of a {@link Map}. Prefer this method if you're somehow
     * incapable of checking keys are all distinct in other {@link #of(Object, Object, Object, Object, Object, Object, Object, Object)}
     * implementations.
     *
     * @implNote this method compacts smaller maps into a relevant fields-based implementation
     * when map size is less than 6.
     */
    static Context of(Map<?, ?> map) {
        int size = Objects.requireNonNull(map, "map").size();
        if (size == 0) return Context.empty();
        if (size <= 5) {
            Map.Entry[] entries = map.entrySet().toArray(new Map.Entry[size]);
            switch (size) {
                case 1:
                    return new Context1(entries[0].getKey(), entries[0].getValue());
                case 2:
                    return new Context2(entries[0].getKey(), entries[0].getValue(),
                            entries[1].getKey(), entries[1].getValue());
                case 3:
                    return new Context3(entries[0].getKey(), entries[0].getValue(),
                            entries[1].getKey(), entries[1].getValue(),
                            entries[2].getKey(), entries[2].getValue());
                case 4:
                    return new Context4(entries[0].getKey(), entries[0].getValue(),
                            entries[1].getKey(), entries[1].getValue(),
                            entries[2].getKey(), entries[2].getValue(),
                            entries[3].getKey(), entries[3].getValue());
                case 5:
                    return new Context5(entries[0].getKey(), entries[0].getValue(),
                            entries[1].getKey(), entries[1].getValue(),
                            entries[2].getKey(), entries[2].getValue(),
                            entries[3].getKey(), entries[3].getValue(),
                            entries[4].getKey(), entries[4].getValue());
            }
        }
        // Since ContextN(Map) is a low level API that DOES NOT perform null checks,
        // we need to check every key/value before passing it to ContextN(Map)
        map.forEach((key, value) -> {
            Objects.requireNonNull(key, "null key found");
            if (value == null) {
                throw new NullPointerException("null value for key " + key);
            }
        });
        @SuppressWarnings("unchecked")
        final Map<Object, Object> generifiedMap = (Map<Object, Object>) map;
        return new ContextN(generifiedMap);
    }

    /**
     * Create a {@link Context} out of a {@link ContextView}, enabling write API on top of
     * the read-only view. If the {@link ContextView} is already a {@link Context}, return
     * the same instance.
     *
     * @param contextView the {@link ContextView} to convert (or cast) to {@link Context}
     * @return the converted {@link Context} for further modifications
     */
    static Context of(ContextView contextView) {
        Objects.requireNonNull(contextView, "contextView");
        if (contextView instanceof Context) {
            return (Context) contextView;
        }
        return Context.empty().putAll(contextView);
    }

    /**
     * Switch to the {@link ContextView} interface, which only allows reading from the
     * context.
     * @return the {@link ContextView} of this context
     */
    default ContextView readOnly() {
        return this;
    }

    /**
     * Create a new {@link Context} that contains all current key/value pairs plus the
     * given key/value pair. If that key existed in the current Context, its associated
     * value is replaced in the resulting {@link Context}.
     *
     * @param key the key to add/update in the new {@link Context}
     * @param value the value to associate to the key in the new {@link Context}
     *
     * @return a new {@link Context} including the provided key/value
     * @throws NullPointerException if either the key or value are null
     */
    Context put(Object key, Object value);

    /**
     * Create a new {@link Context} that contains all current key/value pairs plus the
     * given key/value pair <strong>only if the value is not {@literal null}</strong>. If that key existed in the
     * current Context, its associated value is replaced in the resulting {@link Context}.
     *
     * @param key the key to add/update in the new {@link Context}
     * @param valueOrNull the value to associate to the key in the new {@link Context}, null to ignore the operation
     *
     * @return a new {@link Context} including the provided key/value, or the same {@link Context} if value is null
     * @throws NullPointerException if the key is null
     */
    default Context putNonNull(Object key, @Nullable Object valueOrNull) {
        if (valueOrNull != null) {
            return put(key, valueOrNull);
        }
        return this;
    }

    /**
     * Return a new {@link Context} that will resolve all existing keys except the
     * removed one, {@code key}.
     * <p>
     * Note that if this {@link Context} doesn't contain the key, this method simply
     * returns this same instance.
     *
     * @param key the key to remove.
     * @return a new {@link Context} that doesn't include the provided key
     */
    Context delete(Object key);

    /**
     * Create a new {@link Context} by merging the content of this context and a given
     * {@link ContextView}. If the other context is empty, the same {@link Context} instance
     * is returned.
     *
     * @param other the other {@link ContextView} from which to copy entries
     * @return a new {@link Context} with a merge of the entries from this context and the given context.
     */
    default Context putAll(ContextView other) {
        if (other.isEmpty()) return this;

        if (other instanceof CoreContext) {
            CoreContext coreContext = (CoreContext) other;
            return coreContext.putAllInto(this);
        }

        ContextN newContext = new ContextN(this.size() + other.size());
        this.stream().sequential().forEach(newContext);
        other.stream().sequential().forEach(newContext);
        if (newContext.size() <= 5) {
            // make it return Context{1-5}
            return Context.of((Map<?, ?>) newContext);
        }
        return newContext;
    }

    /**
     * Create a new {@link Context} by merging the content of this context and a given
     * {@link Map}. If the {@link Map} is empty, the same {@link Context} instance
     * is returned.
     *
     * @param from the {@link Map} from which to include entries in the resulting {@link Context}.
     * @return a new {@link Context} with a merge of the entries from this context and the given {@link Map}.
     */
    default Context putAllMap(Map<?, ?> from) {
        if (from.isEmpty()) {
            return this;
        }

        ContextN combined = new ContextN(this.size() + from.size());
        this.forEach(combined);
        from.forEach(combined);
        if (combined.size() <= 5) {
            return Context.of((Map<?, ?>) combined);
        }
        return combined;
    }

}
