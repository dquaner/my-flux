package org.xxts.reactor.util.function;

import org.xxts.reactor.util.annotation.NonNull;
import org.xxts.reactor.util.annotation.Nullable;

import java.io.Serial;
import java.util.Objects;
import java.util.function.Function;

public class Tuple3<T1, T2, T3> extends Tuple2<T1, T2> {
    @Serial
    private static final long serialVersionUID = -4430274211524723033L;
    @NonNull
    final T3 t3;

    Tuple3(T1 t1, T2 t2, T3 t3) {
        super(t1, t2);
        this.t3 = Objects.requireNonNull(t3, "t3");
    }

    @NonNull
    public T3 getT3() {
        return this.t3;
    }

    public <R> Tuple3<R, T2, T3> mapT1(Function<T1, R> mapper) {
        return new Tuple3<>(mapper.apply(this.t1), this.t2, this.t3);
    }

    public <R> Tuple3<T1, R, T3> mapT2(Function<T2, R> mapper) {
        return new Tuple3<>(this.t1, mapper.apply(this.t2), this.t3);
    }

    public <R> Tuple3<T1, T2, R> mapT3(Function<T3, R> mapper) {
        return new Tuple3<>(this.t1, this.t2, mapper.apply(this.t3));
    }

    @Nullable
    public Object get(int index) {
        return switch (index) {
            case 0 -> this.t1;
            case 1 -> this.t2;
            case 2 -> this.t3;
            default -> null;
        };
    }

    public Object[] toArray() {
        return new Object[]{this.t1, this.t2, this.t3};
    }

    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Tuple3)) {
            return false;
        } else if (!super.equals(o)) {
            return false;
        } else {
            Tuple3 tuple3 = (Tuple3)o;
            return this.t3.equals(tuple3.t3);
        }
    }

    public int size() {
        return 3;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.t3.hashCode();
        return result;
    }
}
