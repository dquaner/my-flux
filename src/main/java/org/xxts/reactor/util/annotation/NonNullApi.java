package org.xxts.reactor.util.annotation;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.*;

/**
 * A common Reactor annotation (similar to Spring one) to declare that parameters and return
 * values are to be considered as non-nullable by default for a given package.
 * Leverages JSR 305 meta-annotations to indicate nullability in Java to common tools with
 * JSR 305 support and used by Kotlin to infer nullability of Reactor API.
 * <br>
 * 一个通用的 Reactor 注释（类似于 Spring 注释），用于声明在默认情况下给定 package 的所有参数和返回值不能为空。
 * Java 中利用 JSR 305 元注释来指示那些支持 JSR 305 的通用工具的可空性，
 * Kotlin 使用它来推断 Reactor API 的可空性。
 *
 * <p>
 * Should be used at package level in association with {@link Nullable}
 * annotations at parameter and return value level.
 * <br>
 * 应该被用在 package 级别，与参数和返回值级别的 {@link Nullable} 注释关联使用。
 *
 * <p>
 * {@code @javax.annotation.meta.TypeQualifierDefault}:
 * <br>
 * 该限定符应用于注释接口上，表示该注释定义了一个默认类型的限定符，在其指定的元素类型范围内可见。
 *
 * @see Nullable
 * @see NonNull
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull
@TypeQualifierDefault({ElementType.METHOD, ElementType.PARAMETER})
public @interface NonNullApi {
}
