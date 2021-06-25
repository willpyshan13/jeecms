/*
@Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.admin.controller.interview.dto;

import com.jeecms.util.SystemContextUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author xiaohui
 * @date 2021/1/14 9:16
 */
public class InterviewUploadDTO {
    /**
     * 视频封面图
     */
    private String videoPic;

    /**
     * 在线访谈id
     */
    @NotNull
    private Long onlineId;

    @NotNull
    private String productAppId;

    /**
     * 视频地址（客户端地址）
     */
    @NotBlank
    private String videoUrl;

    /**
     * 时长
     */
    private Integer duration;

    /**
     * 视频大小
     */
    private Long videoSize;

    /**
     * 分辨率
     */
    private String resolution;

    private String createUser;

    public InterviewUploadDTO() {
    }

    public InterviewUploadDTO(String videoPic, @NotNull Long onlineId, @NotBlank String productAppId,
                              @NotBlank String videoUrl, Integer duration, Long videoSize, String resolution) {
        this.videoPic = videoPic;
        this.onlineId = onlineId;
        this.productAppId = productAppId;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.videoSize = videoSize;
        this.resolution = resolution;
        this.createUser = SystemContextUtils.getCurrentUsername();
    }

    public String getVideoPic() {
        return videoPic;
    }

    public void setVideoPic(String videoPic) {
        this.videoPic = videoPic;
    }

    public Long getOnlineId() {
        return onlineId;
    }

    public void setOnlineId(Long onlineId) {
        this.onlineId = onlineId;
    }

    public String getProductAppId() {
        return productAppId;
    }

    public void setProductAppId(String productAppId) {
        this.productAppId = productAppId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Long getVideoSize() {
        return videoSize;
    }

    public void setVideoSize(Long videoSize) {
        this.videoSize = videoSize;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }
}
