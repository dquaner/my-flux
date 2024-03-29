package org.xxts.reactor.util.annotation;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierNickname;
import javax.annotation.meta.When;
import java.lang.annotation.*;

/**
 * A common Reactor annotation (similar to Spring ones) to declare that annotated elements
 * can be {@code null} under some circumstance. Leverages JSR 305 meta-annotations to
 * indicate nullability in Java to common tools with JSR 305 support and used by Kotlin to
 * infer nullability of Reactor API.
 * <br>
 * 一个通用的 Reactor 注释（类似于 Spring 注释），用于声明被注释的元素在某些条件下可以为 {@code null}。
 * Java 中利用 JSR 305 元注释来指示那些支持 JSR 305 的通用工具的可空性，
 * Kotlin 使用它来推断 Reactor API 的可空性。
 *
 * <p>
 * Should be used at parameter, return value, and field level.
 * Methods overrides should repeat parent {@code @Nullable} annotations unless they behave
 * differently.
 * <br>
 * 应该用在参数、返回值、以及属性上。重写方法应该重新声明父类方法的 {@code @Nullable} 注释，除非它们的行为不同。
 *
 * <p>
 * Can be used in association with {@code NonNullApi} to override the default
 * non-nullable semantic to nullable.
 * <br>
 * 可以和 {@code NonNullApi} 一起使用，将默认的非空语义覆盖为允许空值。
 *
 * @see NonNullApi
 * @see NonNull
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull(when = When.MAYBE)
@TypeQualifierNickname
public @interface Nullable {
}
