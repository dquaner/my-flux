package org.xxts.reactor.util.function;

import org.xxts.reactor.util.annotation.NonNull;
import org.xxts.reactor.util.annotation.Nullable;

import java.io.Serial;
import java.util.Objects;
import java.util.function.Function;

public class Tuple6<T1, T2, T3, T4, T5, T6> extends Tuple5<T1, T2, T3, T4, T5> {
    @Serial
    private static final long serialVersionUID = 770306356087176830L;
    @NonNull
    final T6 t6;

    Tuple6(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
        super(t1, t2, t3, t4, t5);
        this.t6 = Objects.requireNonNull(t6, "t6");
    }

    @NonNull
    public T6 getT6() {
        return this.t6;
    }

    public <R> Tuple6<R, T2, T3, T4, T5, T6> mapT1(Function<T1, R> mapper) {
        return new Tuple6<>(mapper.apply(this.t1), this.t2, this.t3, this.t4, this.t5, this.t6);
    }

    public <R> Tuple6<T1, R, T3, T4, T5, T6> mapT2(Function<T2, R> mapper) {
        return new Tuple6<>(this.t1, mapper.apply(this.t2), this.t3, this.t4, this.t5, this.t6);
    }

    public <R> Tuple6<T1, T2, R, T4, T5, T6> mapT3(Function<T3, R> mapper) {
        return new Tuple6<>(this.t1, this.t2, mapper.apply(this.t3), this.t4, this.t5, this.t6);
    }

    public <R> Tuple6<T1, T2, T3, R, T5, T6> mapT4(Function<T4, R> mapper) {
        return new Tuple6<>(this.t1, this.t2, this.t3, mapper.apply(this.t4), this.t5, this.t6);
    }

    public <R> Tuple6<T1, T2, T3, T4, R, T6> mapT5(Function<T5, R> mapper) {
        return new Tuple6<>(this.t1, this.t2, this.t3, this.t4, mapper.apply(this.t5), this.t6);
    }

    public <R> Tuple6<T1, T2, T3, T4, T5, R> mapT6(Function<T6, R> mapper) {
        return new Tuple6<>(this.t1, this.t2, this.t3, this.t4, this.t5, mapper.apply(this.t6));
    }

    @Nullable
    public Object get(int index) {
        return switch (index) {
            case 0 -> this.t1;
            case 1 -> this.t2;
            case 2 -> this.t3;
            case 3 -> this.t4;
            case 4 -> this.t5;
            case 5 -> this.t6;
            default -> null;
        };
    }

    public Object[] toArray() {
        return new Object[]{this.t1, this.t2, this.t3, this.t4, this.t5, this.t6};
    }

    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Tuple6)) {
            return false;
        } else if (!super.equals(o)) {
            return false;
        } else {
            Tuple6 tuple6 = (Tuple6)o;
            return this.t6.equals(tuple6.t6);
        }
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.t6.hashCode();
        return result;
    }

    public int size() {
        return 6;
    }
}
