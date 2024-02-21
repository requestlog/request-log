package io.github.requestlog.test.aop;


import io.github.requestlog.test.model.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * Global controller advice to handle exceptions across all REST controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalRestControllerAdvice {

    /**
     * Exception handler for general exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseModel> handleException(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseModel(500, String.format("error[%s]", exception.getMessage())));
    }

}
