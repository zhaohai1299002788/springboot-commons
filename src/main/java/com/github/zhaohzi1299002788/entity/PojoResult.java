package com.github.zhaohzi1299002788.entity;

import java.io.Serializable;

public class PojoResult<T> implements Serializable {
    private static final long serialVersionUID = 4634363568158615158L;
    private boolean isSuccess;
    private String message;
    private T data;

    public PojoResult() {

    }

    public PojoResult(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public PojoResult(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public PojoResult(boolean isSuccess, String message, T data) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PojoResult{" +
                "isSuccess=" + isSuccess +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
