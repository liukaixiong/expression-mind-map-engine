package com.liukx.expression.engine.server.config.props;

import com.liukx.expression.engine.server.enums.TableSplitRule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;

/**
 * @author liukaixiong
 * @date 2025/2/26 - 13:39
 */
@ConfigurationProperties(prefix = "spring.expression.server")
@Data
@RefreshScope
public class ExpressionServerProperties {
    /**
     * 是否启用登录
     */
    private boolean enableLogin;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 表名规则
     */
    private List<TableRule> tableRuleList;

    @Data
    public static class TableRule {
        /**
         * 需要拆分的表名
         */
        private String tableName;

        /**
         * 表的分割日期规则
         */
        private TableSplitRule tableSplitRule;

        /**
         * 存储的最大数量
         */
        private Integer maxSaveNumber;

    }

}
