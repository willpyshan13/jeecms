package com.jeecms.publish.domain.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;

public class ExcelUserVo {

    /** 名称 */
    @Excel(name = "用户名", isImportField = "true_st", width = 16, orderNum = "1")
    private  String name;
    /** 真实姓名 */
    @Excel(name = "真实姓名", isImportField = "true_st", width = 16, orderNum = "2")
    private  String realName;
    /** 计数 */
    @Excel(name = "内容发布数", isImportField = "true_st", width = 16, orderNum = "3")
    private  Long value;

    public ExcelUserVo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
