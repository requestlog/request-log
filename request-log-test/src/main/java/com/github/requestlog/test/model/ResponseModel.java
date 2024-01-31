package com.github.requestlog.test.model;

import lombok.Data;


/**
 * Response model for controller.
 */
@Data
public class ResponseModel {

    private Integer code;
    private String message;

    public ResponseModel() {
    }

    public ResponseModel(Integer code) {
        this.code = code;
    }

    public ResponseModel(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
