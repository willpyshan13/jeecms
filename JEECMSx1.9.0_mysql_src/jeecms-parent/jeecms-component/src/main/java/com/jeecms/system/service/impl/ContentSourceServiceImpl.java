/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.service.impl;

import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.system.dao.ContentSourceDao;
import com.jeecms.system.domain.ContentSource;
import com.jeecms.system.service.ContentSourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 来源管理service实现
 *
 * @version 1.0
 * @author: wulongwei
 * @date: 2019年5月6日 上午11:45:04
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ContentSourceServiceImpl extends BaseServiceImpl<ContentSource, ContentSourceDao, Integer>
		implements ContentSourceService {

	@Override
	public ResponseInfo saveSysSourceInfo(ContentSource sysSource) throws GlobalException {
		this.checkContentSourceInfo(sysSource);
		super.save(sysSource);
		return new ResponseInfo();
	}

	@Override
	public ResponseInfo updateSysSourceInfo(ContentSource sysSource) throws GlobalException {
		this.checkContentSourceInfo(sysSource);
		super.update(sysSource);
		return new ResponseInfo();
	}

	@Override
	public ContentSource findBySourceName(String sourceName) throws GlobalException {
        List<ContentSource> sources = dao.findBySourceNameAndHasDeleted(sourceName, false);
	    if (CollectionUtils.isEmpty(sources)) {
	        return null;
        }
		return sources.get(0);
	}

	/**
	 * 校验来源信息：<br>
	 * 来源名称不能重复<br>
	 * 默认来源只能存在一条
	 *
	 * @Title: checkSysSourceInfo
	 * @return: void
	 */
	public void checkContentSourceInfo(ContentSource sysSource) throws GlobalException {
	    List<ContentSource> sources = dao.findBySourceNameAndHasDeleted(sysSource.getSourceName(), false);
	    if (!CollectionUtils.isEmpty(sources) && sources.size() > 1) {
            throw new GlobalException(new SystemExceptionInfo(
                            SettingErrorCodeEnum.SOURCE_NAME_ALREADY_EXIST.getDefaultMessage(),
                            SettingErrorCodeEnum.SOURCE_NAME_ALREADY_EXIST.getCode()));
        }
		ContentSource sysSourceInfo = CollectionUtils.isEmpty(sources) ? null : sources.get(0);
		if (sysSourceInfo != null
				&&
				(sysSource.getId() != null ? !sysSource.getId().equals(sysSourceInfo.getId()) : true)) {
			throw new GlobalException(
					new SystemExceptionInfo(
							SettingErrorCodeEnum.SOURCE_NAME_ALREADY_EXIST.getDefaultMessage(),
							SettingErrorCodeEnum.SOURCE_NAME_ALREADY_EXIST.getCode()));
		}
		sysSourceInfo = dao.findByIsDefaultAndHasDeleted(true, false);
		if (sysSource.getIsDefault() && sysSourceInfo != null) {
			sysSourceInfo.setIsDefault(false);
			super.update(sysSourceInfo);
		}
	}

	@Override
	public ContentSource defaultSource() throws GlobalException {
		return dao.findByIsDefaultAndHasDeleted(true, false);
	}

}