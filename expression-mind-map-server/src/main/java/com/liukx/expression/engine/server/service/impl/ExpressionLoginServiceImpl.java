package com.liukx.expression.engine.server.service.impl;

import com.liukx.expression.engine.core.model.ExpressionUser;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties.UserAccount;
import com.liukx.expression.engine.server.model.dto.request.LoginModel;
import com.liukx.expression.engine.server.service.IExpressionLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 登录认证。
 *
 * <p>认证顺序：先在 users 列表中匹配，命中则返回对应账号身份；未命中再回退到单账号
 * （username/password）校验。这样 users 与单账号并存，admin 等单账号也能登录。
 * 仅配置了单账号、未配置 users 时，等价于旧行为。
 *
 * @author liukaixiong
 * @date 2024/10/15 - 13:26
 */
@Service
public class ExpressionLoginServiceImpl implements IExpressionLoginService {

    @Autowired
    private ExpressionServerProperties properties;

    @Override
    public boolean login(LoginModel loginModel) {
        return authenticate(loginModel) != null;
    }

    @Override
    public ExpressionUser authenticate(LoginModel loginModel) {
        // 多用户模式：优先遍历 users 列表匹配
        List<UserAccount> users = properties.getUsers();
        if (users != null && !users.isEmpty()) {
            for (UserAccount account : users) {
                if (match(account.getUsername(), account.getPassword(), loginModel)) {
                    return buildUser(account);
                }
            }
            // users 中未匹配时，继续回退到单账号校验，保证 admin 等单账号也能登录
        }

        // 单账号模式（向后兼容，与 users 并存）
        final String username = properties.getUsername();
        final String password = properties.getPassword();
        if (!(StringUtils.hasText(username) && StringUtils.hasText(password))) {
            return null;
        }
        if (match(username, password, loginModel)) {
            ExpressionUser user = new ExpressionUser();
            user.setUserId(username);
            user.setUsername(username);
            user.setSource(ExpressionUser.Source.CONSOLE);
            return user;
        }
        return null;
    }

    private boolean match(String username, String password, LoginModel loginModel) {
        if (loginModel == null) {
            return false;
        }
        return username != null && username.equals(loginModel.getUsername())
                && password != null && password.equals(loginModel.getPassword());
    }

    private ExpressionUser buildUser(UserAccount account) {
        ExpressionUser user = new ExpressionUser();
        user.setUserId(StringUtils.hasText(account.getUserId()) ? account.getUserId() : account.getUsername());
        user.setUsername(account.getUsername());
        user.setSource(ExpressionUser.Source.CONSOLE);
        return user;
    }
}
