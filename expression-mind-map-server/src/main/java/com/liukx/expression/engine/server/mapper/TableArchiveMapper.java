package com.liukx.expression.engine.server.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    @Delete("drop table IF EXISTS #{tableName}")
    void dropTable(@Param("tableName") String tableName);
}
