package com.github.util.concurrent;

/**
 * @author wangjj7
 * @date 2024/8/26
 * @description
 */
public class CallFrequencyExceedingException extends RuntimeException {

    public CallFrequencyExceedingException() {
    }

    public CallFrequencyExceedingException(String message) {
        super(message);
    }

    public CallFrequencyExceedingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CallFrequencyExceedingException(Throwable cause) {
        super(cause);
    }

    public CallFrequencyExceedingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
