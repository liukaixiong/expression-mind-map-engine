package com.liukx.expression.engine;

import com.liukx.expression.engine.core.model.ExpressionUser;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties.UserAccount;
import com.liukx.expression.engine.server.model.dto.request.LoginModel;
import com.liukx.expression.engine.server.service.IExpressionLoginService;
import com.liukx.expression.engine.server.service.IExpressionTokenService;
import com.liukx.expression.engine.server.service.impl.ExpressionLoginServiceImpl;
import com.liukx.expression.engine.server.service.impl.ExpressionMd5TokenServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

/**
 * 不依赖 Spring 容器的纯逻辑测试：验证单账号 / 多账号 登录 + token 校验链路。
 * 用于排除 nacos/mysql/redis 等运行时干扰，定位"登录不了"是否为代码逻辑问题。
 *
 * @author liukaixiong
 */
public class TokenLoginLogicTest {

    /** 单账号模式（未配置 users）：验证登录、下发 token、token 反解析是否都能通过 */
    @Test
    public void singleAccount_loginAndTokenRoundTrip() {
        ExpressionServerProperties props = new ExpressionServerProperties();
        props.setUsername("admin");
        props.setPassword("admin@123");
        props.setTokenSecret("test-secret");
        // users 不设置 => null => 应走单账号回退分支

        IExpressionLoginService loginService = newLoginService(props);
        IExpressionTokenService tokenService = newTokenService(props);

        // 1. 登录认证
        LoginModel loginModel = new LoginModel();
        loginModel.setUsername("admin");
        loginModel.setPassword("admin@123");
        ExpressionUser user = loginService.authenticate(loginModel);
        Assert.assertNotNull("单账号登录应成功", user);
        Assert.assertEquals("admin", user.getUserId());

        // 2. 下发 token
        String token = tokenService.tokenCreated(loginModel);
        System.out.println("单账号 token = " + token);
        Assert.assertTrue("token 应是 {userId}.{expireAt}.{sign} 三段格式", token.split("\\.").length == 3);

        // 3. token 校验
        Assert.assertTrue("token 应校验通过", tokenService.checkToken(token));

        // 4. token 反解析
        ExpressionUser resolved = tokenService.resolveToken(token);
        Assert.assertNotNull("token 应能反解析出用户", resolved);
        Assert.assertEquals("admin", resolved.getUserId());

        // 5. 错误密码登录失败
        LoginModel wrong = new LoginModel();
        wrong.setUsername("admin");
        wrong.setPassword("wrong");
        Assert.assertNull("错误密码应登录失败", loginService.authenticate(wrong));
    }

    /** 多账号模式（配置了 users）：验证不同账号拿到不同 token 且可正确区分 */
    @Test
    public void multiAccount_loginAndTokenRoundTrip() {
        ExpressionServerProperties props = new ExpressionServerProperties();
        props.setTokenSecret("test-secret");
        java.util.List<UserAccount> users = new java.util.ArrayList<>();
        users.add(newUser("admin", "admin@123", "u_admin"));
        users.add(newUser("alice", "alice@123", "u_alice"));
        props.setUsers(users);

        IExpressionLoginService loginService = newLoginService(props);
        IExpressionTokenService tokenService = newTokenService(props);

        // admin
        LoginModel adminLogin = new LoginModel();
        adminLogin.setUsername("admin");
        adminLogin.setPassword("admin@123");
        ExpressionUser adminUser = loginService.authenticate(adminLogin);
        Assert.assertNotNull(adminUser);
        String adminToken = tokenService.tokenCreated(adminLogin);

        // alice
        LoginModel aliceLogin = new LoginModel();
        aliceLogin.setUsername("alice");
        aliceLogin.setPassword("alice@123");
        ExpressionUser aliceUser = loginService.authenticate(aliceLogin);
        Assert.assertNotNull(aliceUser);
        String aliceToken = tokenService.tokenCreated(aliceLogin);

        Assert.assertNotEquals("两个账号 token 应不同", adminToken, aliceToken);

        // 反解析能区分到各自的 userId
        Assert.assertEquals("u_admin", tokenService.resolveToken(adminToken).getUserId());
        Assert.assertEquals("u_alice", tokenService.resolveToken(aliceToken).getUserId());
    }

    /** 伪造 token（不知 secret）应校验失败 */
    @Test
    public void forgedToken_shouldFail() {
        ExpressionServerProperties props = new ExpressionServerProperties();
        props.setTokenSecret("real-secret");
        IExpressionTokenService tokenService = newTokenService(props);

        // 用错误 secret 构造的 token
        String forged = "admin." + (System.currentTimeMillis() + 9999999999L) + "." + "deadbeef".repeat(4);
        Assert.assertFalse("伪造 token 应校验失败", tokenService.checkToken(forged));
    }

    /** users 配置为空 list 时，应回退到单账号模式 */
    @Test
    public void emptyUsers_fallsBackToSingleAccount() {
        ExpressionServerProperties props = new ExpressionServerProperties();
        props.setUsername("admin");
        props.setPassword("admin@123");
        props.setUsers(Collections.emptyList());

        IExpressionLoginService loginService = newLoginService(props);
        LoginModel loginModel = new LoginModel();
        loginModel.setUsername("admin");
        loginModel.setPassword("admin@123");
        Assert.assertNotNull("空 users 应回退单账号并登录成功", loginService.authenticate(loginModel));
    }

    /** users 与单账号并存：admin 走单账号回退，liukx/tianlinfei 走 users，三者都能登录 */
    @Test
    public void usersAndSingleAccount_bothWork() {
        ExpressionServerProperties props = new ExpressionServerProperties();
        props.setUsername("admin");
        props.setPassword("admin@123");
        props.setTokenSecret("test-secret");
        java.util.List<UserAccount> users = new java.util.ArrayList<>();
        users.add(newUser("liukx", "zhiyue@123", "u_liukx"));
        users.add(newUser("tianlinfei", "zhiyue@123", "u_tianlinfei"));
        props.setUsers(users);

        IExpressionLoginService loginService = newLoginService(props);
        IExpressionTokenService tokenService = newTokenService(props);

        // admin：users 中没有，回退单账号
        ExpressionUser adminUser = loginService.authenticate(login("admin", "admin@123"));
        Assert.assertNotNull("admin 应通过单账号回退登录成功", adminUser);
        Assert.assertEquals("admin", adminUser.getUserId());

        // liukx：在 users 中
        ExpressionUser liukxUser = loginService.authenticate(login("liukx", "zhiyue@123"));
        Assert.assertNotNull(liukxUser);
        Assert.assertEquals("u_liukx", liukxUser.getUserId());

        // tianlinfei：在 users 中
        ExpressionUser tlfUser = loginService.authenticate(login("tianlinfei", "zhiyue@123"));
        Assert.assertNotNull(tlfUser);
        Assert.assertEquals("u_tianlinfei", tlfUser.getUserId());

        // 三者 token 互不相同
        String adminToken = tokenService.tokenCreated(login("admin", "admin@123"));
        String liukxToken = tokenService.tokenCreated(login("liukx", "zhiyue@123"));
        Assert.assertNotEquals(adminToken, liukxToken);

        // admin 单账号 token 也能正确反解析
        Assert.assertEquals("admin", tokenService.resolveToken(adminToken).getUserId());
    }

    private LoginModel login(String username, String password) {
        LoginModel model = new LoginModel();
        model.setUsername(username);
        model.setPassword(password);
        return model;
    }

    private UserAccount newUser(String username, String password, String userId) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPassword(password);
        account.setUserId(userId);
        return account;
    }

    private IExpressionLoginService newLoginService(ExpressionServerProperties props) {
        ExpressionLoginServiceImpl service = new ExpressionLoginServiceImpl();
        ReflectionTestUtils.setField(service, "properties", props);
        return service;
    }

    private IExpressionTokenService newTokenService(ExpressionServerProperties props) {
        ExpressionMd5TokenServiceImpl service = new ExpressionMd5TokenServiceImpl();
        ReflectionTestUtils.setField(service, "serverProperties", props);
        return service;
    }
}
