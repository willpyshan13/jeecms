package com.jeecms.interact.dao.ext;

import com.jeecms.common.exception.GlobalException;
import com.jeecms.interact.domain.UserComment;
import com.jeecms.publish.domain.vo.ContentLikeVo;
import com.jeecms.system.domain.vo.MassScoreVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * 用户评论dao扩展接口
 * @author: chenming
 * @date:   2019年5月6日 下午4:13:09
 */
public interface UserCommentDaoExt {

	/**
	 * 通过查询检索出list集合
	 * @param status	审核状态
	 * @param isTop		是否推荐
	 * @param isReply	回复状态
	 * @param channelId	栏目Id
	 * @param startTime	起始评论时间
	 * @param endTime	截止评论时间
	 * @param replyStartTime	起始回复时间
	 * @param replyEndTime		截止回复时间
	 * @param fuzzySearch	模糊查询字段
	 * @param userName		评论人名称
	 * @param ip	评论Ip
	 * @param commentText	评论内容
	 * @param replytText	回复内容
	 * @param title		文章标题
	 * @throws GlobalException    全局异常
	 * @return: List
	 */
	Page<UserComment> findByList(Integer siteId,Short status, Boolean isTop, 
			Boolean isReply, Integer channelId, Date startTime,
			Date endTime, Date replyStartTime, Date replyEndTime, String fuzzySearch, String userName,
			String ip, String commentText, String replytText, String title, Pageable pageable)
					throws GlobalException;

	/**
	  * 通过条件精准查询检索出list集合
	 * @param status	审核状态
	 * @param isTop		是否推荐
	 * @param isReply	回复状态
	 * @param channelId	栏目Id
	 * @param startTime	起始评论时间
	 * @param endTime	截止评论时间
	 * @param replyStartTime	起始回复时间
	 * @param replyEndTime		截止回复时间
	 * @param fuzzySearch	模糊查询字段
	 * @param userName		评论人名称
	 * @param ip	评论Ip
	 * @param commentText	评论内容
	 * @param replytText	回复内容
	 * @param title		文章标题
	 * @param contentId	文章内容Id
	 * @param userId	用户id
	 * @param precisionIp	精准匹配ip
	 * @param pageable	分页信息
	 * @throws GlobalException    全局异常
	 * @return: Page
	 */
	Page<UserComment> findTermByList(Integer siteId, Short status, Boolean isTop, 
			Boolean isReply,Integer channelId,Date startTime,
			Date endTime, Date replyStartTime, Date replyEndTime, String fuzzySearch, String userName,
			String ip, String commentText, String replytText, String title, Integer contentId,
			Integer userId, String precisionIp, Pageable pageable)
					throws GlobalException;

	/**
	 * 举报列表分页
	 * @param siteId	站点id
	 * @param pageable	分页信息
	 * @throws GlobalException    全局异常
	 * @return: Page
	 */
	Page<UserComment> findReportByList(Integer siteId,Pageable pageable) throws GlobalException;

	/**
	 * 获取评论数量
	 *
	 * @param beginTime 开始时间
	 * @param endTime   结束时间
	 * @param siteId    站点id
	 * @param status    状态（0-待审核 1-审核通过 2-审核未通过）
	 * @return 评论数量
	 */
	long getCount(Date beginTime, Date endTime, Integer siteId, Short status);
	
	/**
	 * 通过内容查询评论数量
	 * @param contentId	内容ID
	 * @param isAll		是否查询内容下所有评论
	 * @param isTop		是否推荐(是否最热)
	 * @return: long
	 */
	long getCount(Integer contentId, boolean isAll, boolean isTop);
	
	/**
	 * PC端根据内容查询分页数据
	 * @param contentId		内容id
	 * @return: Page
	 */
	Page<UserComment> getPcPage(Integer siteId, Integer contentId, Short sortStatus, boolean mobile, 
			Pageable pageable) throws GlobalException;
	
	/**
	 * 手机端查询最热评论(不分页)
	 * @param siteId	站点ID
	 * @param contentId	内容ID
	 * @throws GlobalException  全局异常    
	 * @return: List
	 */
	List<UserComment> getList(Integer siteId,Integer contentId) throws GlobalException;
	
	/**
	 * 我的互动数据
	* @param startTime 评论开始时间
	* @param endTime 评论结束时间
	* @param userId 用户ID
	* @param replys 回复IDs
	* @throws GlobalException 异常
	 */
	List<UserComment> getInteractions(Date startTime, Date endTime, Integer userId, List<Integer> replys) 
			throws GlobalException;

	/**
	 * 计数统计
	 * @param type true栏目，false内容
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return List
	 */
	List<ContentLikeVo> count(boolean type, Date start, Date end);

	/**
	 * 流量计算
	 * @param contentId 内容ID
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return SysAccessRecord
	 */
	List<MassScoreVo> massCount(List<Integer> contentId, Date startDate, Date endDate);

	/**
	 * 得到内容相关的评论
	 * @param contentId 内容ID
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @param province 省份
	 * @return SysAccessRecord
	 */
	List<UserComment> list(List<Integer> contentId, Date startDate, Date endDate, String province);

    /**
     * 根据站点和内容id进行查询，并通过type查询数量
     * @param siteId    站点id
     * @param contentId 内容id
     * @param type 1-查询所有、2-审核中、3-审核成功、4-审核失败、5-举报数量
     * @return long
     */
	Integer getCount(Integer siteId,Integer contentId,Integer type);

    /**
     * 根据站点id和评论id集合查询数量
     * @param siteId    站点id
     * @param ids       评论id集合
     * @return long
     */
	long getCount(Integer siteId,List<Integer> ids);
}
