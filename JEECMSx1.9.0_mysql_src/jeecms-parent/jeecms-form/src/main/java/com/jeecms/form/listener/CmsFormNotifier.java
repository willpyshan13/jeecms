package com.jeecms.form.listener;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.exception.GlobalException;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.event.CmsFormEvent;
import com.jeecms.form.service.CmsFormDataService;
import com.jeecms.interact.service.CmsFormService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

/**
 * 表单事件监听
 *
 * @author: tom
 * @date: 2020/2/14 15:36
 */
@Component
public class CmsFormNotifier {

    private static final Logger logger = LoggerFactory.getLogger(CmsFormNotifier.class);
    @Autowired
    private CmsFormDataService formDataService;

    /**
     * 表单事件监听，处理表单数据删除
     *
     * @param event
     */
    @EventListener
    public void processFormEvent(CmsFormEvent event) {
        if (CmsFormEvent.EVENT_TYPE.equals(event.getEventType())) {
            Integer[] ids = event.getIds();
            try {
                for(Integer id:ids){
                    formDataService.deleteAllByFormId(id);
                }
            }catch (GlobalException e){
                logger.error(e.getMessage());
            }
        }
    }
}
