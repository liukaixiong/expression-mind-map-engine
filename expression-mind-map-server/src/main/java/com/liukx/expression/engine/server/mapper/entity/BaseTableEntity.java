package com.liukx.expression.engine.server.mapper.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

/**
 * 基础实体父类通用字段
 *
 * @author liukaixiong
 * @date 2024/5/13 - 18:46
 */
@Data
public class BaseTableEntity {
    @TableId(value = "id",
            type = IdType.AUTO)
    private Long id;


    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;


    @TableField(value = "created", fill = FieldFill.INSERT)
    private Date created;


    @TableField(value = "creator", fill = FieldFill.INSERT)
    private String creator;


    @TableField(value = "updated", fill = FieldFill.INSERT_UPDATE)
    private Date updated;


    @TableField(value = "updater", fill = FieldFill.INSERT_UPDATE)
    private String updater;

}
