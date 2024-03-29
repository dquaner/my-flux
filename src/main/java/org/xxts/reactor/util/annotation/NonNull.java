package org.xxts.reactor.util.annotation;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierNickname;
import java.lang.annotation.*;

/**
 * A common Reactor annotation (similar to Spring ones) to declare that annotated elements
 * cannot be {@code null}. Leverages JSR 305 meta-annotations to indicate nullability in
 * Java to common tools with JSR 305 support and used by Kotlin to infer nullability of
 * Reactor API.
 * <br>
 * 一个通用的 Reactor 注释（类似于 Spring 注释），用于声明被注释的元素不能为 {@code null}。
 * Java 中利用 JSR 305 元注释来指示那些支持 JSR 305 的通用工具的可空性，
 * Kotlin 使用它来推断 Reactor API 的可空性。
 *
 * <p>
 * Should be used at parameter, return value, and field level.
 * Methods overrides should repeat parent {@code @NonNull} annotations unless they behave
 * differently.
 * <br>
 * 应该用在参数、返回值、以及属性上。重写方法应该重新声明父类方法的 {@code @NonNull} 注释，除非它们的行为不同。
 *
 * <p>
 * Use {@code @NonNullApi} (scope = parameters + return values) to set the default
 * behavior to non-nullable in order to avoid annotating your whole codebase with
 * {@code @NonNull}.
 * <br>
 * 使用 {@code @NonNullApi}（作用范围为参数和返回值）将默认行为设置为非空，来避免使用 {@code @NonNull} 注释掉整个代码。
 *
 * <p>
 * {@code @javax.annotation.Nonnull}:
 * <ul>
 *     <li>被注释的元素必须非空</li>
 *     <li>被注释的属性在构造完成后必须非空</li>
 *     <li>当用于注释方法时，作用于方法的返回值</li>
 * </ul>
 *
 * <p>
 * {@code @javax.annotation.meta.TypeQualifierNickname}:
 * <br>
 * 应用于注释接口上，并将注释标记为一个类型限制昵称。
 * 将一个昵称注释{@code X}用在元素{@code Y}上，则表示将{@code X}上的所有注释（QualifierNickname除外）应用在{@code Y}上。
 *
 * @see NonNullApi
 * @see Nullable
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull
@TypeQualifierNickname
public @interface NonNull {
}
