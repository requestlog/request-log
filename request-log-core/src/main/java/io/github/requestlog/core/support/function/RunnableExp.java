package io.github.requestlog.core.support.function;


/**
 * Represents an action that may throw an exception.
 *
 * This interface is similar to {@link java.lang.Runnable}, but with the ability to
 * declare a checked exception.
 *
 * @param <E> The type of the exception that may be thrown.
 * @see java.lang.Runnable
 */
@FunctionalInterface
public interface RunnableExp<E extends Exception> {

    /**
     * Performs this operation.
     *
     * @throws E if an exception occurs while performing the operation.
     */
    void run() throws E;

}
