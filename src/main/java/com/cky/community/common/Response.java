package com.cky.community.common;

import lombok.Data;

@Data
public class Response<T> {
    private int code;
    private T data;
    private String msg;

    public Response(int code, T data, String msg){
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public static <T> Response<T> success(T data){
        return new Response(0, data, "");
    }

    public static <T> Response<T> success(T data, String msg){
        return new Response(0, data, msg);
    }

    public static <T> Response<T> error(T data){
        return new Response(1, data, "");
    }

    public static <T> Response<T> error(T data, String msg){
        return new Response(1, data, msg);
    }
}
