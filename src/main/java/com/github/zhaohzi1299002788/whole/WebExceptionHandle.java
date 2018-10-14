package com.github.zhaohzi1299002788.whole;

import com.github.zhaohzi1299002788.entity.PojoResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;

/**
 * 全局异常统一处理
 */

@RestControllerAdvice
public class WebExceptionHandle {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebExceptionHandle.class);

    /**
     * 400
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public PojoResult handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        LOGGER.error("参数解析失败 | e : {} ", e);
        PojoResult pojoResult = new PojoResult();
        pojoResult.setSuccess(false);
        pojoResult.setMessage("could_not_read_json");
        return pojoResult;
    }

    /**
     * 500
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(Exception.class)
    public PojoResult handleHttpMediaTypeNoeSupportedException(Exception e) {
        LOGGER.error("服务运行异常", e);
        PojoResult pojoResult = new PojoResult();
        pojoResult.setSuccess(false);
        return pojoResult;
    }

    /**
     * 数据找不到异常
     *
     * @param ex
     * @param request
     * @return
     * @throws IOException
     */
    @ExceptionHandler({IOException.class})
    public PojoResult<Object> handleDataNotFoundException(RuntimeException ex, WebRequest request) throws IOException {
        LOGGER.error("服务运行异常", ex);
        PojoResult pojoResult = new PojoResult();
        pojoResult.setSuccess(false);
        return pojoResult;
    }

}
