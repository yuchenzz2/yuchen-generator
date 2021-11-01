package com.yuchen.generator.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.convert.Convert;
import com.yuchen.generator.domain.GenTable;
import com.yuchen.generator.domain.GenTableColumn;
import com.yuchen.generator.util.AjaxResult;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.yuchen.generator.service.IGenTableColumnService;
import com.yuchen.generator.service.IGenTableService;

/**
 * 代码生成 操作处理
 */
@RequestMapping("/gen")
@RestController
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
    @GetMapping(value = "/{talbleId}")
    public AjaxResult getInfo(@PathVariable Long talbleId) {
        GenTable table = genTableService.selectGenTableById(talbleId);
        List<GenTable> tables = genTableService.selectGenTableAll();
        List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(talbleId);
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
    @GetMapping(value = "/column/{talbleId}")
    //public TableDataInfo columnList(Long tableId)
    public AjaxResult columnList(Long tableId) {
        //TableDataInfo dataInfo = new TableDataInfo();
        List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(tableId);
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
    @GetMapping("/preview/{tableId}")
    public AjaxResult preview(@PathVariable("tableId") Long tableId) throws IOException {
        Map<String, String> dataMap = genTableService.previewCode(tableId);
        return AjaxResult.success(dataMap);
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
