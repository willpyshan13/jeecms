/*
@Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.front.controller.interview.vo;

import java.time.LocalDateTime;

/**
 * @author xiaohui
 * @date 2021/1/28 9:23
 */
public class ReplayVO {

    public static final Integer NOT_DOWNLOADED = 1;
    public static final Integer DOWNLOADING = 2;
    public static final Integer DOWNLOAD_SUCCESS = 3;

    /**
     * id
     */
    private Long id;

    /**
     * 视频地址（客户端地址）
     */
    private String videoUrl;

    /**
     * 时长
     */
    private String duration;

    /**
     * 视频大小
     */
    private String videoSize;

    /**
     * 分辨率
     */
    private String resolution;

    /**
     * 是否用户回放1是 0否
     */
    private Integer enable;

    /**
     * 状态 客户端是否下载（1未下载 2下载中 3下载完成）
     */
    private Integer status;

    /**
     * 下载完成时间
     */
    private LocalDateTime downloadTime;

    /**
     * 视频原地址
     */
    private String origUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getVideoSize() {
        return videoSize;
    }

    public void setVideoSize(String videoSize) {
        this.videoSize = videoSize;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Integer getEnable() {
        return enable;
    }

    public void setEnable(Integer enable) {
        this.enable = enable;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(LocalDateTime downloadTime) {
        this.downloadTime = downloadTime;
    }

    public String getOrigUrl() {
        return origUrl;
    }

    public void setOrigUrl(String origUrl) {
        this.origUrl = origUrl;
    }

    @Override
    public String toString() {
        return "ReplayVO{" +
                "id=" + id +
                ", videoUrl='" + videoUrl + '\'' +
                ", duration='" + duration + '\'' +
                ", videoSize='" + videoSize + '\'' +
                ", resolution='" + resolution + '\'' +
                ", enable=" + enable +
                ", status=" + status +
                ", downloadTime=" + downloadTime +
                ", origUrl='" + origUrl + '\'' +
                '}';
    }
}
