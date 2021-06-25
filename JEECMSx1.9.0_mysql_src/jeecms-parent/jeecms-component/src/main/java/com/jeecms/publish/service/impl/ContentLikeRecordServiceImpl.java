/**
*@Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.service.impl;

import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.publish.dao.ContentLikeRecordDao;
import com.jeecms.publish.domain.ContentLikeRecord;
import com.jeecms.publish.domain.vo.ContentLikeVo;
import com.jeecms.publish.service.ContentLikeRecordService;
import com.jeecms.system.domain.vo.MassScoreVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
* @author ljw
* @version 1.0
* @date 2020-06-17
*/
@Service
@Transactional(rollbackFor = Exception.class)
public class ContentLikeRecordServiceImpl extends BaseServiceImpl<ContentLikeRecord, ContentLikeRecordDao, Integer>
        implements ContentLikeRecordService {


    @Override
    public void deleteByUserIdAndContentId(Integer userId, Integer contentId) {
        dao.deleteByUserIdAndContentId(userId, contentId);
    }

    @Override
    public void deleteByCookieAndContentId(String cookie, Integer contentId) {
        dao.deleteByCookieAndContentId(cookie, contentId);
    }

    @Override
    public List<ContentLikeVo> count(boolean type, Date start, Date end) {
        return dao.count(type, start, end);
    }

    @Override
    public List<MassScoreVo> massCount(List<Integer> contentId, Date startDate, Date endDate) {
        return dao.massCount(contentId, startDate, endDate);
    }

}