package com.ljt.study.core;

import lombok.Data;

@Data
public class R<T> {

    private Integer code;
    private String msg;
    private T data;


    public static R<?> error(String msg) {
        R<?> r = new R<>();
        r.setMsg(msg);
        return r;
    }

}