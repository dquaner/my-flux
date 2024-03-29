package org.xxts.reactor.util.context;

import java.util.AbstractMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

record Context1(Object key, Object value) implements CoreContext {

    Context1(Object key, Object value) {
        this.key = Objects.requireNonNull(key, "key");
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public Context put(Object key, Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        if (this.key.equals(key)) {
            return new Context1(key, value);
        }

        return new Context2(this.key, this.value, key, value);
    }

    @Override
    public Context delete(Object key) {
        Objects.requireNonNull(key, "key");
        if (this.key.equals(key)) {
            return Context.empty();
        }
        return this;
    }

    @Override
    public boolean hasKey(Object key) {
        return this.key.equals(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        if (hasKey(key)) {
            return (T) this.value;
        }
        throw new NoSuchElementException("Context does not contain key: " + key);
    }

    @Override
    public Stream<Map.Entry<Object, Object>> stream() {
        return Stream.of(new AbstractMap.SimpleImmutableEntry<>(key, value));
    }

    @Override
    public void forEach(BiConsumer<Object, Object> action) {
        action.accept(key, value);
    }

    @Override
    public Context putAllInto(Context base) {
        return base.put(key, value);
    }

    @Override
    public void unsafePutAllInto(ContextN other) {
        other.accept(key, value);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public String toString() {
        return "Context1{" + key + '=' + value + '}';
    }

}
