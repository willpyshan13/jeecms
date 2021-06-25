/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.front.controller;

import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.common.base.controller.BaseController;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.content.service.ContentFrontService;
import com.jeecms.member.domain.UserCollection;
import com.jeecms.member.service.UserCollectionService;
import com.jeecms.util.SystemContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 我的收藏
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019-04-24
 */
@RequestMapping("/usercollections")
@RestController
public class UserCollectionController extends BaseController<UserCollection, Integer> {

	@Autowired
	ContentFrontService contentFrontService;
	@Autowired
	private CoreUserService userService;
	@Autowired
    private UserCollectionService service;
	private final transient ReentrantLock lock = new ReentrantLock();

	@PostConstruct
	public void init() {
		String[] queryParams = new String[]{};
		super.setQueryParams(queryParams);
	}

	/**
	 * 添加
	 *
	 * @param userCollection 收藏对象
	 * @param request        HttpServletRequest
	 * @param result         BindingResult
	 * @return ResponseInfo
	 * @throws GlobalException 异常
	 */
	@PostMapping
	public ResponseInfo save(@RequestBody @Valid UserCollection userCollection, HttpServletRequest request,
							 BindingResult result) throws GlobalException {
		validateId(userCollection.getContentId());
		CoreUser user = SystemContextUtils.getUser(request);
		if (user != null) {
			lock.lock();
			try {
				service.save(userCollection, user);
				return new ResponseInfo(true);
			} finally {
				lock.unlock();
			}
		} else {
			return new ResponseInfo(SystemExceptionEnum.ACCOUNT_NOT_LOGIN.getCode(),
				SystemExceptionEnum.ACCOUNT_NOT_LOGIN.getDefaultMessage());
		}

	}

}
