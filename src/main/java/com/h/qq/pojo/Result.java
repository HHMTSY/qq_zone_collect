package com.h.qq.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.lang.reflect.Type;

/**
 * @author hhm
 * @date 2024/8/28
 * @description TODO
 */
@Data
@TableName("t_result")
public class Result {
    @TableId(type = IdType.AUTO)
    private String id;
    private String qqNum;
    private String device;
    private String time;
    private String content;
}
