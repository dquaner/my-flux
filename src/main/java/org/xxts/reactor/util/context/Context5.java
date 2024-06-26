package org.xxts.reactor.util.context;

import java.util.AbstractMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

record Context5(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3, Object key4,
                Object value4, Object key5, Object value5) implements CoreContext {

    Context5(Object key1, Object value1,
             Object key2, Object value2,
             Object key3, Object value3,
             Object key4, Object value4,
             Object key5, Object value5) {
        //TODO merge null check and duplicate check in the util method
        Context4.checkKeys(key1, key2, key3, key4, key5);
        this.key1 = key1;
        this.value1 = Objects.requireNonNull(value1, "value1");
        this.key2 = key2;
        this.value2 = Objects.requireNonNull(value2, "value2");
        this.key3 = key3;
        this.value3 = Objects.requireNonNull(value3, "value3");
        this.key4 = key4;
        this.value4 = Objects.requireNonNull(value4, "value4");
        this.key5 = key5;
        this.value5 = Objects.requireNonNull(value5, "value5");
    }

    @Override
    public Context put(Object key, Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        if (this.key1.equals(key)) {
            return new Context5(key, value, key2, value2, key3, value3, key4, value4, key5, value5);
        }

        if (this.key2.equals(key)) {
            return new Context5(key1, value1, key, value, key3, value3, key4, value4, key5, value5);
        }

        if (this.key3.equals(key)) {
            return new Context5(key1, value1, key2, value2, key, value, key4, value4, key5, value5);
        }

        if (this.key4.equals(key)) {
            return new Context5(key1, value1, key2, value2, key3, value3, key, value, key5, value5);
        }

        if (this.key5.equals(key)) {
            return new Context5(key1, value1, key2, value2, key3, value3, key4, value4, key, value);
        }

        return new ContextN(key1, value1, key2, value2, key3, value3, key4, value4, key5, value5, key, value);
    }

    @Override
    public Context delete(Object key) {
        Objects.requireNonNull(key, "key");

        if (this.key1.equals(key)) {
            return new Context4(key2, value2, key3, value3, key4, value4, key5, value5);
        }

        if (this.key2.equals(key)) {
            return new Context4(key1, value1, key3, value3, key4, value4, key5, value5);
        }

        if (this.key3.equals(key)) {
            return new Context4(key1, value1, key2, value2, key4, value4, key5, value5);
        }

        if (this.key4.equals(key)) {
            return new Context4(key1, value1, key2, value2, key3, value3, key5, value5);
        }

        if (this.key5.equals(key)) {
            return new Context4(key1, value1, key2, value2, key3, value3, key4, value4);
        }

        return this;
    }

    @Override
    public boolean hasKey(Object key) {
        return this.key1.equals(key) || this.key2.equals(key) || this.key3.equals(key)
                || this.key4.equals(key) || this.key5.equals(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        if (this.key1.equals(key)) {
            return (T) this.value1;
        }
        if (this.key2.equals(key)) {
            return (T) this.value2;
        }
        if (this.key3.equals(key)) {
            return (T) this.value3;
        }
        if (this.key4.equals(key)) {
            return (T) this.value4;
        }
        if (this.key5.equals(key)) {
            return (T) this.value5;
        }
        throw new NoSuchElementException("Context does not contain key: " + key);
    }

    @Override
    public int size() {
        return 5;
    }

    @Override
    public Stream<Map.Entry<Object, Object>> stream() {
        return Stream.of(
                new AbstractMap.SimpleImmutableEntry<>(key1, value1),
                new AbstractMap.SimpleImmutableEntry<>(key2, value2),
                new AbstractMap.SimpleImmutableEntry<>(key3, value3),
                new AbstractMap.SimpleImmutableEntry<>(key4, value4),
                new AbstractMap.SimpleImmutableEntry<>(key5, value5));
    }

    @Override
    public void forEach(BiConsumer<Object, Object> action) {
        action.accept(key1, value1);
        action.accept(key2, value2);
        action.accept(key3, value3);
        action.accept(key4, value4);
        action.accept(key5, value5);
    }

    @Override
    public Context putAllInto(Context base) {
        return base
                .put(this.key1, this.value1)
                .put(this.key2, this.value2)
                .put(this.key3, this.value3)
                .put(this.key4, this.value4)
                .put(this.key5, this.value5);
    }

    @Override
    public void unsafePutAllInto(ContextN other) {
        other.accept(key1, value1);
        other.accept(key2, value2);
        other.accept(key3, value3);
        other.accept(key4, value4);
        other.accept(key5, value5);
    }

    @Override
    public String toString() {
        return "Context5{" + key1 + '=' + value1 + ", " + key2 + '=' + value2 + ", " +
                key3 + '=' + value3 + ", " + key4 + '=' + value4 + ", " + key5 + '=' + value5 + '}';
    }

}
