package com.liukx.expression.engine.server.mapper.entity;

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


    @TableField("created")
    private Date created;


    @TableField("creator")
    private String creator;


    @TableField("updated")
    private Date updated;


    @TableField("updater")
    private String updater;

}
