package io.github.requestlog.apachehttpclient.support;

import io.github.requestlog.test.model.RequestParamModel;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;



public class HttpClientTestUtils {


    public static StringEntity createRandomJsonStringEntity() {
        return new StringEntity(RequestParamModel.randomJson(), "UTF-8");
    }


    public static UrlEncodedFormEntity createRandomFormEntity() {
        RequestParamModel randomModel = RequestParamModel.randomObj();

        List<BasicNameValuePair> parameters = new ArrayList<>();

        addParameter(parameters, "stringValue", randomModel.getStringValue());
        addParameter(parameters, "intValue", randomModel.getIntValue());
        addParameter(parameters, "booleanValue", randomModel.getBooleanValue());
        addParameterList(parameters, "stringList", randomModel.getStringList());
        addParameterList(parameters, "integerList", randomModel.getIntegerList());

        try {
            return new UrlEncodedFormEntity(parameters);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    private static void addParameter(List<BasicNameValuePair> parameters, String paramName, Object paramValue) {
        if (paramValue != null) {
            parameters.add(new BasicNameValuePair(paramName, paramValue.toString()));
        }
    }

    private static void addParameterList(List<BasicNameValuePair> parameters, String paramName, List<?> paramValues) {
        if (paramValues != null && !paramValues.isEmpty()) {
            for (Object value : paramValues) {
                addParameter(parameters, paramName, value);
            }
        }
    }


}
