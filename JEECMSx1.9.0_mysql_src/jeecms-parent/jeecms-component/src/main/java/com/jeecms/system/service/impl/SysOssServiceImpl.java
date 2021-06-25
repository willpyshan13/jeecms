/*
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.system.service.impl;

import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.system.dao.SysOssDao;
import com.jeecms.system.domain.SysOss;
import com.jeecms.system.service.SysOssService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * oss云存储service实现
 *
 * @author: wulongwei
 * @date: 2019年4月9日 下午1:57:46
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysOssServiceImpl extends BaseServiceImpl<SysOss, SysOssDao, Integer> implements SysOssService {

    @Override
    public void saveSysOss(SysOss sysOss) throws GlobalException {
        this.ckeckOssInfo(sysOss);
        super.save(sysOss);
    }

    @Override
    public void updateSysOss(SysOss sysOss) throws GlobalException {
        this.ckeckOssInfo(sysOss);
        super.update(sysOss);
    }

    /**
     * 校验不同类型下，必填字段是否填写
     *
     * @param sysOss
     * @throws GlobalException
     * @Title: ckeckOssInfo
     * @return: void
     */
    private void ckeckOssInfo(SysOss sysOss) throws GlobalException {
        if (sysOss.getOssType() == SysOss.TENCENT_CLOUD) {
            this.checkTencentCloud(sysOss.getAppId(), sysOss.getBucketArea());
        } else if (sysOss.getOssType() == SysOss.ALI_CLOUD) {
            this.checkAliCloud(sysOss.getAccessDomain(), sysOss.getEndPoint());
        } else if (sysOss.getOssType() == SysOss.SEVEN_CATTLE_CLOUD) {
            this.checkSevenCattleCloud(sysOss.getAccessDomain());
        }
    }

    /**
     * 校验腾讯云存储时必填字段
     *
     * @param appId      AppId
     * @param bucketArea 地区码
     * @throws GlobalException
     * @Title: checkTencentCloud
     */
    private void checkTencentCloud(String appId, String bucketArea) throws GlobalException {
        if (StringUtils.isBlank(appId)) {
            throw new GlobalException(new SystemExceptionInfo(SettingErrorCodeEnum.APP_ID_NOT_NULL.getDefaultMessage(),
                    SettingErrorCodeEnum.APP_ID_NOT_NULL.getCode()));
        } else if (StringUtils.isBlank(bucketArea)) {
            throw new GlobalException(new SystemExceptionInfo(SettingErrorCodeEnum.BUCKET_AREA_NOT_NULL.getDefaultMessage(),
                    SettingErrorCodeEnum.BUCKET_AREA_NOT_NULL.getCode()));
        }
    }

    /**
     * 校验阿里云存储时必填字段
     *
     * @param accessDomain 访问域名
     * @param endPoint     阿里云endPoint
     * @throws GlobalException 全局异常
     */
    private void checkAliCloud(String accessDomain, String endPoint) throws GlobalException {
        if (StringUtils.isBlank(accessDomain)) {
            throw new GlobalException(new SystemExceptionInfo(SettingErrorCodeEnum.ACCESS_DO_MAIN_NOR_NULL.getDefaultMessage(),
                    SettingErrorCodeEnum.ACCESS_DO_MAIN_NOR_NULL.getCode()));
        } else if (StringUtils.isBlank(endPoint)) {
            throw new GlobalException(new SystemExceptionInfo(SettingErrorCodeEnum.END_POINT_NOT_NULL.getDefaultMessage(),
                    SettingErrorCodeEnum.END_POINT_NOT_NULL.getCode()));
        }
    }

    /**
     * 校验七牛云存储时必填字段
     *
     * @param accessDomain 访问域名
     * @throws GlobalException 全局异常
     */
    private void checkSevenCattleCloud(String accessDomain) throws GlobalException {
        if (StringUtils.isBlank(accessDomain)) {
            throw new GlobalException(new SystemExceptionInfo(SettingErrorCodeEnum.ACCESS_DO_MAIN_NOR_NULL.getDefaultMessage(),
                    SettingErrorCodeEnum.ACCESS_DO_MAIN_NOR_NULL.getCode()));
        }
    }


}
