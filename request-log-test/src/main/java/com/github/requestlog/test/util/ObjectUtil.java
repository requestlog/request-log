package com.github.requestlog.test.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;


public class ObjectUtil {

    /**
     * Converts the given object to a pretty-printed JSON string.
     */
    public static String asStringPretty(Object object) {
        try {
            return new ObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException ignored) {
        }
        return null;
    }

}
