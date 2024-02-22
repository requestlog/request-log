package io.github.requestlog.core.support;

import java.util.*;
import java.util.function.Predicate;


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

    /**
     * Filters a Map based on the provided Predicate for keys.
     *
     * @param map          the original Map to be filtered
     * @param keyPredicate the Predicate for filtering keys
     * @return a new Map containing only the entries whose keys satisfy the Predicate
     */
    public static <K, V> Map<K, V> filterWithKey(Map<K, V> map, Predicate<K> keyPredicate) {
        Map<K, V> newMap = new HashMap<>(map);
        map.forEach((key, value) -> {
            if (keyPredicate.test(key)) {
                newMap.put(key, value);
            }
        });
        return newMap;
    }

    /**
     * Returns size of the map.
     */
    public static int size(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

    /**
     * Returns size of the collection.
     */
    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

}
