/**   
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.common.weibo.bean.response.statuses;

import com.jeecms.common.weibo.bean.response.BaseResponse;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;

/**
 * 微博响应
 * 
 * @author: ljw
 * @date: 2019年6月19日 下午2:47:36
 */
public class StatusesResponse extends BaseResponse {

	@XStreamAlias("previous_cursor")
	private Long previousCursor;
	@XStreamAlias("total_number")
	private Long totalNumber;
	@XStreamAlias("next_cursor")
	private Long nextCursor;
	private List<Statuses> statuses;

	public StatusesResponse() {
		super();
	}

	public Long getPreviousCursor() {
		return previousCursor;
	}

	public void setPreviousCursor(Long previousCursor) {
		this.previousCursor = previousCursor;
	}

	public Long getTotalNumber() {
		return totalNumber;
	}

	public void setTotalNumber(Long totalNumber) {
		this.totalNumber = totalNumber;
	}

	public Long getNextCursor() {
		return nextCursor;
	}

	public void setNextCursor(Long nextCursor) {
		this.nextCursor = nextCursor;
	}

	public List<Statuses> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<Statuses> statuses) {
		this.statuses = statuses;
	}

}
