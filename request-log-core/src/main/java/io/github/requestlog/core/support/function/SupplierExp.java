package io.github.requestlog.core.support.function;


/**
 * Represents a supplier of results that may throw an exception.
 *
 * This interface is similar to {@link java.util.function.Supplier}, but with the ability to
 * declare a checked exception.
 *
 * @param <T> The type of the result.
 * @param <E> The type of the exception that may be thrown.
 * @see java.util.function.Supplier
 */
@FunctionalInterface
public interface SupplierExp<T, E extends Exception> {

    /**
     * Gets a result.
     *
     * @return a result
     * @throws E if an exception occurs while computing the result.
     */
    T get() throws E;

}
