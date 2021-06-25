package com.jeecms.publish.domain.vo;

import java.util.List;

/**
 * 发布统计VO
 * @author ljw
 */
public class FlowContributionVo {

    /** 总浏览量 **/
    private Integer sum;
    /** 栏目浏览量 **/
    private Integer channel;
    /** 内容浏览量  **/
    private Integer content;
    /** 首页浏览量  **/
    private Integer index;
    /** 其他浏览量  **/
    private Integer other;

    /** 栏目集合 **/
    private List<PublishVo> channelList;
    /** 内容集合 **/
    private List<PublishVo> contentList;

    public FlowContributionVo(){}

    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public Integer getContent() {
        return content;
    }

    public void setContent(Integer content) {
        this.content = content;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getOther() {
        return other;
    }

    public void setOther(Integer other) {
        this.other = other;
    }

    public List<PublishVo> getChannelList() {
        return channelList;
    }

    public void setChannelList(List<PublishVo> channelList) {
        this.channelList = channelList;
    }

    public List<PublishVo> getContentList() {
        return contentList;
    }

    public void setContentList(List<PublishVo> contentList) {
        this.contentList = contentList;
    }
}
