/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.system.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 同步子站消息模板扩展dto
 * @author: chenming
 * @date: 2020/6/11 11:39
 */
public class SynchChildSiteTplDto {
    /**
     * 子站id集合
     */
    private List<ChildSite> childSiteList;
    /**
     * 当前站点id
     */
    private Integer siteId;
    /**
     * 绑定的手机号
     */
    private String mobile;

    public SynchChildSiteTplDto() {
    }

    public SynchChildSiteTplDto(Integer childSiteId,String siteName, Integer siteId) {
        ChildSite childSite = new ChildSite(childSiteId,siteName);
        this.childSiteList = Collections.singletonList(childSite);
        this.siteId = siteId;
    }

    @NotNull
    @Size(min = 1)
    public List<ChildSite> getChildSiteList() {
        return childSiteList;
    }

    public void setChildSiteList(List<ChildSite> childSiteList) {
        this.childSiteList = childSiteList;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    /**
     * 子站点对象
     */
    public static class ChildSite {
        /**
         * 站点id
         */
        private Integer siteId;
        /**
         * 站点名称
         */
        private String siteName;

        public ChildSite() {
        }

        public ChildSite(Integer siteId, String siteName) {
            this.siteId = siteId;
            this.siteName = siteName;
        }

        @NotNull
        public Integer getSiteId() {
            return siteId;
        }

        public void setSiteId(Integer siteId) {
            this.siteId = siteId;
        }

        @NotBlank
        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }
    }

    public List<Integer> getChildIds() {
        List<ChildSite> childSites = getChildSiteList();
        return childSites.stream().map(ChildSite::getSiteId).collect(Collectors.toList());
    }
}
