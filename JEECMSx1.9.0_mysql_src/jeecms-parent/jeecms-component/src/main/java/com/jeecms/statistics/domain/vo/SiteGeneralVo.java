package com.jeecms.statistics.domain.vo;

import java.io.Serializable;

/**
 * 网站信息VO
 * @author: ljw
 * @version: 基于x1.4.0
 * @date 2020-06-03
 */
public class SiteGeneralVo implements Serializable {

    private static final long serialVersionUID = -1598004543424905879L;

    /**内容发布数**/
    private SiteGeneralBaseVo contentPublish;
    /**微信文章发布数**/
    private SiteGeneralBaseVo wechatPublish;
    /**微博文章发布数**/
    private SiteGeneralBaseVo weiboPublish;
    /**新增评论数**/
    private SiteGeneralBaseVo addComment;
    /**新建栏目数**/
    private SiteGeneralBaseVo addChannel;

    public SiteGeneralVo() {
    }

    public SiteGeneralBaseVo getContentPublish() {
        return contentPublish;
    }

    public void setContentPublish(SiteGeneralBaseVo contentPublish) {
        this.contentPublish = contentPublish;
    }

    public SiteGeneralBaseVo getWechatPublish() {
        return wechatPublish;
    }

    public void setWechatPublish(SiteGeneralBaseVo wechatPublish) {
        this.wechatPublish = wechatPublish;
    }

    public SiteGeneralBaseVo getWeiboPublish() {
        return weiboPublish;
    }

    public void setWeiboPublish(SiteGeneralBaseVo weiboPublish) {
        this.weiboPublish = weiboPublish;
    }

    public SiteGeneralBaseVo getAddComment() {
        return addComment;
    }

    public void setAddComment(SiteGeneralBaseVo addComment) {
        this.addComment = addComment;
    }

    public SiteGeneralBaseVo getAddChannel() {
        return addChannel;
    }

    public void setAddChannel(SiteGeneralBaseVo addChannel) {
        this.addChannel = addChannel;
    }
}
