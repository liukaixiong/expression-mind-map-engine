package com.liukx.expression.engine;

import cn.hutool.core.lang.UUID;
import com.liukx.expression.engine.client.config.ExpressionConfiguration;
import com.liukx.expression.engine.client.engine.ClientEngineFactory;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.config.TestRedisConfig;
import com.liukx.expression.engine.core.api.model.ClientExpressionSubmitRequest;
import com.liukx.expression.engine.function.DemoExampleFunction;
import com.liukx.expression.engine.function.DemoMapExampleFunction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务场景测试用例
 * <p>
 * 使用前请确保:
 * 1、启动服务端（注意Redis要连接上，具体配置 => src/test/resources/application.yml）
 * 2、在执行器配置页面: <a href="http://localhost:20888/template/executor-list.html">配置页面</a>
 * 3、分别导入以下三个规则JSON文件（位于 src/test/resources/ 下）:
 *    - scenario_shop_order_risk.json       (电商订单风控)
 *    - scenario_finance_loan_approval.json  (金融贷款审批)
 *    - scenario_marketing_promotion.json    (营销活动引擎)
 * 4、执行对应的测试方法
 * 5、在追踪日志页面查看执行结果: <a href="http://localhost:20888/template/trace-list.html">追踪页面</a>
 *
 * @author scenario-test
 * @date 2025/04/02
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RedissonAutoConfigurationV2.class,
        TestRedisConfig.class,
        ExpressionConfiguration.class,
        RestTemplateAutoConfiguration.class,
        DemoExampleFunction.class,
        DemoMapExampleFunction.class
})
public class BusinessScenarioTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ClientEngineFactory clientEngine;

    // ======================== 场景1：电商订单风控 ========================

    /**
     * 电商订单风控 - 正常下单流程
     * <p>
     * 测试内容:
     * - 订单金额在合理范围内，通过金额校验
     * - 用户等级为黄金会员(VIP3)，享受85折优惠
     * - 使用优惠券，满足门槛，核销成功
     * - 最终结算成功
     * <p>
     * 导入文件: scenario_shop_order_risk.json
     */
    @Test
    public void testShopOrder_NormalOrder() throws Exception {
        Map<String, Object> envContext = new ConcurrentHashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", 2001L);
        requestMap.put("orderId", "ORD202504020001");
        requestMap.put("orderAmount", 299.0);
        requestMap.put("userLevel", 3);
        requestMap.put("couponCode", "COUPON50");
        requestMap.put("couponAmount", 50);
        requestMap.put("couponThreshold", 200);
        requestMap.put("eventName", "normal_order");

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("shop");
        request.setExecutorCode("orderRisk");
        request.setUserId(2001L);
        request.setRequest(requestMap);
        request.setUnionId("ORD202504020001");
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        context.addEnvContext("scenarioModules", Arrays.asList("order_risk"));

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("电商下单-正常流程结果: {}", resultContext);
        } catch (Exception e) {
            logger.error("执行异常", e);
        }
        Thread.sleep(3000);
    }

    /**
     * 电商订单风控 - 黑名单用户被拦截
     * <p>
     * 测试内容:
     * - 用户ID=1001在黑名单中
     * - 被最高优先级的黑名单分支拦截
     * - 强制终止流程，返回拒绝原因
     * <p>
     * 导入文件: scenario_shop_order_risk.json
     */
    @Test
    public void testShopOrder_BlacklistUser() throws Exception {
        Map<String, Object> envContext = new ConcurrentHashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", 1001L);
        requestMap.put("orderId", "ORD202504020002");
        requestMap.put("orderAmount", 100.0);
        requestMap.put("userLevel", 1);

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("shop");
        request.setExecutorCode("orderRisk");
        request.setUserId(1001L);
        request.setRequest(requestMap);
        request.setUnionId("ORD202504020002");
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        context.addEnvContext("scenarioModules", Arrays.asList("order_risk"));

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("电商下单-黑名单用户结果: {}", resultContext);
        } catch (Exception e) {
            logger.error("执行异常", e);
        }
        Thread.sleep(3000);
    }

    /**
     * 电商订单风控 - 订单金额不足
     * <p>
     * 测试内容:
     * - 用户ID正常(不在黑名单)
     * - 订单金额低于最低限额(10元)
     * - 被金额校验分支拒绝
     * <p>
     * 导入文件: scenario_shop_order_risk.json
     */
    @Test
    public void testShopOrder_AmountTooLow() throws Exception {
        Map<String, Object> envContext = new ConcurrentHashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", 3001L);
        requestMap.put("orderId", "ORD202504020003");
        requestMap.put("orderAmount", 5.0);
        requestMap.put("userLevel", 1);

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("shop");
        request.setExecutorCode("orderRisk");
        request.setUserId(3001L);
        request.setRequest(requestMap);
        request.setUnionId("ORD202504020003");
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        context.addEnvContext("scenarioModules", Arrays.asList("order_risk"));

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("电商下单-金额不足结果: {}", resultContext);
        } catch (Exception e) {
            logger.error("执行异常", e);
        }
        Thread.sleep(3000);
    }

    /**
     * 电商订单风控 - 秒杀活动（非秒杀时段）
     * <p>
     * 测试内容:
     * - 事件为flash_sale(秒杀活动)
     * - 非秒杀时段(10:00-12:00)，被时间窗口校验拒绝
     * <p>
     * 导入文件: scenario_shop_order_risk.json
     */
    @Test
    public void testShopOrder_FlashSaleOutOfTime() throws Exception {
        Map<String, Object> envContext = new ConcurrentHashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", 4001L);
        requestMap.put("orderId", "ORD202504020004");
        requestMap.put("orderAmount", 99.0);
        requestMap.put("userLevel", 2);
        requestMap.put("eventName", "flash_sale");

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("shop");
        request.setExecutorCode("orderRisk");
        request.setUserId(4001L);
        request.setRequest(requestMap);
        request.setEventName("flash_sale");
        request.setUnionId("ORD202504020004");
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        context.addEnvContext("scenarioModules", Arrays.asList("order_risk"));

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("电商下单-秒杀非时段结果: {}", resultContext);
        } catch (Exception e) {
            logger.error("执行异常", e);
        }
        Thread.sleep(3000);
    }

    // ======================== 场景2：金融贷款审批 ========================

    /**
     * 金融贷款审批 - 正常审批通过
     * <p>
     * 测试内容:
     * - 风控检查通过(非高风险、无逾期)
     * - 信用评分750以上(优秀)，享受利率下浮10%
     * - 收入负债比正常
     * - 贷款金额和期限在合理范围
     * - 最终审批通过
     * <p>
     * 导入文件: scenario_finance_loan_approval.json
     */
    @Test
    public void testFinanceLoan_Approved() throws Exception {
        Map<String, Object> envContext = new ConcurrentHashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("applicantId", "APP20250402001");
        requestMap.put("applicantName", "张三");
        requestMap.put("creditScore", 780);
        requestMap.put("monthlyIncome", 20000);
        requestMap.put("monthlyDebt", 3000);
        requestMap.put("loanAmount", 100000);
        requestMap.put("loanTerm", 24);
        requestMap.put("riskLevel", "LOW");
        requestMap.put("overdueCount", 0);

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("finance");
        request.setExecutorCode("loanApproval");
        request.setUserId(5001L);
        request.setRequest(requestMap);
        request.setUnionId("LOAN20250402001");
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        context.addEnvContext("scenarioModules", Arrays.asList("loan_approval"));

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("金融贷款-审批通过结果: {}", resultContext);
        } catch (Exception e) {
            logger.error("执行异常", e);
        }
        Thread.sleep(3000);
    }

    /**
     * 金融贷款审批 - 高风险用户被拒绝
     * <p>
     * 测试内容:
     * - 用户风险等级为HIGH
     * - 被风控前置检查拦截，强制终止整个流程
     * <p>
     * 导入文件: scenario_finance_loan_approval.json
     */
    @Test
    public void testFinanceLoan_HighRiskReject() throws Exception {
        Map<String, Object> envContext = new ConcurrentHashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("applicantId", "APP20250402002");
        requestMap.put("creditScore", 720);
        requestMap.put("monthlyIncome", 15000);
        requestMap.put("monthlyDebt", 2000);
        requestMap.put("loanAmount", 50000);
        requestMap.put("loanTerm", 12);
        requestMap.put("riskLevel", "HIGH");
        requestMap.put("overdueCount", 0);

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("finance");
        request.setExecutorCode("loanApproval");
        request.setUserId(5002L);
        request.setRequest(requestMap);
        request.setUnionId("LOAN20250402002");
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        context.addEnvContext("scenarioModules", Arrays.asList("loan_approval"));

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("金融贷款-高风险拒绝结果: {}", resultContext);
        } catch (Exception e) {
            logger.error("执行异常", e);
        }
        Thread.sleep(3000);
    }

    /**
     * 金融贷款审批 - 信用评分不足被拒绝
     * <p>
     * 测试内容:
     * - 风控检查通过(非高风险)
     * - 信用评分500，低于最低要求600
     * - 被信用评分校验分支拒绝
     * <p>
     * 导入文件: scenario_finance_loan_approval.json
     */
    @Test
    public void testFinanceLoan_LowCreditScore() throws Exception {
        Map<String, Object> envContext = new ConcurrentHashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("applicantId", "APP20250402003");
        requestMap.put("creditScore", 500);
        requestMap.put("monthlyIncome", 8000);
        requestMap.put("monthlyDebt", 1000);
        requestMap.put("loanAmount", 30000);
        requestMap.put("loanTerm", 12);
        requestMap.put("riskLevel", "MEDIUM");
        requestMap.put("overdueCount", 1);

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("finance");
        request.setExecutorCode("loanApproval");
        request.setUserId(5003L);
        request.setRequest(requestMap);
        request.setUnionId("LOAN20250402003");
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        context.addEnvContext("scenarioModules", Arrays.asList("loan_approval"));

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("金融贷款-信用不足结果: {}", resultContext);
        } catch (Exception e) {
            logger.error("执行异常", e);
        }
        Thread.sleep(3000);
    }

    /**
     * 金融贷款审批 - 负债率过高 + 大额贷款转人工
     * <p>
     * 测试内容:
     * - 风控检查通过
     * - 信用评分650(良好)，执行基准利率
     * - 月负债/月收入比超过50%，被负债率校验拒绝
     * <p>
     * 导入文件: scenario_finance_loan_approval.json
     */
    @Test
    public void testFinanceLoan_HighDTI() throws Exception {
        Map<String, Object> envContext = new ConcurrentHashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("applicantId", "APP20250402004");
        requestMap.put("creditScore", 650);
        requestMap.put("monthlyIncome", 10000);
        requestMap.put("monthlyDebt", 6000);
        requestMap.put("loanAmount", 200000);
        requestMap.put("loanTerm", 48);
        requestMap.put("riskLevel", "MEDIUM");
        requestMap.put("overdueCount", 0);

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("finance");
        request.setExecutorCode("loanApproval");
        request.setUserId(5004L);
        request.setRequest(requestMap);
        request.setUnionId("LOAN20250402004");
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        context.addEnvContext("scenarioModules", Arrays.asList("loan_approval"));

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("金融贷款-负债率过高结果: {}", resultContext);
        } catch (Exception e) {
            logger.error("执行异常", e);
        }
        Thread.sleep(3000);
    }

    // ======================== 场景3：营销活动引擎 ========================

    /**
     * 营销活动 - 新人礼包
     * <p>
     * 测试内容:
     * - 活动时间校验通过(全年有效+8:00-22:00时段)
     * - 参与次数校验通过
     * - 事件为new_user_gift(新人礼包)
     * - 注册天数3天，在7天内，符合新人条件
     * - 发放50积分新人礼包
     * <p>
     * 导入文件: scenario_marketing_promotion.json
     */
    @Test
    public void testMarketing_NewUserGift() throws Exception {
        Map<String, Object> envContext = new ConcurrentHashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", 6001L);
        requestMap.put("eventName", "new_user_gift");
        requestMap.put("registerDays", 3);
        requestMap.put("todayParticipateCount", 0);
        requestMap.put("userLevel", 1);

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("marketing");
        request.setExecutorCode("promotion");
        request.setUserId(6001L);
        request.setRequest(requestMap);
        request.setEventName("new_user_gift");
        request.setUnionId("MKT20250402001");
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        context.addEnvContext("scenarioModules", Arrays.asList("promotion"));

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("营销活动-新人礼包结果: {}", resultContext);
        } catch (Exception e) {
            logger.error("执行异常", e);
        }
        Thread.sleep(3000);
    }

    /**
     * 营销活动 - VIP邀请奖励
     * <p>
     * 测试内容:
     * - 活动时间校验通过
     * - 参与次数校验通过
     * - 事件为invite_reward(邀请有奖)
     * - 邀请人等级VIP2，享受1.5倍邀请积分(100*1.5=150)
     * <p>
     * 导入文件: scenario_marketing_promotion.json
     */
    @Test
    public void testMarketing_VIPInviteReward() throws Exception {
        Map<String, Object> envContext = new ConcurrentHashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", 6002L);
        requestMap.put("eventName", "invite_reward");
        requestMap.put("inviterLevel", 2);
        requestMap.put("inviteeUserId", 7001L);
        requestMap.put("todayParticipateCount", 1);
        requestMap.put("userLevel", 2);

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("marketing");
        request.setExecutorCode("promotion");
        request.setUserId(6002L);
        request.setRequest(requestMap);
        request.setEventName("invite_reward");
        request.setUnionId("MKT20250402002");
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        context.addEnvContext("scenarioModules", Arrays.asList("promotion"));

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("营销活动-VIP邀请奖励结果: {}", resultContext);
        } catch (Exception e) {
            logger.error("执行异常", e);
        }
        Thread.sleep(3000);
    }

    /**
     * 营销活动 - 连续签到翻倍
     * <p>
     * 测试内容:
     * - 活动时间校验通过
     * - 参与次数校验通过
     * - 事件为daily_signin(每日签到)
     * - 连续签到7天，积分翻倍(10*2=20积分)
     * <p>
     * 导入文件: scenario_marketing_promotion.json
     */
    @Test
    public void testMarketing_ConsecutiveSignin() throws Exception {
        Map<String, Object> envContext = new ConcurrentHashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", 6003L);
        requestMap.put("eventName", "daily_signin");
        requestMap.put("consecutiveDays", 7);
        requestMap.put("todayParticipateCount", 0);
        requestMap.put("userLevel", 1);

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("marketing");
        request.setExecutorCode("promotion");
        request.setUserId(6003L);
        request.setRequest(requestMap);
        request.setEventName("daily_signin");
        request.setUnionId("MKT20250402003");
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        context.addEnvContext("scenarioModules", Arrays.asList("promotion"));

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("营销活动-连续签到翻倍结果: {}", resultContext);
        } catch (Exception e) {
            logger.error("执行异常", e);
        }
        Thread.sleep(3000);
    }

    /**
     * 营销活动 - 参与次数已满被拒绝
     * <p>
     * 测试内容:
     * - 活动时间校验通过
     * - 今日参与次数已达3次上限
     * - 被参与次数限制分支拒绝
     * <p>
     * 导入文件: scenario_marketing_promotion.json
     */
    @Test
    public void testMarketing_ParticipateLimitExceeded() throws Exception {
        Map<String, Object> envContext = new ConcurrentHashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", 6004L);
        requestMap.put("eventName", "daily_signin");
        requestMap.put("consecutiveDays", 2);
        requestMap.put("todayParticipateCount", 3);
        requestMap.put("userLevel", 1);

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("marketing");
        request.setExecutorCode("promotion");
        request.setUserId(6004L);
        request.setRequest(requestMap);
        request.setEventName("daily_signin");
        request.setUnionId("MKT20250402004");
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        context.addEnvContext("scenarioModules", Arrays.asList("promotion"));

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("营销活动-参与次数已满结果: {}", resultContext);
        } catch (Exception e) {
            logger.error("执行异常", e);
        }
        Thread.sleep(3000);
    }

    /**
     * 营销活动 - VIP抽奖 + 周末双倍积分
     * <p>
     * 测试内容:
     * - 事件为lottery(抽奖)
     * - VIP3用户进入VIP抽奖通道
     * - 如果当天是周末则触发双倍积分
     * <p>
     * 导入文件: scenario_marketing_promotion.json
     */
    @Test
    public void testMarketing_VIPLottery() throws Exception {
        Map<String, Object> envContext = new ConcurrentHashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", 6005L);
        requestMap.put("eventName", "lottery");
        requestMap.put("userLevel", 3);
        requestMap.put("todayParticipateCount", 0);

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("marketing");
        request.setExecutorCode("promotion");
        request.setUserId(6005L);
        request.setRequest(requestMap);
        request.setEventName("lottery");
        request.setUnionId("MKT20250402005");
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        context.addEnvContext("scenarioModules", Arrays.asList("promotion"));

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("营销活动-VIP抽奖结果: {}", resultContext);
        } catch (Exception e) {
            logger.error("执行异常", e);
        }
        Thread.sleep(3000);
    }

    /**
     * 一键运行所有场景（用于快速验证）
     * <p>
     * 依次运行:
     * 1. 电商订单风控 - 正常下单
     * 2. 金融贷款审批 - 审批通过
     * 3. 营销活动引擎 - 新人礼包
     */
    @Test
    public void testAllScenarios() throws Exception {
        logger.info("========== 开始运行所有业务场景 ==========");

        // 场景1: 电商订单风控 - 正常下单
        logger.info("--- 场景1: 电商订单风控 - 正常下单 ---");
        testShopOrder_NormalOrder();

        // 场景2: 金融贷款审批 - 审批通过
        logger.info("--- 场景2: 金融贷款审批 - 审批通过 ---");
        testFinanceLoan_Approved();

        // 场景3: 营销活动引擎 - 新人礼包
        logger.info("--- 场景3: 营销活动引擎 - 新人礼包 ---");
        testMarketing_NewUserGift();

        logger.info("========== 所有业务场景运行完成 ==========");
    }
}
