/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */  

package com.jeecms.interact.service;

import java.util.List;

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.interact.domain.UserCommentReport;

/**
 * 评论举报service接口
 * @author: chenming
 * @date:   2019年6月15日 下午3:21:05
 */
public interface UserCommentReportService extends IBaseService<UserCommentReport, Integer> {
	
    /**
     * 通过评论id查询该评论举报的list集合
     * @param commentIds 评论id集合
     * @return List<UserCommentReport>
     */
	List<UserCommentReport> findByCommentId(List<Integer> commentIds);

    /**
     * 通过举报的用户id查询举报的list集合
     * @param replyUserId   举报的用户id
     * @return  List<UserCommentReport>
     */
	List<UserCommentReport> findByReplyUserId(Integer replyUserId);

    /**
     * 通过
     * @return
     */
	List<Integer> findByDeleted();
	
}
