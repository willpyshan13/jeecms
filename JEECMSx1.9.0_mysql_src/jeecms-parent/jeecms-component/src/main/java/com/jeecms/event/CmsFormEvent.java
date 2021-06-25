package com.jeecms.event;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import org.springframework.context.ApplicationEvent;

/**
 * 表单事件
 * @author: tom
 * @date: 2020/2/14 15:17
 */
public class CmsFormEvent extends ApplicationEvent {

    /**事件标识*/
    public final  static  String EVENT_TYPE="delete";
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public CmsFormEvent(Object source) {
        super(source);
    }

    private Integer[] ids;
    private  String eventType;

    public CmsFormEvent(Object source,Integer[] ids,String eventType) {
        super(source);
        setIds(ids);
        setEventType(eventType);
    }

    public CmsFormEvent(Object source,Integer[] ids) {
        super(source);
        setIds(ids);
        setEventType(EVENT_TYPE);
    }

    public Integer[] getIds() {
        return ids;
    }

    public void setIds(Integer[] ids) {
        this.ids = ids;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
