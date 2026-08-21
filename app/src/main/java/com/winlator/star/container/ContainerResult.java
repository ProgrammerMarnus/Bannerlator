package com.winlator.star.container;

/**
 * A tiny, dependency-free success/error result used at the container repository boundary.
 * <p>
 * {@link com.winlator.star.container.Container} (the legacy {@code Container.java} facade) and the
 * new {@link ContainerRepository} speak this type instead of throwing raw exceptions across the
 * file/IO boundary. Pure JVM (no android types), so it is usable both by the unit tests under
 * {@code app/src/test/java} and by the on-device container screens.
 *
 * <p>The design follows the plan's "Result&lt;T&gt; for error handling (no exceptions across
 * boundary)" with a small, structural advantage: it carries an optional error message so a caller
 * can surface a human-readable reason (corrupt JSON, missing dir, wipe failure) without catching.
 *
 * @param <T> the value type on success, or {@code null} on failure
 */
public final class ContainerResult<T> {
    private final boolean success;
    private final T value;
    private final String errorMessage;

    private ContainerResult(boolean success, T value, String errorMessage) {
        this.success = success;
        this.value = value;
        this.errorMessage = errorMessage;
    }

    /** Wraps a successful value. */
    public static <T> ContainerResult<T> ok(T value) {
        return new ContainerResult<>(true, value, null);
    }

    /** Wraps a failure with a human-readable reason. */
    public static <T> ContainerResult<T> failure(String message) {
        return new ContainerResult<>(false, null, message);
    }

    /** Wraps a failure derived from a caught exception. */
    public static <T> ContainerResult<T> failure(Throwable throwable) {
        return failure(throwable != null && throwable.getMessage() != null
                ? throwable.getMessage() : throwable != null ? throwable.getClass().getSimpleName() : "unknown");
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public T getValue() {
        return value;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}