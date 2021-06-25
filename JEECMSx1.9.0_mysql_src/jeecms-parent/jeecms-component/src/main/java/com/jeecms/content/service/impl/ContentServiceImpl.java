/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.jeecms.audit.domain.AuditChannelSet;
import com.jeecms.audit.service.AuditChannelSetService;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.collect.service.CollectContentService;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.ContentErrorCodeEnum;
import com.jeecms.common.exception.error.RPCErrorCodeEnum;
import com.jeecms.common.exception.error.UserErrorCodeEnum;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.ueditor.ResourceType;
import com.jeecms.common.util.HibernateProxyUtil;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.util.SnowFlake;
import com.jeecms.common.util.StrUtils;
import com.jeecms.common.util.office.Doc2Html;
import com.jeecms.common.util.office.FileUtils;
import com.jeecms.common.util.office.OpenOfficeConverter;
import com.jeecms.common.web.cache.CacheProvider;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.common.wechat.bean.request.mp.material.AddMaterialRequest;
import com.jeecms.common.wechat.bean.request.mp.material.AddNewsRequest;
import com.jeecms.common.wechat.bean.request.mp.material.UploadImgRequest;
import com.jeecms.common.wechat.bean.request.mp.material.common.SaveArticles;
import com.jeecms.component.listener.*;
import com.jeecms.content.constants.CmsModelConstant;
import com.jeecms.content.constants.ContentButtonConstant;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.constants.ContentConstant.*;
import com.jeecms.content.dao.ContentDao;
import com.jeecms.content.domain.*;
import com.jeecms.content.domain.dto.*;
import com.jeecms.content.domain.vo.ContentButtonVo;
import com.jeecms.content.domain.vo.ContentFindVo;
import com.jeecms.content.domain.vo.ResetSecretVo;
import com.jeecms.content.domain.vo.WechatPushVo;
import com.jeecms.content.service.*;
import com.jeecms.content.util.ContentInitUtils;
import com.jeecms.message.MqConstants;
import com.jeecms.message.MqSendMessageService;
import com.jeecms.message.dto.CommonMqConstants;
import com.jeecms.protection.service.ParameterDecryptionUtilService;
import com.jeecms.publish.service.ContentPublishRecordService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.domain.dto.UploadResult;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.resource.service.impl.UploadService;
import com.jeecms.system.domain.*;
import com.jeecms.system.domain.dto.BeatchDto;
import com.jeecms.system.domain.dto.MessageExpandDto;
import com.jeecms.system.job.factory.JobFactory;
import com.jeecms.system.service.*;
import com.jeecms.util.SystemContextUtils;
import com.jeecms.wechat.constants.WechatConstants;
import com.jeecms.wechat.domain.WechatMaterial;
import com.jeecms.wechat.domain.WechatSend;
import com.jeecms.wechat.service.WechatMaterialService;
import com.jeecms.wechat.service.WechatSendService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jeecms.content.constants.ContentConstant.*;

/**
 * 内容主体service实现类
 *
 * @author: chenming
 * @date: 2019年5月6日 下午2:35:54
 */
@Service
@Transactional(rollbackFor = Exception.class)
@ConditionalOnMissingClass(value = "com.jeecms.workflow.service.impl.CmsWorkflowServiceImpl")
public class ContentServiceImpl extends BaseServiceImpl<Content, ContentDao, Integer>
        implements ContentService, SiteListener, ChannelListener, WorkflowListener, CmsModelListener, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentServiceImpl.class);

    @Autowired
    private SysJobService jobService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        /*
         * 每次系统初始化容器后处理内容老数据添加上模型字段快照
         */
        //ThreadPoolService.getInstance().execute(() -> processOldData());
    }

    private void processOldData() {
        int page = 1;
        int size = ContentLucene.THREAD_PROCESS_NUM;
        Pageable pageable = PageRequest.of(page, size);
        List<CmsModel> models = cmsModelService.findModel(CmsModel.CONTENT_TYPE);
        if (CollectionUtils.isEmpty(models)) {
            return;
        }
        List<Integer> modelIds = models.stream().map(CmsModel::getId).collect(Collectors.toList());
        Page<Content> content = dao.getPage(pageable, modelIds);
        int totalPage = content.getTotalPages();
        Map<Integer, String> modelFieldMap = new HashMap<>();
        for (CmsModel model : models) {
            modelFieldMap.put(model.getId(), model.getModelField());
        }
        for (int i = 0; i < totalPage; i++) {
            pageable = PageRequest.of(i, size);
            Page<Content> contents;
            contents = dao.getPage(pageable, modelIds);
            for (Content bean : contents) {
                bean.setModelFieldSet(modelFieldMap.get(bean.getModelId()));
            }
            try {
                batchUpdateAll(contents);
                flush();
            } catch (GlobalException e) {
                e.printStackTrace();
            }
            try {
                // 此是子线程操作的形式，所以要等待主线程完成才可以进行子线程操作
                Thread.sleep(3000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void afterChannelSave(Channel c) throws GlobalException {
        /* 空实现即可 */
    }

    @Override
    public void beforeChannelDelete(Integer[] ids) throws GlobalException {
        // 栏目加入回收站后对应的所有内容也全部加入回收站
        List<Content> contents = dao.findByChannelIdInAndHasDeleted(ids, false);
        if (!CollectionUtils.isEmpty(contents)) {
            super.delete(contents);
        }
        for (ContentListener listener : listenerList) {
            listener.afterDelete(contents);
        }

    }

    @Override
    public void beforeSiteDelete(Integer[] ids) throws GlobalException {

    }

    @Override
    public void afterSiteSave(CmsSite site) throws GlobalException {
        /* 空实现即可 */
    }

    @Override
    public void afterModelDelete(Collection<CmsModel> models) throws GlobalException {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterModelUpdate(CmsModel model) throws GlobalException {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeWorkflowDelete(Integer[] flowIds) throws GlobalException {

    }

    /**
     * 保存之后调用 根据业务生成索引、文库转换后文件
     *
     * @param content content
     * @return: void
     */
    private void afterSave(Content content) throws GlobalException {
        if (listenerList != null) {
            for (ContentListener listener : listenerList) {
                listener.afterSave(content);
            }
        }
    }

    /**
     * 更新前调用记录 内容状态等数据
     *
     * @param content 内容
     * @Title: preChange
     */
    public List<Map<String, Object>> preChange(Content content) {
        if (listenerList != null) {
            int len = listenerList.size();
            List<Map<String, Object>> list = new ArrayList<>(len);
            for (ContentListener listener : listenerList) {
                list.add(listener.preChange(content));
            }
            return list;
        } else {
            return null;
        }
    }

    /**
     * 更新后调用 根据业务更新索引、静态页、文库转换后文件
     *
     * @param content 内容
     * @param mapList List
     * @Title: afterChange
     * @return: void
     */
    private void afterChange(Content content, List<Map<String, Object>> mapList) {
        if (listenerList != null) {
            Assert.notNull(mapList, "mapList can not null");
            Assert.isTrue(mapList.size() == listenerList.size(), "mapList size not equals listenerList");
            int len = listenerList.size();
            ContentListener listener;
            for (int i = 0; i < len; i++) {
                listener = listenerList.get(i);
                ThreadPoolService.getInstance().execute(new ListenerThread(listener, mapList.get(i), content));
            }
        }
    }


    class ListenerThread implements Runnable {
        ContentListener listener;
        Map<String, Object> map;
        Content content;

        public ListenerThread(ContentListener listener, Map<String, Object> map, Content content) {
            this.listener = listener;
            this.map = map;
            this.content = content;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                listener.afterChange(content, map);
            } catch (GlobalException e) {
                LOGGER.error(e.getMessage());
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }

        }
    }

    /**
     * 刪除时调用 删除索引、静态页、文库转换后文件
     *
     * @param content Content
     * @Title: afterDelete
     * @return: void
     */
    private void afterDelete(Content content) throws GlobalException {
        if (listenerList != null) {
            List<Content> contents = new ArrayList<Content>();
            contents.add(content);
            for (ContentListener listener : listenerList) {
                listener.afterDelete(contents);
            }
        }
    }

    @Override
    public ResponseInfo changeStatus(BeatchDto dto, CoreUser user,Boolean checkComplete) throws GlobalException {
        if (user == null) {
            user = SystemContextUtils.getCoreUser();
        }
        List<Content> list = new ArrayList<Content>(10);
        List<ContentRecord> records = new ArrayList<ContentRecord>(10);
        List<Integer> ids = dto.getIds();
        List<Content> contents = super.findAllById(ids);
        // 智能审核内容集合
        List<Content> checkContents = null;
        boolean isReview = false;
        // 如果状态是发布则判断其内容是否开启是否可以进行智能审核
        if (ContentConstant.STATUS_PUBLISH == dto.getStatus()) {
            checkContents = new ArrayList<Content>();
            if (!checkComplete) {
                for (Content content : contents) {
                    boolean checkReview = false;
                    // 此判断是为了如果内容审核成功没有违禁词则直接进行发布操作，而不会再次进行修改操作
                    if (ContentConstant.STATUS_SMART_AUDIT != content.getStatus()) {
                        /**
                         * TODO 后期优化：checkReview应当修改成返回int类型
                         */
                        checkReview = contentReviewService.reviewContentCheck(content, content.getChannelId(), content.getModelId());
                    }
                    if (checkReview) {
                        isReview = true;
                        checkContents.add(content);
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(checkContents)) {
            if (!contentReviewService.checkAppIdOrPhone()) {
                checkContents = null;
                isReview = false;
            }
        }
        if (dto.isCheckPerm()) {
            /** 忽略权限检查也忽略状态判断 */
            // 判断状态是否可以操作
            checkOperate(contents, dto.getStatus(), dto.getSiteId());
            Map<Integer, List<Content>> map = contents.stream().collect(Collectors.groupingBy(Content::getChannelId));
            // 判断是否存在不可操作数据
            // 判断前台传的状态包含的设置初稿，草稿，流转中为修改权限，发布，下线，归档，是否有操作权限，因为发布与下线、为权限一体，归档另当作权限一体；
            for (Entry<Integer, List<Content>> entry : map.entrySet()) {
                List<Short> opration = user.getContentOperatorByChannelId(entry.getKey());
                // 判断发布与下线
                if (dto.getStatus().equals(STATUS_PUBLISH) || dto.getStatus().equals(STATUS_NOSHOWING)) {
                    // 如果没有权限，则返回存在不可操作数据
                    if (!opration.contains(CmsDataPerm.OPE_CONTENT_PUBLISH)) {
                        return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                                UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
                    }
                } else if (dto.getStatus().equals(STATUS_DRAFT) || dto.getStatus().equals(STATUS_FIRST_DRAFT)
                        || dto.getStatus().equals(STATUS_FLOWABLE)) {
                    // 判断修改
                    if (!opration.contains(CmsDataPerm.OPE_CONTENT_EDIT)) {
                        return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                                UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
                    }
                } else if (dto.getStatus().equals(STATUS_PIGEONHOLE)) {
                    // 判断归档
                    if (!opration.contains(CmsDataPerm.OPE_CONTENT_FILE)) {
                        return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                                UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
                    }
                }

			}
		}
		List<Content> newCheckContents = null;
		if (!CollectionUtils.isEmpty(checkContents)) {
			newCheckContents = new ArrayList<>();
		}
		// 修改状态的同时，也需要修改contentChannel里面的状态值
		for (Content content : contents) {
			boolean isCheck = false;
			Integer status = dto.getStatus();
			// 判断该内容是否在需要智能审核中，如果需要则将传入的状态改成智能审核中的状态
			if (!CollectionUtils.isEmpty(checkContents)) {
				if (checkContents.contains(content)) {
					isCheck = true;
					status = ContentConstant.STATUS_SMART_AUDIT;
					content.setCheckMark(String.valueOf(snowFlake.nextId()));
				}
			}
			List<Map<String, Object>> pre = preChange(content);
			content.setStatus(status);
			content.setEdit(true);
			list.add(content);
			String userName;
			if (user != null) {
				userName = user.getUsername();
			} else {
				userName = content.getUser().getUsername();
			}
			records.add(new ContentRecord(content.getId(), userName,
					"修改状态为" + ContentConstant.status(status)));
			Content bean = updateAll(content);
			if (isCheck) {
				newCheckContents.add(bean);
			}
			super.flush();
			/** 变更处理静态文件、索引等 */
			if (bean.getStatus().equals(STATUS_PUBLISH)) {
				List<ContentTxt> contentTxts = bean.getContentTxts();
				hotWordService.totalUserCount(bean.getChannelId(), contentTxts, content.getSiteId());
			}
			ThreadPoolService.getInstance().execute(() -> {
					afterChange(bean, pre);
			});
			//基于x1.4的网站统计扩展,记录文章发布数据
			if (ContentConstant.STATUS_PUBLISH == dto.getStatus() && !isReview) {
				statisticsContentService.savePublish(bean);
			}
		}
		contentRecordService.saveAll(records);
		if (dto.getStatus() == STATUS_FLOWABLE) {
			for (Content content : contents) {
				doSubmitFlow(content);
			}
		}
		if (newCheckContents != null) {
            CoreUser finalUser = user;
            List<Content> finalNewCheckContents = newCheckContents;
            ThreadPoolService.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        contentReviewService.reviewContents(finalNewCheckContents, finalUser.getId());
                    } catch (GlobalException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        if (isReview) {
            return new ResponseInfo(ContentConstant.STATUS_SMART_AUDIT);
        } else {
            return new ResponseInfo(dto.getStatus());
        }
    }

    /**
     * 判断该内容是否可以修改状态
     *
     * @throws GlobalException 异常
     * @Title: checkOperate
     * @param: contents 内容列表
     * @param: status   修改的内容状态
     */
    public void checkOperate(List<Content> contents, Integer status, Integer siteId) throws GlobalException {
        // 只处理以下几种状态，初稿，草稿，发布，下线，流转中，归档，如果前台传值不包含，则返回错误信息
        if (!Arrays.asList(STATUS_DRAFT, STATUS_FIRST_DRAFT, STATUS_FLOWABLE, STATUS_PUBLISH, STATUS_NOSHOWING,
                STATUS_PIGEONHOLE).contains(status)) {
            throw new GlobalException(ContentErrorCodeEnum.CONTENT_STATUS_ERROR);
        }
        CmsSiteConfig config = cmsSiteService.findById(siteId).getCmsSiteCfg();
        for (Content content : contents) {
            // 判断是否设置工作流
            Integer workflow = content.getChannel().getRealWorkflowId();
            if (workflow != null) {
                // 存在工作流
                switch (content.getStatus()) {
                    case STATUS_DRAFT:
                    case STATUS_FIRST_DRAFT:
                        // 原状态为草稿，初稿状态时，可以设置为初稿，流转中，草稿， 归档
                        if (!Arrays.asList(STATUS_DRAFT, STATUS_FIRST_DRAFT, STATUS_FLOWABLE, STATUS_PIGEONHOLE)
                                .contains(status)) {
                            throw new GlobalException(ContentErrorCodeEnum.CONTENT_STATUS_ERROR);
                        }
                        break;
                    case STATUS_FLOWABLE:
                        // 原状态为流转中状态时，不处理任何状态
                        throw new GlobalException(ContentErrorCodeEnum.CONTENT_STATUS_ERROR);
                    case STATUS_WAIT_PUBLISH:
                        // 原状态为已审核状态时，可以设置为初稿，发布，草稿，流转， 归档
                        if (!Arrays.asList(STATUS_DRAFT, STATUS_FIRST_DRAFT, STATUS_PUBLISH, STATUS_FLOWABLE,
                                STATUS_PIGEONHOLE).contains(status)) {
                            throw new GlobalException(ContentErrorCodeEnum.CONTENT_STATUS_ERROR);
                        }
                        break;
                    case STATUS_PUBLISH:
                        // 判断站点设置是否允许内容发布编辑， 归档
                        Boolean flag = config.getContentCommitAllowUpdate();
                        if (!flag) {
                            // 原状态为发布状态时，可以设置为下线， 归档
                            if (!Arrays.asList(STATUS_NOSHOWING, STATUS_PIGEONHOLE).contains(status)) {
                                throw new GlobalException(ContentErrorCodeEnum.CONTENT_STATUS_ERROR);
                            }
                        } else {
                            // 原状态为发布状态时，可以设置为初稿，发布，草稿，下线， 归档
                            if (!Arrays.asList(STATUS_DRAFT, STATUS_FIRST_DRAFT, STATUS_PUBLISH, STATUS_NOSHOWING,
                                    STATUS_FLOWABLE, STATUS_PIGEONHOLE).contains(status)) {
                                throw new GlobalException(ContentErrorCodeEnum.CONTENT_STATUS_ERROR);
                            }
                        }
                        break;
                    case STATUS_NOSHOWING:
                        // 原状态为下线状态时，可以设置为初稿，发布，草稿，流转中， 归档
                        if (!Arrays.asList(STATUS_DRAFT, STATUS_FIRST_DRAFT, STATUS_FLOWABLE, STATUS_PUBLISH,
                                STATUS_PUBLISH, STATUS_PIGEONHOLE).contains(status)) {
                            throw new GlobalException(ContentErrorCodeEnum.CONTENT_STATUS_ERROR);
                        }
                        break;
                    case STATUS_BACK:
                        // 原状态为驳回状态时，可以设置为初稿，流转中，草稿， 归档
                        if (!Arrays.asList(STATUS_DRAFT, STATUS_FIRST_DRAFT, STATUS_FLOWABLE, STATUS_PIGEONHOLE)
                                .contains(status)) {
                            throw new GlobalException(ContentErrorCodeEnum.CONTENT_STATUS_ERROR);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                // 不存在工作流
                switch (content.getStatus()) {
                    case STATUS_DRAFT:
                    case STATUS_FIRST_DRAFT:
                        // 原状态为草稿状态时，可以设置为初稿，发布，草稿， 归档
                        if (!Arrays.asList(STATUS_DRAFT, STATUS_FIRST_DRAFT, STATUS_PUBLISH, STATUS_PIGEONHOLE)
                                .contains(status)) {
                            throw new GlobalException(ContentErrorCodeEnum.CONTENT_STATUS_ERROR);
                        }
                        break;
                    case STATUS_PUBLISH:
                        // 原状态为发布状态时，可以设置下线， 归档
                        Boolean flag = config.getContentCommitAllowUpdate();
                        // 判断站点设置是否允许内容发布编辑
                        if (!flag) {
                            if (!Arrays.asList(STATUS_NOSHOWING, STATUS_PIGEONHOLE).contains(status)) {
                                throw new GlobalException(ContentErrorCodeEnum.CONTENT_STATUS_ERROR);
                            }
                        } else {
                            // 原状态为发布状态时，可以设置为初稿，发布，草稿，下线， 归档
                            if (!Arrays.asList(STATUS_DRAFT, STATUS_FIRST_DRAFT, STATUS_PUBLISH, STATUS_NOSHOWING,
                                    STATUS_PIGEONHOLE).contains(status)) {
                                throw new GlobalException(ContentErrorCodeEnum.CONTENT_STATUS_ERROR);
                            }
                        }
                        break;
                    case STATUS_NOSHOWING:
                        // 原状态为下线状态时，可以设置为初稿，发布，草稿, 归档
                        if (!Arrays.asList(STATUS_DRAFT, STATUS_FIRST_DRAFT, STATUS_PUBLISH, STATUS_PIGEONHOLE)
                                .contains(status)) {
                            throw new GlobalException(ContentErrorCodeEnum.CONTENT_STATUS_ERROR);
                        }
                        break;
                    case STATUS_FLOWABLE:
                        throw new GlobalException(ContentErrorCodeEnum.CHANNEL_NOT_WORKFLOW_NOT_REVIEW);
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void publish(List<Integer> ids) throws GlobalException {
//		List<Content> contents = super.findAllById(ids);
        CoreUser user = SystemContextUtils.getCoreUser();
//		Integer userId = user.getId();
//		List<Content>  needYunCheckContents = new ArrayList<>();
//		/**将需要智能审核的内容送审*/
//		ContentUpdateDto dto = new ContentUpdateDto();
//		dto.setUser(user);
//		dto.setForce(false);
//		for (Content c : contents) {
//			dto.setId(c.getId());
//			/**查询是否需要智能参数*/
//			dto.setType(c.getStatus());
//			dto.setChannelId(c.getChannelId());
//			dto.setModelId(c.getModelId());
//			if(getNeedYunCheck(dto,c)){
//				needYunCheckContents.add(c);
//			}
//		}
//		contents.removeAll(needYunCheckContents);
//		/**其余内容不需要智能审核，走普通发布了*/
//		for (Content c : contents) {
//			List<Map<String, Object>> mapList = preChange(c);
//			c.setStatus(ContentConstant.STATUS_PUBLISH);
//			c.setReleaseTime(new Date());
//			c.getOriContentChannel().setStatus(ContentConstant.STATUS_PUBLISH);
//			afterChange(c, mapList);
//		}
//		contentReviewService.reviewContents(needYunCheckContents,userId);
        BeatchDto dto = new BeatchDto(ids, ContentConstant.STATUS_PUBLISH, false);
        this.changeStatus(dto, user,false);
    }

    @Override
    public ResponseInfo operation(OperationDto dto) throws GlobalException {
        // 查询得到内容列表
        List<Content> contents = super.findAllById(dto.getIds());
        // 判断权限
        if (!validType(CmsDataPerm.OPE_CONTENT_TYPE, contents)) {
            return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                    UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
        }
        // 内容类型操作
        type(dto, contents);
        return new ResponseInfo();
    }

    /**
     * 类型操作
     **/
    protected void type(OperationDto dto, List<Content> contents) throws GlobalException {
        List<Content> list = new ArrayList<Content>(10);
        // 得到内容类型ID
        Integer type = dto.getContentTypeId();
        // 判断是否新增内容类型
        if (dto.getAdd()) {
            ContentType contentType = contentTypeService.findById(type);
            for (Content content : contents) {
                List<Integer> integers = content.getContentTypes().stream().map(ContentType::getId)
                        .collect(Collectors.toList());
                // 判断是否包含内容类型
                if (integers.contains(type)) {
                    continue;
                }
                content.getContentTypes().add(contentType);
                content.setContentTypes(content.getContentTypes());
                list.add(content);
            }
        } else {
            // 取消内容类型
            for (Content content : contents) {
                // 得到内容类型
                List<ContentType> contentTypes = content.getContentTypes();
                CopyOnWriteArrayList<ContentType> cowList = new CopyOnWriteArrayList<ContentType>(contentTypes);
                for (ContentType contentType : cowList) {
                    if (contentType.getId().equals(type)) {
                        cowList.remove(contentType);
                    }
                }
                content.setContentTypes(cowList);
                list.add(content);
            }
        }
        batchUpdate(list);
    }

    protected void order(OperationDto dto, List<Content> contents) throws GlobalException {
        List<Content> list = new ArrayList<Content>(10);
        //得到要排序的最大值
        if (contents.isEmpty()) {
            return;
        }
        //得到平移值
        Integer size = contents.size();
        //得到最大的排序值
        Content content = dao.findFirstByOrderBySortNumDesc();
        //得到待排序的内容
        Content con = super.findById(dto.getContentId());
        List<Boolean> booleans = contents.stream().map(Content::getTop).collect(Collectors.toList());
        if (content.getSortNum() / 2 > con.getSortNum()) {
            //表示在前半段
            dao.updateSortNum(con.getSortNum(), size, false, !dto.getLocation());
            if (!dto.getLocation()) {
                for (int i = 0; i < contents.size(); i++) {
                    //排序值在前
                    contents.get(i).setSortNum(con.getSortNum() - (i + 1));
                    list.add(contents.get(i));
                }
            } else {
                for (int i = 0; i < contents.size(); i++) {
                    //排序值在后
                    contents.get(i).setSortNum(con.getSortNum() + i);
                    list.add(contents.get(i));
                }
            }

        } else {
            //表示在后半段
            dao.updateSortNum(con.getSortNum(), size, true, !dto.getLocation());
            if (!dto.getLocation()) {
                for (int i = 0; i < contents.size(); i++) {
                    //排序值之前
                    contents.get(i).setSortNum(con.getSortNum() + i);
                    list.add(contents.get(i));
                }
            } else {
                for (int i = 0; i < contents.size(); i++) {
                    //排序值之后
                    contents.get(i).setSortNum(con.getSortNum() + (i + 1));
                    list.add(contents.get(i));
                }
            }

        }
        if (dto.getLocation()) {
            // 将非置顶内容放到置顶内容之前，需要加置顶状态
            if (con.getTop() && booleans.contains(false)) {
                List<Content> lists = list.stream().filter(x -> x.getTop().equals(false))
                        .collect(Collectors.toList());
                for (Content c : lists) {
                    c.setTop(true);
                }
            }
        } else {
            // 将非置顶内容放到置顶内容之后，需要取消置顶状态
            if (!con.getTop() && booleans.contains(true)) {
                List<Content> lists = list.stream().filter(x -> x.getTop().equals(true))
                        .collect(Collectors.toList());
                for (Content c : lists) {
                    c.setTop(false);
                }
            }
        }
        batchUpdate(list);
    }

    @Override
    public ResponseInfo top(OperationDto dto) throws Exception {
        // 查询得到内容列表
        List<Content> contents = super.findAllById(dto.getIds());
        if (!validType(CmsDataPerm.OPE_CONTENT_TOP, contents)) {
            return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                    UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
        }
        for (Content content : contents) {
            content.setTopStartTime(dto.getStartTime());
            content.setTopEndTime(dto.getEndTime());
            content.setTop(true);
            //添加定时任务
            if (dto.getEndTime() != null && dto.getEndTime().after(new Date())) {
                SysJob job = JobFactory.createContentTopJob(content.getId(), dto.getEndTime());
                // 新增job
                jobService.addJob(job);
            }
        }
        batchUpdate(contents);
        return new ResponseInfo();
    }

    @Override
    public ResponseInfo notop(OperationDto dto) throws GlobalException {
        // 查询得到内容列表
        List<Content> contents = super.findAllById(dto.getIds());
        if (!validType(CmsDataPerm.OPE_CONTENT_TOP, contents)) {
            return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                    UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
        }
        for (Content content : contents) {
            //删除任务
            if (content.getTopEndTime() != null) {
                jobService.jobDelete(JobFactory.createContentTopJob(content.getId(), Calendar.getInstance().getTime()));
            }
            content.setTopStartTime(null);
            content.setTopEndTime(null);
            content.setTop(false);
        }
        batchUpdate(contents);
        return new ResponseInfo();
    }

    @Override
    public ResponseInfo move(OperationDto dto) throws GlobalException {
        // 接收操作完成后的集合
        List<Content> list = new ArrayList<Content>(10);
        // 查询得到内容列表
        List<Content> contents = super.findAllById(dto.getIds());
        // 判断操作类型
        if (!validType(CmsDataPerm.OPE_CONTENT_MOVE, contents)) {
            return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                    UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
        }
        // 移动
        Integer channelId = dto.getChannelId();
        Channel channel = channelService.findById(channelId);
        List<Channel> channelList = new ArrayList<Channel>(10);
        channelList.add(channel);
        for (Content content : contents) {
            content.setChannelId(channelId);
            content.setChannel(channel);
            list.add(content);
        }
        batchUpdate(list);
        return new ResponseInfo();
    }

	@Override
	public synchronized ResponseInfo sort(OperationDto dto) throws GlobalException {
        // 查询得到内容列表
        List<Content> contents = this.findAllById(dto.getIds());
	    order(dto, contents);
		return new ResponseInfo();
	}

    @Override
    public void rubbish(OperationDto dto) throws GlobalException {
        // 查询得到内容列表
        List<Content> contents = this.findAllByIdForCache(dto.getIds());
        // 删除内容，加入回收站
        for (Content content : contents) {
            content.setRecycle(true);
            // 引用内容同步改状态
            for (Content c : content.getQuoteContents()) {
                c.setRecycle(true);
                update(c);
            }
        }
        batchUpdate(contents);
        if (!CollectionUtils.isEmpty(contents)) {
            List<Integer> contentIds = contents.stream().filter(content -> content.getStatus().equals(STATUS_FLOWABLE))
                    .map(Content::getId).collect(Collectors.toList());
            flowService.doInterruptDataFlow(ContentConstant.WORKFLOW_DATA_TYPE_CONTENT, contentIds,
                    SystemContextUtils.getCoreUser());
        }
        if (!CollectionUtils.isEmpty(contents)) {
            List<Integer> contentIds = contents.stream().map(Content::getId).collect(Collectors.toList());
            for (ContentListener lis : listenerList) {
                try {
                    lis.afterContentRecycle(contentIds);
                } catch (GlobalException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }

    /**
     * 根据IDs查询内容
     *
     * @param ids 集合
     * @return List
     */
    @Override
    public List<Content> findAllByIdForCache(List<Integer> ids) {
        List<Content> contents = dao.getListForCache(ids);
        /**设置引用到了哪些目标内容  这样方便取应用到了哪些栏目*/
        for (Content c : contents) {
            c.setQuoteContents(dao.findByOriContentIdAndRecycleAndHasDeleted(c.getId(), false, false));
        }
        return contents;
    }

    /**
     * 收益统计
     *
     * @param sortType
     * @return
     */
    @Override
    public Page<Content> getProfitCount(int sortType, Pageable pageable) {

        return dao.getProfitCount(sortType,pageable);
    }

    /**
     * 付费统计 内容top10
     *
     * @param sortType
     * @return
     */
    @Override
    public List<Content> getContentTopTen(int sortType) {

        return dao.getContentTopTen(sortType);
    }

    @Override
    public ResetSecretVo resetSecret(ResetSecretDto dto, CoreUser user, Integer siteId) {
        // 获取唯一标识
        String code = String.valueOf(snowFlake.nextId());
        cacheProvider.setCache(Content.CONTENT_CACHE_KEY, code, WebConstants.STRING_FALSE);
        ThreadPoolService.getInstance().execute(() -> {
            String creatTime = MyDateUtils.formatDate(new Date(), MyDateUtils.COM_Y_M_D_H_M_S_PATTERN);
            boolean requestStatus = true;
            boolean contentSecretType = true;
            try {
                if (dto.getModuleId() != null) {
                    resetSecret(dto.getSecretIds(), dto.getModuleId());
                }
                if (!CollectionUtils.isEmpty(dto.getSecretIds())) {
                    if (SysSecret.CONTENT_SECRET.equals(dto.getType())) {
                        resetSecret(dto.getSecretIds(), null);
                    }
                    if (SysSecret.ANNEX_SECRET.equals(dto.getType())) {
                        contentSecretType = false;
                        contentAttrResService.resetAnnexSecret(dto.getSecretIds());
                    }
                }
            } catch (GlobalException e) {
                e.printStackTrace();
                requestStatus = false;
            }
            String endTime = MyDateUtils.formatDate(new Date(), MyDateUtils.COM_Y_M_D_H_M_S_PATTERN);
            cacheProvider.setCache(Content.CONTENT_CACHE_KEY, code, "true");
            String requestStatusStr = requestStatus ? "成功" : "失败";
            String secretType = contentSecretType ? "内容" : "附件";
            String title = secretType + "密级重置" + requestStatusStr;
            String content = "操作：重置" + secretType + "密级; 开始时间：" + creatTime + "; 结束时间：" + endTime + "; 结果：" + requestStatusStr + "; 操作人：" + user.getUsername();
            MessageExpandDto messageExpandDto = new MessageExpandDto(requestStatus, SysMessage.REQUEST_TYPE_RESET_SECRET, JSONObject.toJSONString(dto));
            try {
                mqSendMessageService.sendMemberMsg(MqConstants.ASSIGN_MANAGER, null, null, null, Collections.singletonList(user.getId()), null, CommonMqConstants.MessageSceneEnum.USER_MESSAGE, title, content, null, null, null, MqConstants.SEND_SYSTEM_STATION, siteId, messageExpandDto);
            } catch (GlobalException e) {
                e.printStackTrace();
            }
        });

        int time = 0;
        if (!CollectionUtils.isEmpty(dto.getSecretIds())) {
            if (SysSecret.CONTENT_SECRET.equals(dto.getType())) {
                time = (int) (UPDATE_CONTENT_AVG_TIME * dao.getCountBySecretId(dto.getSecretIds(), null));
            }
            if (SysSecret.ANNEX_SECRET.equals(dto.getType())) {
                time = (int) (UPDATE_CONTENT_ATTR_AVG_TIME * contentAttrResService.getCountBySecretId(dto.getSecretIds()));
            }
        }
        if (dto.getModuleId() != null) {
            time = (int) (UPDATE_CONTENT_AVG_TIME * dao.getCountBySecretId(dto.getSecretIds(), dto.getModuleId()));
        }
        return new ResetSecretVo(code, (time / 1000));

    }

    /**
     * 重置内容密级预估平均时间
     */
    private static final int UPDATE_CONTENT_AVG_TIME = 57;
    /**
     * 重置内容多资源对象(附件密级)预估平均时间
     */
    private static final int UPDATE_CONTENT_ATTR_AVG_TIME = 2;

    /**
     * 重置密级操作
     * @param secretIds     密级集合
     * @param mododuleId    模型id
     * @throws GlobalException  全局异常
     */
    private void resetSecret(List<Integer> secretIds,Integer mododuleId) throws GlobalException{
        int page = 1;
        int size = ContentLucene.THREAD_PROCESS_NUM;
        Pageable pageable = PageRequest.of(page, size);
        Page<Content> content = dao.getPageBySecretId(pageable, secretIds,mododuleId);
        int totalPage = content.getTotalPages();
        for (int i = 0; i < totalPage; i++) {
            pageable = PageRequest.of(i, size);
            Page<Content> contents = dao.getPageBySecretId(pageable, secretIds,mododuleId);
            for (Content bean:contents) {
                bean.setSecret(null);
                bean.setContentSecretId(0);
            }
            batchUpdateAll(contents);
            flush();
        }
    }


    /**
    @Override
    public List<Integer> getRecycleIds(Integer siteId) {
        return null;
    }

    /**
	 * 检查权限,只要有一个没有权限的，则直接返回false
	 */
	protected Boolean validType(Short opration, List<Content> contents) {
		CoreUser user = SystemContextUtils.getUser(RequestUtils.getHttpServletRequest());
		Map<Integer, List<Content>> map = contents.stream().collect(Collectors.groupingBy(Content::getChannelId));
		// 判断类型
		for (Entry<Integer, List<Content>> entry : map.entrySet()) {
			List<Short> oprations = user.getContentOperatorByChannelId(entry.getKey());
			// 如果没有权限，直接返回false
			if (!oprations.contains(opration)) {
				return false;
			}
		}
		return true;
	}

    @Override
    public void noquote(OperationDto dto) throws GlobalException {
        List<Integer> contents = dto.getIds();
        // 查询得到内容列表
        List<Content> contentList = super.findAllById(dto.getIds());
        // 得到引用栏目ID集合
        List<Integer> cids = dto.getChannelIds();
        // 检测栏目是否存在该内容的引用
        List<Integer> create = ImmutableList.<Integer>builder().add(ContentConstant.CONTENT_CREATE_TYPE_URL)
                .add(ContentConstant.CONTENT_CREATE_TYPE_MIRROR).build();
        // 判断权限
        for (Content content : contentList) {
            List<Short> oprations = SystemContextUtils.getUser(RequestUtils.getHttpServletRequest())
                    .getContentOperatorByChannelId(content.getChannelId());
            if (!oprations.contains(CmsDataPerm.OPE_CONTENT_QUOTE)) {
                throw new GlobalException(new SystemExceptionInfo(
                        UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(),
                        UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode()));
            }
            for (Integer cid : cids) {
                List<Content> quoteContents = dao.findByChannelIdAndOriContentIdAndCreateTypeIn(cid, content.getId(), create);
                if (!quoteContents.isEmpty()) {
                    physicalDeleteInBatch(quoteContents);
                }
            }
        }
    }

	@Override
	public Content save(ContentSaveDto dto, CmsSite site) throws GlobalException {
        dto.setTplPc(decryptionUtilService.decryChannelOrContentParameter(dto.getTplPc()));
        dto.setTplMobile(decryptionUtilService.decryChannelOrContentParameter(dto.getTplMobile()));
        JSONObject outLinkJson = dto.getOutLink();
        if (dto.isBase64()&&outLinkJson != null) {
            outLinkJson.put(CmsModelConstant.FIELD_SYS_CONTENT_OUTLINK,
                    decryptionUtilService.decryChannelOrContentParameter(outLinkJson.getString(CmsModelConstant.FIELD_SYS_CONTENT_OUTLINK)));
        }
        dto.setOutLink(outLinkJson);
        JSONObject contentSourceJson = dto.getContentSourceId();
        if (contentSourceJson != null) {
            contentSourceJson.put(ContentExt.SOURCE_LINK,
                    decryptionUtilService.decryChannelOrContentParameter(contentSourceJson.getString(ContentExt.SOURCE_LINK)));
        }
        boolean isCheck = false;
        if (ContentConstant.STATUS_PUBLISH == dto.getType()) {
            boolean checkReview = contentReviewService.reviewContentCheck(null, dto.getChannelId(), dto.getModelId());
            if (checkReview) {
                dto.setType(STATUS_SMART_AUDIT);
                isCheck = true;

            }
        }
        if (isCheck) {
            if (!contentReviewService.checkAppIdOrPhone()) {
                dto.setType(ContentConstant.STATUS_PUBLISH);
                isCheck = false;
            }
        }
		Content content = new Content();
		content.setSite(site);
		CmsModel model = cmsModelService.findById(dto.getModelId());
		content.setModel(model);
		Channel channel = channelService.findById(dto.getChannelId());
		content = dto.initContent(dto, content, site, site.getGlobalConfig(), false, channel);
		if (dto.getCreateType() != null) {
			content.setCreateType(dto.getCreateType());
		}
		// 校验传送过来的状态
		ContentInitUtils.checkStatus(content.getStatus(), dto.getType(),
				channel.getRealWorkflowId() != null ? true : false, false);
		ContentExt contentExt = new ContentExt();
		// 初始化tag将传递过来的string数组进行组装成contentTag集合，而后set到content进行级联新增
		List<ContentTag> tags = contentTagService.initTags(dto.getContentTag(), site.getId());
		content.setContentTags(tags);
		// 初始化contentExt对象
		contentExt = this.spliceContentExt(dto, contentExt, site.getId(), false);
		contentExt.setContent(content);
		content.setContentExt(contentExt);
		content.init();
		content.setChannel(channel);
		if (isCheck) {
			content.setCheckMark(String.valueOf(snowFlake.nextId()));
		}
		content.setModelFieldSet(model.getModelField());
		//新增组织ID
        /**采集使用的子线程方式获取当前用户采用参数传递的方式*/
        if(SystemContextUtils.getCoreUser()!=null){
            content.setOrgId(SystemContextUtils.getCoreUser().getOrgId());
        }else{
            content.setOrgId(coreUserService.findById(dto.getUserId()).getOrgId());
        }
		//新增内容默认内容密级为0，为了优化查询需要
		if(dto.getContentSecretId() == null) {
			content.setContentSecretId(0);
		}
		Content bean = save(content);
		// super.flush();
		if (bean.getContentExt().getPicResId() != null) {
			bean.getContentExt().setReData(resourcesSpaceDataService.findById(bean.getContentExt().getPicResId()));
		}

		Map<String, String> txtMap = contentTxtService.initContentTxt(dto.getJson(), dto.getModelId(), dto, false);
		if(dto.isBase64()){
            txtMap = decryptionUtilService.decryChannelOrContentContent(txtMap);
        }
		if (txtMap != null && txtMap.size() > 0) {
			// 存储内容的文本内容需要进行额外处理作为单独的对象进行处理
			List<ContentTxt> contentTxts = ContentInitUtils.toListTxt(txtMap);
			// 初始化contentTxts并执行新增操作
			bean.setContentTxts(contentTxtService.saveTxts(contentTxts, bean));
			if (bean.getStatus().equals(STATUS_PUBLISH)) {
				hotWordService.totalUserCount(bean.getChannelId(), contentTxts, site.getId());
			}
		}
		if (Content.AUTOMATIC_SAVE_VERSION_TRUE.equals(site.getConfig().getContentSaveVersion())) {
			// 此处Map无需处理为空的情况在其具体方法中已经处理了
			contentVersionService.save(txtMap, bean.getId(), null);
			//contentVersionService.flush();
		}
		Integer createType = dto.getCreateType() != null ? dto.getCreateType()
				: ContentConstant.CONTENT_CREATE_TYPE_ADD;
		bean = initAttr(bean, dto.getJson(), dto.getModelId(), bean.getId(), site.getGlobalConfig());
		CoreUser user = coreUserService.findById(bean.getUserId());
		this.initContentObject(bean);
		this.initContentExtObject(bean.getContentExt());
		final Content c = bean;
		ThreadPoolService.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try{
					afterSave(c);
                    ContentRecord contentRecord = new ContentRecord(c.getId(), user.getUsername(), "新增", "", null, c);
                    contentRecordService.save(contentRecord);
				}catch (GlobalException e){
					LOGGER.error(e.getMessage());
				}
			}
		});

        if (isCheck) {
            List<Content> contents = new ArrayList<Content>();
            contents.add(bean);
            ThreadPoolService.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        contentReviewService.reviewContents(contents, dto.getUserId());
                    } catch (GlobalException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
		//基于x1.4的网站统计扩展,记录文章发布数据
		if (ContentConstant.STATUS_PUBLISH == dto.getType() && !isCheck) {
			statisticsContentService.savePublish(bean);
		}
		return bean;
	}

    /**
     * 初始化内容扩展
     */
    private ContentExt spliceContentExt(ContentSaveDto dto, ContentExt contentExt, Integer siteId, boolean isUpdate)
            throws GlobalException {
        contentExt = dto.initContentExt(dto, contentExt, siteId, isUpdate);
        List<CmsModelItem> cmsModelItem = cmsModelItemService.findByModelId(dto.getModelId());
        if (cmsModelItem != null && cmsModelItem.size() > 0) {
            List<String> modelItems = cmsModelItem.stream().map(CmsModelItem::getField).collect(Collectors.toList());
            if (modelItems.contains(CmsModelConstant.FIELD_SYS_CONTENT_SOURCE)) {
                JSONObject contentSourceJson = dto.getContentSourceId();
                if (contentSourceJson != null) {
                    String sourceName = contentSourceJson.getString(ContentExt.SOURCE_NAME);
                    String sourceLink = contentSourceJson.getString(ContentExt.SOURCE_LINK);
                    ContentSource contentSource = contentSourceService.findBySourceName(sourceName);
                    if (contentSource != null) {
                        if (StringUtils.isNotBlank(sourceLink)) {
                            if (!sourceLink.equals(contentSource.getSourceLink())) {
                                contentSource.setSourceLink(sourceLink);
                                contentSourceService.update(contentSource);
                            }
                        } else {
                            contentSource.setSourceLink(sourceLink);
                            contentSourceService.update(contentSource);
                        }
                        contentExt.setContentSourceId(contentSource.getId());
                    } else {
                        if (!StringUtils.isBlank(sourceName)) {
                            ContentSource newSource = new ContentSource();
                            newSource.setIsDefault(false);
                            newSource.setSourceName(sourceName);
                            newSource.setSourceLink(sourceLink);
                            newSource.setIsOpenTarget(false);
                            ContentSource bean = contentSourceService.save(newSource);
                            contentExt.setContentSource(bean);
                            contentExt.setContentSourceId(bean.getId());
                        } else {
                            contentExt.setContentSourceId(null);
                        }
                    }
                }
            } else {
                ContentSource contentSource = contentSourceService.defaultSource();
                contentExt.setContentSourceId(contentSource != null ? contentSource.getId() : null);
            }
        }
        return contentExt;
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ContentFindVo findContent(Integer id, GlobalConfig globalConfig) throws GlobalException {
        Content content = dao.findByIdAndRecycleAndHasDeleted(id, false, false);
        ContentExt contentExt = content.getContentExt();
        ContentFindVo findVo = new ContentFindVo();
        CmsModel model = cmsModelService.getChannelOrContentModel(content.getModelId());
        List<CmsModelItem> modelItems = cmsModelItemService.findByModelId(content.getModelId());
        List<ContentAttr> contentAttrs = content.getContentAttrs();
        List<Integer> cmsOrgIds = new ArrayList<Integer>();
        content.setSite(cmsSiteService.findById(content.getSiteId()));
        for (ContentAttr attr : contentAttrs) {
            if (CmsModelConstant.TISSUE.equals(attr.getAttrType())) {
                cmsOrgIds.add(attr.getOrgId());
            }
        }
        Map<Integer, CmsOrg> cmsOrgMap = null;
        if (cmsOrgIds.size() > 0) {
            List<CmsOrg> cmsOrgs = cmsOrgService.findAllById(cmsOrgIds);
            if (cmsOrgs != null && cmsOrgs.size() > 0) {
                cmsOrgMap = cmsOrgs.stream().collect(Collectors.toMap(CmsOrg::getId, c -> c));
            }
        }
        if (content.getContentExt().getDocResourceId() != null) {
            content.getContentExt()
                    .setDocResource(resourcesSpaceDataService.findById(content.getContentExt().getDocResourceId()));
        } else {
            content.getContentExt().setDocResource(null);
        }
        if (content.getContentExt().getContentSourceId() != null) {
            content.getContentExt()
                    .setContentSource(contentSourceService.findById(content.getContentExt().getContentSourceId()));
        } else {
            content.getContentExt().setContentSource(null);
        }
        if (contentExt.getPicResId() != null) {
            contentExt.setReData(resourcesSpaceDataService.findById(contentExt.getPicResId()));
        } else {
            contentExt.setReData(null);
        }
        ContentCheckDetail detail = null;
        if (StringUtils.isNotBlank(content.getCheckMark())) {
            detail = checkDetailService.findByCheckMark(content.getCheckMark(), null);
        }
        findVo = findVo.spliceContentFindVo(content, globalConfig, model, modelItems,
                content.getContentExt().getContentSource(), cmsOrgMap, detail, checkDetailService.getCheckBanContent(detail, modelItems));
        /** 组装contentFindVo对象 */
        doFillActions(content, findVo, SystemContextUtils.getCoreUser());
        return findVo;
    }



    private Content initContentModelFileSet(Content content, CmsModel cmsModel) {
        Set<CmsModelItem> items = cmsModel.getItems();
        List<String> fileList = items.stream().map(CmsModelItem::getField).collect(Collectors.toList());
        String modelFieldSet = StringUtils.join(fileList);
        content.setModelFieldSet(modelFieldSet);
        return content;
    }

    /**
     * 自定义字段及其资源进行初始化和数据组装
     *
     * @param content
     * @param json
     * @param modelId
     * @param id
     * @param globalConfig
     * @return
     * @throws GlobalException
     */
    private Content initAttr(Content content, JSONObject json, Integer modelId, Integer id, GlobalConfig globalConfig)
            throws GlobalException {
        List<ContentAttr> contentAttrs = contentAttrService.initContentAttr(json, modelId);
        if (contentAttrs.size() > 0) {
            // 部分默认字段的初始化
            contentAttrs = contentAttrService.initContentAttr(contentAttrs, id, globalConfig);
            List<ContentAttr> newContentAttrs = new ArrayList<ContentAttr>();
            for (ContentAttr contentAttr : contentAttrs) {
                ContentAttr attrBean = contentAttrService.save(contentAttr);
                //contentAttrService.flush();
                List<ContentAttrRes> attrRes = contentAttr.getContentAttrRes();
                if (attrRes != null && attrRes.size() > 0) {
                    for (ContentAttrRes attrRe : attrRes) {
                        attrRe.setContentAttr(attrBean);
                        attrRe.setContentAttrId(attrBean.getId());
                    }
                    List<ContentAttrRes> newAttrRes = contentAttrResService.saveAll(attrRes);
                    //contentAttrResService.flush();
                    attrBean.setContentAttrRes(newAttrRes);
                }
                newContentAttrs.add(attrBean);
            }
            if (newContentAttrs.size() > 0) {
                content.setContentAttrs(newContentAttrs);
            }
        }
        return content;
    }

    private boolean getNeedYunCheck(ContentUpdateDto dto, Content content) throws GlobalException {
        boolean isCheck = false;
        Integer oldType = dto.getType();
        if (dto.getForce() != null) {
            if (!dto.getForce()) {
                if (ContentConstant.STATUS_PUBLISH == dto.getType()
                        || ContentConstant.STATUS_WAIT_PUBLISH == dto.getType()
                        || ContentConstant.STATUS_FLOWABLE == dto.getType()) {

                    boolean checkReview = false;
                    if (ContentConstant.STATUS_SMART_AUDIT != content.getStatus()) {
                        checkReview = contentReviewService.reviewContentCheck(content, dto.getChannelId(), content.getModelId());
                    }
                    if (checkReview) {
                        dto.setType(STATUS_SMART_AUDIT);
                        isCheck = true;

                    }
                }

            }
        } else {
            if (ContentConstant.STATUS_PUBLISH == dto.getType()
                    || ContentConstant.STATUS_WAIT_PUBLISH == dto.getType()
                    || ContentConstant.STATUS_FLOWABLE == dto.getType()) {
                boolean checkReview = false;
                if (ContentConstant.STATUS_SMART_AUDIT != content.getStatus()) {
                    checkReview = contentReviewService.reviewContentCheck(content, dto.getChannelId(), content.getModelId());
                }
                if (checkReview) {
                    dto.setType(STATUS_SMART_AUDIT);
                    isCheck = true;
                }
            }
        }
        if (isCheck) {
            if (!contentReviewService.checkAppIdOrPhone()) {
                dto.setType(oldType);
                isCheck = false;
            }
        }
        return isCheck;
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Content update(ContentUpdateDto dto, HttpServletRequest request) throws GlobalException {
        dto.setTplPc(decryptionUtilService.decryChannelOrContentParameter(dto.getTplPc()));
        dto.setTplMobile(decryptionUtilService.decryChannelOrContentParameter(dto.getTplMobile()));
        JSONObject outLinkJson = dto.getOutLink();
        if (outLinkJson != null) {
            outLinkJson.put(CmsModelConstant.FIELD_SYS_CONTENT_OUTLINK,
                    decryptionUtilService.decryChannelOrContentParameter(outLinkJson.getString(CmsModelConstant.FIELD_SYS_CONTENT_OUTLINK)));
        }
        dto.setOutLink(outLinkJson);
        JSONObject contentSourceJson = dto.getContentSourceId();
        if (contentSourceJson != null) {
            contentSourceJson.put(ContentExt.SOURCE_LINK,
                    decryptionUtilService.decryChannelOrContentParameter(contentSourceJson.getString(ContentExt.SOURCE_LINK)));
        }
        Content content = findById(dto.getId());
		content.setModel(cmsModelService.findById(content.getModelId()));
		boolean isCheck = getNeedYunCheck(dto,content);
		dto.setModelId(content.getModelId());
		ContentExt contentExt = content.getContentExt();
		List<Map<String, Object>> mapList = this.preChange(content);
		final SpliceCheckUpdateDto oldDto = this.initSpliceCheckUpdateDto(content);
		CmsSite site = SystemContextUtils.getSite(request);
		GlobalConfig globalConfig = SystemContextUtils.getGlobalConfig(request);
		// 如果要修改的内容的状态为已发布，且站点 发布的内容不允许编辑，则抛出异常
		if (content.getStatus() == ContentConstant.STATUS_PUBLISH) {
			if (!site.getCmsSiteCfg().getContentCommitAllowUpdate()) {
				throw new GlobalException(new SystemExceptionInfo(
						ContentErrorCodeEnum.PUBLISHED_CONTENT_CANNOT_BE_EDITED.getDefaultMessage(),
						ContentErrorCodeEnum.PUBLISHED_CONTENT_CANNOT_BE_EDITED.getCode()));
			}
		}
		dto.setModelId(content.getModelId());
		Channel channel = channelService.findById(dto.getChannelId());
		// 校验传送过来的状态
		ContentInitUtils.checkStatus(content.getStatus(), dto.getType(), channel.getRealWorkflowId() != null, true);
		content = dto.initContent(dto, content, site, globalConfig, true, channel);
		contentTagService.deleteTagQuote(content.getContentTags(), site.getId(), null);
		List<ContentTag> tags = contentTagService.initTags(dto.getContentTag(), site.getId());
		content.setContentTags(tags);
		contentExt = this.spliceContentExt(dto, contentExt, site.getId(), true);
		contentExt.setContent(content);
		content.setContentExt(contentExt);
		content.setContentTxts(null);
		//修改内容密级
		if (dto.getContentSecretId() == null) {
			content.setContentSecretId(0);
		} else {
			content.setContentSecretId(dto.getContentSecretId());
		}
		update(content);
		contentAttrService.deleteByContent(content.getId());
		contentAttrService.flush();
		content = initAttr(content, dto.getJson(), dto.getModelId(), dto.getId(), globalConfig);

		if (isCheck) {
			content.setCheckMark(String.valueOf(snowFlake.nextId()));
		}

        Map<String, String> txtMap = contentTxtService.initContentTxt(dto.getJson(), dto.getModelId(), dto, true);
		txtMap = decryptionUtilService.decryChannelOrContentContent(txtMap);
        content.setModelFieldSet(cmsModelService.findById(content.getModelId()).getModelField());
		//修改内容密级
		if (dto.getContentSecretId() == null) {
			content.setContentSecretId(0);
		} else {
			content.setContentSecretId(dto.getContentSecretId());
		}
		Content bean = updateAll(content);
        super.flush();
        bean = findById(bean.getId());
        if (txtMap != null && txtMap.size() > 0) {
            List<ContentTxt> contentTxts = ContentInitUtils.toListTxt(txtMap);
            contentTxtService.deleteTxts(bean.getId());
            // 初始化contentTxts并执行新增操作
            bean.setContentTxts(contentTxtService.saveTxts(contentTxts, bean));
            if (bean.getStatus().equals(STATUS_PUBLISH)) {
                hotWordService.totalUserCount(bean.getChannelId(), contentTxts, site.getId());
            }
        } else {
            contentTxtService.deleteTxts(bean.getId());
            if (CollectionUtils.isEmpty(bean.getContentTxts())) {
                bean.setContentTxts(new ArrayList<>());
            } else {
                bean.getContentTxts().clear();
            }
        }
        contentTxtService.flush();
        if (Content.AUTOMATIC_SAVE_VERSION_TRUE.equals(site.getConfig().getContentSaveVersion())) {
            contentVersionService.save(txtMap, content.getId(), null);
        }
        this.initContentObject(bean);
        this.initContentExtObject(contentExt);
        this.afterChange(bean, mapList);
        SpliceCheckUpdateDto newDto = this.initSpliceCheckUpdateDto(bean);
        this.checkUpdate(oldDto, newDto, globalConfig, bean, SystemContextUtils.getUserId(request));
        contentExtService.update(contentExt);
        contentExtService.flush();

        if (isCheck) {
            List<Content> contents = new ArrayList<Content>();
            contents.add(bean);
            ThreadPoolService.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        contentReviewService.reviewContents(contents, dto.getUserId());
                    } catch (GlobalException e) {
                        e.printStackTrace();
                    }
                }
            });
		}
		//基于x1.4的网站统计扩展,记录文章发布数据
		if (!isCheck && content.getStatus() == ContentConstant.STATUS_PUBLISH) {
			statisticsContentService.savePublish(content);
		}
		return bean;
	}

    @Override
    public Content update(Content bean) throws GlobalException {
        bean = super.update(bean);
        setContentListPos(bean);
        return bean;
    }

    @Override
    public Content updateAll(Content bean) throws GlobalException {
        bean = super.updateAll(bean);
        setContentListPos(bean);
        return bean;
    }

    @Override
    public Iterable<Content> batchUpdate(Iterable<Content> entities) throws GlobalException {
        Iterable<Content> contents = super.batchUpdate(entities);
        for (Content c : contents) {
            setContentListPos(c);
        }
        return contents;
    }

    @Override
    public Content save(Content bean) throws GlobalException {
        setContentListPos(bean);
        bean = super.save(bean);
        return bean;
    }

    /**
     * 根据内容状态设置内容列表位置值
     *
     * @param bean
     * @return
     */
    private Content setContentListPos(Content bean) {
        //暂存状态
        if (bean.getStatus().equals(STATUS_TEMPORARY_STORAGE)) {
            bean.setPos(POS_MEMBER_SAVE);
        }
        /**小于10内容列表中数据*/
        else if (bean.getStatus() < STATUS_SMART_AUDIT) {
            bean.setPos(POS_CONTENT_LIST);
        } else if (bean.getStatus() <= STATUS_SMART_AUDIT_FAILURE) {
            bean.setPos(POS_AUDIT);
        } else if (bean.getStatus() == STATUS_PIGEONHOLE) {
            bean.setPos(POS_PIGEONHOLE);
        } else {
            bean.setPos(POS_CONTENT_LIST);
        }
        if (bean.getRecycle() != null && bean.getRecycle()) {
            bean.setPos(POS_RECYCLE);
        }
        return bean;
    }

    /**
     * 初始化spliceCheckUpdateDto
     */
    @Override
    public SpliceCheckUpdateDto initSpliceCheckUpdateDto(Content content) {
        SpliceCheckUpdateDto spDto = new SpliceCheckUpdateDto();
        Map<String, String> txtMap = ContentInitUtils.toMapTxt(content.getContentTxts());
        if (content.getChannelId() != null) {
            Channel channel = channelService.findById(content.getChannelId());
            content.setChannel(channel);
        }
        if (content.getContentSecretId() != null) {
            SysSecret secret = secretService.findById(content.getContentSecretId());
            content.setSecret(secret);
        }
        spDto.initSpliceCheckUpdateDto(content, spDto, txtMap);
        return spDto;
    }

    /**
     * 获取到操作记录中的备注
     */
    @Override
    public void checkUpdate(SpliceCheckUpdateDto oldUpdateDto, SpliceCheckUpdateDto newUpdateDto,
                            GlobalConfig globalConfig, Content bean, Integer userId) throws GlobalException {
        HibernateProxyUtil.loadHibernateProxy(oldUpdateDto);
        HibernateProxyUtil.loadHibernateProxy(oldUpdateDto.getContentAttrs());
        HibernateProxyUtil.loadHibernateProxy(oldUpdateDto.getContentTags());
        HibernateProxyUtil.loadHibernateProxy(oldUpdateDto.getTxts());

        ThreadPoolService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                SystemContextUtils.setCoreUser(coreUserService.findById(userId));
                try {
                    contentRecordService.checkUpdate(oldUpdateDto, newUpdateDto, globalConfig, bean);
                } catch (GlobalException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void submit(ContentUpdateDto dto, HttpServletRequest request, boolean contribute, Content content)
            throws GlobalException {
        if (!contribute) {
            Integer userId = SystemContextUtils.getUserId(request);
            dto.setUserId(userId);
            dto.setPublishUserId(userId);
            if (dto.getId() != null) {
                content = update(dto, request);
            } else {
                content = save(dto, SystemContextUtils.getSite(request));
            }
        }
        doSubmitFlow(content);
    }

    @Override
    public void copy(ContentCopyDto dto, HttpServletRequest request, CmsSiteConfig siteConfig) throws GlobalException {
        GlobalConfig globalConfig = globalConfigService.getGlobalConfig();
        Channel channel = channelService.findById(dto.getChannelId());
        // 从站点中取出默认设置
        List<String> contentTitles = null;
        // 校验栏目内标题是否允许重复
        if (siteConfig.getTitleRepeat().equals(3)) {
            /*
             * 此处可以使用索引进行判断，但是可能一次性复制的内容过多比如200，那么每次都去查询一次索引不如直接将其全部查询出来，用大量的空间换取下次校验的时间
             */
            contentTitles = dao.findContentTitles(channel.getId());
        }
        if(SystemContextUtils.getCoreUser()!=null){
            dto.setCurrUsername(SystemContextUtils.getUser(request).getUsername());
        }
        for (int i = 0; i < dto.getIds().size(); i++) {
            Content content = findById(dto.getIds().get(i));
            if (!CollectionUtils.isEmpty(contentTitles)) {
                if (contentTitles.contains(content.getTitle())) {
                    throw new GlobalException(ContentErrorCodeEnum.CONTENT_TITLE_IS_NOT_ALLOWED_TO_REPEAT);
                }
                contentTitles.add(content.getTitle());
            }
            SysSecret secret = null;
            if (content.getContentSecretId() != null) {
                secret = secretService.findById(content.getContentSecretId());
            }
            // 撰写管理员为空那么之前所有的逻辑都有问题了
            CoreUser user = coreUserService.findById(content.getUserId());
            Content newContent = ContentCopyDto.initCopyContent(content, globalConfig, channel, secret, user);
            ContentExt contentExt = content.getContentExt();
            ContentExt newContentExt = ContentInitUtils.initCopyContentExt(contentExt, content.getSiteId(),
                    contentExt.getSueOrg(), contentExt.getSueYear(), contentExt.getContentSource(),
                    contentExt.getReData());
            newContent.setContentExt(newContentExt);
            newContent.setContentTxts(null);
            List<ContentTag> newTags = new ArrayList<>();
            // tags无需特意进行修改，因为进行set操作后会自动进行update操作
            List<ContentTag> contentTags = content.getContentTags();
            if (!CollectionUtils.isEmpty(contentTags)) {
                for (ContentTag contentTag : contentTags) {
                    contentTag.setRefCounter(contentTag.getRefCounter() + 1);
                }
                newTags = (List<ContentTag>) contentTagService.batchUpdateAll(contentTags);
            }
            super.flush();
            newContentExt.setContent(newContent);
            List<Content> contentCopys = new ArrayList<>();
            contentCopys.add(content);
            newContent.setContentCopys(contentCopys);
            newContent.setModelFieldSet(cmsModelService.findById(newContent.getModelId()).getModelField());
            newContent.setCreateUser(dto.getCurrUsername());
            Content bean = save(newContent);
            super.flush();
            bean.setContentTags(newTags);
            List<ContentTxt> contentTxt = ContentCopyDto.copyInitTxt(content.getContentTxts());
            List<ContentTxt> newContentTxt = contentTxtService.saveTxts(contentTxt, bean);
            bean.setContentTxts(newContentTxt);
            List<ContentAttr> contentAttrs = contentAttrService.copyInitContentAttr(content.getContentAttrs(),
                    bean.getId());
            bean = contentAttrService.copySaveContentAttr(contentAttrs, bean);
            if (Content.AUTOMATIC_SAVE_VERSION_TRUE
                    .equals(siteConfig.getContentSaveVersion())) {
                contentVersionService.save(ContentInitUtils.toMapTxt(bean.getContentTxts()), bean.getId(), null);
            }
            ContentRecord contentRecord = new ContentRecord(bean.getId(),dto.getCurrUsername(), "复制",
                    null, null, bean);
            contentRecordService.save(contentRecord);
            this.initContentObject(bean);
            this.initContentExtObject(bean.getContentExt());
            this.afterSave(bean);
        }
    }

    @Override
    public void quote(OperationDto dto) throws GlobalException {
        List<Integer> create = ImmutableList.<Integer>builder().add(ContentConstant.CONTENT_CREATE_TYPE_URL)
                .add(ContentConstant.CONTENT_CREATE_TYPE_MIRROR).build();
        GlobalConfig globalConfig = globalConfigService.getGlobalConfig();
        for (int i = 0; i < dto.getIds().size(); i++) {
            Content content = findById(dto.getIds().get(i));
            SysSecret secret = null;
            if (content.getContentSecretId() != null) {
                secret = secretService.findById(content.getContentSecretId());
            }
            // 撰写管理员为空那么之前所有的逻辑都有问题了
            CoreUser user = coreUserService.findById(content.getUserId());
            for (Integer cid : dto.getChannelIds()) {
                Channel channel = channelService.findById(cid);
                // 检测栏目是否存在该内容的引用
                if (dao.getCount(cid, content.getId(), create) > 0) {
                    throw new GlobalException(ContentErrorCodeEnum.ALREADY_QUOTE_CHANNEL_CONTENT);
                }
                Content newContent = ContentCopyDto.initCopyContent(content, globalConfig, channel, secret, user);
                ContentExt contentExt = content.getContentExt();
                ContentExt newContentExt = ContentInitUtils.initCopyContentExt(contentExt, content.getSiteId(),
                        contentExt.getSueOrg(), contentExt.getSueYear(), contentExt.getContentSource(),
                        contentExt.getReData());
                newContent.setContentExt(newContentExt);
                newContent.setContentTxts(null);
                super.flush();
                newContentExt.setContent(newContent);
                newContent.setModelFieldSet(cmsModelService.findById(newContent.getModelId()).getModelField());
                if (dto.getCreateType().equals(1)) {
                    newContent.setCreateType(ContentConstant.CONTENT_CREATE_TYPE_URL);
                } else {
                    newContent.setCreateType(ContentConstant.CONTENT_CREATE_TYPE_MIRROR);
                }
                newContent.setCopySourceContentId(null);
                newContent.setOriContentId(content.getId());
                newContent.setOriContent(content);
                Content bean = save(newContent);
                //content.getQuoteContents().add(bean);
                super.flush();
                this.initContentObject(bean);
                this.initContentExtObject(bean.getContentExt());
                this.afterSave(bean);
            }
        }
    }

	@Override
	public boolean checkTitle(String title, Integer channelId, Integer siteId) {
		Long contentIdConunt = contentLuceneService.searchCount(title, SearchPosition.title, channelId, siteId, true,
				ContentConstant.getNotAllowRepeatStatus());
        return contentIdConunt > 0;
    }



//    @Override
//    public ResponseInfo recoveryVersion(ContentVersion version, Integer contentId) throws GlobalException {
//        Content content = super.findById(contentId);
//        if (content == null) {
//            return new ResponseInfo();
//        }
//        if (!this.validType(CmsDataPerm.OPE_CONTENT_EDIT, null, content)) {
//            return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
//                    UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
//        }
//
//        JSONObject jsonTxt = version.getJsonTxt();
//        List<ContentTxt> contentTxts = new ArrayList<ContentTxt>();
//        List<CmsModelItem> cmsModelItem = cmsModelItemService.findByModelIdAndDataType(content.getModelId(),
//                CmsModelConstant.CONTENT_TXT);
//        for (CmsModelItem modelItem : cmsModelItem) {
//            String field = modelItem.getField();
//            String txt = jsonTxt.getString(field);
//            if (StringUtils.isNotBlank(txt)) {
//                ContentTxt contentTxt = new ContentTxt();
//                contentTxt.setAttrKey(field);
//                contentTxt.setAttrTxt(txt);
//                contentTxts.add(contentTxt);
//            }
//        }
//        if (contentTxts.size() > 0) {
//            contentTxtService.deleteTxts(content.getId());
//            // 初始化contentTxts并执行新增操作
//            content.setContentTxts(contentTxtService.saveTxts(contentTxts, content));
//        }
//        return new ResponseInfo();
//    }
	@Override
	public synchronized void recoveryVersion(Integer versionId,Integer contentId) throws GlobalException {
		ContentVersion version = contentVersionService.findById(versionId);
        Content content = findById(contentId);
	    JSONObject jsonTxt = version.getJsonTxt();
		List<ContentTxt> contentTxts = new ArrayList<>();
		List<CmsModelItem> cmsModelItem = cmsModelItemService.findByModelIdAndDataType(content.getModelId(),
				CmsModelConstant.CONTENT_TXT);
		for (CmsModelItem modelItem : cmsModelItem) {
			String field = modelItem.getField();
			String txt = jsonTxt.getString(field);
			if (StringUtils.isNotBlank(txt)) {
				ContentTxt contentTxt = new ContentTxt();
				contentTxt.setAttrKey(field);
				contentTxt.setAttrTxt(txt);
				contentTxts.add(contentTxt);
			}
		}
		if (!CollectionUtils.isEmpty(contentTxts)) {
			contentTxtService.deleteTxts(content.getId());
			// 初始化contentTxts并执行新增操作
			content.setContentTxts(contentTxtService.saveTxts(contentTxts, content));
		}
	}

    @Override
    public Boolean validType(Short opration, List<Content> contents, Content content) {
        if (contents == null) {
            contents = new ArrayList<Content>();
        }
        if (content != null) {
            contents.add(content);
        }
        return this.validType(opration, contents);
    }

    @Override
    public Boolean validType(Short opration, Integer channId) {
        CoreUser user = SystemContextUtils.getUser(RequestUtils.getHttpServletRequest());
        // 判断类型
        List<Short> oprations = user.getContentOperatorByChannelId(channId);
        // 有权限
        if (oprations.contains(opration)) {
            return true;
        }
        // 没有权限
        return false;
    }

    @Override
    public List<Content> findByChannels(Integer[] channelIds) {
        return dao.findByChannelIdInAndHasDeleted(channelIds, false);
    }

    @Override
    public long countByChannelIdInAndRecycle(Integer[] channelIds, boolean recycle) {
        return dao.countByChannelIdInAndRecycleAndHasDeleted(channelIds, recycle, false);
    }

    @Override
    public void pushSites(ContentPushSitesDto dto, HttpServletRequest request,GlobalConfig globalConfig,CmsSite site) throws GlobalException {
        List<Content> contents = dto.getContents();
        Channel channel = dto.getChannel();
        List<CmsModel> models = cmsModelService.findList(CmsModel.CONTENT_TYPE, dto.getSiteId());
        if(SystemContextUtils.getCoreUser()!=null){
            dto.setCurrUsername(SystemContextUtils.getUser(request).getUsername());
        }
        for (Content content : contents) {
            Content newContent = ContentPushSitesDto.initContent(content, globalConfig, channel, dto.getSiteId(),
                    models, content.getSecret(), content.getUser());
            ContentExt contentExt = content.getContentExt();
            ContentExt newContentExt = ContentInitUtils.initCopyContentExt(contentExt, content.getSiteId(),
                    contentExt.getSueOrg(), contentExt.getSueYear(), contentExt.getContentSource(),
                    contentExt.getReData());
            newContent.setContentExt(newContentExt);
            List<String> itemFiles = cmsModelItemService.findByModelId(newContent.getModelId()).stream()
                    .map(CmsModelItem::getField).collect(Collectors.toList());
            if (itemFiles.contains(CmsModelConstant.FIELD_SYS_CONTENT_CONTENTTAG)) {
                // tags无需特意进行修改，因为进行set操作后会自动进行update操作
                List<ContentTag> newTags = new ArrayList<>();
                List<ContentTag> contentTags = content.getContentTags();
                if (!CollectionUtils.isEmpty(contentTags)) {
                    for (ContentTag contentTag : contentTags) {
                        contentTag.setRefCounter(contentTag.getRefCounter() + 1);
                    }
                    newTags = (List<ContentTag>) contentTagService.batchUpdateAll(contentTags);
                }
                newContent.setContentTags(newTags);
            }
            newContentExt.setContent(newContent);
            newContent.setCreateUser(dto.getCurrUsername());
            Content bean = save(newContent);
            super.flush();
            List<ContentTxt> contentTxt = ContentCopyDto.copyInitTxt(content.getContentTxts());
            List<ContentTxt> newTxt = new ArrayList<>();
            for (ContentTxt txt : contentTxt) {
                if (itemFiles.contains(txt.getAttrKey())) {
                    newTxt.add(txt);
                }
            }
            List<ContentTxt> newContentTxt = contentTxtService.saveTxts(newTxt, bean);
            bean.setContentTxts(newContentTxt);
            List<ContentAttr> contentAttrs = contentAttrService.pushSiteInitContentAttr(content.getContentAttrs(),
                    bean.getId(), itemFiles, globalConfig);
            bean = contentAttrService.copySaveContentAttr(contentAttrs, bean);
            if (Content.AUTOMATIC_SAVE_VERSION_TRUE
                    .equals(site.getConfig().getContentSaveVersion())) {
                contentVersionService.save(ContentInitUtils.toMapTxt(bean.getContentTxts()), bean.getId(), null);
            }
            ContentRecord contentRecord = new ContentRecord(bean.getId(), dto.getCurrUsername(), "推送",
                    null, null, bean);
            contentRecordService.save(contentRecord);
            this.initContentObject(bean);
            this.initContentExtObject(bean.getContentExt());
            this.afterSave(bean);
        }
    }

    @Override
    public ResponseInfo preview(WechatViewDto dto) throws GlobalException {
        //剩余群发次数
        Integer number = 1;
        List<WechatPushVo> vos = new ArrayList<WechatPushVo>(8);
        if (!dto.getContentIds().isEmpty()) {
            if (dto.getContentIds().size() > 8) {
                return new ResponseInfo(RPCErrorCodeEnum.NO_MORE_THAN_PUSHED.getDefaultMessage(),
                        RPCErrorCodeEnum.NO_MORE_THAN_PUSHED.getCode(), false);
            }
            // 判断今日群发次数
            List<WechatSend> send = sendService.listWechatSend(Arrays.asList(dto.getAppid()),
                    MyDateUtils.getStartDate(new Date()), MyDateUtils.getFinallyDate(new Date()));
            // 判断月群发次数是否超过微信限制
            Boolean flag = sendService.service(dto.getAppid(), new Date());
            //只有满足今日已群发或者月群发次数已满，就把剩余次数置为0
            if (!send.isEmpty() || !flag) {
                number = 0;
            }
            List<Content> contents = super.findAllById(dto.getContentIds());
            for (Content content : contents) {
                if (!content.getWechatPushContentAble()) {
                    return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                            UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
                }
                WechatPushVo vo = new WechatPushVo();
                StringBuilder builder = new StringBuilder();
                List<ContentTxt> txts = content.getContentTxts();
                for (ContentTxt contentTxt : txts) {
                    builder.append(contentTxt.getAttrTxt());
                }
                vo.setSengNumber(number);
                vo.setContent(builder.toString());
                // 设置封面
                ResourcesSpaceData data = content.getContentExt().getReData();
                if (data != null) {
                    vo.setPicId(data.getId());
                    vo.setCover(data.getUrl());
                }
                vo.setTitle(content.getTitle());
                vo.setAuthor(content.getContentExt().getAuthor());
                // 原创链接
                vo.setSourceUrl(content.getUrlWhole());
                vo.setAppId(dto.getAppid());
                vos.add(vo);
            }
        }
        return new ResponseInfo(vos);
    }

	@Override
	public ResponseInfo push(WechatPushDto dto) throws Exception {
		List<WechatPushVo> vos = dto.getVo();
		if (!vos.isEmpty()) {
			List<Integer> contentdIds = new ArrayList<>(16);
			AddNewsRequest addNewsRequest = new AddNewsRequest();
			List<SaveArticles> articles = new ArrayList<>(8);
			// 构造新增素材
			for (WechatPushVo vo : vos) {
				contentdIds.add(vo.getContentId());
				SaveArticles art = new SaveArticles();
				// 封面素材ID
				art.setThumbMediaId(material(dto.getAppid(), vo.getPicId()));
				art.setTitle(vo.getTitle());
				art.setAuthor(vo.getAuthor());
				art.setContent(replaceImage(vo.getContent(), dto.getAppid()));
				art.setContentSourceUrl(vo.getSourceUrl());
				art.setShowCoverPic(0);
				// 判断留言
//				if (dto.getMessage().equals(WechatPushDto.MESSAGE_1)) {
//					art.setNeedOpenComment(1);
//					art.setOnlyFansCanComment(0);
//				} else if (dto.getMessage().equals(WechatPushDto.MESSAGE_2)) {
//					art.setNeedOpenComment(1);
//					art.setOnlyFansCanComment(1);
//				} else {
//					art.setNeedOpenComment(0);
//				}
				articles.add(art);
			}
			addNewsRequest.setArticles(articles);
			// 上传素材
			WechatMaterial material = wechatMaterialService.saveNews(addNewsRequest, dto.getAppid());
			// 构造群发对象
			WechatSend wechatSend = new WechatSend();
			//绩效新增字段赋值
			wechatSend.setUserId(dto.getUserId());
			wechatSend.setSiteId(dto.getSiteId());
			wechatSend.setContentIds(contentdIds.toString());
			wechatSend.setAppId(dto.getAppid());
			wechatSend.setMaterialId(material.getId());
			// 发送对象
			if (dto.getTagId() != null) {
				wechatSend.setTagId(dto.getTagId());
			}
			if (dto.getType().equals(WechatPushDto.TYPE_1)) {
				// 立即发送
				wechatSend.setType(WechatConstants.SEND_TYPE_NOW);
				return wechatSendService.send(wechatSend);
			} else {
				// 定时发送
				wechatSend.setSendDate(dto.getSendDate());
				wechatSend.setSendHour(dto.getSendHour());
				wechatSend.setSendMinute(dto.getSendMinute());
				return wechatSendService.saveWechatSend(wechatSend);
			}
		}
		return new ResponseInfo();
	}

	/**
	 * 替换内容中的图片地址
	 * @param content 内容
	 * @return String
	 */
	private String replaceImage(String content, String appId) throws GlobalException, IOException {
		String host = "mmbiz.qpic.cn";
		//得到当前站点
		CmsSite site = SystemContextUtils.getSite(Objects.requireNonNull(RequestUtils.getHttpServletRequest()));
		String webUrl = site.getUrlWhole();
		Element doc = Jsoup.parseBodyFragment(content).body();
		Elements pngs = doc.select("img[src]");
		for (Element png : pngs) {
			// 获取到图片url
			String imgUrl = png.attr("src");
			// 没有匹配到正则则进行数据处理
			imgUrl = this.processImgUrlFullPath(imgUrl, webUrl);
			//如果是微信自己的图片则跳过
			URL url = new URL(imgUrl);
			if (host.equals(url.getHost())) {
				continue;
			}
			UploadImgRequest uploadImgRequest = new UploadImgRequest();
			File file = downImage(imgUrl);
			if (file != null) {
				uploadImgRequest.setFile(file);
				String wechatUrl = wechatMaterialService.uploadImg(uploadImgRequest, appId);
				png.attr("src", wechatUrl);
				file.delete();
			}

		}
		return doc.html();
	}

    /**
     * 补全路径
     */
    private String processImgUrlFullPath(String imgUrl, String webUrl) {
        // 校验图片URL的正则表达式
        String CHECK_IMG_URL_REGULAR = "^((https|http)?://)";
        // 判断图片是否存在http开头(存在http开头说明不需要添加上网址url)
        Pattern p = Pattern.compile(CHECK_IMG_URL_REGULAR);
        Matcher matcher = p.matcher(imgUrl);
        if (!matcher.find()) {
            webUrl = webUrl.substring(0, webUrl.length() - 1);
            imgUrl = webUrl + imgUrl;
        }
        return imgUrl;
    }

	/**
	 * 下载图片
	 * @param url 内容url
	 * @return File
	 * @throws IOException 异常
	 */
	private File downImage(String url) throws IOException {
		InputStream input;
		CloseableHttpResponse httpResponse;
		HttpEntity responseEntity;
		// 获得Http客户端
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		// 创建Get请求
		HttpGet httpGet = new HttpGet(url);
		// 响应模型
		// 由客户端执行(发送)Get请求
		httpResponse = httpClient.execute(httpGet);
		Header[] heads = httpResponse.getHeaders("Content-Type");
		//需要处理没有后缀的url,直接随便生成
		// 获取文件后缀
		String prefix = ".jpg";
		// 获取文件名
		String fileName = cn.hutool.core.lang.UUID.randomUUID().toString();
		if (heads != null && heads.length != 0) {
			for (Header head : heads) {
				if(StringUtils.isNotBlank(head.getValue())) {
					String value = head.getValue();
					//不是图片直接略过
					if(!value.contains("image")) {
						return null;
					}
					if (value.contains("png")) {
						prefix = ".png";
					} else if (value.contains("gif")) {
						prefix = ".gif";
					}
				}
			}
		}
		final File file = File.createTempFile(fileName, prefix);
		// 从响应模型中获取响应实体
		responseEntity = httpResponse.getEntity();
		if (responseEntity != null && HttpServletResponse.SC_OK == httpResponse.getStatusLine()
				.getStatusCode()) {
			input = responseEntity.getContent();
			org.apache.commons.io.FileUtils.copyInputStreamToFile(input, file);
		}
		return file;
	}

    /**
     * 得到微信素材ID
     **/
    public String material(String appId, Integer picId) throws Exception {
        if (picId == null) {
            throw new GlobalException(RPCErrorCodeEnum.PUSH_ERROR_IMAGE_NOT_NULL);
        }
        ResourcesSpaceData data = resourcesSpaceDataService.findById(picId);
        if (data == null) {
            throw new GlobalException(RPCErrorCodeEnum.PUSH_ERROR_IMAGE_NOT_NULL);
        }
        File file = resourcesSpaceDataService.getFileWithFilename(data, null);
        AddMaterialRequest addMaterialRequest = new AddMaterialRequest();
        addMaterialRequest.setFileName(data.getAlias());
        addMaterialRequest.setType("image");
        WechatMaterial material = wechatMaterialService.saveMaterial(addMaterialRequest, appId, file);
        // 执行业务之后删除临时文件
        file.delete();
        return material.getMediaId();
    }

    @Override
    public void afterChannelChange(Channel c) throws GlobalException {
    }

    @Override
    public void afterChannelRecycle(List<Channel> channels) throws GlobalException {
        // 栏目加入回收站后对应的所有内容也全部加入回收站
        Integer[] cids = new Integer[channels.size()];
        Channel.fetchIds(channels).toArray(cids);
        ThreadPoolService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<Content> contents = dao.findByChannelIdInAndHasDeleted(cids, false);
                if (contents.size() > 0) {
                    try {
                        for (Content content : contents) {
                            /**设置引用到了哪些目标内容  这样方便取应用到了哪些栏目*/
                            content.setQuoteContents(dao.findByOriContentIdAndRecycleAndHasDeleted(content.getId(), false, false));
                            content.setRecycle(true);
                            /**引用内容同步改状态*/
                            for (Content c : content.getQuoteContents()) {
                                c.setRecycle(true);
                                update(c);
                            }
                        }
                        batchUpdate(contents);
                        List<Integer> contentIds = contents.stream().map(Content::getId).collect(Collectors.toList());
                        for (ContentListener listener : listenerList) {
                            listener.afterContentRecycle(contentIds);
                        }
                    } catch (GlobalException e) {
                    }
                }
            }
        });
    }

    @Override
    public String docImport(MultipartFile file, Integer type) throws Exception {
        // 得到文件名称
        String fileName = FileUtils.getFileName(Objects.requireNonNull(file.getOriginalFilename()));
        //判断上传文档是否存在中文
        if (checkChinese(fileName)) {
            fileName = UUID.randomUUID().toString();
        }
        // 得到文件后缀,带.
        String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        // 创建临时文件，接收上传的文件，后缀写死
        File changeFile = File.createTempFile(fileName + StrUtils.getRandStr(6), ext);
        file.transferTo(changeFile);
        // 后缀写死
        File tempFile = File.createTempFile(fileName + StrUtils.getRandStr(6), ".html");
        String html = openOfficeConverter.convertToHtmlString(changeFile, tempFile);
        // 替换
        html = replaceImg(html, tempFile.getParent());
        // 删除临时文件
        changeFile.delete();
        tempFile.delete();
        if (type.equals(ContentConstant.IMPORT_TYPE_2)) {
            // 清除格式
            return Doc2Html.clearStyle(html);
        } else if (type.equals(ContentConstant.IMPORT_TYPE_3)) {
            // 仅导入文字
            return Doc2Html.onlyWords(html);
        } else {
            // 导入方式，直接导入（不做任何处理）
            return html;
        }
    }

    /**
     * 判断该字符串是否存在中文
     *
     * @param name 字符串
     * @return boolean
     */
    private boolean checkChinese(String name) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(name);
        return m.find();
    }

    /**
     * 替换文件路径以及上传
     *
     * @param html html片段
     * @throws IOException     异常
     * @throws GlobalException 异常
     * @Title: replaceImg
     */
    public String replaceImg(String html, String path) throws IOException, GlobalException {
        List<ResourcesSpaceData> datas = new ArrayList<>(10);
        CmsSite site = SystemContextUtils.getSite(Objects.requireNonNull(RequestUtils.getHttpServletRequest()));
        Document doc = Jsoup.parse(html);
        // 获取 带有src属性的img元素
        Elements imgTags = doc.select("img[src]");
        Set<File> set = new HashSet<>();
        for (org.jsoup.nodes.Element element : imgTags) {
            // 获取src的绝对路径
            String src = path + File.separatorChar + element.attr("src");
            File file = new File(src);
            if (!file.exists() || file.isDirectory()) {
                continue;
            }
            set.add(file);
            // 上传文件
            UploadResult result = uploadService.doUpload(file, null, null, ResourceType.IMAGE, site);
            // 应前端需求图片地址给绝对地址
            if (result.getFileUrl().contains("http")) {
                element.attr("src", result.getFileUrl());
            } else {
                element.attr("src", site.getUrlWhole() + result.getFileUrl().substring(1));
            }
            ResourcesSpaceData source = new ResourcesSpaceData();
            source.init();
            source.setUrl(result.getFileUrl());
            source.setAlias(file.getName());
            source.setResourceType(ResourceType.RESOURCE_TYPE_IMAGE);
            datas.add(source);
            // 删除本地的图片
            //file.delete();
        }
        for (File file : set) {
            file.delete();
        }
        resourcesSpaceDataService.saveAll(datas);
        return doc.html().toString();
    }

    @Override
    public void restore(List<Integer> contentIds, Integer siteId, List<Integer> channelIds) throws GlobalException {
        // 判断系统设置是否允许标题重复
        Integer type = cmsSiteService.findById(siteId).getCmsSiteCfg().getTitleRepeat();
        List<Content> contents = new ArrayList<Content>(10);
        List<Content> list = findAllById(contentIds);
        // 判断内容所属的栏目是不是底层栏目，不是的话直接报错,只有是底层栏目，都还原
        for (Content content : list) {
            List<Map<String, Object>> mapList = this.preChange(content);
            // 没有引用，需要判断所属栏目是否是底层栏目
            if (!content.getChannel().getIsBottom()) {
                throw new GlobalException(ContentErrorCodeEnum.CHANNEL_HAS_CHILD_ERROR);
            }
            check(type, siteId, content.getTitle(), content.getChannelId());
            content.setRecycle(false);
            /**引用内容同步改状态*/
            for (Content c : content.getQuoteContents()) {
                c.setRecycle(false);
                update(c);
            }
            if (STATUS_FLOWABLE == content.getStatus()) {
                content.setStatus(ContentConstant.STATUS_FIRST_DRAFT);
            }
            if (STATUS_WAIT_PUBLISH == content.getStatus()) {
                content.setStatus(ContentConstant.STATUS_FIRST_DRAFT);
            }
            contents.add(content);
            update(content);
            afterChange(content, mapList);
        }
        batchUpdate(contents);
    }

    /**
     * 检验数据
     **/
    public void check(Integer type, Integer siteId, String title, Integer channelId) throws GlobalException {
        Map<String, String[]> params = new HashMap<String, String[]>(16);
        // 2站点内不允许重复
        if (type == 2) {
            params.put("EQ_siteId_Integer", new String[]{siteId.toString()});
            params.put("EQ_title_String", new String[]{title});
            params.put("EQ_recycle_Boolean", new String[]{"false"});
            Long contentList = super.count(params);
            ;
            if (contentList > 0) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_TITLE_IS_NOT_ALLOWED_TO_REPEAT);
            }
        } else if (type == 3) {
            // 3同一栏目下不允许重复
            params.put("EQ_channelId_Integer", new String[]{channelId.toString()});
            params.put("EQ_siteId_Integer", new String[]{siteId.toString()});
            params.put("EQ_title_String", new String[]{title});
            params.put("EQ_recycle_Boolean", new String[]{"false"});
            Long contentList = super.count(params);
            ;
            if (contentList > 0) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_TITLE_IS_NOT_ALLOWED_TO_REPEAT);
            }
        }
    }

	@Override
	public void deleteContent(List<Integer> contentIds) throws GlobalException {
		List<Content> contents = super.findAllById(contentIds);
		for (Content content : contents) {
			afterDelete(content);
			contentTagService.deleteTagQuote(null, content.getSiteId(), content);
			/**引用内容也删除*/
			contentIds.addAll(Content.fetchIds(content.getQuoteContents()));
		}
		super.delete(contentIds.toArray(new Integer[contentIds.size()]));
	}

    /**
     * 提交工作流模板方法
     *
     * @param content 内容对象
     * @Title: doSubmitFlow
     * @return: void
     */
    protected void doSubmitFlow(Content content) throws GlobalException {

    }

    /**
     * 填充内容当前支持的动作 模板方法
     *
     * @param content 内容对象
     * @param vo      详情视图vo对象
     * @Title: doFillActions
     * @return: void
     */
    protected void doFillActions(Content content, ContentFindVo vo, CoreUser user) {

    }

    /**
     * 填充内容按钮
     *
     * @param content 需要查询的内容
     * @param vo      内容按钮vo
     * @param user    用户user对象
     */
    protected void doFillActionsButton(Content content, ContentButtonVo vo, CoreUser user) {

    }

    @Override
    public void initContentObject(Content content) throws GlobalException {
        content.setSite(cmsSiteService.findById(content.getSiteId()));
        if (content.getChannelId() != null) {
            content.setChannel(channelService.findById(content.getChannelId()));
        }
        if (content.getUserId() != null) {
            content.setUser(coreUserService.findById(content.getUserId()));
        }
        if (content.getPublishUserId() != null) {
            content.setPublishUser(coreUserService.findById(content.getPublishUserId()));
        } else {
            content.setPublishUser(null);
        }
        content.setModel(cmsModelService.findById(content.getModelId()));
    }

    @Override
    public void initContentExtObject(ContentExt contentExt) throws GlobalException {
        if (contentExt.getDocResourceId() != null) {
            contentExt.setDocResource(resourcesSpaceDataService.findById(contentExt.getDocResourceId()));
        } else {
            contentExt.setDocResource(null);
        }
        if (contentExt.getContentSourceId() != null) {
            contentExt.setContentSource(contentSourceService.findById(contentExt.getContentSourceId()));
        } else {
            contentExt.setContentSource(null);
        }
        if (contentExt.getPicResId() != null) {
            contentExt.setReData(resourcesSpaceDataService.findById(contentExt.getPicResId()));
        } else {
            contentExt.setReData(null);
        }
        if (contentExt.getIssueOrg() != null) {
            contentExt.setSueOrg(contentMarkService.findById(contentExt.getIssueOrg()));
        } else {
            contentExt.setSueOrg(null);
        }
        if (contentExt.getIssueYear() != null) {
            contentExt.setSueYear(contentMarkService.findById(contentExt.getIssueYear()));
        } else {
            contentExt.setSueYear(null);
        }
    }

    @Override
    public Boolean getForceReleaseButton(Integer channelId) {
        // 查询该内容所在的栏目是否存在策略
        AuditChannelSet auditChannelSet = auditChannelSetService.findByChannelId(channelId, false);
        if (auditChannelSet != null) {
            // 如果存在策略则直接查看按钮
            return auditChannelSet.getIsCompel();
        }
        return false;
    }

    @Override
    public ContentButtonVo findByContentButton(Integer status, Integer id, Integer channelId, Boolean quote) {
        Content content = null;
        // id为空或者查询出的内容对象为null，说明其是新增内容查询接口
        if (id != null) {
            content = findById(id);
        }
        Boolean forceReleaseButton = null;
        // 为审核成功(有违禁词)、审核失败的内容查询是否存在强制通过按钮
        if (content != null && ContentButtonConstant.FORECE_RELEASE_STATUS.contains(content.getStatus())) {
            forceReleaseButton = this.getForceReleaseButton(content.getChannelId());
        }
        CmsSiteConfig config = null;
        // 获取到全局配置(主要为了判断已发布内容是否允许编辑与发布)
        if (content != null && ContentConstant.STATUS_PUBLISH == content.getStatus()) {
            config = cmsSiteService.findById(content.getSiteId()).getCmsSiteCfg();
        }
        Channel channel = null;
        if (channelId != null) {
            channel = channelService.findById(channelId);
        }
        List<ContentType> types = contentTypeService.findAll(false);
        ContentButtonVo contentButtonVo = ContentButtonVo.initButtonVo(content, forceReleaseButton, config, status, types, channel, quote != null ? quote : false);
        // 如果是流转中的内容需要判断是否存在撤回或者其它按钮(这样写主要是因为需要处理试用版不存在工作流)
        if (content != null && ContentConstant.STATUS_FLOWABLE == content.getStatus()) {
            doFillActionsButton(content, contentButtonVo, SystemContextUtils.getCoreUser());
        }
        return contentButtonVo;
    }

    @Override
    public Content findFirstByOrderByIdDesc() {
        return dao.findFirstByOrderBySortNumDesc();
    }

    @Override
    public Integer findMaxSortNum() {
        Content content = dao.findFirstByOrderBySortNumDesc();
        if (content != null) {
            return content.getSortNum();
        } else {
            return 0;
        }
    }

    @Override
    public long countByTpl(Integer siteId, String pcTpl, String mobileTpl) {
        return dao.countByTpl(siteId, pcTpl, mobileTpl);
    }

    @Override
    public Content findById(Integer id) {
        Content c = super.findById(id);
        /**设置引用到了哪些目标内容  这样方便取应用到了哪些栏目*/
        c.setQuoteContents(dao.findByOriContentIdAndRecycleAndHasDeleted(id, false, false));
        return c;
    }

    @Override
    public List<Content> findAllById(Iterable<Integer> integers) {
        List<Content> contents = super.findAllById(integers);
        /**设置引用到了哪些目标内容  这样方便取应用到了哪些栏目*/
        for (Content c : contents) {
            c.setQuoteContents(dao.findByOriContentIdAndRecycleAndHasDeleted(c.getId(), false, false));
        }
        return contents;
    }

    // 获取内容标识
    private SnowFlake snowFlake = new SnowFlake(SnowFlake.LONG_STR_CODE);

    @Autowired
    private CmsModelItemService cmsModelItemService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private ContentVersionService contentVersionService;
    @Autowired
    private ContentAttrService contentAttrService;
    @Autowired
    @Lazy
    private ContentRecordService contentRecordService;
    @Autowired
    private ContentTagService contentTagService;
    @Autowired
    private ContentTypeService contentTypeService;
    @Autowired
    private List<ContentListener> listenerList;
    @Autowired
    private ContentTxtService contentTxtService;
    @Autowired
    private ContentExtService contentExtService;
    @Autowired
    private CollectContentService collectContentService;
    @Autowired
    private CmsSiteService cmsSiteService;
    @Autowired
    private WechatMaterialService wechatMaterialService;
    @Autowired
    private WechatSendService wechatSendService;
    @Autowired
    private ResourcesSpaceDataService resourcesSpaceDataService;
    @Autowired
    private OpenOfficeConverter openOfficeConverter;
    @Autowired
    private ContentSourceService contentSourceService;
    @Autowired
    private ContentMarkService contentMarkService;
    @Autowired
    private CmsModelService cmsModelService;
    @Autowired
    private CmsOrgService cmsOrgService;
    @Autowired
    private SysSecretService secretService;
    @Autowired
    private CoreUserService coreUserService;
    @Autowired
    private FlowService flowService;
    @Autowired
    private ContentLuceneService contentLuceneService;
    @Autowired
    private ContentAttrResService contentAttrResService;
    @Autowired
    private WechatSendService sendService;
    @Autowired
    private UploadService uploadService;// doUpload
    @Autowired
    private SysHotWordService hotWordService;
    @Autowired
    private ContentReviewService contentReviewService;
    @Autowired
    private ContentCheckDetailService checkDetailService;
    @Autowired
    private AuditChannelSetService auditChannelSetService;
    @Autowired
	private GlobalConfigService globalConfigService;
	@Autowired
	private ContentPublishRecordService statisticsContentService;
	@Autowired
	private CacheProvider cacheProvider;
	@Autowired
	private MqSendMessageService mqSendMessageService;
    @Autowired
    private ParameterDecryptionUtilService decryptionUtilService;
}