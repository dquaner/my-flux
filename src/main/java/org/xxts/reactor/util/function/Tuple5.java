package org.xxts.reactor.util.function;

import org.xxts.reactor.util.annotation.NonNull;
import org.xxts.reactor.util.annotation.Nullable;

import java.io.Serial;
import java.util.Objects;
import java.util.function.Function;

public class Tuple5<T1, T2, T3, T4, T5> extends Tuple4<T1, T2, T3, T4> {
    @Serial
    private static final long serialVersionUID = 3541548454198133275L;
    @NonNull
    final T5 t5;

    Tuple5(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
        super(t1, t2, t3, t4);
        this.t5 = Objects.requireNonNull(t5, "t5");
    }

    @NonNull
    public T5 getT5() {
        return this.t5;
    }

    public <R> Tuple5<R, T2, T3, T4, T5> mapT1(Function<T1, R> mapper) {
        return new Tuple5<>(mapper.apply(this.t1), this.t2, this.t3, this.t4, this.t5);
    }

    public <R> Tuple5<T1, R, T3, T4, T5> mapT2(Function<T2, R> mapper) {
        return new Tuple5<>(this.t1, mapper.apply(this.t2), this.t3, this.t4, this.t5);
    }

    public <R> Tuple5<T1, T2, R, T4, T5> mapT3(Function<T3, R> mapper) {
        return new Tuple5<>(this.t1, this.t2, mapper.apply(this.t3), this.t4, this.t5);
    }

    public <R> Tuple5<T1, T2, T3, R, T5> mapT4(Function<T4, R> mapper) {
        return new Tuple5<>(this.t1, this.t2, this.t3, mapper.apply(this.t4), this.t5);
    }

    public <R> Tuple5<T1, T2, T3, T4, R> mapT5(Function<T5, R> mapper) {
        return new Tuple5<>(this.t1, this.t2, this.t3, this.t4, mapper.apply(this.t5));
    }

    @Nullable
    public Object get(int index) {
        return switch (index) {
            case 0 -> this.t1;
            case 1 -> this.t2;
            case 2 -> this.t3;
            case 3 -> this.t4;
            case 4 -> this.t5;
            default -> null;
        };
    }

    public Object[] toArray() {
        return new Object[]{this.t1, this.t2, this.t3, this.t4, this.t5};
    }

    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Tuple5)) {
            return false;
        } else if (!super.equals(o)) {
            return false;
        } else {
            Tuple5 tuple5 = (Tuple5)o;
            return this.t5.equals(tuple5.t5);
        }
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.t5.hashCode();
        return result;
    }

    public int size() {
        return 5;
    }
}

