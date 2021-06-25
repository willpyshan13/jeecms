package com.jeecms.form.service;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.page.Paginable;
import com.jeecms.form.domain.CmsFormDataEntity;
import com.jeecms.form.domain.vo.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * form service接口
 *
 * @author: tom
 * @date:
 */
public interface CmsFormDataService extends IBaseService<CmsFormDataEntity, Integer> {
    /**
     * 根据表单ID删除表单提交数据
     * @param formId
     */
    void deleteAllByFormId(Integer formId) throws  GlobalException;

    /**
     * 提交表单
     * @param formId 表单ID
     * @param request
     * @param response
     * @return
     * @throws GlobalException
     */
    CmsFormSubmitVo submitForm(Integer formId, HttpServletRequest request, HttpServletResponse response)throws GlobalException;

    /**
     *查询列表
     * @param isRead 是否已读
     * @param proviceCode 省份代码
     * @param cityCode
     * @param isPc 是否pc
     * @param username 用户名
     * @param ip ip
     * @param cookieIdentity cookie标识
     * @param wxopenId 微信openid
     * @param createTimeMin  查询提交时间最小值
     * @param createTimeMax 查询提交时间最大值
     * @param pageable
     * @return
     */
    Page<CmsFormDataEntity> getPage(Integer formId, Boolean isRead, String proviceCode, String cityCode, Boolean isPc, String username,
                                    String ip, String cookieIdentity, String wxopenId, Integer userId,
                                    Date createTimeMin, Date createTimeMax, Pageable pageable);

    /**
     *查询列表
     * @param isRead 是否已读
     * @param proviceCode 省份代码
     * @param cityCode
     * @param isPc 是否pc
     * @param username 用户名
     * @param ip ip
     * @param cookieIdentity cookie标识
     * @param wxopenId 微信openid
     * @param createTimeMin  查询提交时间最小值
     * @param createTimeMax 查询提交时间最大值
     * @param paginable
     * @return
     */
    List<CmsFormDataEntity> getList(Integer formId, Boolean isRead, String proviceCode, String cityCode, Boolean isPc, String username,
                                    String ip, String cookieIdentity, String wxopenId, Integer userId,
                                    Date createTimeMin, Date createTimeMax, Paginable paginable);
    /**
     *查询列表
     * @param isRead 是否已读
     * @param proviceCode 省份代码
     * @param cityCode
     * @param isPc 是否pc
     * @param username 用户名
     * @param ip ip
     * @param cookieIdentity cookie标识
     * @param wxopenId 微信openid
     * @param createTimeMin  查询提交时间最小值
     * @param createTimeMax 查询提交时间最大值
     * @return
     */
    Long getCount(Integer formId, Boolean isRead, String proviceCode, String cityCode, Boolean isPc, String username,
                  String ip, String cookieIdentity, String wxopenId, Integer userId,
                  Date createTimeMin, Date createTimeMax);


    /**
     * 查询表单统计数据 根据字段分组
     * @param formId
     * @return
     */
    List<CmsFormDataAttrStatisticVo> getAttrGroupByField(Integer formId);

    /**
     * 查询表单统计数据 根据省份分组
     * @param formId
     * @return
     */
    List<CmsFormDataProviceVo> staticByProvince(Integer formId);

    /**
     * 查询表单统计数据 根据时间分组
     * @param formId
     * @return
     */
    List<CmsFormDataTimeVo> staticCountGroupByTime(Integer formId, Boolean pc, String province, String city,
                                                   Integer showType, Date beginTime, Date endTime);

}
