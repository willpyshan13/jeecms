package com.jeecms.interact.service.impl;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.util.MyBeanUtils;
import com.jeecms.common.web.cache.CacheConstants;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.content.domain.CmsModelItem;
import com.jeecms.event.CmsFormEvent;
import com.jeecms.interact.dao.CmsFormDao;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.domain.CmsFormItemEntity;
import com.jeecms.interact.domain.dto.CmsFormFastEditDto;
import com.jeecms.interact.domain.dto.CmsFormPublishDto;
import com.jeecms.interact.service.CmsFormItemService;
import com.jeecms.interact.service.CmsFormService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.system.domain.SysJob;
import com.jeecms.system.job.factory.JobFactory;
import com.jeecms.system.service.SysJobService;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 信件类型service实现类
 * @author: tom
 * @date: 2020/1/4 13:58   
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CmsFormServiceImpl extends BaseServiceImpl<CmsFormEntity, CmsFormDao,Integer> implements CmsFormService,
        ApplicationEventPublisherAware, DisposableBean {
    @Autowired
    private CmsFormItemService itemService;
    @Autowired
    private ResourcesSpaceDataService resourcesSpaceDataService;
    private final transient ReentrantLock lock = new ReentrantLock();

    @Override
    @Transactional(rollbackFor = Exception.class,readOnly = true)
    public boolean checkByName(Short scene, String name, Integer id, Integer siteId) {
        if (StringUtils.isBlank(name)) {
            return true;
        }
        /***
         * 领导信箱查询忽略站点
         */
        List<CmsFormEntity> lists;
        if(CmsFormConstant.FORM_SCENE_LETTER.equals(scene)){
            lists  = dao.findByTitleAndFormSceneAndHasDeleted(name,scene,false);
        }else{
            lists  = dao.findByTitleAndFormSceneAndHasDeletedAndSiteId(name,scene,false, siteId);
        }
        if (lists == null || lists.size() == 0) {
            return true;
        } else {
            if (id == null) {
                return false;
            }
            return lists.get(0).getId().equals(id);
        }
    }

    @Override
    public CmsFormEntity updateFields(CmsFormEntity dto) throws GlobalException {
        List<CmsFormItemEntity> items = new ArrayList<>();
        JSONArray jsons = dto.getFields();
        itemService.deleteByFormId(dto.getId());
        boolean  needCap = false;
        for (int i = 0; i < jsons.size(); i++) {
            JSONObject itemVal = jsons.getJSONObject(i);
            JSONObject itemValueJson = itemVal.getJSONObject("value");
            CmsFormItemEntity formItem = new CmsFormItemEntity();
            formItem.setForm(dto);
            // 字段名称
            formItem.setField(itemValueJson.getString(CmsFormConstant.FIELD));
            // 字段label
            formItem.setItemLabel(itemValueJson.getString(CmsFormConstant.ITEM_LABEL));
            // 字段默认值
            formItem.setDefValue(itemValueJson.getString(CmsFormConstant.DEF_VALUE));
            // 字段是否必填
            Boolean isRequired = itemValueJson.getBoolean(CmsFormConstant.IS_REQUIRED);
            isRequired = isRequired==null?false:isRequired;
            formItem.setIsRequired(isRequired);
            // 字段提示文字
            formItem.setPlaceholder(itemValueJson.getString(CmsFormConstant.PLACEHOLDER));
            // 字段辅助文字
            formItem.setTipText(itemValueJson.getString(CmsFormConstant.TIP_TEXT));
            // 字段类型
            formItem.setDataType(itemVal.getString(CmsFormConstant.INPUT_TYPE));
            // 字段分组
            formItem.setGroupType(itemVal.getString(CmsFormConstant.ELE_GROUP));

            // 字段是否自定义
            Boolean  isCustom = itemVal.getBoolean(CmsModelItem.IS_CUSTOM);
            isCustom = isCustom==null?true:isRequired;
            formItem.setIsCustom(isCustom);
            // 字段在当前模型中的分组中的显示顺序
            formItem.setSortNum(itemVal.getInteger(CmsModelItem.INDEX));
            // 该字段的完整json字符串格式数据
            formItem.setContent(itemVal.toJSONString());
            /**图片多选、图片单选加载图片url一并修改*/
            if(CmsFormConstant.FIELD_TYPE_IMG_CHECKBOX.equals(formItem.getDataType())
                    ||CmsFormConstant.FIELD_TYPE_IMG_RADIO.equals(formItem.getDataType())){
                JSONObject contentJson = JSON.parseObject(formItem.getContent());
                JSONObject valJson = contentJson.getJSONObject("value");
                if(valJson!=null){
                    JSONArray optionsJson = valJson.getJSONArray("options");
                    if(optionsJson!=null){
                        for(int j=0;j<optionsJson.size();j++){
                            JSONObject optionJson = optionsJson.getJSONObject(j);
                            Integer resId = optionJson.getInteger("value");
                            if(resId!=null){
                                ResourcesSpaceData res = resourcesSpaceDataService.findById(resId);
                                if(res!=null){
                                    optionJson.put(CmsFormConstant.FIELD_OPTION_IMG_URL,res.getUrl());
                                }
                            }
                        }
                    }
                }
                formItem.setContent(contentJson.toJSONString());
            }
            /**存在验证码组件则需要*/
            if(formItem.getDataType().equals(CmsFormConstant.FIELD_DEF_CAPTCHA)){
                needCap = true;
            }
            formItem.setFormId(dto.getId());
            formItem.setForm(findById(dto.getId()));
            items.add(formItem);
        }
        JSONObject bgConfJson = JSON.parseObject(dto.getBgConfig());
        if (bgConfJson != null) {
            Integer bgImgId = bgConfJson.getInteger(CmsFormConstant.FIELD_DEF_BG_IMG);
            dto.setBgImgId(bgImgId);
            if (dto.getBgImgId() != null) {
                ResourcesSpaceData bgImg = resourcesSpaceDataService.findById(dto.getBgImgId());
                dto.setBgImg(bgImg);
                /**处理JSON中图片url*/
                if (bgImg != null) {
                    bgConfJson.put(CmsFormConstant.FIELD_DEF_BG_IMG_URL, bgImg.getUrl());
                    dto.setBgConfig(bgConfJson.toJSONString());
                }
            }
        }
        JSONObject hdConfJson = JSON.parseObject(dto.getHeadConfig());
        if (hdConfJson != null) {
            Integer hdImgId = hdConfJson.getInteger(CmsFormConstant.FIELD_DEF_BG_IMG);
            dto.setHeadImgId(hdImgId);
            if (dto.getHeadImgId() != null) {
                ResourcesSpaceData hdImg = resourcesSpaceDataService.findById(dto.getHeadImgId());
                if (hdImg != null) {
                    hdConfJson.put(CmsFormConstant.FIELD_DEF_BG_IMG_URL, hdImg.getUrl());
                    dto.setBgConfig(bgConfJson.toJSONString());
                    dto.setHeadImg(hdImg);
                }
            }
        }
        if(dto.getCoverPicId()!=null){
            dto.setCoverPic(resourcesSpaceDataService.findById(dto.getCoverPicId()));
        }
        if(dto.getShareLogoId()!=null){
            dto.setShareLogo(resourcesSpaceDataService.findById(dto.getShareLogoId()));
        }
        dto.setIsCaptcha(needCap);
        CmsFormEntity dbForm = findById(dto.getId());
        /**修改字段不接收前端传递的状态值，以数据库原有值为准*/
        dto.setStatus(dbForm.getStatus());
        super.update(dto);
        itemService.saveAll(items);
        super.flush();
        return dto;
    }

    @Override
    public CmsFormEntity getVo(Integer id) {
        CmsFormEntity entity = findById(id);
        JSONObject bgConfJson = JSON.parseObject(entity.getBgConfig());
        /**处理JSON中图片url*/
        if(entity.getBgImgId()!=null){
            ResourcesSpaceData bgImg = resourcesSpaceDataService.findById(entity.getBgImgId());
            if(bgImg!=null){
                bgConfJson.put(CmsFormConstant.FIELD_DEF_BG_IMG_URL,bgImg.getUrl());
                entity.setBgConfig(bgConfJson.toJSONString());
            }
        }
        JSONObject hdConfJson = JSON.parseObject(entity.getHeadConfig());
        if(entity.getHeadImgId()!=null){
            ResourcesSpaceData hdImg = resourcesSpaceDataService.findById(entity.getHeadImgId());
            if(hdImg!=null){
                hdConfJson.put(CmsFormConstant.FIELD_DEF_BG_IMG_URL,hdImg.getUrl());
                entity.setBgConfig(bgConfJson.toJSONString());
            }
        }
        List<CmsFormItemEntity>items = entity.getItems();
        for(CmsFormItemEntity item:items){
            /**图片多选、图片单选加载图片url*/
            if(CmsFormConstant.FIELD_TYPE_IMG_CHECKBOX.equals(item.getDataType())
                    ||CmsFormConstant.FIELD_TYPE_IMG_RADIO.equals(item.getDataType())){
                JSONObject contentJson = JSON.parseObject(item.getContent());
                JSONObject valJson = contentJson.getJSONObject("value");
                if(valJson!=null){
                    JSONArray optionsJson = valJson.getJSONArray("options");
                    if(optionsJson!=null){
                        for(int i=0;i<optionsJson.size();i++){
                           JSONObject optionJson = optionsJson.getJSONObject(i);
                           Integer resId = optionJson.getInteger("value");
                           if(resId!=null){
                               ResourcesSpaceData res = resourcesSpaceDataService.findById(resId);
                               if(res!=null){
                                   optionJson.put(CmsFormConstant.FIELD_OPTION_IMG_URL,res.getUrl());
                               }
                           }
                        }
                    }
                }
                item.setContent(contentJson.toJSONString());
            }
        }
        return entity;
    }

    @Override
    public void publish(CmsFormPublishDto dto) throws GlobalException {
        List<CmsFormEntity> entities = new ArrayList<>();
        for(Integer id:dto.getIds()){
            CmsFormEntity entity = findById(id);
            Date now = Calendar.getInstance().getTime();
            if(dto.getPublish()){
                entity.setStatus(CmsFormConstant.FORM_STATU_PUBLISH);
                /**开始时间自动变为发布时间；*/
                entity.setBeginTime(now);
                /**若设定某个结束时间并保存后，等到超过结束时间后再点击发布时，结束时间自动变为空；*/
                if(entity.getEndTime()!=null){
                    if(entity.getEndTime().before(now)){
                        entity.setEndTime(null);
                    }
                }
            }else{
                entity.setStatus(CmsFormConstant.FORM_STATU_STOP);
            }
            entities.add(entity);
        }
        batchUpdateAll(entities);
    }

    @Override
    public CmsFormEntity save(CmsFormEntity bean) throws GlobalException {
        bean = super.save(bean);
        /**若是开始时间未到，则开启任务定时发布表单*/
        restartFormPublishJob(bean,true);
        return bean;
    }

    @Override
    public CmsFormEntity update(CmsFormEntity bean) throws GlobalException {
        CmsFormEntity dbForm = super.findById(bean.getId());
        boolean needRestartFormPublishJob = true;
        Date now = Calendar.getInstance().getTime();
        /**当开始时间是以后的时间，且修改了开始时间 则需要重新启动定时任务*/
        needRestartFormPublishJob = dbForm!=null&&dbForm.getBeginTime()!=null&&bean.getBeginTime()!=null&&!dbForm.getBeginTime().equals(bean.getBeginTime())&&bean.getBeginTime().after(now);
        bean = super.update(bean);
        restartFormPublishJob(bean,needRestartFormPublishJob);
        return bean;
    }

    @Override
    public CmsFormEntity updateAll(CmsFormEntity bean) throws GlobalException {
        CmsFormEntity dbForm = super.findById(bean.getId());
        boolean needRestartFormPublishJob = true;
        Date now = Calendar.getInstance().getTime();
        /**当开始时间是以后的时间，且修改了开始时间 则需要重新启动定时任务*/
        needRestartFormPublishJob = dbForm!=null&&dbForm.getBeginTime()!=null&&bean.getBeginTime()!=null&&!dbForm.getBeginTime().equals(bean.getBeginTime())&&bean.getBeginTime().after(now);
        bean =  super.updateAll(bean);
        restartFormPublishJob(bean,needRestartFormPublishJob);
        return bean;
    }

    private void restartFormPublishJob(CmsFormEntity bean,boolean needRestartFormPublishJob){
        ThreadPoolService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                /**目前只有场景-表单需要发布*/
                if(CmsFormConstant.FORM_SCENE_FORM.equals(bean.getFormScene())&&needRestartFormPublishJob){
                    /**若是开始时间未到，则撤销之前任务，并开启新任务定时发布表单*/
                    try {
                        /**删除任务,存在之前任务则删除*/
                        SysJob job = JobFactory.createFormPublishJob(bean.getId(), Calendar.getInstance().getTime());
                        if(jobService.checkJobExist(job)){
                            jobService.jobDelete(job);
                        }
                        if(bean.getBeginTime()!=null){
                            try {
                                jobService.addJob(JobFactory.createFormPublishJob(bean.getId(),bean.getBeginTime()));
                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.error(" add job error-->" + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        logger.error(" add job error-->" + e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public Page<CmsFormEntity> getPage(Short scene, Integer siteId, Integer typeId, Short status, String name, Pageable pageable) {
        return dao.getPage(scene, siteId, typeId, status,name,pageable );
    }

    @Override
    public List<CmsFormEntity> getList(Short scene, Integer siteId, Integer typeId, Short status, String name, Paginable paginable) {
        return dao.getList(scene, siteId, typeId, status,name,paginable );
    }

    @Override
    public Long getCount(Short scene, Integer siteId, Integer typeId, Short status, String name) {
        return dao.getCount(scene, siteId, typeId, status,name);
    }

    @Override
    public CmsFormEntity copy(CmsFormFastEditDto dto) throws GlobalException {
        CmsFormEntity entity = new CmsFormEntity();
        CmsFormEntity fromEntity = findById(dto.getId());
        MyBeanUtils.copyProperties(fromEntity,entity,"items");
        MyBeanUtils.copyProperties(dto,entity);
        if (StringUtils.isNotBlank(dto.getTitle())) {
            entity.setTitle(dto.getTitle());
        }
        if (StringUtils.isNotBlank(dto.getDescription())) {
            entity.setDescription(dto.getDescription());
        }
        entity.setId(null);
        entity.setJoinCount(0);
        entity.setViewCount(0);
        entity.setStatus(CmsFormConstant.FORM_STATU_NO_PUBLISH);
        entity = save(entity);
        List<CmsFormItemEntity>items = fromEntity.getItems();
        List<CmsFormItemEntity>toAddItems = new ArrayList<>();
        for(CmsFormItemEntity item:items){
            CmsFormItemEntity toAddItem = new CmsFormItemEntity();
            MyBeanUtils.copyProperties(item,toAddItem);
            toAddItem.setForm(entity);
            toAddItem.setFormId(entity.getId());
            toAddItem.setId(null);
            toAddItems.add(toAddItem);
        }
        toAddItems = itemService.saveAll(toAddItems);
        entity.setItems(toAddItems);
        return entity;
    }

    @Override
    public List<CmsFormEntity> physicalDelete(Integer[] ids) throws GlobalException {
        /**发布表单删除事件*/
        CmsFormEvent event = new CmsFormEvent(this,ids);
        publisher.publishEvent(event);
        return super.physicalDelete(ids);
    }

    @Override
    public Integer getViewAndRefreshCache(Integer id) {
        Element e = cache.get(id);
        Integer views;
        if (e != null) {
            views = (Integer) e.getObjectValue() + 1;
        } else {
            views = 1;
        }
        cache.put(new Element(id, views));
        refreshToDB();
        CmsFormEntity form = findById (id);
        return views + form.getViewCount();
    }

    private void refreshToDB() {
        long time = System.currentTimeMillis();
        if (time > refreshTime + interval) {
            refreshTime = time;
            int count = freshCacheToDB(cache);
            // 清除缓存
            cache.removeAll();
            logger.info("refresh cache views to DB: {}", count);
        }
    }

    /**
     * 销毁BEAN时，缓存入库。
     */
    public void destroy() throws Exception {
        int count = freshCacheToDB(cache);
        logger.info("Bean destroy.refresh cache views to DB: {}", count);
    }

    private int freshCacheToDB(Ehcache cache) {
        List<Integer> keys = cache.getKeys();
        if (keys.size() <= 0) {
            return 0;
        }
        Element e;
        Integer views;
        int i = 0;
        Set<CmsFormEntity> forms = new HashSet<>();
        for (Integer id : keys) {
            e = cache.get(id);
            if (e != null) {
                views = (Integer) e.getObjectValue();
                if (views != null) {
                    CmsFormEntity form = findById(id);
                    if(form != null) {
                        form.setViewCount(form.getViewCount() == null ? views : (form.getViewCount()+views));
                        forms.add(form);
                        i++;
                    }
                }
            }
        }
        try {
            batchUpdate(forms);
        }catch (GlobalException ge){
            logger.error(ge.getMessage());
        }
        return i;
    }


    private ApplicationEventPublisher publisher;
    @Autowired
    private SysJobService jobService;
    private Logger logger = LoggerFactory.getLogger(CmsFormServiceImpl.class);

    @Resource(name = CacheConstants.SMART_FORM_VIEW_CACHE)
    private Ehcache cache;

    // 间隔时间
    private int interval =  10* 60 * 1000; // 10分钟
    // 最后刷新时间
    private long refreshTime = System.currentTimeMillis();


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

}
