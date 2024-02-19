package com.github.requestlog.okhttp.support;

import com.github.requestlog.test.model.RequestParamModel;
import okhttp3.FormBody;
import org.springframework.util.CollectionUtils;


public class OkHttpTestUtils {

    public static FormBody createRandomFormBody() {
        RequestParamModel randomModel = RequestParamModel.randomObj();

        FormBody.Builder builder = new FormBody.Builder()
                .add("stringValue", randomModel.getStringValue())
                .add("intValue", String.valueOf(randomModel.getIntValue()))
                .add("booleanValue", String.valueOf(randomModel.getBooleanValue()));
        if (!CollectionUtils.isEmpty(randomModel.getStringList())) {
            randomModel.getStringList().forEach(value -> builder.add("stringList", value));
        }
        if (!CollectionUtils.isEmpty(randomModel.getIntegerList())) {
            randomModel.getIntegerList().forEach(value -> builder.add("integerList", String.valueOf(value)));
        }

        return builder.build();
    }

}
