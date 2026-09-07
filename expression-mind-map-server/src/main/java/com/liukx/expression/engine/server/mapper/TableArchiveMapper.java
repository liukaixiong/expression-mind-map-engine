package com.liukx.expression.engine.server.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @author liukaixiong
 * @date 2025/11/26 - 10:47
 */
@Mapper
public interface TableArchiveMapper {
    @Select("CREATE TABLE IF NOT EXISTS ${newTable} LIKE ${sourceTable}")
    void createTableLike(@Param("newTable") String newTable, @Param("sourceTable") String sourceTable);

    @Select("RENAME TABLE ${oldTable} TO ${newTable}")
    void renameTable(@Param("oldTable") String oldTable, @Param("newTable") String newTable);


    @Select("SHOW TABLES LIKE #{tableName}")
    List<String> showTables(@Param("tableName") String tableName);

    @Delete("drop table IF EXISTS ${tableName}")
    void dropTable(@Param("tableName") String tableName);

    /**
     * 查询基准表中滞留的历史周期后缀（yyMMdd / yyMM），用于启动自愈迁移。
     * 只取当前周期之前的数据，与在线写入互不冲突。
     */
    @Select("SELECT DISTINCT DATE_FORMAT(created, '${dateFormat}') FROM ${tableName} WHERE created < #{before}")
    List<String> selectDistinctPeriodSuffixes(@Param("tableName") String tableName,
                                              @Param("dateFormat") String dateFormat,
                                              @Param("before") Date before);

    /**
     * 周期区间数据迁移（保留主键，INSERT IGNORE 保证重跑幂等）。
     */
    @Insert("INSERT IGNORE INTO ${targetTable} SELECT * FROM ${sourceTable} WHERE created >= #{start} AND created < #{end}")
    int insertIgnoreRange(@Param("targetTable") String targetTable,
                          @Param("sourceTable") String sourceTable,
                          @Param("start") Date start,
                          @Param("end") Date end);

    /**
     * 迁移完成后从基准表删除对应区间的数据。
     */
    @Delete("DELETE FROM ${sourceTable} WHERE created >= #{start} AND created < #{end}")
    int deleteRange(@Param("sourceTable") String sourceTable,
                    @Param("start") Date start,
                    @Param("end") Date end);
}
