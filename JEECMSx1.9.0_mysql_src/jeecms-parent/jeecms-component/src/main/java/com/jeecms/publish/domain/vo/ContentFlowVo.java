package com.jeecms.publish.domain.vo;

/**
 * 发布统计VO
 *
 * @author ljw
 */
public class ContentFlowVo {

    /**
     * 页面类型 1.首页 2.栏目页 3.内容页 4.其他页
     **/
    private Integer pageType;
    /**
     * 栏目ID
     **/
    private Integer channelId;
    /**
     * 内容ID
     **/
    private Integer contentId;

    public ContentFlowVo() {
    }

    public Integer getPageType() {
        return pageType;
    }

    public void setPageType(Integer pageType) {
        this.pageType = pageType;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }
}
