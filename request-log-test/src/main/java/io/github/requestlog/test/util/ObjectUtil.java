package io.github.requestlog.test.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.core.NestedRuntimeException;

import java.io.IOException;
import java.text.SimpleDateFormat;


public class ObjectUtil {

    /**
     * Converts the given object to a pretty-printed JSON string.
     */
    public static String asStringPretty(Object object) {
        try {
            return new ObjectMapper()
                    .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"))

                    // reference chain error when meets org.springframework.core.NestedRuntimeException.getMostSpecificCause
                    .registerModule(new SimpleModule().addSerializer(
                            NestedRuntimeException.class,
                            new JsonSerializer<NestedRuntimeException>() {
                                @Override
                                public void serialize(NestedRuntimeException e, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                                    if (e.getMostSpecificCause() == e) {
                                        jsonGenerator.writeStartObject();
                                        jsonGenerator.writeStringField("mostSpecificCauseMessage", "[skip reference chain]");
                                        jsonGenerator.writeEndObject();
                                    } else {
                                        serializerProvider.defaultSerializeValue(e, jsonGenerator);
                                    }
                                }
                            }
                    ))
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(object);
        } catch (JsonProcessingException ignored) {
        }
        return null;
    }

}
