package com.github.requestlog.core.support;

import java.util.function.Function;


/**
 * Utility class for precondition checks, like Google Guava's Preconditions.
 */
public class Preconditions {


    /**
     * Checks that the given boolean expression is true, throwing an {@code IllegalArgumentException} if false.
     */
    public static void check(boolean expression, String errorMessage) {
        check(expression, errorMessage, IllegalArgumentException::new);
    }


    /**
     * Checks that the given boolean expression is true, throwing a custom runtime exception if false.
     *
     * @param expression        The boolean expression to check.
     * @param errorMessage      The error message to be used in the exception if the check fails.
     * @param exceptionFunction A function that creates a runtime exception with the given error message.
     * @throws RuntimeException If the expression is false, based on the provided exceptionFunction.
     */
    public static void check(boolean expression, String errorMessage, Function<String, ? extends RuntimeException> exceptionFunction) {
        if (!expression) {
            throw exceptionFunction.apply(errorMessage);
        }
    }

}
