package com.github.requestlog.core.support;


import org.springframework.lang.Nullable;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;


/**
 * Chaining utility for {@link Supplier}.
 */
public class SupplierChain<T> {

    /**
     * Empty instance.
     */
    @SuppressWarnings("rawtypes")
    private static final SupplierChain EMPTY = new SupplierChain<>(null);


    /**
     * Value, can be null.
     */
    private final T value;


    private SupplierChain(T value) {
        this.value = value;
    }


    /**
     * Returns an empty {@link SupplierChain} instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> SupplierChain<T> empty() {
        return (SupplierChain<T>) EMPTY;
    }

    /**
     * Returns an {@link SupplierChain} instance with specified nullable value.
     *
     * @param value may be null.
     */
    public static <T> SupplierChain<T> of(@Nullable T value) {
        return of(true, value);
    }

    /**
     * Returns and {@link Supplier} instance with specified nullable value.
     *
     * @param expression returns {@link #empty()} when expression is false.
     * @param value      may be null.
     */
    @SuppressWarnings("unchecked")
    public static <T> SupplierChain<T> of(boolean expression, @Nullable T value) {
        return expression && value != null ? new SupplierChain<>(value) : (SupplierChain<T>) EMPTY;
    }

    public static <T> SupplierChain<T> of(Supplier<T> supplier) {
        return of(true, supplier);
    }

    @SuppressWarnings("unchecked")
    public static <T> SupplierChain<T> of(boolean expression, Supplier<T> supplier) {
        return expression ? of(supplier.get()) : (SupplierChain<T>) EMPTY;
    }

    /**
     * Returns an {@link SupplierChain} instance with supplier.
     * Value will get from supplier.
     * Returns {@link #EMPTY} if value is null.
     * Exceptions thrown by supplier will be silenced.
     *
     * @param supplier value supplier.
     */
    public static <T> SupplierChain<T> ofQuietly(Supplier<T> supplier) {
        return ofQuietly(true, supplier);
    }

    /**
     * see {@link #ofQuietly(Supplier)}
     */
    @SuppressWarnings("unchecked")
    public static <T> SupplierChain<T> ofQuietly(boolean expression, Supplier<T> supplier) {
        try {
            return expression ? of(supplier.get()) : (SupplierChain<T>) EMPTY;
        } catch (Exception ignored) {
        }
        return (SupplierChain<T>) EMPTY;
    }


    public SupplierChain<T> or(@Nullable T value) {
        return or(true, value);
    }

    public SupplierChain<T> or(boolean expression, @Nullable T value) {
        if (this.value != null) {
            return this;
        }
        return expression ? of(value) : this;
    }

    public SupplierChain<T> or(Supplier<T> supplier) {
        return or(true, supplier);
    }

    public SupplierChain<T> or(boolean expression, Supplier<T> supplier) {
        if (this.value != null) {
            return this;
        }
        return expression ? of(supplier.get()) : this;
    }

    public SupplierChain<T> orQuietly(Supplier<T> supplier) {
        return orQuietly(true, supplier);
    }

    public SupplierChain<T> orQuietly(boolean expression, Supplier<T> supplier) {
        if (this.value != null) {
            return this;
        }
        try {
            return expression ? of(supplier.get()) : this;
        } catch (Exception ignored) {
            return this;
        }
    }

    public SupplierChain<T> discardIf(Predicate<T> predicate) {
        return this.value == null ? this : discardIf(predicate.test(this.value));
    }

    @SuppressWarnings("unchecked")
    public SupplierChain<T> discardIf(boolean expression) {
        return expression ? (SupplierChain<T>) EMPTY : this;
    }

    public T get() {
        return this.value;
    }

    public Optional<T> getOptional() {
        return Optional.ofNullable(this.value);
    }

}