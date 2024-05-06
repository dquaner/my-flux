package org.xxts.reactor.util;

import org.xxts.reactor.util.annotation.Nullable;

import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Matcher;


/**
 * Expose static methods to get a logger depending on the environment. If SL4J is on the
 * classpath, it will be used. Otherwise, there are two possible fallbacks: Console or
 * {@link java.util.logging.Logger}. By default, the Console
 * fallback is used. To use the JDK loggers, set the {@value #FALLBACK_PROPERTY}
 * {@link System#setProperty(String, String) System property} to "{@code JDK}".
 * <br>
 * 提供根据环境获取日志记录器的静态方法。如果 SL4J 在类路径中，则使用 SL4J。否则，提供两种可能的
 * 回退：Console 或 {@link java.util.logging.Logger}。默认情况下，使用 Console。要使用 JDK 日志记录器，
 * 请设置 {@value #FALLBACK_PROPERTY} {@link System#setProperty(String, String) System property}
 * 为 "{@code JDK}"。
 * <p>
 * <p>
 * One can also force the implementation by using the "useXXX" static methods:
 * {@link #useConsoleLoggers()}, {@link #useVerboseConsoleLoggers()}, {@link #useJdkLoggers()}
 * and {@link #useSl4jLoggers()} (which may throw an Exception if the library isn't on the
 * classpath). Note that the system property method above is preferred, as no cleanup of
 * the logger factory initialized at startup is attempted by the useXXX methods.
 * <br>
 * 还可以通过使用 "useXXX" 静态方法强制指定实现：
 * {@link #useConsoleLoggers()}, {@link #useVerboseConsoleLoggers()}, {@link #useJdkLoggers()}
 * 和 {@link #useSl4jLoggers()} (如果库不在类路径上，可能会抛出异常)。
 * 注意，上面的设置系统属性的方法是首选，因为 "useXXX" 方法不会尝试清除在启动时初始化的记录器工厂。
 */
public abstract class Loggers {

    /**
     * The system property that determines which fallback implementation to use for loggers
     * when SLF4J isn't available. Use {@code JDK} for the JDK-backed logging and anything
     * else for Console-based (the default).
     * <br>
     * 系统属性，决定了当 SLF4J 不可用时，使用哪个回退的日志记录器实现。
     * {@code JDK} 表示使用 JDK 提供的日志记录，其他值表示使用基于控制台的日志记录（默认值）。
     */
    public static final String FALLBACK_PROPERTY = "reactor.logging.fallback";

    private static Function<String, ? extends Logger> LOGGER_FACTORY;

    static {
        resetLoggerFactory();
    }

    public static void resetLoggerFactory() {
        try {
            useSl4jLoggers();
        } catch (Throwable t) {
            if (isFallbackToJdk()) {
                useJdkLoggers();
            } else {
                useConsoleLoggers();
            }
        }
    }

    /**
     * Return true if {@link #resetLoggerFactory()} would fallback to java.util.logging
     * rather than console-based logging, as defined by the {@link #FALLBACK_PROPERTY}
     * System property.
     *
     * @return true if falling back to JDK, false for Console.
     */
    static boolean isFallbackToJdk() {
        return "JDK".equalsIgnoreCase(System.getProperty(FALLBACK_PROPERTY));
    }

    /**
     * Force the usage of Console-based {@link Logger Loggers}, even if SLF4J is available
     * on the classpath. Console loggers will output {@link Logger#error(String) ERROR} and
     * {@link Logger#warn(String) WARN} levels to {@link System#err} and levels below to
     * {@link System#out}. All levels <strong>except TRACE and DEBUG</strong> are
     * considered enabled. TRACE and DEBUG are omitted.
     * <p>
     * The previously active logger factory is simply replaced without
     * any particular clean-up.
     */
    public static void useConsoleLoggers() {
        String name = Loggers.class.getName();
        Function<String, Logger> loggerFactory = new Loggers.ConsoleLoggerFactory(false);
        LOGGER_FACTORY = loggerFactory;
        loggerFactory.apply(name).debug("Using Console logging");
    }

    /**
     * Force the usage of Console-based {@link Logger Loggers}, even if SLF4J is available
     * on the classpath. Console loggers will output {@link Logger#error(String) ERROR} and
     * {@link Logger#warn(String) WARN} levels to {@link System#err} and levels below to
     * {@link System#out}. All levels (including TRACE and DEBUG) are considered enabled.
     * <p>
     * The previously active logger factory is simply replaced without
     * any particular clean-up.
     */
    public static void useVerboseConsoleLoggers() {
        String name = Loggers.class.getName();
        Function<String, Logger> loggerFactory = new Loggers.ConsoleLoggerFactory(true);
        LOGGER_FACTORY = loggerFactory;
        loggerFactory.apply(name).debug("Using Verbose Console logging");
    }

    /**
     * Force the usage of JDK-based {@link Logger Loggers}, even if SLF4J is available
     * on the classpath.
     * <p>
     * The previously active logger factory is simply replaced without
     * any particular clean-up.
     */
    public static void useJdkLoggers() {
        String name = Loggers.class.getName();
        Function<String, Logger> loggerFactory = new Loggers.JdkLoggerFactory();
        LOGGER_FACTORY = loggerFactory;
        loggerFactory.apply(name).debug("Using JDK logging framework");
    }

    /**
     * Force the usage of SL4J-based {@link Logger Loggers}, throwing an exception if
     * SLF4J isn't available on the classpath. Prefer using {@link #resetLoggerFactory()}
     * as it will fallback in the later case.
     * <p>
     * The previously active logger factory is simply replaced without
     * any particular clean-up.
     */
    public static void useSl4jLoggers() {
        String name = Loggers.class.getName();
        Function<String, Logger> loggerFactory = new Loggers.Slf4JLoggerFactory();
        LOGGER_FACTORY = loggerFactory;
        loggerFactory.apply(name).debug("Using Slf4j logging framework");
    }

    /**
     * Use a custom type of {@link Logger} created through the provided {@link Function},
     * which takes a logger name as input.
     * <p>
     * The previously active logger factory is simply replaced without
     * any particular clean-up.
     *
     * <h4>Thread-safety</h4>
     * <p>
     * Given logger acquisition function <em>must</em> be thread-safe.
     * It means that it is user responsibility to ensure that any internal state and cache
     * used by the provided function is properly synchronized.
     *
     * @param loggerFactory the {@link Function} that provides a (possibly cached) {@link Logger}
     *                      given a name.
     */
    public static void useCustomLoggers(final Function<String, ? extends Logger> loggerFactory) {
        String name = Loggers.class.getName();
        LOGGER_FACTORY = loggerFactory;
        loggerFactory.apply(name).debug("Using custom logging");
    }

    /**
     * Get a {@link Logger}.
     * <p>
     * For a notion of how the backing implementation is chosen, see
     * {@link #resetLoggerFactory()} (or call one of the {@link #useConsoleLoggers() useXxxLoggers}
     * methods).
     *
     * @param name the category or logger name to use
     * @return a new {@link Logger} instance
     */
    public static Logger getLogger(String name) {
        return LOGGER_FACTORY.apply(name);
    }

    /**
     * Get a {@link Logger}, backed by SLF4J if present on the classpath or falling back
     * to {@link java.util.logging.Logger java.util.logging.Logger}.
     *
     * @param cls the source {@link Class} to derive the logger name from.
     * @return a new {@link Logger} instance
     */
    public static Logger getLogger(Class<?> cls) {
        return LOGGER_FACTORY.apply(cls.getName());
    }

    private static class Slf4JLoggerFactory implements Function<String, Logger> {
        @Override
        public Logger apply(String name) {
            return new Loggers.Slf4JLogger(org.slf4j.LoggerFactory.getLogger(name));
        }
    }

    private record Slf4JLogger(org.slf4j.Logger logger) implements Logger {

        @Override
        public String getName() {
            return logger.getName();
        }

        @Override
        public boolean isTraceEnabled() {
            return logger.isTraceEnabled();
        }

        @Override
        public void trace(String msg) {
            logger.trace(msg);
        }

        @Override
        public void trace(String format, Object... arguments) {
            logger.trace(format, arguments);
        }

        @Override
        public void trace(String msg, Throwable t) {
            logger.trace(msg, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        @Override
        public void debug(String msg) {
            logger.debug(msg);
        }

        @Override
        public void debug(String format, Object... arguments) {
            logger.debug(format, arguments);
        }

        @Override
        public void debug(String msg, Throwable t) {
            logger.debug(msg, t);
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }

        @Override
        public void info(String msg) {
            logger.info(msg);
        }

        @Override
        public void info(String format, Object... arguments) {
            logger.info(format, arguments);
        }

        @Override
        public void info(String msg, Throwable t) {
            logger.info(msg, t);
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isWarnEnabled();
        }

        @Override
        public void warn(String msg) {
            logger.warn(msg);
        }

        @Override
        public void warn(String format, Object... arguments) {
            logger.warn(format, arguments);
        }

        @Override
        public void warn(String msg, Throwable t) {
            logger.warn(msg, t);
        }

        @Override
        public boolean isErrorEnabled() {
            return logger.isErrorEnabled();
        }

        @Override
        public void error(String msg) {
            logger.error(msg);
        }

        @Override
        public void error(String format, Object... arguments) {
            logger.error(format, arguments);
        }

        @Override
        public void error(String msg, Throwable t) {
            logger.error(msg, t);
        }
    }


    private static class JdkLoggerFactory implements Function<String, Logger> {
        @Override
        public Logger apply(String name) {
            return new Loggers.JdkLogger(java.util.logging.Logger.getLogger(name));
        }
    }

    /**
     * Wrapper over JDK logger
     */
    static final class JdkLogger implements Logger {

        private final java.util.logging.Logger logger;

        public JdkLogger(java.util.logging.Logger logger) {
            this.logger = logger;
        }

        @Override
        public String getName() {
            return logger.getName();
        }

        @Override
        public boolean isTraceEnabled() {
            return logger.isLoggable(Level.FINEST);
        }

        @Override
        public void trace(String msg) {
            logger.log(Level.FINEST, msg);
        }

        @Override
        public void trace(String format, Object... arguments) {
            logger.log(Level.FINEST, format(format, arguments));
        }

        @Override
        public void trace(String msg, Throwable t) {
            logger.log(Level.FINEST, msg, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isLoggable(Level.FINE);
        }

        @Override
        public void debug(String msg) {
            logger.log(Level.FINE, msg);
        }

        @Override
        public void debug(String format, Object... arguments) {
            logger.log(Level.FINE, format(format, arguments));
        }

        @Override
        public void debug(String msg, Throwable t) {
            logger.log(Level.FINE, msg, t);
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isLoggable(Level.INFO);
        }

        @Override
        public void info(String msg) {
            logger.log(Level.INFO, msg);
        }

        @Override
        public void info(String format, Object... arguments) {
            logger.log(Level.INFO, format(format, arguments));
        }

        @Override
        public void info(String msg, Throwable t) {
            logger.log(Level.INFO, msg, t);
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isLoggable(Level.WARNING);
        }

        @Override
        public void warn(String msg) {
            logger.log(Level.WARNING, msg);
        }

        @Override
        public void warn(String format, Object... arguments) {
            logger.log(Level.WARNING, format(format, arguments));
        }

        @Override
        public void warn(String msg, Throwable t) {
            logger.log(Level.WARNING, msg, t);
        }

        @Override
        public boolean isErrorEnabled() {
            return logger.isLoggable(Level.SEVERE);
        }

        @Override
        public void error(String msg) {
            logger.log(Level.SEVERE, msg);
        }

        @Override
        public void error(String format, Object... arguments) {
            logger.log(Level.SEVERE, format(format, arguments));
        }

        @Override
        public void error(String msg, Throwable t) {
            logger.log(Level.SEVERE, msg, t);
        }

        @Nullable
        final String format(@Nullable String from, @Nullable Object... arguments) {
            if (from != null) {
                String computed = from;
                if (arguments != null) {
                    for (Object argument : arguments) {
                        computed = computed.replaceFirst("\\{\\}", Matcher.quoteReplacement(String.valueOf(argument)));
                    }
                }
                return computed;
            }
            return null;
        }
    }

    record ConsoleLoggerFactory(boolean verbose) implements Function<String, Logger> {

        private static final Map<ConsoleLoggerKey, WeakReference<Logger>> consoleLoggers = new WeakHashMap<>();

        @Override
        public Logger apply(String name) {
            final ConsoleLoggerKey key = new ConsoleLoggerKey(name, verbose);
            synchronized (consoleLoggers) {
                final WeakReference<Logger> ref = consoleLoggers.get(key);
                Logger cached = ref == null ? null : ref.get();
                if (cached == null) {
                    cached = new ConsoleLogger(key);
                    consoleLoggers.put(key, new WeakReference<>(cached));
                }
                return cached;
            }
        }
    }

    /**
     * A key object to serve a dual purpose:
     * <ul>
     *     <li>Allow consistent identification of cached console loggers using not
     *     only its name, but also its verbosity level</li>
     *     <li>Provide an object eligible to cache eviction. Contrary to a logger or
     *     a string (logger name) object, this is a good candidate for weak reference key,
     *     because it should be held only internally by the attached logger and by the
     *     logger cache (as evictable key).</li>
     * </ul>
     */
    private record ConsoleLoggerKey(String name, boolean verbose) { }

    /**
     * A {@link Logger} that has all levels enabled. error and warn log to {@code System.err}
     * while all other levels log to {@code System.out} (printStreams can be changed via constructor).
     */
    static final class ConsoleLogger implements Logger {

        private final Loggers.ConsoleLoggerKey identifier;
        private final PrintStream err;
        private final PrintStream log;

        ConsoleLogger(Loggers.ConsoleLoggerKey identifier, PrintStream log, PrintStream err) {
            this.identifier = identifier;
            this.log = log;
            this.err = err;
        }

        ConsoleLogger(String name, PrintStream log, PrintStream err, boolean verbose) {
            this(new Loggers.ConsoleLoggerKey(name, verbose), log, err);
        }

        ConsoleLogger(Loggers.ConsoleLoggerKey identifier) {
            this(identifier, System.out, System.err);
        }

        @Override
        public String getName() {
            return identifier.name;
        }

        @Nullable
        String format(@Nullable String from, @Nullable Object... arguments) {
            if (from != null) {
                String computed = from;
                if (arguments != null) {
                    for (Object argument : arguments) {
                        computed = computed.replaceFirst("\\{\\}", Matcher.quoteReplacement(String.valueOf(argument)));
                    }
                }
                return computed;
            }
            return null;
        }

        @Override
        public boolean isTraceEnabled() {
            return identifier.verbose;
        }

        @Override
        public synchronized void trace(String msg) {
            if (!identifier.verbose) {
                return;
            }
            this.log.format("[TRACE] (%s) %s\n", Thread.currentThread().getName(), msg);
        }

        @Override
        public synchronized void trace(String format, Object... arguments) {
            if (!identifier.verbose) {
                return;
            }
            this.log.format("[TRACE] (%s) %s\n", Thread.currentThread().getName(), format(format, arguments));
        }

        @Override
        public synchronized void trace(String msg, Throwable t) {
            if (!identifier.verbose) {
                return;
            }
            this.log.format("[TRACE] (%s) %s - %s\n", Thread.currentThread().getName(), msg, t);
            t.printStackTrace(this.log);
        }

        @Override
        public boolean isDebugEnabled() {
            return identifier.verbose;
        }

        @Override
        public synchronized void debug(String msg) {
            if (!identifier.verbose) {
                return;
            }
            this.log.format("[DEBUG] (%s) %s\n", Thread.currentThread().getName(), msg);
        }

        @Override
        public synchronized void debug(String format, Object... arguments) {
            if (!identifier.verbose) {
                return;
            }
            this.log.format("[DEBUG] (%s) %s\n", Thread.currentThread().getName(), format(format, arguments));
        }

        @Override
        public synchronized void debug(String msg, Throwable t) {
            if (!identifier.verbose) {
                return;
            }
            this.log.format("[DEBUG] (%s) %s - %s\n", Thread.currentThread().getName(), msg, t);
            t.printStackTrace(this.log);
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public synchronized void info(String msg) {
            this.log.format("[ INFO] (%s) %s\n", Thread.currentThread().getName(), msg);
        }

        @Override
        public synchronized void info(String format, Object... arguments) {
            this.log.format("[ INFO] (%s) %s\n", Thread.currentThread().getName(), format(format, arguments));
        }

        @Override
        public synchronized void info(String msg, Throwable t) {
            this.log.format("[ INFO] (%s) %s - %s\n", Thread.currentThread().getName(), msg, t);
            t.printStackTrace(this.log);
        }

        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public synchronized void warn(String msg) {
            this.err.format("[ WARN] (%s) %s\n", Thread.currentThread().getName(), msg);
        }

        @Override
        public synchronized void warn(String format, Object... arguments) {
            this.err.format("[ WARN] (%s) %s\n", Thread.currentThread().getName(), format(format, arguments));
        }

        @Override
        public synchronized void warn(String msg, Throwable t) {
            this.err.format("[ WARN] (%s) %s - %s\n", Thread.currentThread().getName(), msg, t);
            t.printStackTrace(this.err);
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }

        @Override
        public synchronized void error(String msg) {
            this.err.format("[ERROR] (%s) %s\n", Thread.currentThread().getName(), msg);
        }

        @Override
        public synchronized void error(String format, Object... arguments) {
            this.err.format("[ERROR] (%s) %s\n", Thread.currentThread().getName(), format(format, arguments));
        }

        @Override
        public synchronized void error(String msg, Throwable t) {
            this.err.format("[ERROR] (%s) %s - %s\n", Thread.currentThread().getName(), msg, t);
            t.printStackTrace(this.err);
        }

        @Override
        public String toString() {
            return "ConsoleLogger[name=" + getName() + ", verbose=" + identifier.verbose + "]";
        }
    }

    Loggers() {
    }
}

