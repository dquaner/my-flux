package org.xxts.reactor.core;

import org.xxts.reactivestreams.Publisher;
import org.xxts.reactivestreams.Subscriber;
import org.xxts.reactor.core.publisher.Hooks;
import org.xxts.reactor.util.context.Context;

import java.util.function.Function;

/**
 * A {@link CoreSubscriber} aware publisher.
 * <br> 一个支持 {@link CoreSubscriber} 的订阅者。
 *
 * @param <T> the {@link CoreSubscriber} data type
 *           <br> 数据类型
 */
public interface CorePublisher<T> extends Publisher<T> {

    /**
     * An internal {@link Publisher#subscribe(Subscriber)} that will bypass
     * {@link Hooks#onLastOperator(Function)} pointcut.
     * <br>
     * 一次内部的订阅 {@link Publisher#subscribe(Subscriber)}，该订阅会绕开 {@link Hooks#onLastOperator(Function)} 切点。
     * <p>
     *
     * In addition to behave as expected by {@link Publisher#subscribe(Subscriber)}
     * in a controlled manner, it supports direct subscribe-time {@link Context} passing.
     * <br> 除了按照 {@link Publisher#subscribe(Subscriber)} 的期望以受控的方式行事以外，它还支持直接的订阅时的 {@link Context} 传递。
     *
     * @param subscriber the {@link Subscriber} interested into the published sequence
     *                   <br> 对发布的序列数据感兴趣的 {@link Subscriber}
     * @see Publisher#subscribe(Subscriber)
     */
    void subscribe(CoreSubscriber<? super T> subscriber);
}
