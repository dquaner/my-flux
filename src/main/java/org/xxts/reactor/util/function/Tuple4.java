package org.xxts.reactor.util.function;

import org.xxts.reactor.util.annotation.NonNull;
import org.xxts.reactor.util.annotation.Nullable;

import java.io.Serial;
import java.util.Objects;
import java.util.function.Function;

public class Tuple4<T1, T2, T3, T4> extends Tuple3<T1, T2, T3> {
    @Serial
    private static final long serialVersionUID = -4898704078143033129L;
    @NonNull
    final T4 t4;

    Tuple4(T1 t1, T2 t2, T3 t3, T4 t4) {
        super(t1, t2, t3);
        this.t4 = Objects.requireNonNull(t4, "t4");
    }

    @NonNull
    public T4 getT4() {
        return this.t4;
    }

    public <R> Tuple4<R, T2, T3, T4> mapT1(Function<T1, R> mapper) {
        return new Tuple4<>(mapper.apply(this.t1), this.t2, this.t3, this.t4);
    }

    public <R> Tuple4<T1, R, T3, T4> mapT2(Function<T2, R> mapper) {
        return new Tuple4<>(this.t1, mapper.apply(this.t2), this.t3, this.t4);
    }

    public <R> Tuple4<T1, T2, R, T4> mapT3(Function<T3, R> mapper) {
        return new Tuple4<>(this.t1, this.t2, mapper.apply(this.t3), this.t4);
    }

    public <R> Tuple4<T1, T2, T3, R> mapT4(Function<T4, R> mapper) {
        return new Tuple4<>(this.t1, this.t2, this.t3, mapper.apply(this.t4));
    }

    @Nullable
    public Object get(int index) {
        return switch (index) {
            case 0 -> this.t1;
            case 1 -> this.t2;
            case 2 -> this.t3;
            case 3 -> this.t4;
            default -> null;
        };
    }

    public Object[] toArray() {
        return new Object[]{this.t1, this.t2, this.t3, this.t4};
    }

    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Tuple4)) {
            return false;
        } else if (!super.equals(o)) {
            return false;
        } else {
            Tuple4 tuple4 = (Tuple4)o;
            return this.t4.equals(tuple4.t4);
        }
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.t4.hashCode();
        return result;
    }

    public int size() {
        return 4;
    }
}
