package com.liukx.expression.engine.server.config.handler;

import com.liukx.expression.engine.server.constants.enums.ErrorEnum;
import com.liukx.expression.engine.server.exception.BusinessException;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@ControllerAdvice
public class GlobalErrorHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public RestResult<Void> fissionExceptionHandler(BusinessException e) {
        logger.warn("#fissionExceptionHandler:{}", e.getMessage());
        return RestResult.failed(e.getCode(), e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public RestResult<Void> exceptionHandler(Exception e) {
        logger.warn("#exceptionHandler:", e);
        return RestResult.failed(ErrorEnum.SYSTEM_ERROR.code(), ErrorEnum.SYSTEM_ERROR.message());
    }


    @ResponseBody
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public RestResult<Void> exceptionHandler(MissingServletRequestParameterException e) {
        logger.warn("#exceptionHandler:{}", e.getMessage());
        return RestResult.failed(ErrorEnum.PARAMS_ERROR.code(), e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = ServletRequestBindingException.class)
    public RestResult<Void> servletRequestBindingExceptionHandler(ServletRequestBindingException e) {
        logger.warn("#servletRequestBindingExceptionHandler:{}", e.getMessage());
        return RestResult.failed(ErrorEnum.PARAMS_ERROR.code(), e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public RestResult<Void> invalidFormatException(HttpMessageNotReadableException e) {
        logger.warn("#invalidFormatException:{}", e.getMessage());
        return RestResult.failed(ErrorEnum.PARAMS_ERROR.code(), ErrorEnum.PARAMS_ERROR.message());
    }

    @ResponseBody
    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public RestResult<Void> httpMediaTypeNotSupportedException(HttpMessageNotReadableException e) {
        return RestResult.failed(ErrorEnum.PARAMS_ERROR.code(), e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public RestResult<Void> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        logger.warn("#methodArgumentTypeMismatchException:{}", e.getMessage());
        return RestResult.failed(ErrorEnum.PARAMS_ERROR.code(), "[" + e.getName() + "]" + " 参数类型不匹配");
    }

    @ResponseBody
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public RestResult<Void> multipartExceptionExceptionHandler(HttpRequestMethodNotSupportedException e) {
        logger.warn("#multipartExceptionExceptionHandler:{}", e.getMessage());
        return RestResult.failed(ErrorEnum.PARAMS_ERROR.code(), e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public RestResult<Void> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.warn("#methodArgumentNotValidException:{}", e.getMessage());

        List<FieldError> errors = e.getBindingResult().getFieldErrors();

        StringBuilder builder = new StringBuilder();
        for (FieldError error : errors) {
            String field = error.getField();
            String message = error.getDefaultMessage();
            builder.append("[").append(field).append("]").append(message).append("; ");
        }

        return RestResult.failed(ErrorEnum.SYSTEM_ERROR.code(), builder.toString());
    }


    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public RestResult<Void> bindExceptionHandler(BindException e) {
        logger.warn("#bindExceptionHandler:{}", e.getMessage());

        List<FieldError> errors = e.getFieldErrors();
        StringBuilder builder = new StringBuilder();
        for (FieldError error : errors) {
            String field = error.getField();
            String message = error.getDefaultMessage();
            builder.append("[").append(field).append("]").append(message).append("; ");
        }

        return RestResult.failed(ErrorEnum.PARAMS_ERROR.code(), builder.toString());
    }
}
