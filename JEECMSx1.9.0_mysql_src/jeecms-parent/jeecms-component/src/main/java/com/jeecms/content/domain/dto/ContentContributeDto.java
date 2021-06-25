package com.jeecms.content.domain.dto;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.alibaba.fastjson.annotation.JSONField;
import com.jeecms.common.annotation.EncryptField;
import com.jeecms.common.configuration.AmountPropertyDeserializer;
import com.jeecms.common.constants.WebConstants;
import org.hibernate.validator.constraints.Length;

import com.jeecms.channel.domain.Channel;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.ContentExt;
import com.jeecms.content.util.ContentInitUtils;
import com.jeecms.system.domain.CmsSiteConfig;

/**
 * 投稿内容dto
 * @author: chenming
 * @date:   2019年7月23日 下午2:25:46
 */
public class ContentContributeDto {
	/** 栏目id值 */
	private Integer channnelId;
	/** 栏目 */
	private String title;
	/** 摘要 */
	private String description;
	/** 作者 */
	private String author;
	/** 正文 */
    @EncryptField
	private String contxt;
	/** 状态为：提交*/
	private Boolean isSubmit;
	/** 内容的id值*/
	private Integer contentId;

	/**
	 * 付费阅读
	 */
	private ContentSaveDto.ContentPayRead payRead;

	/**
	 * 是否开启赞赏
	 */
	private Integer reward;


	@NotNull(groups = {UpdateContribute.class,SaveContribute.class})
	public Integer getChannnelId() {
		return channnelId;
	}

	public void setChannnelId(Integer channnelId) {
		this.channnelId = channnelId;
	}

	@NotBlank(groups = {UpdateContribute.class,SaveContribute.class})
	@Length(max = 50)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Length(max = 150)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@NotBlank(groups = {UpdateContribute.class,SaveContribute.class})
	@Length(max = 50)
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getContxt() {
		return contxt;
	}

	public void setContxt(String contxt) {
		this.contxt = contxt;
	}

	@NotNull(groups = {UpdateContribute.class,SaveContribute.class})
	public Boolean getIsSubmit() {
		return isSubmit;
	}

	public void setIsSubmit(Boolean isSubmit) {
		this.isSubmit = isSubmit;
	}

	@NotNull(groups = {UpdateContribute.class,DeleteContribute.class})
	@Null(groups = SaveContribute.class)
	public Integer getContentId() {
		return contentId;
	}

	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}

	@NotNull(groups = {SaveContribute.class,UpdateContribute.class})
	public ContentSaveDto.ContentPayRead getPayRead() {
		return payRead;
	}

	public void setPayRead(ContentSaveDto.ContentPayRead payRead) {
		this.payRead = payRead;
	}

	@NotNull(groups = {SaveContribute.class,UpdateContribute.class})
	public Integer getReward() {
		return reward;
	}

	public void setReward(Integer reward) {
		this.reward = reward;
	}

	public interface SaveContribute {
		
	}
	
	public interface UpdateContribute {
		
	}
	
	public interface DeleteContribute {
		
	}
	
	public Content initContent(Content content, ContentContributeDto dto,Channel channel,CmsSiteConfig cmsSiteConfig,Integer siteId,Boolean isWorkflow,Boolean isUpdate) {
		content.setChannelId(dto.getChannnelId());
		content.setTitle(dto.getTitle());
		content.setTitleIsBold(false);
		content.setTitleColor(ContentConstant.TITLE_DEFAULT_COLOR);
		content.setChannelId(channel.getId());
		content.setChannel(channel);
		if (dto.getPayRead() != null) {
			content.setPayRead(dto.getPayRead().getPayRead());
			if (WebConstants.INTEGER_TRUE == dto.getPayRead().getPayRead()) {
				content.setPayPrice(dto.getPayRead().getPayPrice());
				content.setTrialReading(dto.getPayRead().getTrialReading());
			}
		}
		content.setPayPraise(dto.getReward());
		Short viewControl = null;
		if (channel.getChannelExt().getViewControl() != null) {
			viewControl = ContentInitUtils.initViewControl(channel.getChannelExt().getViewControl());
		} else {
			viewControl = ContentInitUtils.initViewControl(
					Short.valueOf(cmsSiteConfig.getChannelVisitLimitType()));
		}
		content.setHasStatic(false);
		content.setViewControl(viewControl);
		Integer status = ContentConstant.STATUS_FIRST_DRAFT;
		if (dto.getIsSubmit()) {
			// 如果有工作流那么状态就是流转中，如果没有则是初稿
			if (isWorkflow) {
				status = ContentConstant.STATUS_FLOWABLE;
			} else {
				status = ContentConstant.STATUS_FIRST_DRAFT;
			}
		} else {
			status = ContentConstant.STATUS_TEMPORARY_STORAGE;
		}
		content.setStatus(status);
		content.setCreateType(ContentConstant.CONTENT_CREATE_TYPE_CONTRIBUTE);
		// 此处无需判断栏目为空，因为栏目那边会保证其值存在
		content.setCommentControl(
				Integer.valueOf(channel.getChannelExt().getCommentControl() + ""));
		content.setReleaseTime(new Date());
		// 如果小于4，则说明其内容状态是待发布之下，所以发布管理员必须置空
		content.setPublishUserId(null);
		content.setSiteId(siteId);
		if (!isUpdate) {
			content = ContentInitUtils.initTrueContentRelease(content);
			content = ContentInitUtils.initContentDefault(content);
			content = ContentInitUtils.initContentNum(content);
		}
		return content;
	}
	
	public ContentExt initContentExt(ContentExt contentExt, ContentContributeDto dto) {
		return contentExt;
	}
}
