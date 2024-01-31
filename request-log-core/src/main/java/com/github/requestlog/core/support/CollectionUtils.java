package com.github.requestlog.core.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Utility class for working with collections.
 */
public class CollectionUtils extends org.springframework.util.CollectionUtils {


    /**
     * Returns an unmodifiable view of the specified list.
     */
    public static <T> List<T> unmodifiableList(List<T> list) {
        return list == null ? null : Collections.unmodifiableList(list);
    }

    /**
     * Returns an unmodifiable view of the specified map.
     */
    public static <K, V> Map<K, V> unmodifiableMap(Map<K, V> map) {
        return map == null ? null : Collections.unmodifiableMap(map);
    }

}
