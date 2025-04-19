package com.h.qq.pojo;

import lombok.Data;

/**
 * @author hhm
 * @date 2024/8/28
 * @description TODO
 */
@Data
public class Response {
    private String code;
    private String subcode;
    private String message;
    private com.h.qq.pojo.Data data;
}
