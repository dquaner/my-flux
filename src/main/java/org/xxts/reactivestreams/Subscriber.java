package org.xxts.reactivestreams;

/**
 * Will receive call to {@link #onSubscribe(Subscription)} once after passing an instance of {@link Subscriber} to {@link Publisher#subscribe(Subscriber)}.
 * <br> 一旦传递了一个 {@link Subscriber} 对象给 {@link Publisher#subscribe(Subscriber)}，{@link #onSubscribe(Subscription)} 方法会立即被调用。
 * <p>
 * No further notifications will be received until {@link Subscription#request(long)} is called.
 * <br> 在 {@link Subscription#request(long)} 被调用之前不会收到任何通知。
 * <p>
 * After signaling demand:
 * <br> 当发送了请求之后：
 * <ul>
 * <li>
 * One or more invocations of {@link #onNext(Object)} up to the maximum number defined by {@link Subscription#request(long)}
 * <br> 一到多（最多为 {@link Subscription#request(long)} 定义的值）次 {@link #onNext(Object)} 方法的调用
 * </li>
 * <li>
 * Single invocation of {@link #onError(Throwable)} or {@link #onComplete()} which signals a terminal state after which no further events will be sent.
 * <br> 一次 {@link #onError(Throwable)} 或 {@link #onComplete()} 方法的调用，来发送一个终止状态信号，此信号之后没有更多的事件会被发送
 * </li>
 * </ul>
 * <p>
 * Demand can be signaled via {@link Subscription#request(long)} whenever the {@link Subscriber} instance is capable of handling more.
 * <br> 当 {@link Subscriber} 有能力处理更多事件时，可以通过 {@link Subscription#request(long)} 方法发送需求。
 *
 * @param <T> the type of element signaled
 *            <br> element 的类型
 */
public interface Subscriber<T> {

    /**
     * Invoked after calling {@link Publisher#subscribe(Subscriber)}.
     * <br> 在调用了 {@link Publisher#subscribe(Subscriber)} 之后被调用。
     * <p>
     * No data will start flowing until {@link Subscription#request(long)} is invoked.
     * <br> 在调用 {@link Subscription#request(long)} 请求之前，没有数据会开始流动。
     * <p>
     * It is the responsibility of this {@link Subscriber} instance to call {@link Subscription#request(long)} whenever more data is wanted.
     * <br> 当需要更多数据时，该 {@link Subscriber} 对象需要主动调用 {@link Subscription#request(long)} 方法。
     * <p>
     * The {@link Publisher} will send notifications only in response to {@link Subscription#request(long)}.
     * <br> {@link Publisher} 仅在响应 {@link Subscription#request(long)} 时发送通知。
     *
     * @param s the {@link Subscription} that allows requesting data via {@link Subscription#request(long)}
     *          <br> 允许通过 {@link Subscription#request(long)} 方法请求数据的 {@link Subscription}
     */
    public void onSubscribe(Subscription s);

    /**
     * Data notification sent by the {@link Publisher} in response to requests to {@link Subscription#request(long)}.
     * <br> 为了响应 {@link Subscription#request(long)} 的请求，由 {@link Publisher} 发来的数据通知。
     *
     * @param t the element signaled
     */
    public void onNext(T t);

    /**
     * Failed terminal state.
     * <br> 失败终止状态。
     * <p>
     * No further events will be sent even if {@link Subscription#request(long)} is invoked again.
     * <br> 没有更多事件会被发送过来，即使再次调用了 {@link Subscription#request(long)}。
     *
     * @param t the throwable signaled
     */
    public void onError(Throwable t);

    /**
     * Successful terminal state.
     * <br> 成功终止状态。
     * <p>
     * No further events will be sent even if {@link Subscription#request(long)} is invoked again.
     * <br> 没有更多事件会被发送过来，即使再次调用了 {@link Subscription#request(long)}。
     */
    public void onComplete();
}
