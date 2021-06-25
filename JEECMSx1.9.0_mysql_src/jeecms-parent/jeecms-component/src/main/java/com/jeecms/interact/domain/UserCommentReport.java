/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.interact.domain;

import com.jeecms.auth.domain.CoreUser;
import com.jeecms.common.base.domain.AbstractDomain;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 评论举报实体类
 * 
 * @author: chenming
 * @date: 2019年6月15日 下午3:20:04
 */
@Entity
@Table(name = "jc_user_comment_report")
@Where(clause = "deleted_flag = false")
public class UserCommentReport extends AbstractDomain<Integer> implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	/** 评论id */
	private Integer commentId;
	/** 举报用户id */
	private Integer replyUserId;
	/** 举报ip */
	private String ip;

	/** 评论对象 */
	private UserComment userComment;
	/** 举报用户*/
	private CoreUser user;
	
	public UserCommentReport(Integer id, Integer commentId, Integer replyUserId, String ip, UserComment userComment,
			CoreUser user) {
		super();
		this.id = id;
		this.commentId = commentId;
		this.replyUserId = replyUserId;
		this.ip = ip;
		this.userComment = userComment;
		this.user = user;
	}

	public UserCommentReport() {

	}

	@Id
	@Column(name = "id", nullable = false, length = 11)
	@TableGenerator(name = "jc_user_comment_report", pkColumnValue = "jc_user_comment_report", 
					initialValue = 0, allocationSize = 10)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_user_comment_report")
	@Override
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@NotNull
	@Column(name = "comment_id", nullable = false, length = 11)
	public Integer getCommentId() {
		return commentId;
	}

	public void setCommentId(Integer commentId) {
		this.commentId = commentId;
	}

	@Column(name = "reply_user_id", nullable = false, length = 11)
	public Integer getReplyUserId() {
		return replyUserId;
	}

	public void setReplyUserId(Integer replyUserId) {
		this.replyUserId = replyUserId;
	}

	@Column(name = "ip", nullable = true, length = 50)
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id", insertable = false, updatable = false)
	public UserComment getUserComment() {
		return userComment;
	}

	public void setUserComment(UserComment userComment) {
		this.userComment = userComment;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reply_user_id", insertable = false, updatable = false)
	public CoreUser getUser() {
		return user;
	}

	public void setUser(CoreUser user) {
		this.user = user;
	}

}