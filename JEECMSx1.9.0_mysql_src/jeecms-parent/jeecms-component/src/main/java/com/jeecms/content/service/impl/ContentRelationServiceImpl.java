/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.service.impl;

import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.RPCErrorCodeEnum;
import com.jeecms.common.exception.error.UserErrorCodeEnum;
import com.jeecms.content.dao.ContentRelationDao;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.ContentRelation;
import com.jeecms.content.domain.dto.ContentRelationDto;
import com.jeecms.content.service.ContentRelationService;
import com.jeecms.content.service.ContentService;
import com.jeecms.system.domain.CmsDataPerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容"相关内容"service实现类
 *
 * @author: chenming
 * @date: 2019年6月21日 下午4:32:12
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ContentRelationServiceImpl extends BaseServiceImpl<ContentRelation, ContentRelationDao, Integer>
		implements ContentRelationService {

	@Override
	public void save(ContentRelationDto dto) throws GlobalException {
		List<Content> contents = contentService.findAllById(dto.getContentIds());
		Content sourceContent = contentService.findById(dto.getContentId());
		if (contents == null || contents.size() == 0 || sourceContent == null) {
			return;
		}
		// 校验权限
		List<Content> validContents = new ArrayList<Content>(contents);
		validContents.add(sourceContent);
		if (!contentService.validType(CmsDataPerm.OPE_CONTENT_EDIT, validContents, null)) {
			throw new GlobalException(new SystemExceptionInfo(
					UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(),
					UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode()));
		}
		if (dto.getContentIds().contains(dto.getContentId())) {
			throw new GlobalException(new SystemExceptionInfo(
					RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getDefaultMessage(),
					RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getCode()));
		}
		List<ContentRelation> list = findByContentId(dto.getContentId());
		List<Integer> original = new ArrayList<Integer>(list.size());
		for (ContentRelation relation : list) {
			original.add(relation.getRelationContentId());
		}
		List<ContentRelation> contentRelations = new ArrayList<ContentRelation>();
		for (Content content : contents) {
			if (!original.contains(content.getId())) {
				ContentRelation contentRelation = new ContentRelation();
				contentRelation.setRelationContentId(content.getId());
				contentRelation.setContentId(dto.getContentId());
				contentRelation.setSortNum(10);
				contentRelation.setSortWeight(0);
				contentRelation.setContent(sourceContent);
				contentRelation.setRelationContent(content);
				contentRelations.add(contentRelation);
			}
		}
		if (!CollectionUtils.isEmpty(contentRelations)) {
			super.saveAll(contentRelations);
		}
	}

	@Override
	public void sort(ContentRelationDto dto) throws GlobalException {
	    // 源相关内容(需要排序的内容)
        ContentRelation sourceContentRelation = super.findById(dto.getContentRelationId());
        if (sourceContentRelation == null) {
            return;
        }
        // 查询出前台展示的相关内容的list集合
        List<ContentRelation> contentRelations = dao.findByContentIdAndHasDeletedOrderBySortNumAscSortWeightDescCreateTimeDesc(sourceContentRelation.getContentId(),false);
        int fixed = 0;
        // 源相关内容id值
        int sourceId = dto.getContentRelationId();
        // 目标
        int aimsId = dto.getContentRelationIds().get(0);
        ContentRelation aimsContentRelation = null;
        List<ContentRelation> newContentRelatios = new ArrayList<>();
        for (int i = 0; i < contentRelations.size(); i++) {
            ContentRelation relation = contentRelations.get(i);
            // 如果当前是源头
            if (sourceId == relation.getId()) {
                continue;
            }
            relation.setSortNum(i);
            relation.setSortWeight(0);
            if (aimsId == relation.getId()) {
                fixed = i;
                aimsContentRelation = relation;
                continue;
            }
            newContentRelatios.add(relation);
        }
        // 排序true之前，false之后
        if (dto.getLocation()) {
            // 使得源相关内容=目标相关内容的排序值，源相关内容sortWeight>目标相关内容的sortWeight
            sourceContentRelation.setSortNum(fixed);
            sourceContentRelation.setSortWeight(1);
        } else {
            // 使得源相关内容=目标相关内容的排序值，目标相关内容sortWeight>源相关内容的sortWeight
            sourceContentRelation.setSortNum(fixed);
            sourceContentRelation.setSortWeight(0);
            aimsContentRelation.setSortWeight(1);
        }
        newContentRelatios.add(sourceContentRelation);
        newContentRelatios.add(aimsContentRelation);
        super.batchUpdateAll(newContentRelatios);
        super.flush();
	}

	@Override
	public List<ContentRelation> findByContentId(Integer contentId) {
		return dao.findByContentIdAndHasDeleted(contentId,false);
	}

	@Autowired
	private ContentService contentService;


}