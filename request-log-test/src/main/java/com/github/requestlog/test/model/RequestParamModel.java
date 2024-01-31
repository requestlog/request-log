package com.github.requestlog.test.model;

import com.github.requestlog.test.util.RandomUtil;
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

}
