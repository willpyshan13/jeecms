/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.service.impl;

import com.jeecms.common.base.domain.DragSortDto;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.util.StrUtils;
import com.jeecms.system.dao.ContentMarkDao;
import com.jeecms.system.domain.ContentMark;
import com.jeecms.system.domain.dto.ContentMarkBatchDto;
import com.jeecms.system.service.ContentMarkService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 发文字号管理Service实现类
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019-05-21
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class ContentMarkServiceImpl extends BaseServiceImpl<ContentMark, ContentMarkDao, Integer>
        implements ContentMarkService {

    @Override
    public List<ContentMark> saveBatch(@NotBlank String markNames, Integer markType) throws GlobalException {
        if (markNames == null) {
            throw new GlobalException(new SystemExceptionInfo(SystemExceptionEnum.ILLEGAL_PARAM.getCode(),
                    SystemExceptionEnum.ILLEGAL_PARAM.getDefaultMessage()));
        }
        String[] marks = StrUtils.splitAndTrim(markNames, ",", "，");
        Set<String> set = new HashSet<String>(Arrays.asList(marks));
        List<ContentMark> list = new ArrayList<ContentMark>();
        Integer sortNum = dao.getMaxSortNum(markType);
        sortNum = sortNum == null ? 0 : sortNum;
        for (String markName : set) {
            if (StringUtils.isNotBlank(markName) && checkByMarkName(markName, markType, null)) {
                list.add(init(markName, markType, sortNum + 1));
            }
            sortNum++;
        }
        return super.saveAll(list);
    }

    @Override
    public boolean checkByMarkName(String markName, Integer markType, Integer id) {
        if (StringUtils.isBlank(markName)) {
            return true;
        }
        List<ContentMark> contentMark = dao.findByMarkNameAndMarkType(markName, markType);
        if (contentMark == null || contentMark.isEmpty()) {
            return true;
        } else {
            if (id == null) {
                return false;
            }
            return contentMark.get(0).getId().equals(id);
        }
    }

    @Override
    public void deleteByIdAndMarkType(Integer[] ids, Integer markType) {
        for (Integer id : ids) {
            dao.deleteByIdAndMarkType(id, markType);
        }
    }

    @Override
    public ContentMark findByIdAndMarkType(Integer id, Integer markType) {
        List<ContentMark> entity = dao.findByIdAndMarkType(id, markType);
        if (entity != null && !entity.isEmpty()) {
            return entity.get(0);
        }
        return new ContentMark();
    }

    @Override
    public void sort(DragSortDto sorts, Integer markType) throws GlobalException {
        ContentMark from = findByIdAndMarkType(sorts.getFromId(), markType);
        ContentMark to = findByIdAndMarkType(sorts.getToId(), markType);
        Integer fromSort = from.getSortNum();
        Integer toSort = to.getSortNum();
        List<ContentMark> marks;
        if (fromSort < toSort) {
            marks = dao.findContentMarksByMarkTypeAndSortNumBetween(markType, fromSort, toSort);
        } else {
            marks = dao.findContentMarksByMarkTypeAndSortNumBetween(markType, toSort, fromSort);
        }
        for (ContentMark mark : marks) {
            if (mark.getId().equals(sorts.getFromId())) {
                mark.setSortNum(toSort);
            } else {
                if (fromSort < toSort) {
                    mark.setSortNum(mark.getSortNum() - 1);
                } else {
                    mark.setSortNum(mark.getSortNum() + 1);
                }
            }
        }

        super.batchUpdate(marks);
    }

    @Override
    public List<ContentMark> entryBatch(ContentMarkBatchDto dto) throws GlobalException {
        Integer begin = dto.getBeginYear();
        Integer end = dto.getEndYear();
        Integer[] excludes = dto.getExcludeYear();
        List<ContentMark> list = new ArrayList<>();
        // 循环遍历年号区间
        int num = 1;
        Integer sortNum = dao.getMaxSortNum(ContentMark.MARK_TYPE_YEAR);
        sortNum = sortNum == null ? 0 : sortNum;
        for (Integer i = begin; i <= end; i++) {
            // 如果排除年号存在则遍历排除年号，不存在则直接录入
            if (excludes != null && excludes.length > 0) {
                Set<Integer> set = new HashSet<>(Arrays.asList(excludes));
                /* 判断当前年号是否存在与排除年号，不存在贼添加，存在则跳过
                如果当前年号不存在则添加 */
                if (!set.contains(i) && checkByMarkName(String.valueOf(i), ContentMark.MARK_TYPE_YEAR, null)) {
                    list.add(init(String.valueOf(i), ContentMark.MARK_TYPE_YEAR, sortNum + num));
                    num++;
                }
            } else {
                if (checkByMarkName(String.valueOf(i), ContentMark.MARK_TYPE_YEAR, null)) {
                    list.add(init(String.valueOf(i), ContentMark.MARK_TYPE_YEAR, sortNum + num));
                    num++;
                }
            }
        }
        return super.saveAll(list);
    }

    /**
     * 初始化
     *
     * @param markName 机关代字/年号
     * @param markType 标记词种类(1机关代字 2年份)
     * @return ContentMark
     */
    private ContentMark init(String markName, Integer markType, int sortNum) {
        ContentMark mark = new ContentMark();
        mark.setMarkName(markName);
        mark.setMarkType(markType);
        mark.setSortNum(sortNum);
        return mark;
    }

    @Override
    public List<ContentMark> getList(Integer markType) {
        return dao.findByMarkTypeAndHasDeleted(markType, false);
    }

}