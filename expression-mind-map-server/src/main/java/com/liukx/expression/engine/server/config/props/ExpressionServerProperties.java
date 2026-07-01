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
     * 用户名（单账号，与 users 并存：users 中未匹配时会回退到此账号校验）
     */
    private String username;

    /**
     * 密码（单账号，与 users 并存：users 中未匹配时会回退到此账号校验）
     */
    private String password;

    /**
     * 多用户账号列表（配置驱动，不落库）。与单账号并存：登录时优先匹配 users，
     * 未命中再回退到 username/password。用于支持多人登录，并在操作记录中区分操作人。
     */
    private List<UserAccount> users;

    /**
     * Token 签名密钥；缺省时回退到 apiKey，再缺省回退到内置默认值。
     * 注意：生产环境务必显式配置，避免被猜到。
     */
    private String tokenSecret;

    /**
     * API Key，用于服务间调用鉴权
     */
    private String apiKey;

    /**
     * 表名规则
     */
    private List<TableRule> tableRuleList;

    /**
     * ai表达式配置
     */
    private AiExpressionProperties ai;

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

    /**
     * 配置驱动的用户账号（不落库）
     */
    @Data
    public static class UserAccount {
        /**
         * 用户名
         */
        private String username;

        /**
         * 密码（明文配置，与单账号模式保持一致）
         */
        private String password;

        /**
         * 用户唯一ID；缺省时用 username 兜底
         */
        private String userId;
    }

}
