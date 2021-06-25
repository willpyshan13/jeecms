/*
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.system.service;

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.system.domain.SysOss;

/**
 * oss存储Service层
 * @author: wulongwei
 * @date:   2019年4月9日 下午1:56:46     
 */
public interface SysOssService extends IBaseService<SysOss, Integer>{

	/**
	 * 添加oss存储信息
	 * @Title: saveSysOss  
	 * @param sysOss
	 * @throws GlobalException GlobalException
	 */
	void saveSysOss(SysOss sysOss)throws GlobalException;
	
	/**
	 * 修改oss存储信息
	 * @Title: updateSysOss  
	 * @param sysOss
	 * @return
	 * @throws GlobalException      
	 */
	void updateSysOss(SysOss sysOss)throws GlobalException;
}
