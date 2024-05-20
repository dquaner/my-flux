package org.xxts.reactor.util.function;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.io.Serial;
import java.util.Objects;
import java.util.function.Function;

/**
 * A tuple that holds six values
 *
 * @param <T1> The type of the first value held by this tuple
 * @param <T2> The type of the second value held by this tuple
 * @param <T3> The type of the third value held by this tuple
 * @param <T4> The type of the fourth value held by this tuple
 * @param <T5> The type of the fifth value held by this tuple
 * @param <T6> The type of the sixth value held by this tuple
 */
public class Tuple6<T1, T2, T3, T4, T5, T6> extends Tuple5<T1, T2, T3, T4, T5> {

	@Serial
	private static final long serialVersionUID = 770306356087176830L;

	@NonNull final T6 t6;

	Tuple6(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
		super(t1, t2, t3, t4, t5);
		this.t6 = Objects.requireNonNull(t6, "t6");
	}

	/**
	 * Type-safe way to get the sixth object of this {@link Tuples}.
	 *
	 * @return The sixth object
	 */
	public T6 getT6() {
		return t6;
	}

	/**
	 * Map the 1st part (T1) of this {@link Tuple6} into a different value and type,
	 * keeping the other parts.
	 *
	 * @param mapper the mapping {@link Function} for the T1 part
	 * @param <R> the new type for the T1 part
	 * @return a new {@link Tuple6} with a different T1 value
	 */
	public <R> Tuple6<R, T2, T3, T4, T5, T6> mapT1(Function<T1, R> mapper) {
		return new Tuple6<>(mapper.apply(t1), t2, t3, t4, t5, t6);
	}

	/**
	 * Map the 2nd part (T2) of this {@link Tuple6} into a different value and type,
	 * keeping the other parts.
	 *
	 * @param mapper the mapping {@link Function} for the T2 part
	 * @param <R> the new type for the T2 part
	 * @return a new {@link Tuple6} with a different T2 value
	 */
	public <R> Tuple6<T1, R, T3, T4, T5, T6> mapT2(Function<T2, R> mapper) {
		return new Tuple6<>(t1, mapper.apply(t2), t3, t4, t5, t6);
	}

	/**
	 * Map the 3rd part (T3) of this {@link Tuple6} into a different value and type,
	 * keeping the other parts.
	 *
	 * @param mapper the mapping {@link Function} for the T3 part
	 * @param <R> the new type for the T3 part
	 * @return a new {@link Tuple6} with a different T3 value
	 */
	public <R> Tuple6<T1, T2, R, T4, T5, T6> mapT3(Function<T3, R> mapper) {
		return new Tuple6<>(t1, t2, mapper.apply(t3), t4, t5, t6);
	}

	/**
	 * Map the 4th part (T4) of this {@link Tuple6} into a different value and type,
	 * keeping the other parts.
	 *
	 * @param mapper the mapping {@link Function} for the T4 part
	 * @param <R> the new type for the T4 part
	 * @return a new {@link Tuple6} with a different T4 value
	 */
	public <R> Tuple6<T1, T2, T3, R, T5, T6> mapT4(Function<T4, R> mapper) {
		return new Tuple6<>(t1, t2, t3, mapper.apply(t4), t5, t6);
	}

	/**
	 * Map the 5th part (T5) of this {@link Tuple6} into a different value and type,
	 * keeping the other parts.
	 *
	 * @param mapper the mapping {@link Function} for the T5 part
	 * @param <R> the new type for the T5 part
	 * @return a new {@link Tuple6} with a different T5 value
	 */
	public <R> Tuple6<T1, T2, T3, T4, R, T6> mapT5(Function<T5, R> mapper) {
		return new Tuple6<>(t1, t2, t3, t4, mapper.apply(t5), t6);
	}

	/**
	 * Map the 6th part (T6) of this {@link Tuple6} into a different value and type,
	 * keeping the other parts.
	 *
	 * @param mapper the mapping {@link Function} for the T6 part
	 * @param <R> the new type for the T6 part
	 * @return a new {@link Tuple6} with a different T6 value
	 */
	public <R> Tuple6<T1, T2, T3, T4, T5, R> mapT6(Function<T6, R> mapper) {
		return new Tuple6<>(t1, t2, t3, t4, t5, mapper.apply(t6));
	}

	@Nullable
	@Override
	public Object get(int index) {
        return switch (index) {
            case 0 -> t1;
            case 1 -> t2;
            case 2 -> t3;
            case 3 -> t4;
            case 4 -> t5;
            case 5 -> t6;
            default -> null;
        };
	}

	@Override
	public Object[] toArray() {
		return new Object[]{t1, t2, t3, t4, t5, t6};
	}

	@Override
	public int size() {
		return 6;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) return true;
		if (!(o instanceof @SuppressWarnings("rawtypes")Tuple6 tuple6)) return false;
		if (!super.equals(o)) return false;

        return t6.equals(tuple6.t6);

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + t6.hashCode();
		return result;
	}

}
