package com.yuchen.generator.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuchen.generator.domain.GenTable;
import com.yuchen.generator.domain.GenTableColumn;
import com.yuchen.generator.util.AjaxResult;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.yuchen.generator.service.IGenTableColumnService;
import com.yuchen.generator.service.IGenTableService;

/**
 * 代码生成 操作处理
 */
@RequestMapping("/gen")
@RestController
@CrossOrigin
public class GenController {

    @Autowired
    private IGenTableService genTableService;

    @Autowired
    private IGenTableColumnService genTableColumnService;

    /**
     * 查询代码生成列表
     */
    //@RequiresPermissions("tool:gen:list")
    @GetMapping("/list")
    //public TableDataInfo genList(GenTable genTable)
    public AjaxResult genList(GenTable genTable) {
        //startPage();
        List<GenTable> list = genTableService.selectGenTableList(genTable);
        //return getDataTable(list);
        //return
        return AjaxResult.success(list);
    }

    /**
     * 修改代码生成业务
     */
    //@RequiresPermissions("tool:gen:query")
    @GetMapping(value = "/{tableId}")
    public AjaxResult getInfo(@PathVariable Long tableId) {
        GenTable table = genTableService.selectGenTableById(tableId);
        List<GenTable> tables = genTableService.selectGenTableAll();
        List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(tableId);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("info", table);
        map.put("rows", list);
        map.put("tables", tables);
        return AjaxResult.success(map);
    }

    /**
     * 查询数据库列表
     */
    //@RequiresPermissions("tool:gen:list")
    @GetMapping("/db/list")
    //public TableDataInfo dataList(GenTable genTable)
    public AjaxResult dataList(GenTable genTable) {
        //startPage();
        List<GenTable> list = genTableService.selectDbTableList(genTable);
        //return getDataTable(list);
        return AjaxResult.success(list);
    }

    /**
     * 查询数据表字段列表
     */
    @GetMapping(value = "/column/{tableName}")
    //public TableDataInfo columnList(Long tableId)
    public AjaxResult columnList(@PathVariable String tableName) {
        //TableDataInfo dataInfo = new TableDataInfo();
        GenTable table = genTableService.getBaseMapper().selectOne(
                new QueryWrapper<GenTable>().lambda()
                        .eq(GenTable::getTableName, tableName)
        );
        List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(table.getTableId());
        //dataInfo.setRows(list);
        //dataInfo.setTotal(list.size());
        return AjaxResult.success(list);
    }

    /**
     * 导入表结构（保存）
     */
    //@RequiresPermissions("tool:gen:import")
    //@Log(title = "代码生成", businessType = BusinessType.IMPORT)
    @PostMapping("/importTable")
    public AjaxResult importTableSave(String tables) {
        String[] tableNames = Convert.toStrArray(tables);
        // 查询表信息
        List<GenTable> tableList = genTableService.selectDbTableListByNames(tableNames);
        genTableService.importGenTable(tableList);
        return AjaxResult.success();
    }

    /**
     * 修改保存代码生成业务
     */
    //@RequiresPermissions("tool:gen:edit")
    //@Log(title = "代码生成", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult editSave(@Validated @RequestBody GenTable genTable) {
        genTableService.validateEdit(genTable);
        genTableService.updateGenTable(genTable);
        return AjaxResult.success();
    }

    /**
     * 删除代码生成
     */
    //@RequiresPermissions("tool:gen:remove")
    //@Log(title = "代码生成", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tableIds}")
    public AjaxResult remove(@PathVariable Long[] tableIds) {
        genTableService.deleteGenTableByIds(tableIds);
        return AjaxResult.success();
    }

    /**
     * 预览代码
     */
    //@RequiresPermissions("tool:gen:preview")
    @GetMapping("/preview/{tableName}")
    public AjaxResult preview(@PathVariable("tableName") String tableName) throws IOException {
        List<GenTable> genTables = genTableService.getBaseMapper().selectList(
                new QueryWrapper<GenTable>().lambda()
                        .eq(GenTable::getTableName, tableName)
        );
        Long tableId = genTables.get(0).getTableId();
        List<Map<String, Object>> genMap = genTableService.previewCode(tableId);
        return AjaxResult.success(genMap);
    }

    /**
     * 生成代码（下载方式）
     */
    //@RequiresPermissions("tool:gen:code")
    //@Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/download/{tableName}")
    public void download(HttpServletResponse response, @PathVariable("tableName") String tableName) throws IOException {
        byte[] data = genTableService.downloadCode(tableName);
        genCode(response, data);
    }

    /**
     * 生成代码（自定义路径）
     */
    //@RequiresPermissions("tool:gen:code")
    //@Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/genCode/{tableName}")
    public AjaxResult genCode(@PathVariable("tableName") String tableName) {
        genTableService.generatorCode(tableName);
        return AjaxResult.success();
    }

    /**
     * 同步数据库
     */
    //@RequiresPermissions("tool:gen:edit")
    //@Log(title = "代码生成", businessType = BusinessType.UPDATE)
    @GetMapping("/synchDb/{tableName}")
    public AjaxResult synchDb(@PathVariable("tableName") String tableName) {
        genTableService.synchDb(tableName);
        return AjaxResult.success();
    }

    /**
     * 批量生成代码
     */
    //@RequiresPermissions("tool:gen:code")
    //@Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/batchGenCode")
    public void batchGenCode(HttpServletResponse response, String tables) throws IOException {
        String[] tableNames = Convert.toStrArray(tables);
        byte[] data = genTableService.downloadCode(tableNames);
        genCode(response, data);
    }

    /**
     * 生成zip文件
     */
    private void genCode(HttpServletResponse response, byte[] data) throws IOException {
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"ruoyi.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IOUtils.write(data, response.getOutputStream());
    }
}
