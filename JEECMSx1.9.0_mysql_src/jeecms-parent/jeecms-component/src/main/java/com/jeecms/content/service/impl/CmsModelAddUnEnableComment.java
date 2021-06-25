package com.jeecms.content.service.impl;

import com.jeecms.common.exception.GlobalException;
import com.jeecms.content.constants.CmsModelConstant;
import com.jeecms.content.dao.CmsModelDao;
import com.jeecms.content.domain.CmsModel;
import com.jeecms.content.service.CmsModelAddUnEnableService;
import com.jeecms.content.service.CmsModelService;
import io.jsonwebtoken.lang.Collections;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author: chenming
 * @date: 2021/4/24 14:29
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@Component
@ConditionalOnClass(name = "com.jeecms.workflow.service.impl.CmsWorkflowServiceImpl")
public class CmsModelAddUnEnableComment implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${product.version}")
    private String version;
    @Autowired
    private CmsModelService cmsModelService;
    @Autowired
    private CmsModelDao cmsModelDao;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

        String newVersion = version.substring(1, Math.min(version.indexOf(".") + 2, version.length()));
        double a = Double.parseDouble(newVersion);
        if (a > 1.8) {
            try {
                addPayReadAndReward(cmsModelDao.findByTplTypeAndHasDeleted(CmsModel.CONTENT_TYPE, false));
            } catch (GlobalException e) {
                e.printStackTrace();
            }
        }
    }

    public void addPayReadAndReward(List<CmsModel> models) throws GlobalException {
        if (!Collections.isEmpty(models)) {
            for (CmsModel model : models) {
                String unEnableStr = model.getUnEnableJsonStr();
                if (StringUtils.isNotBlank(unEnableStr)) {
                    if (!model.existItem(CmsModelConstant.FIELD_SYS_PAY_READ) &&
                            !model.existUnEnableItem(CmsModelConstant.FIELD_SYS_PAY_READ)) {
                        if (unEnableStr.length() > 2) {
                            unEnableStr = unEnableStr.substring(0, unEnableStr.length() - 1);
                            unEnableStr = unEnableStr + "," + CmsModelConstant.PAY_READ_VALUE + "]";
                        } else {
                            unEnableStr = unEnableStr.substring(0, unEnableStr.length() - 1);
                            unEnableStr = unEnableStr + CmsModelConstant.PAY_READ_VALUE + "]";
                        }
                    }
                    if (!model.existItem(CmsModelConstant.FIELD_SYS_PAY_REWARD) &&
                            !model.existUnEnableItem(CmsModelConstant.FIELD_SYS_PAY_REWARD)) {
                        if (unEnableStr.length() > 2) {
                            unEnableStr = unEnableStr.substring(0, unEnableStr.length() - 1);
                            unEnableStr = unEnableStr + "," + CmsModelConstant.REWARD_VALUE + "]";
                        } else {
                            unEnableStr = unEnableStr.substring(0, unEnableStr.length() - 1);
                            unEnableStr = unEnableStr + CmsModelConstant.REWARD_VALUE + "]";
                        }
                    }
                    model.setUnEnableJsonStr(unEnableStr);
                }
            }
            cmsModelService.batchUpdateAll(models);
        }

    }
}
