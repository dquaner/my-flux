package org.xxts.reactor.core;

import org.xxts.reactivestreams.Subscriber;
import org.xxts.reactivestreams.Subscription;
import org.xxts.reactor.util.Logger;
import org.xxts.reactor.util.context.Context;

/**
 * A {@link Context} aware subscriber which has relaxed rules for §1.3 and §3.9
 * compared to the original {@link Subscriber} from Reactive Streams.
 * If an invalid request {@code <= 0} is done on the received subscription, the request
 * will not produce an onError and will simply be ignored.
 * <br>
 * 一个含有 {@link Context} 的订阅者，与响应式流的原始订阅者相比，它放宽了 §1.3 和 §3.9 的规则。
 * 如果在接收到的 subscription 上执行了一个无效的请求数 {@code <= 0}，该请求会被直接忽略，并且不会产生 onError。
 *
 * <p>
 * The rule relaxation has been initially established under reactive-streams-commons.
 * <br>
 * 规则放松在 reactive-streams-commons 下被初始建立。
 *
 * @param <T> the {@link Subscriber} data type
 *           <br> 数据类型
 */
public interface CoreSubscriber<T> extends Subscriber<T> {

    /**
     * Request a {@link Context} from dependent components which can include downstream
     * operators during subscribing or a terminal {@link Subscriber}.
     * <br> 从依赖的组件请求 {@link Context}，该组件可以在订阅或终端 {@link Subscriber} 期间包含下游操作符。
     *
     * @return a resolved context or {@link Context#empty()}
     *         <br> 已解析的上下文或 {@link Context#empty()}
     */
    default Context currentContext(){
        return Context.empty();
    }

    /**
     * Implementors should initialize any state used by {@link #onNext(Object)} before
     * calling {@link Subscription#request(long)}. Should further {@code onNext} related
     * state modification occur, thread-safety will be required.
     * <br>
     * 此方法的实现应该在调用 {@link Subscription#request(long)} 之前初始化 {@link #onNext(Object)} 会使用的任何状态。
     * 如果发生进一步的 {@code onNext} 相关状态的修改，则需要线程安全性。
     * <p>
     *
     * Note that an invalid request {@code <= 0} will not produce an onError and
     * will simply be ignored or reported through a debug-enabled {@link Logger}.
     * <br>
     * 请注意，无效请求 {@code <= 0} 不会产生 onError，只会被忽略或通过启用调试等级的 {@link Logger} 报告。
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    void onSubscribe(Subscription s);
}
