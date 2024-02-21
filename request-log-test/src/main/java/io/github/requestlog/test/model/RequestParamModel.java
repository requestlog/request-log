package io.github.requestlog.test.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.requestlog.test.util.RandomUtil;
import lombok.Data;

import java.util.List;


/**
 * Test model for controller parameters.
 */
@Data
public class RequestParamModel {

    private String stringValue;
    private Integer intValue;
    private Boolean booleanValue;
    private List<String> stringList;
    private List<Integer> integerList;


    /**
     * Generate a random {@link RequestParamModel}.
     */
    public static RequestParamModel randomObj() {
        RequestParamModel model = new RequestParamModel();
        model.setStringValue(RandomUtil.randomString(4));
        model.setIntValue(RandomUtil.randomInt(1, 100));
        model.setBooleanValue(RandomUtil.randomBoolean());
        // TODO: 2024/1/30  string list int list
        return model;
    }

    /**
     * Generate a random json string for {@link RequestParamModel}.
     */
    public static String randomJson() {
        try {
            return new ObjectMapper().writeValueAsString(randomObj());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

}
