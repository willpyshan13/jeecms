package com.jeecms.form.service.impl;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.google.gson.internal.LinkedHashTreeMap;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.service.CaptchaService;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.constants.UploadEnum;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.CaptchaExceptionInfo;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.IllegalParamExceptionInfo;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.InteractErrorCodeEnum;
import com.jeecms.common.image.ImageUtils;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.page.PaginableRequest;
import com.jeecms.common.ueditor.ResourceType;
import com.jeecms.common.util.MathUtil;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.util.StrUtils;
import com.jeecms.common.util.mediautil.MediaUtil;
import com.jeecms.common.util.office.FileUtils;
import com.jeecms.common.web.Location;
import com.jeecms.common.web.session.SessionProvider;
import com.jeecms.common.web.util.CookieUtils;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.content.util.ContentUtil;
import com.jeecms.form.dao.CmsFormDataDao;
import com.jeecms.form.domain.CmsFormDataAttrEntity;
import com.jeecms.form.domain.CmsFormDataAttrResEntity;
import com.jeecms.form.domain.CmsFormDataEntity;
import com.jeecms.form.domain.vo.CmsFormDataAttrPipVo;
import com.jeecms.form.domain.vo.CmsFormDataAttrStatisticVo;
import com.jeecms.form.domain.vo.CmsFormDataProviceVo;
import com.jeecms.form.domain.vo.CmsFormDataTimeVo;
import com.jeecms.form.domain.vo.CmsFormSubmitVo;
import com.jeecms.form.service.CmsFormDataAttrResService;
import com.jeecms.form.service.CmsFormDataAttrService;
import com.jeecms.form.service.CmsFormDataService;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.domain.CmsFormItemEntity;
import com.jeecms.interact.service.CmsFormService;
import com.jeecms.questionnaire.domain.SysQuestionnaireAnswer;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.domain.UploadFtp;
import com.jeecms.resource.domain.UploadOss;
import com.jeecms.resource.domain.dto.UploadResult;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.resource.service.impl.UploadService;
import com.jeecms.system.domain.Area;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.SysAccessRecord;
import com.jeecms.system.service.AddressService;
import com.jeecms.system.service.AreaService;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 表单service实现类
 *
 * @author: tom
 * @date: 2020/1/9 20:58
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CmsFormDataServiceImpl extends BaseServiceImpl<CmsFormDataEntity, CmsFormDataDao, Integer> implements CmsFormDataService {

    /**
     * ffmpeg的执行路径
     */
    @Value("${tool.ffmpeg}")
    private String ffmpegPath;

    public void deleteAllByFormId(Integer formId) throws GlobalException {
        dao.deleteAllByFormId(formId);
        CmsFormEntity formEntity = formService.findById(formId);
        if (formEntity != null) {
            formEntity.setJoinCount(0);
            formService.update(formEntity);
        }
    }

    @Override
    public CmsFormSubmitVo submitForm(Integer formId, HttpServletRequest request, HttpServletResponse response) throws GlobalException {
        CmsFormDataEntity data = new CmsFormDataEntity();
        if (formId == null) {
            throw new GlobalException(new IllegalParamExceptionInfo());
        }
        CmsFormEntity form = formService.findById(formId);
        /**未找到表单或者表单是未发布状态*/
        if (form == null || !CmsFormConstant.FORM_STATU_PUBLISH.equals(form.getStatus())) {
            throw new GlobalException(new IllegalParamExceptionInfo());
        }
        if (!CmsFormConstant.FORM_STATU_PUBLISH.equals(form.getStatus())) {
            throw new GlobalException(new SystemExceptionInfo(
                    InteractErrorCodeEnum.FORM_NOT_PUBLISH.getDefaultMessage(),
                    InteractErrorCodeEnum.FORM_NOT_PUBLISH.getCode()));
        }
        /**表单已结束调查*/
        if (CmsFormConstant.FORM_VIEW_STATU_STOP.equals(form.getStatus())) {
            throw new GlobalException(new SystemExceptionInfo(
                    InteractErrorCodeEnum.FORM_HAS_STOP.getDefaultMessage(),
                    InteractErrorCodeEnum.FORM_HAS_STOP.getCode()));
        }
        data.setFormId(formId);
        data.setForm(form);
        /**客户端信息*/
        Map<String, String> device = getDevice(request);
        data.setSystemInfo(device.get("device"));
        Boolean isPc = Integer.parseInt(device.get("deviceType")) == 1;
        data.setIsPc(isPc);
        data.setIsRecycle(false);
        data.setIsRead(false);
        /**微信客户端访问*/
        //TODO 此处还需要静默获取用户openid
        if (SystemContextUtils.isWxH5()) {
            Object wechatOpenId = sessionProvider.getAttribute(request, WebConstants.WECHAT_OPEN_ID);
            if (wechatOpenId != null) {
                data.setWxopenId(wechatOpenId.toString());
            }
        }
        /**开启了验证码*/
        if (form.getIsCaptcha() &&
                !captchaService.validCaptcha(request, response,
                        request.getParameter("captcha"),
                        request.getParameter("sessionId"))) {
            throw new GlobalException(new CaptchaExceptionInfo());
        }
        CoreUser user = SystemContextUtils.getUser(request);
        Integer userId = null;
        String ip = RequestUtils.getRemoteAddr(request);
        String cookieIdentity = "";
        Cookie cookie = CookieUtils.getCookie(request, WebConstants.IDENTITY_COOKIE);
        if (cookie != null) {
            cookieIdentity = cookie.getValue();
        }
        if (user != null) {
            userId = user.getUserId();
        }
        /***判断是否需要登录以及ip、cookie、微信限制**/
        checkUserLimit(form, userId, ip, cookieIdentity, data.getWxopenId());
        Map<String, String> map = area(request);
        String province = map.get("province");
        String city = map.get("city");
        data.setCookieIdentity(cookieIdentity);
        data.setIp(ip);
        data.setCityCode(city);
        data.setProvinceCode(province);
        List<Area> areas = areaService.findByAreaCode(city);
        if (areas != null && areas.size() > 0) {
            data.setCity(areas.get(0));
        }
        List<Area> pros = areaService.findByAreaCode(province);
        if (pros != null && pros.size() > 0) {
            data.setProvince(pros.get(0));
        }
        data.setIsRecycle(false);
        if (user != null) {
            data.setUserId(user.getId());
        }
        data = save(data);
        List<CmsFormDataAttrEntity> attrs = new ArrayList<CmsFormDataAttrEntity>();
        for (CmsFormItemEntity item : form.getInputItems()) {
            /**附件、图片、视频、音频*/
            if (CmsFormConstant.SINGLE_CHART_UPLOAD.equals(item.getDataType())
                    || CmsFormConstant.MANY_CHART_UPLOAD.equals(item.getDataType())
                    || CmsFormConstant.VIDEO_UPLOAD.equals(item.getDataType())
                    || CmsFormConstant.AUDIO_UPLOAD.equals(item.getDataType())
                    || CmsFormConstant.ANNEX_UPLOAD.equals(item.getDataType())) {
                List<MultipartFile> filess = new ArrayList<MultipartFile>();
                if (request instanceof MultipartHttpServletRequest) {
                    MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                    filess = multipartRequest.getFiles(item.getField());
                }
                //判断是否必填
                if (item.getIsRequired() && filess.size() == 0) {
                    throw new GlobalException(new SystemExceptionInfo(
                            InteractErrorCodeEnum.WRITER_LETTER_REQUIRED.getDefaultMessage(),
                            InteractErrorCodeEnum.WRITER_LETTER_REQUIRED.getCode()));
                }
                List<String> fileSuffixes = new ArrayList<String>();
                List<Long> fileSizes = new ArrayList<Long>();
                long totalFileSize = 0;
                for (MultipartFile multipartFile : filess) {
                    totalFileSize += multipartFile.getSize() / 1024;
                    fileSizes.add(multipartFile.getSize() / 1024);
                    String fileName = multipartFile.getOriginalFilename();
                    if (fileName != null) {
                        fileName = fileName.substring(fileName.lastIndexOf(".") + 1);
                        fileSuffixes.add(fileName);
                    }
                }
                //文件类型校验
                for (String suffix : fileSuffixes) {
                    if (!item.isAllowSuffix(suffix)) {
                        throw new GlobalException(new SystemExceptionInfo(
                                InteractErrorCodeEnum.WRITER_LETTER_FILE_TYPE_NOT_ALLOW.getDefaultMessage(),
                                InteractErrorCodeEnum.WRITER_LETTER_FILE_TYPE_NOT_ALLOW.getCode()));
                    }
                }
                //文件大小校验
                for (Long fileSize : fileSizes) {
                    if (!item.isAllowMaxFile(fileSize)) {
                        throw new GlobalException(new SystemExceptionInfo(
                                InteractErrorCodeEnum.WRITER_LETTER_FILE_SIZE_NOT_ALLOW.getDefaultMessage(),
                                InteractErrorCodeEnum.WRITER_LETTER_FILE_SIZE_NOT_ALLOW.getCode()));
                    }
                }
                //文件数量校验 0不验证
                if (item.getLimitFileNumber() != 0) {
                    if (item.getLimitFileNumber() < filess.size()) {
                        throw new GlobalException(new SystemExceptionInfo(
                                InteractErrorCodeEnum.WRITER_LETTER_FILE_NUMBER_NOT_ALLOW.getDefaultMessage(),
                                InteractErrorCodeEnum.WRITER_LETTER_FILE_NUMBER_NOT_ALLOW.getCode()));
                    }
                }
                /**执行上传*/
                if (filess.size() > 0) {
                    CmsFormDataAttrEntity attrEntity = new CmsFormDataAttrEntity();
                    attrEntity.setAttrType(CmsFormConstant.FIELD_ATTR_FILE_PROP_RES);
                    attrEntity.setAttrName(item.getField());
                    attrEntity.setDataId(data.getId());
                    attrEntity.setData(data);
                    attrs.add(attrEntity);
                    String typeStr = null;
                    /**待转pdf的资源属性*/
                    List<Integer>attrResIds = new ArrayList<>();
                    CmsSite site = SystemContextUtils.getSite(request);
                    for (MultipartFile file : filess) {
                        try {
                            if (file != null && ImageUtils.isImage(file.getInputStream())) {
                                typeStr = ResourceType.IMAGE.getName();
                            }
                            ResourceType resourceType = ResourceType.getDefaultResourceType(typeStr);
                            UploadFtp ftp = null;
                            UploadOss oss = null;
                            if (UploadEnum.UploadServerType.ftp.equals(site.getUploadServerType()) && site.getUploadFtp() != null) {
                                ftp = site.getUploadFtp();
                            } else if (UploadEnum.UploadServerType.oss.equals(site.getUploadServerType()) && site.getUploadOss() != null) {
                                oss = site.getUploadOss();
                            }
                            /**视频还需要自动生成缩率图并上传*/
                            File tempFile = null;
                            File tempFileForCutCover = null;
                            String origName = file.getOriginalFilename();
                            String fileName = origName;
                            String ext = FilenameUtils.getExtension(origName).toLowerCase(Locale.ENGLISH);
                            String tempPath = com.jeecms.common.util.FileUtils.getTempPath();
                            tempFile = new File(tempPath, String.valueOf(System.currentTimeMillis()) + "." + ext);
                            file.transferTo(tempFile);
                            UploadResult videoCover = null;
                            if (CmsFormConstant.VIDEO_UPLOAD.equals(item.getDataType()) && MediaUtil.isRun(ffmpegPath)) {
                                tempFileForCutCover = new File(tempPath, String.valueOf(System.currentTimeMillis() + 2000) + ".jpg");
                                MediaUtil.cutVideoCover(tempFile, tempFileForCutCover.getAbsolutePath());
                                /**上传视频抽帧截图*/
                                if (UploadEnum.UploadServerType.ftp.equals(site.getUploadServerType()) && site.getUploadFtp() != null) {
                                    ftp = site.getUploadFtp();
                                } else if (UploadEnum.UploadServerType.oss.equals(site.getUploadServerType()) && site.getUploadOss() != null) {
                                    oss = site.getUploadOss();
                                }
                                String spaceParam = RequestUtils.getParam(request, "spaceId");
                                Integer spaceId = null;
                                if (spaceParam != null && StrUtils.isNumeric(spaceParam)) {
                                    spaceId = Integer.parseInt(spaceParam);
                                }
                                if (tempFileForCutCover != null && tempFileForCutCover.exists()) {
                                    videoCover = uploadService.doUpload(tempFileForCutCover, null, null, ResourceType.IMAGE, site);
                                    ResourcesSpaceData videoCoverRes = resourcesSpaceDataService.save(userId, spaceId, videoCover.getOrigName(),
                                            videoCover.getFileSize().intValue(), videoCover.getFileUrl(), videoCover.getDimensions(),
                                            videoCover.getResourceType(), false, site.getId(), null, ftp, oss, null);
                                    videoCover.setResourceId(videoCoverRes.getId());
                                }
                            }
                            //UploadResult result = uploadService.doUpload(file, false, null, resourceType, site);
                            /**空的excel会导致转换不了pdf，此处不允许上传空的excel*/
                            boolean isEmptyExcel = false;
                            if(ext.equals("xls")||ext.equals("xlsx")){
                                try {
                                    isEmptyExcel = FileUtils.isEmptyExcel(new FileInputStream(tempFile));
                                }catch (Exception e){
                                }
                            }
                            if(isEmptyExcel){
                                if (tempFile != null && tempFile.exists()) {
                                    tempFile.delete();
                                }
                                if (tempFileForCutCover != null && tempFileForCutCover.exists()) {
                                    tempFileForCutCover.delete();
                                }
                                throw new GlobalException(new SystemExceptionInfo(
                                        InteractErrorCodeEnum.EXCEL_NOT_ALLOW_BLANK.getDefaultMessage(),
                                        InteractErrorCodeEnum.EXCEL_NOT_ALLOW_BLANK.getCode()));
                            }
                            UploadResult result = uploadService.doUpload(tempFile, false, null, resourceType, site);
                            result.setOrigName(file.getOriginalFilename());
                            /**单资源*/
                            if (CmsFormConstant.SINGLE_CHART_UPLOAD.equals(item.getDataType())
                                    || CmsFormConstant.VIDEO_UPLOAD.equals(item.getDataType())
                                    || CmsFormConstant.AUDIO_UPLOAD.equals(item.getDataType())) {
                                /**有可能未上传成功*/
                                if (result.getFileUrl() != null) {
                                    Integer resId = null;
                                    ResourcesSpaceData resourceData = resourcesSpaceDataService.save(userId, null, result.getOrigName(),
                                            result.getFileSize().intValue(), result.getFileUrl(), result.getDimensions(),
                                            result.getResourceType(), false, site.getId(), result.getDuration(), ftp, oss, null);
                                    if (resourceData != null) {
                                        resId = resourceData.getId();
                                        attrEntity.setResId(resId);
                                        attrEntity.setResourcesSpaceData(resourceData);
                                        /**如果是视频、保存视频抽帧图片*/
                                        if (CmsFormConstant.VIDEO_UPLOAD.equals(item.getDataType()) && videoCover != null) {
                                            attrEntity.setCoverImgId(videoCover.getResourceId());
                                            attrEntity.setCoverImg(resourcesSpaceDataService.findById(videoCover.getResourceId()));
                                        }
                                        attrEntity = attrService.save(attrEntity);
                                    }
                                }
                            } else {
                                attrEntity.setAttrType(CmsFormConstant.FIELD_ATTR_FILE_PROP_RESES);
                                attrEntity = attrService.save(attrEntity);
                                /**多资源*/
                                CmsFormDataAttrResEntity attrResEntity = new CmsFormDataAttrResEntity();
                                attrResEntity.setAttrId(attrEntity.getId());
                                attrResEntity.setAttr(attrEntity);
                                if (result.getFileUrl() != null) {
                                    Integer resId = null;
                                    ResourcesSpaceData resourceData = resourcesSpaceDataService.save(userId, null, result.getOrigName(),
                                            result.getFileSize().intValue(), result.getFileUrl(), result.getDimensions(),
                                            result.getResourceType(), false, site.getId(), result.getDuration(), ftp, oss, null);
                                    if (resourceData != null) {
                                        resId = resourceData.getId();
                                        attrResEntity.setResId(resId);
                                        attrResEntity.setResourcesSpaceData(resourceData);
                                        attrResEntity.setResDesc(RequestUtils.getParam(request, item.getField() + "Desc"));
                                        attrResEntity = attrResService.save(attrResEntity);
                                        /**只有附件里面的文档类型才需要转换pdf*/
                                        if(CmsFormConstant.ANNEX_UPLOAD.equals(item.getDataType())&& ContentUtil.isDocSuffix(ext)){
                                            attrResIds.add(attrResEntity.getId());
                                        }
                                    }
                                }
                            }
                            if (tempFile != null && tempFile.exists()) {
                                tempFile.delete();
                            }
                            if (tempFileForCutCover != null && tempFileForCutCover.exists()) {
                                tempFileForCutCover.delete();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    attrResService.uploadDoc(attrResIds, site);
                }
            } else {
                /***
                 * 前端需要加上省市县字段后缀P C A  单选、多选、下拉选择其他选项值需要附加参数 字段名称+Txt后缀的参数
                 */
                //TODO 需要加上字段校验
                CmsFormDataAttrEntity attrEntity = new CmsFormDataAttrEntity();
                attrEntity.setAttrType(CmsFormConstant.FIELD_ATTR_FILE_PROP_INPUT);
                attrEntity.setAttrName(item.getField());
                attrEntity.setDataId(data.getId());
                attrEntity.setData(data);
                String otherInput = RequestUtils.getParam(request, item.getField() + "Txt");
                if(StringUtils.isNotBlank(otherInput)){
                    attrEntity.setOtherInput(otherInput);
                }
                /**城市*/
                if (CmsFormConstant.CITY.equals(item.getDataType()) || CmsFormConstant.ADDRESS.equals(item.getDataType())) {
                    String proviceCode = RequestUtils.getParam(request, item.getField() + "P");
                    attrEntity.setProvinceCode(proviceCode);
                    List<Area> list = areaService.findByAreaCode(proviceCode);
                    if (list != null && list.size() > 0) {
                        attrEntity.setProvince(list.get(0));
                    }
                    String cityCode = RequestUtils.getParam(request, item.getField() + "C");
                    attrEntity.setCityCode(cityCode);
                    List<Area> citys = areaService.findByAreaCode(cityCode);
                    if (citys != null && citys.size() > 0) {
                        attrEntity.setCity(citys.get(0));
                    }
                    attrEntity.setAttrType(CmsFormConstant.FIELD_ATTR_FILE_PROP_CITY);
                }
                if (CmsFormConstant.ADDRESS.equals(item.getDataType())) {
                    /**详细地址*/
                    String areaCode = RequestUtils.getParam(request, item.getField() + "A");
                    attrEntity.setAreaCode(areaCode);
                    List<Area> citys = areaService.findByAreaCode(areaCode);
                    if (citys != null && citys.size() > 0) {
                        attrEntity.setArea(citys.get(0));
                    }
                    attrEntity.setAttrValue(RequestUtils.getParam(request, item.getField()));
                    attrEntity.setAttrType(CmsFormConstant.FIELD_ATTR_FILE_PROP_ADDRESS);
                } else if (CmsFormConstant.MANY_CHOOSE.equals(item.getDataType())
                        || CmsFormConstant.FIELD_TYPE_IMG_CHECKBOX.equals(item.getDataType())
                ) {
                    /**多选、图片多选 有多个结果，json数组存储*/
                    String val = RequestUtils.getParam(request, item.getField());
                    JSONArray array = new JSONArray();
                    if (StringUtils.isNotBlank(val)) {
                        String[] valArray = val.split(",");
                        array = new JSONArray(Arrays.asList(valArray));
                    }
                    /**图片多选和下拉选择显示的不一样，图片多选显示图片地址，下拉和多选都是显示lable*/
                    attrEntity.setAttrType(CmsFormConstant.FIELD_ATTR_FILE_PROP_ITEM_MULTI);
                    if (CmsFormConstant.FIELD_TYPE_IMG_CHECKBOX.equals(item.getDataType())) {
                        attrEntity.setAttrType(CmsFormConstant.FIELD_ATTR_FILE_PROP_IMG_CHECKBOX);
                    }
                    attrEntity.setAttrValue(array.toJSONString());
                } else {
                    attrEntity.setAttrValue(RequestUtils.getParam(request, item.getField()));
                    /**图片单选显示图片地址*/
                    if (CmsFormConstant.FIELD_TYPE_IMG_RADIO.equals(item.getDataType())) {
                        attrEntity.setAttrType(CmsFormConstant.FIELD_ATTR_FILE_PROP_IMG_RADIO);
                    } else if (CmsFormConstant.SEX.equals(item.getDataType())
                            || CmsFormConstant.DROP_DOWN.equals(item.getDataType())
                            || CmsFormConstant.SINGLE_CHOOSE.equals(item.getDataType())) {
                        /**其他单值*/
                        attrEntity.setAttrType(CmsFormConstant.FIELD_ATTR_FILE_PROP_ITEM);
                    }
                }
                /**没有有效值的不保存 */
                if (attrEntity.getEffectiveValue(item)) {
                    attrService.save(attrEntity);
                    attrs.add(attrEntity);
                }
            }
        }
        /**更新参与人次*/
        form.setJoinCount(form.getJoinCount() + 1);
        formService.update(form);
        data.setAttrs(attrs);
        CmsFormSubmitVo vo = new CmsFormSubmitVo();
        vo.setProcessType(form.getProcessType());
        vo.setPrompt(form.getPrompt());
        return vo;
    }


    /**
     * @param form          表单
     * @param userId        用户id
     * @param ip            ip
     * @param cookieIdentiy cookie标识
     * @param wechatOpenId  微信openid
     * @throws GlobalException
     */
    private void checkUserLimit(CmsFormEntity form, Integer userId, String ip, String cookieIdentiy, String wechatOpenId) throws GlobalException {
        boolean exceedLimit = false;
        if (form.getSubmitLimitLogin() != null && form.getSubmitLimitLogin()) {
            if (userId == null) {
                throw new GlobalException(new SystemExceptionInfo(
                        InteractErrorCodeEnum.USER_NOT_LOGIN.getDefaultMessage(),
                        InteractErrorCodeEnum.USER_NOT_LOGIN.getCode()));
            }
            //用户次数单位限制
            Short userLimitUnit = form.getUserSubLimitUnit();
            //用户次数限制
            Integer userFrequencyLimit = form.getUserSubLimit();
            Date date = Calendar.getInstance().getTime();
            /**开启了用户限制*/
            if (userFrequencyLimit != null && userLimitUnit != null &&
                    !CmsFormConstant.SUBMIT_LIMT_UNIT_NO.equals(userLimitUnit)) {
                if (CmsFormConstant.SUBMIT_LIMT_UNIT_ONE.equals(userLimitUnit)) {
                    /**只允许n次*/
                    long count = getCountForLimit(form.getId(), userId, null, null, null, null, null);
                    if (count >= userFrequencyLimit) {
                        exceedLimit = true;
                    }
                } else if (CmsFormConstant.SUBMIT_LIMT_UNIT_DAY.equals(userLimitUnit)) {
                    /**每天多少次(过去24小时)*/
                    Long count = getCountForLimit(form.getId(), userId, null, null, null, DateUtil.offsetDay(date, -1), date);
                    if (count >= userFrequencyLimit) {
                        exceedLimit = true;
                    }
                } else if (CmsFormConstant.SUBMIT_LIMT_UNIT_HOUR.equals(userLimitUnit)) {
                    /**每小时多少次*/
                    Long count = getCountForLimit(form.getId(), userId, null, null, null, MyDateUtils.getHourAfterTime(date, -1), date);
                    if (count >= userFrequencyLimit) {
                        exceedLimit = true;
                    }
                }
            }
        }
        //IP次数单位限制
        Short ipSubLimitUnit = form.getIpSubLimitUnit();
        //IP次数限制
        Integer ipSubLimit = form.getIpSubLimit();
        Date date = Calendar.getInstance().getTime();
        /**开启了IP限制*/
        if (ipSubLimit != null && ipSubLimitUnit != null &&
                !CmsFormConstant.SUBMIT_LIMT_UNIT_NO.equals(ipSubLimitUnit)) {
            if (CmsFormConstant.SUBMIT_LIMT_UNIT_ONE.equals(ipSubLimitUnit)) {
                /**只允许*/
                long count = getCountForLimit(form.getId(), null, ip, null, null, null, null);
                if (count >= ipSubLimit) {
                    exceedLimit = true;
                }
            } else if (CmsFormConstant.SUBMIT_LIMT_UNIT_DAY.equals(ipSubLimitUnit)) {
                /**每天多少次(过去24小时)*/
                Long count = getCountForLimit(form.getId(), null, ip, null, null, DateUtil.offsetDay(date, -1), date);
                if (count >= ipSubLimit) {
                    exceedLimit = true;
                }
            } else if (CmsFormConstant.SUBMIT_LIMT_UNIT_HOUR.equals(ipSubLimitUnit)) {
                /**每小时多少次*/
                Long count = getCountForLimit(form.getId(), null, ip, null, null, MyDateUtils.getHourAfterTime(date, -1), date);
                if (count >= ipSubLimit) {
                    exceedLimit = true;
                }
            }
        }
        //cookie次数单位限制
        Short deviceSubLimitUnit = form.getDeviceSubLimitUnit();
        //cookie次数限制
        Integer deviceSubLimit = form.getDeviceSubLimit();
        /**开启了设备限制*/
        if (deviceSubLimit != null && deviceSubLimitUnit != null &&
                !CmsFormConstant.SUBMIT_LIMT_UNIT_NO.equals(deviceSubLimitUnit)) {
            if (CmsFormConstant.SUBMIT_LIMT_UNIT_ONE.equals(deviceSubLimitUnit)) {
                /**只允许n次*/
                long count = getCountForLimit(form.getId(), null, null, cookieIdentiy, null, null, null);
                if (count >= deviceSubLimit) {
                    exceedLimit = true;
                }
            } else if (CmsFormConstant.SUBMIT_LIMT_UNIT_DAY.equals(ipSubLimitUnit)) {
                /**每天多少次(过去24小时)*/
                Long count = getCountForLimit(form.getId(), null, null, cookieIdentiy, null, DateUtil.offsetDay(date, -1), date);
                if (count >= deviceSubLimit) {
                    exceedLimit = true;
                }
            } else if (CmsFormConstant.SUBMIT_LIMT_UNIT_HOUR.equals(ipSubLimitUnit)) {
                /**每小时多少次*/
                Long count = getCountForLimit(form.getId(), null, null, cookieIdentiy, null, MyDateUtils.getHourAfterTime(date, -1), date);
                if (count >= deviceSubLimit) {
                    exceedLimit = true;
                }
            }
        }
        //微信限制
        if (form.getIsOnlyWechat()) {
            /**当前浏览器非微信客户端*/
            if (!SystemContextUtils.isWxH5()) {
                throw new GlobalException(new SystemExceptionInfo(
                        InteractErrorCodeEnum.FORM_NOLY_SUBMIT_WHCHAT.getDefaultMessage(),
                        InteractErrorCodeEnum.FORM_NOLY_SUBMIT_WHCHAT.getCode()));
            }
            /**成功获取到用户openid，则限制检查，否则忽略限制检查*/
            if (StringUtils.isNotBlank(wechatOpenId)) {
                Short wechatSubLimitUnit = form.getWechatSubLimitUnit();
                //微信次数限制
                Integer userFrequencyLimit = form.getWechatSubLimit();
                /**开启了微信限制*/
                if (userFrequencyLimit != null && wechatSubLimitUnit != null &&
                        !CmsFormConstant.SUBMIT_LIMT_UNIT_NO.equals(wechatSubLimitUnit)) {
                    if (CmsFormConstant.SUBMIT_LIMT_UNIT_ONE.equals(wechatSubLimitUnit)) {
                        /**只允许n次*/
                        long count = getCountForLimit(form.getId(), null, null, null, wechatOpenId, null, null);
                        if (count >= userFrequencyLimit) {
                            exceedLimit = true;
                        }
                    } else if (CmsFormConstant.SUBMIT_LIMT_UNIT_DAY.equals(wechatSubLimitUnit)) {
                        /**每天多少次(过去24小时)*/
                        Long count = getCountForLimit(form.getId(), null, null, null, wechatOpenId, DateUtil.offsetDay(date, -1), date);
                        if (count >= userFrequencyLimit) {
                            exceedLimit = true;
                        }
                    } else if (CmsFormConstant.SUBMIT_LIMT_UNIT_HOUR.equals(wechatSubLimitUnit)) {
                        /**每小时多少次*/
                        Long count = getCountForLimit(form.getId(), null, null, null, wechatOpenId, MyDateUtils.getHourAfterTime(date, -1), date);
                        if (count >= userFrequencyLimit) {
                            exceedLimit = true;
                        }
                    }
                }
            }
        }
        if (exceedLimit) {
            throw new GlobalException(new SystemExceptionInfo(
                    InteractErrorCodeEnum.WRITER_LETTER_EXCEED_COUNT.getDefaultMessage(),
                    InteractErrorCodeEnum.WRITER_LETTER_EXCEED_COUNT.getCode()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Page<CmsFormDataEntity> getPage(Integer formId, Boolean isRead, String proviceCode, String cityCode, Boolean isPc, String username, String ip,
                                           String cookieIdentity, String wxopenId, Integer userId,
                                           Date createTimeMin, Date createTimeMax, Pageable pageable) {
        /**地区编码转换为地区名查询*/
        Map<String, String> map = handleArea(proviceCode, cityCode);
        return dao.getPage(formId, isRead,  map.get("province"),  map.get("city"), isPc, username, ip, cookieIdentity, wxopenId, userId, createTimeMin, createTimeMax, pageable);
    }

    @Override
    public List<CmsFormDataEntity> getList(Integer formId, Boolean isRead, String proviceCode, String cityCode, Boolean isPc, String username, String ip, String cookieIdentity, String wxopenId, Integer userId, Date createTimeMin, Date createTimeMax, Paginable paginable) {
        /**地区编码转换为地区名查询*/
        Map<String, String> map = handleArea(proviceCode, cityCode);
        return dao.getList(formId, isRead, map.get("province"),  map.get("city"), isPc, username, ip, cookieIdentity, wxopenId, userId, createTimeMin, createTimeMax, paginable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Long getCount(Integer formId, Boolean isRead, String proviceCode, String cityCode, Boolean isPc, String username, String ip, String cookieIdentity,
                         String wxopenId, Integer userId, Date createTimeMin, Date createTimeMax) {
        /**地区编码转换为地区名查询*/
        Map<String, String> map = handleArea(proviceCode, cityCode);
        return dao.getCount(formId, isRead,  map.get("province"),  map.get("city"), isPc, username, ip, cookieIdentity, wxopenId, userId, createTimeMin, createTimeMax);
    }

    private Long getCountForLimit(Integer formId, Integer userId, String ip, String cookieIdentity, String wxopenId, Date createTimeMin, Date createTimeMax) {
        return getCount(formId, null, null, null, null, null, ip, cookieIdentity, wxopenId, userId, createTimeMin, createTimeMax);
    }

    @Override
    public List<CmsFormDataAttrStatisticVo> getAttrGroupByField(Integer formId) {
        CmsFormEntity formEntity = formService.findById(formId);
        List<CmsFormDataAttrStatisticVo> vos = new ArrayList<>();
        /**表单类数据不会太多，此处直接查询出再统计*/
        List<CmsFormDataEntity> datas = getList(formId, null, null, null, null, null, null, null, null, null, null, null, paginable);
        Map<String, Set<CmsFormDataAttrEntity>> dataAttrMap = new HashMap<>();
        for (CmsFormDataEntity d : datas) {
            for (CmsFormItemEntity itemEntity : formEntity.getInputItems()) {
                Set<CmsFormDataAttrEntity> attrs = dataAttrMap.get(itemEntity.getField());
                if (attrs == null) {
                    attrs = new HashSet<>();
                }
                attrs.add(d.getAttr().get(itemEntity.getField()));
                dataAttrMap.put(itemEntity.getField(), attrs);
            }
        }
        for (CmsFormItemEntity itemEntity : formEntity.getInputItems()) {
            CmsFormDataAttrStatisticVo vo = new CmsFormDataAttrStatisticVo();
            vo.setField(itemEntity.getField());
            vo.setItemLabel(itemEntity.getItemLabel());
            /**饼状图字段则显示各个选项的结果数据*/
            if (itemEntity.isPipField()) {
                Set<CmsFormDataAttrPipVo> dataAttrPipVos = new HashSet<>();
                if(dataAttrMap.get(itemEntity.getField())!=null){
                    Set<CmsFormDataAttrEntity> atts = dataAttrMap.get(itemEntity.getField()).stream().filter(a -> a != null).collect(Collectors.toSet());
                    int total = atts != null ? atts.size() : 0;
                    if (atts != null) {
                        Map<String, List<CmsFormDataAttrEntity>> valMap =
                                atts.stream().filter(p -> p != null && p.getAttrValueForType() != null)
                                        .collect(Collectors.groupingBy(CmsFormDataAttrEntity::getAttrValueForType));
                        if (CmsFormConstant.MANY_CHOOSE.equals(itemEntity.getDataType())
                                || CmsFormConstant.FIELD_TYPE_IMG_CHECKBOX.equals(itemEntity.getDataType())
                        ) {
                            /**多选值 json存储且应该显示lable，每条就记录包含多个选项*/
                            Map<String, Integer> valCountMap = new HashMap<>();
                            /**单选可以以数据条数为总数，多选则以每个选项总和为总数*/
                            total = 0;
                            for (CmsFormDataAttrEntity att : atts) {
                                List<String> vals = JSONArray.parseArray(att.getAttrValue(), String.class);
                                for (String v : vals) {
                                    Integer count = valCountMap.get(v) != null ? valCountMap.get(v) + 1 : 1;
                                    valCountMap.put(v, count);
                                    total++;
                                }
                            }
                            Set<String> keySet = valCountMap.keySet();
                            Iterator<String> keyIt = keySet.iterator();
                            int j = 0;
                            while (keyIt.hasNext()) {
                                String key = keyIt.next();
                                dataAttrPipVos.add(new CmsFormDataAttrPipVo(itemEntity.getOptionLabel(key), valCountMap.get(key)));
                            }
                        } else if (CmsFormConstant.CITY.equals(itemEntity.getDataType())) {
                            /**城市统计只统计省份label*/
                            Map<String, Integer> valCountMap = new HashMap<>();
                            for (CmsFormDataAttrEntity att : atts) {
                                Integer count = valCountMap.get(att.getProvinceCode()) != null ? valCountMap.get(att.getProvinceCode()) + 1 : 1;
                                valCountMap.put(att.getProvinceCode(), count);
                                total++;
                            }
                            Set<String> keySet = valCountMap.keySet();
                            Iterator<String> keyIt = keySet.iterator();
                            while (keyIt.hasNext()) {
                                String key = keyIt.next();
                                float d = valCountMap.get(key) * 1.0f / total;
                                try {
                                    List<Area> areas = areaService.findByAreaCode(key);
                                    String proviceName = "";
                                    if (areas != null && areas.size() > 0) {
                                        proviceName = areas.get(0).getAreaName();
                                    }
                                    dataAttrPipVos.add(new CmsFormDataAttrPipVo(proviceName,  valCountMap.get(key)));
                                } catch (GlobalException e) {

                                }
                            }
                        } else if (CmsFormConstant.FIELD_TYPE_IMG_RADIO.equals(itemEntity.getDataType())
                                ||CmsFormConstant.SINGLE_CHOOSE.equals(itemEntity.getDataType())
                                ||CmsFormConstant.DROP_DOWN.equals(itemEntity.getDataType())
                                ||CmsFormConstant.SEX.equals(itemEntity.getDataType())) {
                            /**单选label*/
                            Map<String, Integer> valCountMap = new HashMap<>();
                            for (CmsFormDataAttrEntity att : atts) {
                                Integer count = valCountMap.get(att.getAttrValue()) != null ? valCountMap.get(att.getAttrValue()) + 1 : 1;
                                valCountMap.put(att.getAttrValue(), count);
                                total++;
                            }
                            Set<String> keySet = valCountMap.keySet();
                            Iterator<String> keyIt = keySet.iterator();
                            while (keyIt.hasNext()) {
                                String key = keyIt.next();
                                dataAttrPipVos.add(new CmsFormDataAttrPipVo(itemEntity.getOptionLabel(key),  valCountMap.get(key)));
                            }
                        } else  {
                            /**级联直接传递的是lable，此处直接拿*/
                            Map<String, Integer> valCountMap = new HashMap<>();
                            total = 0;
                            for (CmsFormDataAttrEntity att : atts) {
                                Integer count = valCountMap.get(att.getAttrValue()) != null ? valCountMap.get(att.getAttrValue()) + 1 : 1;
                                valCountMap.put(att.getAttrValue(), count);
                                total++;
                            }
                            Set<String> keySet = valCountMap.keySet();
                            Iterator<String> keyIt = keySet.iterator();
                            while (keyIt.hasNext()) {
                                String key = keyIt.next();
                                dataAttrPipVos.add(new CmsFormDataAttrPipVo(key, valCountMap.get(key)));
                            }
                        }
                    }
                }
                /**过滤掉为空的值*/
                dataAttrPipVos = dataAttrPipVos.stream().filter(d->StringUtils.isNotBlank(d.getLabel())).collect(Collectors.toSet());
                vo.setData(dataAttrPipVos);
                vo.setPip(true);
            }
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public List<CmsFormDataProviceVo> staticByProvince(Integer formId) {
        List<CmsFormDataProviceVo> vos = dao.staticByProvince(formId);
        long total = vos.stream().mapToLong(CmsFormDataProviceVo::getDataCount).sum();
        BigDecimal previewRatio = BigDecimal.ZERO;
        int i = 0;
        for (CmsFormDataProviceVo vo : vos) {
            if(StringUtils.isNotBlank(vo.getProvince())){
                vo.setProvince(vo.getProvince());
            }else{
                vo.setProvince("其他");
            }
            if (i < vos.size() - 1) {
                vo.setRatio(BigDecimal.valueOf(floorNumber(vo.getDataCount() * 1.0f / total)*100));
                previewRatio = MathUtil.add(previewRatio,vo.getRatio());
            } else {
                /**最后一条,对应处理除不尽的情况*/
                vo.setRatio(MathUtil.sub(BigDecimal.valueOf(100L),previewRatio));
            }
            i++;
        }
        return vos;
    }

    @Override
    public List<CmsFormDataTimeVo> staticCountGroupByTime(Integer formId, Boolean pc, String province, String city, Integer showType, Date beginTime, Date endTime) {
        Date now = Calendar.getInstance().getTime();
        if (beginTime == null) {
            beginTime = now;
        }
        if (endTime == null) {
            endTime = now;
        }
        beginTime = MyDateUtils.getStartDate(beginTime);
        endTime = MyDateUtils.getFinallyDate(endTime);
        List<String> times = MyDateUtils.betweenTime(showType, beginTime, endTime);
        Map<String, List<CmsFormDataEntity>> dataMap = new LinkedHashTreeMap<String, List<CmsFormDataEntity>>();
        List<CmsFormDataTimeVo> vos = new ArrayList<>();
        List<CmsFormDataEntity> datas = getList(formId, null,   province, city, pc, null, null, null, null, null, beginTime, endTime, paginable);
        if (CmsFormConstant.GROUP_HOUR == showType
                || CmsFormConstant.GROUP_DAY == showType) {
            Map<String, List<CmsFormDataEntity>> map = datas.parallelStream().filter(o -> o.getCreateTime() != null)
                    .collect(Collectors.groupingBy(o -> MyDateUtils.formatDate(o.getCreateTime(), getPattern(showType))));
            for (String time : times) {
                CmsFormDataTimeVo vo = new CmsFormDataTimeVo();
                vo.setTime(time);
                if (map.containsKey(time)) {
                    vo.setCount(map.get(time).size());
                } else {
                    vo.setCount(0);
                }
                vos.add(vo);
            }
        } else if (CmsFormConstant.GROUP_WEEK == showType) {
            beginTime = beginTime != null ? MyDateUtils.getStartDate(beginTime) : Calendar.getInstance().getTime();
            endTime = endTime != null ? MyDateUtils.getFinallyDate(endTime) : Calendar.getInstance().getTime();
            Map<Date, Date> dateMap = MyDateUtils.groupByWeek(beginTime, endTime);
            for (Date date : dateMap.keySet()) {
                Date value = dateMap.get(date);
                List<CmsFormDataEntity> dataList = datas.parallelStream().filter(
                        o -> o.getCreateTime() != null && o.getCreateTime().getTime() >= date.getTime() &&
                                o.getCreateTime().getTime() <= value.getTime()).collect(Collectors.toList());
                String key1 = MyDateUtils.formatDate(date);
                String key2 = MyDateUtils.formatDate(value);
                dataMap.put(key1 + "-" + key2, dataList);
            }
            for (String s : dataMap.keySet()) {
                CmsFormDataTimeVo vo = new CmsFormDataTimeVo();
                vo.setTime(s);
                vo.setCount(dataMap.get(s).size());
                vos.add(vo);
            }
        } else if (CmsFormConstant.GROUP_MOUTH == showType) {
            beginTime = beginTime != null ? MyDateUtils.getStartDate(beginTime) : Calendar.getInstance().getTime();
            endTime = endTime != null ? MyDateUtils.getFinallyDate(endTime) : Calendar.getInstance().getTime();
            Map<Date, Date> dateMap = MyDateUtils.groupByMonth(beginTime, endTime);
            for (Date date : dateMap.keySet()) {
                Date value = dateMap.get(date);
                List<CmsFormDataEntity> dataList = datas.parallelStream().filter(
                        o -> o.getCreateTime() != null && o.getCreateTime().getTime() >= date.getTime() &&
                                o.getCreateTime().getTime() <= value.getTime()).collect(Collectors.toList());
                String key1 = MyDateUtils.formatDate(date);
                String key2 = MyDateUtils.formatDate(value);
                dataMap.put(key1 + "-" + key2, dataList);
            }
            for (String s : dataMap.keySet()) {
                CmsFormDataTimeVo vo = new CmsFormDataTimeVo();
                vo.setTime(s);
                vo.setCount(dataMap.get(s).size());
                vos.add(vo);
            }
        }
        return vos;
    }


    @Override
    public List<CmsFormDataEntity> delete(Integer[] ids) throws GlobalException {
        List<CmsFormDataEntity> datas = super.delete(ids);
        /**删除数据重新计算参与人次*/
        if (!datas.isEmpty()) {
            CmsFormEntity form = datas.get(0).getForm();
            Long total = getCount(form.getId(), null, null, null, null, null, null, null, null, null, null, null);
            form.setJoinCount(total.intValue());
            formService.update(form);
        }
        return datas;
    }

    @Override
    public List<CmsFormDataEntity> physicalDelete(Integer[] ids) throws GlobalException {
        List<CmsFormDataEntity> datas = super.physicalDelete(ids);
        /**删除数据重新计算参与人次*/
        if (!datas.isEmpty()) {
            CmsFormEntity form = datas.get(0).getForm();
            Long total = getCount(form.getId(), null, null, null, null, null, null, null, null, null, null, null);
            form.setJoinCount(total.intValue());
            formService.update(form);
        }
        return datas;
    }

    private String getPattern(int showType) {
        String s = null;
        switch (showType) {
            case CmsFormConstant.GROUP_DAY:
            case CmsFormConstant.GROUP_WEEK:
                s = MyDateUtils.COM_Y_M_D_PATTERN;
                break;
            case CmsFormConstant.GROUP_MOUTH:
                s = MyDateUtils.COM_Y_M_PATTERN;
                break;
            case CmsFormConstant.GROUP_HOUR:
                s = MyDateUtils.COM_H_PATTERN;
            default:
                break;
        }
        return s;
    }

    /**
     * 省，市，
     *
     * @param request {@link HttpServletRequest}
     */
    protected Map<String, String> area(HttpServletRequest request) {
        Map<String, String> map = new LinkedHashMap<>(2);
        //省份
        String province = null;
        //市区
        String city = null;
        Location location = null;
        Location.LocationResult.AdInfo adInfo = null;
        // 定位
        String currentIp = RequestUtils.getRemoteAddr(request);
        try {
            location = addressService.getAddressByIP(currentIp);
            request.getSession().setAttribute(Area.CURRENT_ADDRESS_ATTRNAME, location);
            if (location != null && location.getResult() != null) {
                adInfo = location.getResult().getAdInfo();
                city = adInfo.getCity();
                province = adInfo.getProvince();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //应前端需要，将省份，城市中的XX省，XX市去除
        if (StringUtils.isNotBlank(province) && province.contains("省")) {
            province = province.replace("省", "");
        }
        //去除直辖市
        if (StringUtils.isNotBlank(province) && province.contains("市")) {
            province = province.replace("市", "");
        }
        //去除自治区
        if (StringUtils.isNotBlank(province) && province.contains("自治区")) {
            province = province.replace("自治区", "");
            //特殊处理内蒙古
            if (province.contains("内蒙古")) {
                province = province.substring(0, 3);
            } else {
                province = province.substring(0, 2);
            }
        }
        if (StringUtils.isNotBlank(city) && city.contains("市")) {
            city = city.replace("市", "");
        }
        map.put("city", city);
        map.put("province", province);
        return map;
    }


    /**
     * 处理省和市
     *
     * @param provinceCode 省
     * @param cityCode     市
     * @return
     */
    private Map<String, String> handleArea(String provinceCode, String cityCode) {
        Map<String, String> map = new LinkedHashMap<>(2);
        String province = null;
        String city = null;
        try {
            if (StringUtils.isNotBlank(provinceCode)) {
                List<Area> provinceAreas = areaService.findByAreaCode(provinceCode);
                if(!provinceAreas.isEmpty()){
                    province = provinceAreas.get(0).getAreaName();
                }
            }
            if (StringUtils.isNotBlank(cityCode)) {
                List<Area> cityAreas = areaService.findByAreaCode(cityCode);
                if(!cityAreas.isEmpty()){
                    city = cityAreas.get(0).getAreaName();
                }
            }
        }catch (GlobalException e){
        }

        //应前端需要，将省份，城市中的XX省，XX市去除
        if (StringUtils.isNotBlank(province) && province.contains("省")) {
            province = province.replace("省", "");
        }
        //去除直辖市
        if (StringUtils.isNotBlank(province) && province.contains("市")) {
            province = province.replace("市", "");
        }
        //去除自治区
        if (StringUtils.isNotBlank(province) && province.contains("自治区")) {
            province = province.replace("自治区", "");
            //特殊处理内蒙古
            if (province.contains("内蒙古")) {
                province = province.substring(0, 3);
            } else {
                province = province.substring(0, 2);
            }
        }
        if (StringUtils.isNotBlank(city) && city.contains("市")) {
            city = city.replace("市", "");
        }
        map.put("city", city);
        map.put("province", province);
        return map;
    }



    private float floorNumber(float f) {
        /**保留4位小数*/
        return (float) Math.floor((double) (f * 10000.0F)) / 10000.0F;
    }

    /**
     * 访客设备系统
     *
     * @param request 请求
     * @Title: device
     */
    protected Map<String, String> getDevice(HttpServletRequest request) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        // 分析浏览器UserAgent,得到设备信息
        String userAgent = request.getHeader("User-Agent");
        if (!StringUtils.isNotBlank(userAgent)) {
            map.put("device", "移动设备");
            map.put("deviceType", "2");
        }
        if (userAgent.contains("Windows NT 10.0") || userAgent.contains("Windows NT 6.4")) {
            map.put("device", "Windows 10");
            map.put("deviceType", "1");
        } else if (userAgent.contains("Windows NT 6.2")) {
            map.put("device", "Windows 8");
            map.put("deviceType", "1");
        } else if (userAgent.contains("Windows NT 6.1")) {
            map.put("device", "Windows 7");
            map.put("deviceType", "1");
        } else if (userAgent.contains("iPhone OS 12")) {
            map.put("device", "iPhone OS 12");
            map.put("deviceType", "2");
        } else if (userAgent.contains("iPhone OS 11")) {
            map.put("device", "iPhone OS 11");
            map.put("deviceType", "2");
        } else if (userAgent.contains("iPhone OS 10")) {
            map.put("device", "iPhone OS 10");
            map.put("deviceType", "2");
        } else if (userAgent.contains("Android 10")) {
            map.put("device", "Android 10");
            map.put("deviceType", "2");
        } else if (userAgent.contains("Android 9")) {
            map.put("device", "Android 9");
            map.put("deviceType", "2");
        } else if (userAgent.contains("Android 8")) {
            map.put("device", "Android 8");
            map.put("deviceType", "2");
        } else if (userAgent.contains("Android 7")) {
            map.put("device", "Android 7");
            map.put("deviceType", "2");
        } else if (userAgent.contains("Android 6")) {
            map.put("device", "Android 6");
            map.put("deviceType", "2");
        } else if (userAgent.contains(SysAccessRecord.DEVICE_MAC)) {
            map.put("device", "Mac");
            map.put("deviceType", "1");
        } else {
            map.put("device", "PC");
            map.put("deviceType", "1");
        }
        return map;
    }

    @Autowired
    private CmsFormService formService;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private CmsFormDataAttrService attrService;
    @Autowired
    private CmsFormDataAttrResService attrResService;
    @Autowired
    private AreaService areaService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private ResourcesSpaceDataService resourcesSpaceDataService;
    @Autowired
    SessionProvider sessionProvider;
    private Paginable paginable = new PaginableRequest(0, Integer.MAX_VALUE);

}
