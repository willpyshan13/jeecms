/**
* @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.weibo.service;

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.weibo.domain.WeiboArticlePush;
import com.jeecms.weibo.domain.dto.PreviewDto;
import com.jeecms.weibo.domain.dto.PushDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * 微博推送Service
* @author ljw
* @version 1.0
* @date 2019-06-18
*/
public interface WeiboArticlePushService extends IBaseService<WeiboArticlePush, Integer> {

	/**
	 * 推送列表分页
	* @Title: page 
	* @param uid 微博UID
	* @param title 标题
	* @param pageable 分页对象
	* @param status 发送状态
	* @return Page
	* @throws GlobalException 异常
	 */
	Page<WeiboArticlePush> page(Long uid,String title,Integer status,Pageable pageable) throws GlobalException;
	
	/**
	 * 推送文章到新浪微博
	* @Title: push 
	* @param dto 推送Dto
	* @return ResponseInfo 返回
	* @throws GlobalException 异常
	 */
	ResponseInfo push(PushDto dto) throws GlobalException;
	
	/**
	 * 推送预览
	* @Title: push 
	* @param dto 推送预览Dto
	* @return ResponseInfo 返回
	* @throws GlobalException 异常
	 */
	ResponseInfo preview(PreviewDto dto) throws GlobalException;
	
	/**
	 * 修改后推送
	* @Title: push 
	* @param dto 推送Dto
	* @return ResponseInfo 返回
	* @throws GlobalException 异常
	 */
	ResponseInfo updatePush(PreviewDto dto) throws GlobalException;

	/**
	 * 查询推送列表
	 * @param start 开始发布时间
	 * @param end 结束发布时间
	 * @param users 用户集合
	 * @param uuids 微博UUId集合
	 * @return List
	 */
	List<WeiboArticlePush> getList(Date start, Date end, List<Integer> users, List<String> uuids);
}
