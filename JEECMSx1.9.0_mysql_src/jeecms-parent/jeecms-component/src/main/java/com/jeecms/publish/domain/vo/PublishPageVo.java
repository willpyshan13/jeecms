package com.jeecms.publish.domain.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 发布统计VO
 * @author ljw
 */
public class PublishPageVo {

    private  Integer id;
    /** 名称 */
    @Excel(name = "名称", isImportField = "true_st", width = 16, orderNum = "1")
    private  String name;
    /** 真实姓名 */
    private  String realName;
    /** 计数 */
    @Excel(name = "数量", isImportField = "true_st", width = 16, orderNum = "2")
    private  Long value;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PublishPageVo(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
}
