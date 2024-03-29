package org.xxts.reactor.util.context;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

final class Context0 implements CoreContext {

    static final Context0 INSTANCE = new Context0();

    @Override
    public Context put(Object key, Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        return new Context1(key, value);
    }

    @Override
    public Context delete(Object key) {
        return this;
    }

    @Override
    public <T> T get(Object key) {
        throw new NoSuchElementException("Context is empty");
    }

    @Override
    public boolean hasKey(Object key) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public String toString() {
        return "Context0{}";
    }

    @Override
    public Stream<Map.Entry<Object, Object>> stream() {
        return Stream.empty();
    }

    @Override
    public void forEach(BiConsumer<Object, Object> action) {
    }

    @Override
    public Context putAllInto(Context base) {
        return base;
    }

    @Override
    public void unsafePutAllInto(ContextN other) {
    }
}
