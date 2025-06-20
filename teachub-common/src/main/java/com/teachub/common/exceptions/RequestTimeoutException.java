package com.teachub.common.exceptions;

/**
 * 请求超时异常
 *
 * @ClassName RequestTimeoutException
 *  wusongsong
 * @  /6/30 16:58
 * @version 1.0.0
 **/
public class RequestTimeoutException  extends CommonException{
    public RequestTimeoutException(String message) {
        super(message);
    }

    public RequestTimeoutException(int code, String message) {
        super(code, message);
    }

    public RequestTimeoutException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
