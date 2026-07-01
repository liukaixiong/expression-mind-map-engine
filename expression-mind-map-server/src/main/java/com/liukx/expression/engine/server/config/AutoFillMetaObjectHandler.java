package com.liukx.expression.engine.server.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.liukx.expression.engine.core.model.ExpressionUserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatis Plus 自动填充 creator/updater/createBy/updateBy
 *
 * @author liukaixiong
 */
@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 统一从 token 上下文取操作人；未登录兜底为 system，保证审计字段非空且来源可信
        String username = ExpressionUserContext.currentUsernameOrSystem();

        this.setFieldValByName("creator", username, metaObject);
        this.setFieldValByName("updater", username, metaObject);
        this.setFieldValByName("createBy", username, metaObject);
        this.setFieldValByName("updateBy", username, metaObject);

        this.setFieldValByName("created", new Date(), metaObject);
        this.setFieldValByName("updated", new Date(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String username = ExpressionUserContext.currentUsernameOrSystem();

        this.setFieldValByName("updater", username, metaObject);
        this.setFieldValByName("updateBy", username, metaObject);

        this.setFieldValByName("updated", new Date(), metaObject);
    }
}
