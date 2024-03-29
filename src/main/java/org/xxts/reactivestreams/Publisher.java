package org.xxts.reactivestreams;

/**
 * A {@link Publisher} is a provider of a potentially unbounded number of sequenced elements, publishing them according to
 * the demand received from its {@link Subscriber}(s).
 * <br> 一个 {@link Publisher} 提供潜在的无限数量的有序元素，并且根据从它的 {@link Subscriber}(s) 那里收到的需求发布这些元素。
 * <p>
 * A {@link Publisher} can serve multiple {@link Subscriber}s subscribed {@link Publisher#subscribe(Subscriber)} dynamically
 * at various points in time.
 * <br> 一个 {@link Publisher} 可以为多个在不同时间动态地订阅了 {@link Publisher#subscribe(Subscriber)} 的 {@link Subscriber}s 提供服务。
 *
 * @param <T> the type of element signaled
 *            <br> 元素的类型
 */
public interface Publisher<T> {

    /**
     * Request {@link Publisher} to start streaming data.
     * <br> 请求 {@link Publisher} 开始发布数据。
     * <p>
     * This is a "factory method" and can be called multiple times, each time starting a new {@link Subscription}.
     * Each {@link Subscription} will work for only a single {@link Subscriber}.
     * <br> 这是一个"工厂方法"，可以被调用多次，每次调用都会启动一个新的 {@link Subscription}。
     * 每个 {@link Subscription} 只为单个 {@link Subscriber} 工作。
     * <p>
     * A {@link Subscriber} should only subscribe once to a single {@link Publisher}.
     * If the {@link Publisher} rejects the subscription attempt or otherwise fails it will
     * signal the error via {@link Subscriber#onError(Throwable)}.
     * <br> 一个 {@link Subscriber} 应该只订阅一个 {@link Publisher} 一次。
     * 如果这个 {@link Publisher} 拒绝了订阅请求或失败了，它会通过 {@link Subscriber#onError(Throwable)} 方法发送一个错误信号。
     *
     * @param s the {@link Subscriber} that will consume signals from this {@link Publisher}
     *          <br> 消费 {@link Publisher} 发送的数据的 {@link Subscriber}
     */
    public void subscribe(Subscriber<? super T> s);
}

