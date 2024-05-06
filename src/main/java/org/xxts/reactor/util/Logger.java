package org.xxts.reactor.util;


import java.util.function.Supplier;

/**
 * Logger interface designed for internal Reactor usage.
 * <br> 为 Reactor 内部使用设计的日志接口。
 */
public interface Logger {

    /**
     * A kind of {@link java.util.function.Predicate} and {@link Supplier} mix, provides two
     * variants of a message {@link String} depending on the level of detail desired.
     * <br> 一种 {@link java.util.function.Predicate} 和 {@link Supplier} 的混合，
     * 根据具体所需的消息级别提供消息 {@link String} 的两种变体。
     */
    @FunctionalInterface
    interface ChoiceOfMessageSupplier {
        /**
         * Provide two possible versions of a message {@link String}, depending on the
         * level of detail desired.
         *
         * @param isVerbose {@code true} for higher level of detail, {@code false} for lower level of detail
         * @return the message {@link String} according to the passed level of detail
         */
        String get(boolean isVerbose);
    }

    /**
     * Return the name of this <code>Logger</code> instance.
     * @return name of this logger instance
     */
    String getName();

    /**
     * Is the logger instance enabled for the TRACE level?
     *
     * @return True if this Logger is enabled for the TRACE level,
     *         false otherwise.
     */
    boolean isTraceEnabled();
    /**
     * Log a message at the TRACE level.
     *
     * @param msg the message string to be logged
     */
    void trace(String msg);
    void trace(String format, Object... arguments);
    void trace(String msg, Throwable t);

    /**
     * Is the logger instance enabled for the DEBUG level?
     *
     * @return True if this Logger is enabled for the DEBUG level,
     *         false otherwise.
     */
    boolean isDebugEnabled();
    /**
     * Log a message at the DEBUG level.
     *
     * @param msg the message string to be logged
     */
    void debug(String msg);
    void debug(String format, Object... arguments);
    void debug(String msg, Throwable t);

    /**
     * Is the logger instance enabled for the INFO level?
     *
     * @return True if this Logger is enabled for the INFO level,
     *         false otherwise.
     */
    boolean isInfoEnabled();
    /**
     * Log a message at the INFO level.
     *
     * @param msg the message string to be logged
     */
    void info(String msg);
    void info(String format, Object... arguments);
    void info(String msg, Throwable t);

    /**
     * Convenience method to log a message that is different according to the log level.
     * In priority, DEBUG level is used if {@link #isDebugEnabled()}.
     * Otherwise, INFO level is used (unless {@link #isInfoEnabled()} is false).
     * <p>
     * This can be used to log different level of details according to the active
     * log level.
     *
     * @param messageSupplier the {@link ChoiceOfMessageSupplier} invoked in priority
     * with {@code true} for the DEBUG level message, or {@code false} for INFO level
     * @see #info(String)
     */
    default void infoOrDebug(ChoiceOfMessageSupplier messageSupplier) {
        if (isDebugEnabled()) {
            debug(messageSupplier.get(true));
        }
        else if (isInfoEnabled()) {
            info(messageSupplier.get(false));
        }
    }

    default void infoOrDebug(ChoiceOfMessageSupplier messageSupplier, Throwable cause) {
        if (isDebugEnabled()) {
            debug(messageSupplier.get(true), cause);
        }
        else if (isInfoEnabled()) {
            info(messageSupplier.get(false), cause);
        }
    }

    /**
     * Is the logger instance enabled for the WARN level?
     *
     * @return True if this Logger is enabled for the WARN level,
     *         false otherwise.
     */
    boolean isWarnEnabled();
    /**
     * Log a message at the WARN level.
     *
     * @param msg the message string to be logged
     */
    void warn(String msg);
    void warn(String format, Object... arguments);
    void warn(String msg, Throwable t);

    /**
     * Convenience method to log a message that is different according to the log level.
     * In priority, DEBUG level is used if {@link #isDebugEnabled()}.
     * Otherwise, WARN level is used (unless {@link #isWarnEnabled()} is false).
     * <p>
     * This can be used to log different level of details according to the active
     * log level.
     *
     * @param messageSupplier the {@link ChoiceOfMessageSupplier} invoked in priority
     * with {@code true} for the DEBUG level message, or {@code false} for WARN level
     * @see #warn(String)
     */
    default void warnOrDebug(ChoiceOfMessageSupplier messageSupplier) {
        if (isDebugEnabled()) {
            debug(messageSupplier.get(true));
        }
        else if (isWarnEnabled()) {
            warn(messageSupplier.get(false));
        }
    }

    default void warnOrDebug(ChoiceOfMessageSupplier messageSupplier, Throwable cause) {
        if (isDebugEnabled()) {
            debug(messageSupplier.get(true), cause);
        }
        else if (isWarnEnabled()) {
            warn(messageSupplier.get(false), cause);
        }
    }

    /**
     * Is the logger instance enabled for the ERROR level?
     *
     * @return True if this Logger is enabled for the ERROR level,
     *         false otherwise.
     */
    boolean isErrorEnabled();
    /**
     * Log a message at the ERROR level.
     *
     * @param msg the message string to be logged
     */
    void error(String msg);
    void error(String format, Object... arguments);
    void error(String msg, Throwable t);

    /**
     * Convenience method to log a message that is different according to the log level.
     * In priority, DEBUG level is used if {@link #isDebugEnabled()}.
     * Otherwise, ERROR level is used (unless {@link #isErrorEnabled()} is false).
     * <p>
     * This can be used to log different level of details according to the active
     * log level.
     *
     * @param messageSupplier the {@link ChoiceOfMessageSupplier} invoked in priority
     * with {@code true} for the DEBUG level message, or {@code false} for ERROR level
     * @see #error(String)
     */
    default void errorOrDebug(ChoiceOfMessageSupplier messageSupplier) {
        if (isDebugEnabled()) {
            debug(messageSupplier.get(true));
        }
        else if (isErrorEnabled()) {
            error(messageSupplier.get(false));
        }
    }

    default void errorOrDebug(ChoiceOfMessageSupplier messageSupplier, Throwable cause) {
        if (isDebugEnabled()) {
            debug(messageSupplier.get(true), cause);
        }
        else if (isErrorEnabled()) {
            error(messageSupplier.get(false), cause);
        }
    }

}
