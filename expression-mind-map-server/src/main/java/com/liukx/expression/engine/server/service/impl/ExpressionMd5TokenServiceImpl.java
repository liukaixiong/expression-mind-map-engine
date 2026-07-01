package com.liukx.expression.engine.server.service.impl;

import com.liukx.expression.engine.core.model.ExpressionUser;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties.UserAccount;
import com.liukx.expression.engine.server.model.dto.request.LoginModel;
import com.liukx.expression.engine.server.service.IExpressionTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 默认的 token 授权校验。
 *
 * <p>token 为无状态签名格式：{@code {userId}.{expireAt}.{sign}}，
 * 其中 {@code sign = md5(userId + "." + expireAt + tokenSecret)}。
 * <ul>
 *   <li>支持多用户：token 中携带 userId，可反解析出具体的操作人。</li>
 *   <li>不可伪造：依赖 tokenSecret 签名，仅知道用户名无法构造合法 token。</li>
 *   <li>可过期：携带 expireAt，校验时判断有效期。</li>
 * </ul>
 * 未配置 users 时回退到单账号模式（向后兼容）。
 *
 * @author liukaixiong
 * @date 2024/10/15 - 13:34
 */
@Service
public class ExpressionMd5TokenServiceImpl implements IExpressionTokenService {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionMd5TokenServiceImpl.class);

    /**
     * token 有效期（天），与登录下发的 cookie 过期保持一致。
     */
    private static final int TOKEN_TIMEOUT_DAY = 1;

    /**
     * tokenSecret 缺省时的内置兜底值；生产环境务必显式配置 spring.expression.server.token-secret。
     */
    private static final String DEFAULT_TOKEN_SECRET = "expression-engine-default-token-secret";

    @Autowired
    private ExpressionServerProperties serverProperties;

    @Override
    public String tokenCreated(LoginModel loginModel) {
        final String username = loginModel.getUsername();
        final String userId = resolveUserId(username);
        return buildSignedToken(userId);
    }

    @Override
    public boolean checkToken(String token) {
        return parseAndVerify(token) != null;
    }

    @Override
    public ExpressionUser resolveToken(String token) {
        String userId = parseAndVerify(token);
        if (userId == null) {
            return null;
        }
        // 回查账号拿到展示用的 username；查不到时用 userId 兜底
        UserAccount account = findAccountByUserId(userId);
        String username = account != null ? account.getUsername() : userId;
        ExpressionUser user = new ExpressionUser();
        user.setUserId(userId);
        user.setUsername(username);
        user.setSource(ExpressionUser.Source.CONSOLE);
        return user;
    }

    // ==================== 内部方法 ====================

    /**
     * 构造带签名的 token：{userId}.{expireAt}.{sign}
     */
    private String buildSignedToken(String userId) {
        long expireAt = System.currentTimeMillis() + TOKEN_TIMEOUT_DAY * 24L * 60L * 60L * 1000L;
        String sign = sign(userId, expireAt);
        return userId + "." + expireAt + "." + sign;
    }

    /**
     * 解析并校验 token，成功返回 userId，失败返回 null。
     */
    private String parseAndVerify(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        String userId = parts[0];
        long expireAt;
        try {
            expireAt = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
        String sign = parts[2];
        if (!StringUtils.hasText(userId) || !sign(userId, expireAt).equals(sign)) {
            logger.debug("token 签名校验失败: {}", userId);
            return null;
        }
        if (System.currentTimeMillis() > expireAt) {
            logger.debug("token 已过期: {}", userId);
            return null;
        }
        return userId;
    }

    private String sign(String userId, long expireAt) {
        return md5(userId + "." + expireAt + "." + getTokenSecret());
    }

    /**
     * 按用户名解析 userId：优先在 users 列表中查找，未配置或找不到时用 username 兜底。
     */
    private String resolveUserId(String username) {
        List<UserAccount> users = serverProperties.getUsers();
        if (users != null) {
            for (UserAccount account : users) {
                if (username != null && username.equals(account.getUsername())) {
                    return StringUtils.hasText(account.getUserId()) ? account.getUserId() : account.getUsername();
                }
            }
        }
        // 单账号模式或 users 中未找到，回退用 username 作为身份
        return username;
    }

    private UserAccount findAccountByUserId(String userId) {
        List<UserAccount> users = serverProperties.getUsers();
        if (users == null) {
            return null;
        }
        for (UserAccount account : users) {
            String accountUserId = StringUtils.hasText(account.getUserId()) ? account.getUserId() : account.getUsername();
            if (userId.equals(accountUserId)) {
                return account;
            }
        }
        return null;
    }

    private String getTokenSecret() {
        if (StringUtils.hasText(serverProperties.getTokenSecret())) {
            return serverProperties.getTokenSecret();
        }
        if (StringUtils.hasText(serverProperties.getApiKey())) {
            return serverProperties.getApiKey();
        }
        return DEFAULT_TOKEN_SECRET;
    }

    private String md5(String input) {
        return DigestUtils.md5DigestAsHex(input.getBytes());
    }
}
