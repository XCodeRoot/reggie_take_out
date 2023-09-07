package com.ithema.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)//公共字段填充策略 : 插入时,填充字段
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)//公共字段填充策略 : 插入或更新时,填充字段
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)//公共字段填充策略 : 插入时,填充
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)//公共字段填充策略 : 插入或更新时,填充字段
    private Long updateUser;

}
