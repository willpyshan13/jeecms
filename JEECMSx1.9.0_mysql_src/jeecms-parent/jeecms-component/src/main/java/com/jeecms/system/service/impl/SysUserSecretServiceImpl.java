/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.service.impl;

import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.SysOtherErrorCodeEnum;
import com.jeecms.system.dao.SysUserSecretDao;
import com.jeecms.system.domain.SysSecret;
import com.jeecms.system.domain.SysUserSecret;
import com.jeecms.system.domain.dto.UserSecretDto;
import com.jeecms.system.service.SysUserSecretService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 人员密级Service实现类
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019-04-25
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserSecretServiceImpl extends BaseServiceImpl<SysUserSecret, SysUserSecretDao, Integer> implements SysUserSecretService {

	@Override
	public void save(UserSecretDto userSecretDto, List<SysSecret> sysSecrets) throws GlobalException {
		String name = userSecretDto.getName();
		if (!checkByName(name, null)) {
			throw new GlobalException(new SystemExceptionInfo(
					SysOtherErrorCodeEnum.USER_SECRET_EXIST_ERROR.getDefaultMessage(),
					SysOtherErrorCodeEnum.USER_SECRET_EXIST_ERROR.getCode()));
		}
		SysUserSecret sysUserSecret = new SysUserSecret();
		sysUserSecret.setName(name);
		sysUserSecret.setRemark(userSecretDto.getRemark());
		sysUserSecret.setSortNum(userSecretDto.getSortNum());
		sysUserSecret.setSysSecrets(sysSecrets);
		super.save(sysUserSecret);
	}

	@Override
	public void update(UserSecretDto userSecretDto, List<SysSecret> sysSecrets) throws GlobalException {
		if (!checkByName(userSecretDto.getName(), userSecretDto.getId())) {
			throw new GlobalException(new SystemExceptionInfo(
					SysOtherErrorCodeEnum.USER_SECRET_EXIST_ERROR.getDefaultMessage(),
					SysOtherErrorCodeEnum.USER_SECRET_EXIST_ERROR.getCode()));
		}
		SysUserSecret sysUserSecret = super.findById(userSecretDto.getId());
		sysUserSecret.setName(userSecretDto.getName());
		sysUserSecret.setRemark(userSecretDto.getRemark());
		sysUserSecret.setSortNum(userSecretDto.getSortNum());
		//添加关联前先清空原有关联
		sysUserSecret.getSysSecrets().clear();
		sysUserSecret.setSysSecrets(sysSecrets);
		//super.update(sysUserSecret);
	}

	/**
	 * 校验密集名称是否可用
	 *
	 * @param name 密级名称
	 * @param id   密级id
	 * @return boolean
	 */
	@Override
	public boolean checkByName(String name, Integer id) {
		if (StringUtils.isBlank(name)) {
			return true;
		}
		SysUserSecret sysUserSecret = dao.findByName(name);
		if (sysUserSecret == null) {
			return true;
		} else {
			return sysUserSecret.getId().equals(id);
		}
	}

}