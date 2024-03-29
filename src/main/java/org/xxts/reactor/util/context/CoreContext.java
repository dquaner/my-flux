package org.xxts.reactor.util.context;

import java.util.Map;

/**
 * Abstract base to optimize interactions between reactor core {@link Context} implementations.
 * <br>
 * 抽象基础，用来优化 reactor 核心 {@link Context} 实现之间的交互。
 */
interface CoreContext extends Context {

    @Override
    default boolean isEmpty() {
        // Overridden in Context0#isEmpty
        return false;
    }

    @Override
    default Context putAll(ContextView other) {
        if (other.isEmpty()) return this;

        if (other instanceof CoreContext coreContext) {
            return coreContext.putAllInto(this);
        }

        ContextN newContext = new ContextN(this.size() + other.size());
        this.unsafePutAllInto(newContext);
        other.stream().sequential().forEach(newContext);
        if (newContext.size() <= 5) {
            // make it return Context{1-5}
            return Context.of((Map<?, ?>) newContext);
        }
        return newContext;
    }

    /**
     * Let this Context add its internal values to the given base Context, avoiding creating
     * intermediate holders for key-value pairs as much as possible.
     * <br>
     * 让当前 Context 将它的内部值添加到给定的 base Context 中，尽可能避免为键值对创建中间持有者。
     *
     * @param base the {@link Context} in which we're putting all our values
     * @return a new context containing all the base values merged with all our values
     */
    Context putAllInto(Context base);

    /**
     * Let this Context add its internal values to the given ContextN, <strong>mutating it</strong>,
     * but avoiding creating intermediate holders for key-value pairs as much as possible.
     * <br>
     * 让当前 Context 将它的内部值添加到给定的 ContextN 中，<strong>改变它</strong>，但尽可能避免为键值对创建中间持有者。
     *
     * @param other the {@link ContextN} we're mutating by putting all our values into
     */
    void unsafePutAllInto(ContextN other);

}
