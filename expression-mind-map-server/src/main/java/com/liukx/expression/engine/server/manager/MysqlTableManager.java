package com.liukx.expression.engine.server.manager;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.enums.TableSplitRule;
import com.liukx.expression.engine.server.mapper.TableArchiveMapper;
import com.liukx.expression.engine.server.mapper.entity.BaseTableEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表管理
 *
 * @author liukaixiong
 * @date 2025/11/26 - 11:46
 */
@Component
public class MysqlTableManager implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TableArchiveMapper tableArchiveMapper;

    @Autowired
    private ExpressionServerProperties expressionServerProperties;

    private Map<String, TableSplitRule> tableRuleMap;

    private final Set<String> currentTableNameCache = new HashSet<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        this.tableRuleMap = expressionServerProperties.getTableRuleList().stream().collect(Collectors.toMap(ExpressionServerProperties.TableRule::getTableName, ExpressionServerProperties.TableRule::getTableSplitRule));
    }

    public Set<ExpressionServerProperties.TableRule> getTableNameList(TableSplitRule tableSplitRule) {
        return expressionServerProperties.getTableRuleList().stream().filter(var -> Objects.equals(var.getTableSplitRule(), tableSplitRule)).collect(Collectors.toSet());
    }

    public ExpressionServerProperties.TableRule getTableConfigInfo(String tableName) {
        return expressionServerProperties.getTableRuleList().stream().filter(var -> Objects.equals(var.getTableName(), tableName)).findFirst().orElse(null);
    }

    /**
     * 原子性表轮转操作
     *
     * @param tableName 源表名
     * @return 归档表名
     */
    @Transactional
    public String tableArchiveByMonth(String tableName) {
        String newTable = getTableName(tableName, -1);

        if (checkTableExist(newTable)) {
            logger.warn("数据库表名已存在:{} , 不做归档处理!", newTable);
            return null;
        }

        String tmpTableName = newTable + "_tmp";
        // 创建表: CREATE TABLE IF NOT EXISTS tmpTableName LIKE tableName;
        tableArchiveMapper.createTableLike(tmpTableName, tableName);
        // 截断表: RENAME TABLE tableName TO newTable;
        tableArchiveMapper.renameTable(tableName, newTable);
        // 新表替代原表: RENAME TABLE tmpTableName TO tableName;
        tableArchiveMapper.renameTable(tmpTableName, tableName);
        return newTable;
    }

    /**
     * 清除指定表名中已过期的数据的方法
     *
     * @param tableName 表名
     */
    public void clearExpiredTableName(String tableName) {
        final TableSplitRule tableSplitRule = tableRuleMap.get(tableName);
        if (tableSplitRule != null) {

            final ExpressionServerProperties.TableRule tableConfigInfo = getTableConfigInfo(tableName);
            final Integer maxSaveNumber = tableConfigInfo.getMaxSaveNumber();
            if (maxSaveNumber != null) {
                final String fullTableName = tableSplitRule.getFullTableName(tableName, -maxSaveNumber - 1);

                if (checkTableExist(fullTableName)) {
                    tableArchiveMapper.dropTable(fullTableName);
                    logger.info("已删除过期表:{}", fullTableName);
                } else {
                    logger.info("过期的表不存在:{} , 不做处理!", fullTableName);
                }
            }
        }
    }

    public String getLastTableNameList(Class<? extends BaseTableEntity> entityClass, Integer offsetNumber) {
        final String entityTableName = TableInfoHelper.getTableInfo(entityClass).getTableName();
        final String tableName = getTableName(entityTableName, offsetNumber);

        if (!checkTableExist(tableName)) {
            logger.debug("数据库表名不存在:{}", tableName);
            return null;
        }

        return tableName;
    }

    public boolean checkTableExist(String tableName) {
        if (currentTableNameCache.contains(tableName)) {
            return true;
        }

        final List<String> tableList = tableArchiveMapper.showTables(tableName);

        if (!tableList.isEmpty()) {
            currentTableNameCache.add(tableName);
            return true;
        }

        return false;
    }

    private String getTableName(String entityTableName, Integer offsetNumber) {
        final TableSplitRule tableSplitRule = tableRuleMap.get(entityTableName);
        return tableSplitRule.getFullTableName(entityTableName, offsetNumber);
    }

}
