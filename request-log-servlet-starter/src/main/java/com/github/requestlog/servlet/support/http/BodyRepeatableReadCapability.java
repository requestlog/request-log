package com.github.requestlog.servlet.support.http;

import javax.servlet.ServletRequest;


/**
 * Marker interface used to indicate whether the body of a {@link ServletRequest} can be read repeatedly.
 */
public interface BodyRepeatableReadCapability {


    /**
     * Returns whether the body of the ServletRequest can be read repeatedly.
     * Default implementation returns false.
     */
    default boolean isBodyRepeatableRead() {
        return false;
    }

}
