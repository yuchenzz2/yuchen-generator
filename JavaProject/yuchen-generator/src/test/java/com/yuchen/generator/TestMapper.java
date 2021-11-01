package com.yuchen.generator;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuchen.generator.domain.GenTable;
import com.yuchen.generator.domain.GenTableColumn;
import com.yuchen.generator.mapper.GenTableColumnMapper;
import com.yuchen.generator.mapper.GenTableMapper;
import com.yuchen.generator.mapper.QueryTableInfo;
import com.yuchen.generator.service.IGenTableService;
import com.yuchen.generator.util.GenConstants;
import com.yuchen.generator.util.GenUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.table.TableColumn;
import java.util.*;

/**
 * TODO
 *
 * @author yuchen
 * @since 2021/10/29 11:20
 **/

@SpringBootTest
public class TestMapper {

    @Autowired
    private QueryTableInfo queryTableInfo;

    @Autowired
    private IGenTableService tableService;

    @Autowired
    private GenTableMapper tableMapper;

    @Autowired
    private GenTableColumnMapper tableColumnMapper;

    @Test
    public void testTable(){
        List<Map> maps = queryTableInfo.listTables();

        List<GenTable> tableList = new ArrayList<>();

        for (Map map : maps) {
            GenTable table = new GenTable();
            table.setTableComment((String) map.get("TABLE_COMMENT"));
            table.setTableName((String) map.get("TABLE_NAME"));
            table.setCreateTime((Date) map.get("CREATE_TIME"));
            String className =StrUtil.toCamelCase((String) map.get("TABLE_NAME"));
            table.setClassName(StrUtil.upperFirst(className));
            table.setFunctionName(StrUtil.upperFirst(className));
            tableMapper.insertGenTable(table);
            tableList.add(table);
        }
        System.out.println(tableList);
    }

    @Test
    public void testTableColumn(){
        //获取table信息
        for (GenTable tableInfo : tableMapper.selectList(null)) {
            //获取table columns
            List<Map> columns = queryTableInfo.listTableColumns(tableInfo.getTableName());
            GenTableColumn column = new GenTableColumn();
            column.setTableId(tableInfo.getTableId());
            for (Map tableColumn : columns) {
                String columnName = (String) tableColumn.get("COLUMN_NAME");
                String dataType = (String) tableColumn.get("COLUMN_TYPE");
                String columnComment = (String) tableColumn.get("COLUMN_COMMENT");

                column.setColumnComment(columnComment);
                column.setColumnName(columnName);
                column.setColumnType(dataType);
                column.setJavaField(StrUtil.toCamelCase(columnName));
                column.setCreateTime(new DateTime());
                // 置默认类型
                column.setJavaType(GenConstants.TYPE_STRING);
                column.setCreateBy("yuchen");
                if (arraysContains(GenConstants.COLUMNTYPE_TIME, dataType)) {
                    column.setJavaType(GenConstants.TYPE_DATE);
                    column.setHtmlType(GenConstants.HTML_DATETIME);
                }
                else if (arraysContains(GenConstants.COLUMNTYPE_NUMBER, dataType)) {
                    column.setHtmlType(GenConstants.HTML_INPUT);
                    // 如果是浮点型 统一用BigDecimal
                    String[] str = StrUtil.split(StringUtils.substringBetween(column.getColumnType(), "(", ")"), ",");
                    if (str != null && str.length == 2 && Integer.parseInt(str[1]) > 0) {
                        column.setJavaType(GenConstants.TYPE_BIGDECIMAL);
                    }
                    // 如果是整形
                    else if (str != null && str.length == 1 && Integer.parseInt(str[0]) <= 10) {
                        column.setJavaType(GenConstants.TYPE_INTEGER);
                    }
                    // 长整形
                    else {
                        column.setJavaType(GenConstants.TYPE_LONG);
                    }
                }
                //插入 gen_table_column
                tableColumnMapper.insert(column);
            }
        }
    }

    public static boolean arraysContains(String[] arr, String targetValue) {
        return Arrays.asList(arr).contains(targetValue);
    }

    @Test
    public void testPM(){
        List<GenTableColumn> genTableColumns = tableColumnMapper.selectList(null);
        System.out.println(genTableColumns);
    }

    @Test
    public void testGenerator(){
        tableService.generatorCode("sys_user");
    }

    @Test
    public void tesstUpsate(){

        for (GenTable table : tableMapper.selectList(null)) {
            List<GenTableColumn> columns = tableColumnMapper.selectList(
                    new QueryWrapper<GenTableColumn>().lambda()
                            .eq(GenTableColumn::getTableId, table.getTableId())
            );
            System.out.println(columns);
        }
    }
}
