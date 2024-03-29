package org.xxts.reactivestreams;

/**
 * A {@link Subscription} represents a one-to-one lifecycle of a {@link Subscriber} subscribing to a {@link Publisher}.
 * <br> 一个 {@link Subscription} 可以代表一次 {@link Subscriber} 订阅 {@link Publisher} 的一对一的生命周期。
 * <p>
 * It can only be used once by a single {@link Subscriber}.
 * <br> 它只能被一个 {@link Subscriber} 对象使用一次。
 * <p>
 * It is used to both signal desire for data and cancel demand (and allow resource cleanup).
 * <br> 它被用来发送数据请求或取消需求（允许资源的清理）。
 */
public interface Subscription {

    /**
     * No events will be sent by a {@link Publisher} until demand is signaled via this method.
     * <br> {@link Publisher} 不会开始发送事件，直到通过此方法发送了请求。
     * <p>
     * It can be called however often and whenever needed—but if the outstanding cumulative demand ever becomes Long.MAX_VALUE or more,
     * it may be treated by the {@link Publisher} as "effectively unbounded".
     * <br> 无论何时需要，它都可以被调用；但是如果累积的需求达到了 Long.MAX_VALUE 或者更多，它会被 {@link Publisher} 视为"有效无界"。
     * <p>
     * Whatever has been requested can be sent by the {@link Publisher} so only signal demand for what can be safely handled.
     * <br> 需要注意的是，任何请求都会被 {@link Publisher} 发送，所以请只发送可以被安全处理的请求。
     * <p>
     * A {@link Publisher} can send less than is requested if the stream ends but
     * then must emit either {@link Subscriber#onError(Throwable)} or {@link Subscriber#onComplete()}.
     * <br> 如果流结束了，{@link Publisher} 可以发送少于请求数量的数据，否则，必须发送 {@link Subscriber#onError(Throwable)} 或 {@link Subscriber#onComplete()} 信号。
     *
     * @param n the strictly positive number of elements to requests to the upstream {@link Publisher}
     *          <br> 严格的正数，代表向上游 {@link Publisher} 请求的元素数量
     */
    public void request(long n);

    /**
     * Request the {@link Publisher} to stop sending data and clean up resources.
     * <br> 请求 {@link Publisher} 停止发送数据并清理资源。
     * <p>
     * Data may still be sent to meet previously signalled demand after calling cancel.
     * <br> 在调用了 cancel 方法之后，数据仍可以被发送，来满足之前的请求。
     */
    public void cancel();
}
