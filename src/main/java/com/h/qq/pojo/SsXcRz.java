package com.h.qq.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author hhm
 * @date 2024/8/29
 * @description TODO
 */
@Data
@TableName("t_ss_xc_rz")
public class SsXcRz {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String qqNum;
    private String remark;
    private int ss;
    private int xc;
    private int rz;
}
