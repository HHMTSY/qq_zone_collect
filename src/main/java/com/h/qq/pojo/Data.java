package com.h.qq.pojo;

import java.util.List;

/**
 * @author hhm
 * @date 2024/8/28
 * @description TODO
 */
@lombok.Data
public class Data {
    private List<MyFriend> items;

    private Response module_16;

    //日志
    private String RZ;
    //说说
    private String SS;
    //照片数量
    private String XC;

}
