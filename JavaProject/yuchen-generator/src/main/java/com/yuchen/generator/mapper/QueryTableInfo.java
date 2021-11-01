package com.yuchen.generator.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author yuchen
 * @since 2021/10/29 11:04
 **/

@Mapper
public interface QueryTableInfo {
    //@Select("select table_name ,create_time , engine, table_collation, table_comment from information_schema.TABLES " +
    //        "where TABLE_SCHEMA=(select database())")
    @Select("select * from information_schema.TABLES " +
            "where TABLE_SCHEMA=(select database())")
    List<Map> listTables();

    @Select("select * from information_schema.COLUMNS " +
            "where table_name = #{tableName} and table_schema = 'yuchen-cloud'")
    List<Map> listTableColumns(String tableName);
}
