package org.xxts.reactor.core;

import org.xxts.reactor.util.annotation.Nullable;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Indicates that a task or resource can be cancelled/disposed.
 * <p>
 * Call to the dispose method is/should be idempotent(幂等的).
 */
@FunctionalInterface
public interface Disposable {

    /**
     * Cancel or dispose the underlying task or resource.
     * <br> 取消或处置底层任务或资源。
     * <p>
     * Implementations are required to make this method idempotent.
     */
    void dispose();

    /**
     * Optionally return {@literal true} when the resource or task is disposed.
     * <p>
     * Implementations are not required to track disposition and as such may never
     * return {@literal true} even when disposed. However, they MUST only return true
     * when there's a guarantee the resource or task is disposed.
     * <br> 此方法的实现不需要去追踪处置，如果追踪的话即使在处置时也可能永远不返回 {@literal true}。
     * 但是，实现必须保证资源或任务被处置了才能返回 {@literal true}。
     *
     * @return {@literal true} when there's a guarantee the resource or task is disposed.
     */
    default boolean isDisposed() {
        return false;
    }

    /**
     * A {@link Disposable} container that allows updating/replacing its inner Disposable
     * atomically and with respect of disposing the container itself.
     * <br>
     * 一个 {@link Disposable} 容器，允许自动更新/替换其内部的 {@link Disposable}，并允许处置容器本身。
     */
    interface Swap extends Disposable, Supplier<Disposable> {

        /**
         * Atomically set the next {@link Disposable} on this container and dispose the previous
         * one (if any). If the container has been disposed, fall back to disposing {@code next}.
         *
         * @param next the {@link Disposable} to set, may be null
         * @return true if the operation succeeded, false if the container has been disposed
         * @see #replace(Disposable)
         */
        boolean update(@Nullable Disposable next);

        /**
         * Atomically set the next {@link Disposable} on this container but don't dispose the previous
         * one (if any). If the container has been disposed, fall back to disposing {@code next}.
         *
         * @param next the {@link Disposable} to set, may be null
         * @return true if the operation succeeded, false if the container has been disposed
         * @see #update(Disposable)
         */
        boolean replace(@Nullable Disposable next);
    }

    /**
     * A container of {@link Disposable} that is itself {@link Disposable}. Accumulate
     * disposables and dispose them all in one go by using {@link #dispose()}. Using
     * the {@link #add(Disposable)} methods give ownership to the container, which is now
     * responsible for disposing them. You can however retake ownership of individual
     * elements by keeping a reference and using {@link #remove(Disposable)}, which puts
     * the responsibility of disposing said elements back in your hands. Note that once
     * disposed, the container cannot be reused, and you will need a new {@link Disposable.Composite}.
     * <br>
     * 一个 {@link Disposable} 容器，其本身也是 {@link Disposable}。
     * 通过 {@link #dispose()} 方法可以累积并一起处置 disposables。
     * 使用 {@link #add(Disposable)} 方法赋予容器所有权，代表容器现在负责处置它们。
     * 但是，可以通过保留引用并使用 {@link #remove(Disposable)} 来重新获得单个元素的所有权，这将把处置该元素的责任交还给您。
     * 注意，容器一旦被处置，就不能被重用，您将需要一个新的 {@link Disposable.Composite}。
     */
    interface Composite extends Disposable {

        /**
         * Atomically mark the container as {@link #isDisposed() disposed}, clear it and then
         * dispose all the previously contained Disposables. From there on the container cannot
         * be reused, as {@link #add(Disposable)} and {@link #addAll(Collection)} methods
         * will immediately return {@literal false}.
         */
        @Override
        void dispose();

        /**
         * Indicates if the container has already been disposed.
         * <p>Note that if that is the case, attempts to add new disposable to it via
         * {@link #add(Disposable)} and {@link #addAll(Collection)} will be rejected.
         *
         * @return true if the container has been disposed, false otherwise.
         */
        @Override
        boolean isDisposed();

        /**
         * Add a {@link Disposable} to this container, if it is not {@link #isDisposed() disposed}.
         * Otherwise, d is disposed immediately.
         *
         * @param d the {@link Disposable} to add.
         * @return true if the disposable could be added, false otherwise.
         */
        boolean add(Disposable d);

        /**
         * Adds the given collection of Disposables to the container or disposes them
         * all if the container has been disposed.
         *
         * @implNote The default implementation is not atomic, meaning that if the container is
         * disposed while the content of the collection is added, first elements might be
         * effectively added. Stronger consistency is enforced by composites created via
         * {@link Disposables#composite()} variants.
         * <br> 该默认实现不是原子性的，这意味着如果在添加集合内容的同时处置容器，则可能会有效地添加第一个元素。
         * 通过 {@link Disposables#composite()} 方法创建的组合实现了更强的一致性。
         * @param ds the collection of Disposables
         * @return true if the operation was successful, false if the container has been disposed
         */
        default boolean addAll(Collection<? extends Disposable> ds) {
            boolean abort = isDisposed();
            for (Disposable d : ds) {
                if (abort) {
                    //multi-add aborted,
                    d.dispose();
                }
                else {
                    //update the abort flag by attempting to add the disposable
                    //if not added, it has already been disposed. we abort and will
                    //dispose all subsequent Disposable
                    abort = !add(d);
                }
            }
            return !abort;
        }

        /**
         * Delete the {@link Disposable} from this container, without disposing it.
         * <p>
         * It becomes the responsibility of the caller to dispose the value themselves,
         * which they can do by a simple call to {@link Disposable#dispose()} on said
         * value (probably guarded by a check that this method returned true, meaning the
         * disposable was actually in the container).
         * <p>
         *     它把处理值的责任交给了调用者，可以通过对所述值的简单调用 {@link Disposable#dispose()} 来完成
         *     （可能通过检查该方法是否返回 true 来保证，返回 true 意味着可处理的值实际上在容器中）。
         * </p>
         *
         * @param d the {@link Disposable} to remove.
         * @return true if the disposable was successfully deleted, false otherwise.
         */
        boolean remove(Disposable d);

        /**
         * Returns the number of currently held Disposables.
         * @return the number of currently held Disposables
         */
        int size();
    }
}

