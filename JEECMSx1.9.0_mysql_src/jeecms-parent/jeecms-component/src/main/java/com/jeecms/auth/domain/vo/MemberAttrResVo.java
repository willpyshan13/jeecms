package com.jeecms.auth.domain.vo;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.resource.domain.ResourcesSpaceData;

import java.io.Serializable;

/**
 * 信件资源vo
 * @author: tom
 * @date: 2020/2/26 15:34   
 */
public class MemberAttrResVo implements Serializable {

    private static final long serialVersionUID = -4458084388664599189L;
    private String resDesc;/**描述*/
    private String resAlias;/**资源名称*/
    private String url;/**资源地址*/
    private String sizeUnit;/**资源大小*/
    private Integer id;/**id*/
    private Integer resId;/**资源表id*/
    /**
     * 视频封面图资源对象
     */
    private ResourcesSpaceData coverImg;

    public ResourcesSpaceData getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(ResourcesSpaceData coverImg) {
        this.coverImg = coverImg;
    }

    public String getResDesc() {
        return resDesc;
    }

    public void setResDesc(String resDesc) {
        this.resDesc = resDesc;
    }

    public String getResAlias() {
        return resAlias;
    }

    public void setResAlias(String resAlias) {
        this.resAlias = resAlias;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSizeUnit() {
        return sizeUnit;
    }

    public void setSizeUnit(String sizeUnit) {
        this.sizeUnit = sizeUnit;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getResId() {
        return resId;
    }

    public void setResId(Integer resId) {
        this.resId = resId;
    }
}
