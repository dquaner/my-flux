package org.xxts.reactor.util.context;

import org.xxts.reactor.util.annotation.Nullable;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * A read-only view of a collection of key/value pairs that is propagated between components
 * such as operators via the context protocol. Contexts are ideal to transport orthogonal
 * information such as tracing or security tokens.
 * <br>
 * 通过上下文协议在操作符等组件之间传播的键/值对集合的只读视图。
 * 上下文是传输正交信息(指两个或多个序列信号之间存在的特定的关系，如追踪或安全令牌)的理想选择。
 *
 * <p>
 * {@link Context} is an immutable variant of the same key/value pairs structure which exposes
 * a write API that returns new instances on each write.
 * <br>
 * {@link Context} 是同一键/值对结构的不可变变量，它提供了一个写API，在每次写入时返回新实例。
 */
public interface ContextView {

    /**
     * Resolve a value given a key that exists within the {@link Context}, or throw
     * a {@link NoSuchElementException} if the key is not present.
     *
     * @param key a lookup key to resolve the value within the context
     * @param <T> an unchecked cast generic for fluent typing convenience
     *
     * @return the value resolved for this key (throws if key not found)
     *
     * @throws NoSuchElementException when the given key is not present
     * @see #getOrDefault(Object, Object)
     * @see #getOrEmpty(Object)
     * @see #hasKey(Object)
     */
    <T> T get(Object key);

    /**
     * Resolve a value given a type key within the {@link Context}.
     *
     * @param key a type key to resolve the value within the context
     * @param <T> an unchecked casted generic for fluent typing convenience
     *
     * @return the value resolved for this type key (throws if key not found)
     *
     * @throws NoSuchElementException when the given type key is not present
     * @see #getOrDefault(Object, Object)
     * @see #getOrEmpty(Object)
     */
    default <T> T get(Class<T> key) {
        T v = get((Object) key);
        if (key.isInstance(v)) {
            return v;
        }
        throw new NoSuchElementException("Context does not contain a value of type " + key.getName());
    }

    /**
     * Resolve a value given a key within the {@link Context}. If unresolved return the
     * passed default value.
     *
     * @param key a lookup key to resolve the value within the context
     * @param defaultValue a fallback value if key doesn't resolve
     *
     * @return the value resolved for this key, or the given default if not present
     */
    @Nullable
    default <T> T getOrDefault(Object key, @Nullable T defaultValue) {
        if (!hasKey(key)) {
            return defaultValue;
        }
        return get(key);
    }

    /**
     * Resolve a value given a key within the {@link Context}.
     *
     * @param key a lookup key to resolve the value within the context
     *
     * @return an {@link Optional} of the value for that key.
     */
    default <T> Optional<T> getOrEmpty(Object key) {
        if (hasKey(key)) {
            return Optional.of(get(key));
        }
        return Optional.empty();
    }

    /**
     * Return true if a particular key resolves to a value within the {@link Context}.
     *
     * @param key a lookup key to test for
     *
     * @return true if this context contains the given key
     */
    boolean hasKey(Object key);

    /**
     * Return true if the {@link Context} is empty.
     *
     * @return true if the {@link Context} is empty.
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Return the size of this {@link Context}, the number of immutable key/value pairs stored inside it.
     *
     * @return the size of the {@link Context}
     */
    int size();

    /**
     * Stream key/value pairs from this {@link Context}
     *
     * @return a {@link Stream} of key/value pairs held by this context
     */
    Stream<Map.Entry<Object, Object>> stream();

    /**
     * Perform the given action for each entry in this {@link ContextView}. If the action throws an
     * exception, it is immediately propagated to the caller and the remaining items
     * will not be processed.
     *
     * @param action The action to be performed for each entry
     * @throws NullPointerException if the specified action is null
     */
    default void forEach(BiConsumer<Object, Object> action) {
        stream().forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

}
