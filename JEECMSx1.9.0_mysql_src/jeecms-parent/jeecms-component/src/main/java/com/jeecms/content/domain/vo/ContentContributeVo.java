package com.jeecms.content.domain.vo;

import java.math.BigDecimal;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.jeecms.channel.domain.Channel;
import com.jeecms.common.configuration.AmountPropertyDeserializer;
import com.jeecms.content.domain.dto.ContentSaveDto;

/**
 * 查询详细投稿信息
 * 
 * @author: chenming
 * @date: 2019年7月31日 上午11:28:28
 */
public class ContentContributeVo {
	/** 栏目对象 */
	private Channel channel;
	/** 标题 */
	private String title;
	/** 摘要 */
	private String description;
	/** 作者 */
	private String author;
	/** 正文 */
	private String contxt;
	/** 内容的id值 */
	private Integer contentId;

    private Integer modelId;

    private Integer userId;

	/**
	 * 付费阅读
	 */
	private ContentPayRead payRead;

	/**
	 * 是否开启赞赏
	 */
	private Integer reward;

	/**
	 * 是否开启付费阅读：0-关闭、1-开启
	 */
	private Integer payread;

	/**
	 * 售价(默认毫)
	 */
	private Long payPrice;

	/**
	 * 试读字数
	 */
	private Integer trialReading;


    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

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

	public Integer getContentId() {
		return contentId;
	}

	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}

	public List<Integer> getChannelIds() {
		return getChannel().getChildAllIds();
	}

	public ContentPayRead getPayRead() {
		return payRead;
	}

	public void setPayRead(ContentPayRead payRead) {
		this.payRead = payRead;
	}

	public Integer getReward() {
		return reward;
	}

	public void setReward(Integer reward) {
		this.reward = reward;
	}

	public Long getPayPrice() {
		return payPrice;
	}

	public void setPayPrice(Long payPrice) {
		this.payPrice = payPrice;
	}

	public Integer getTrialReading() {
		return trialReading;
	}

	public void setTrialReading(Integer trialReading) {
		this.trialReading = trialReading;
	}

	public Integer getPayread() {
		return payread;
	}

	public void setPayread(Integer payread) {
		this.payread = payread;
	}

	public static class ContentPayRead {
		/**
		 * 是否开启付费阅读：0-关闭、1-开启
		 */
		private Integer payRead;

		/**
		 * 售价(默认毫)
		 */
		private BigDecimal payPrice;

		/**
		 * 试读字数
		 */
		private Integer trialReading;

		public Integer getPayRead() {
			return payRead;
		}

		public void setPayRead(Integer payRead) {
			this.payRead = payRead;
		}

		public BigDecimal getPayPrice() {
			return payPrice;
		}

		public void setPayPrice(BigDecimal payPrice) {
			this.payPrice = payPrice;
		}

		public Integer getTrialReading() {
			return trialReading;
		}

		public void setTrialReading(Integer trialReading) {
			this.trialReading = trialReading;
		}
	}
}
