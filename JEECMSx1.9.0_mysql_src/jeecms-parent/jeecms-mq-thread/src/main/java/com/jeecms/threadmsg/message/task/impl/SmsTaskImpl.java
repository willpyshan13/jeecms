package com.jeecms.threadmsg.message.task.impl;

import com.alibaba.fastjson.JSONObject;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.web.util.HttpClientUtil;
import com.jeecms.content.constants.ContentReviewConstant;
import com.jeecms.element.service.ElementConfigService;
import com.jeecms.message.MqConstants;
import com.jeecms.message.dto.CommonMqConstants;
import com.jeecms.sso.dto.response.SyncResponseBaseVo;
import com.jeecms.system.domain.GlobalConfig;
import com.jeecms.system.domain.GlobalConfigAttr;
import com.jeecms.system.domain.MessageTplDetails;
import com.jeecms.system.domain.dto.SmsSendDto;
import com.jeecms.system.service.GlobalConfigService;
import com.jeecms.system.service.MessageTplDetailsService;
import com.jeecms.threadmsg.common.MessageInfo;
import com.jeecms.threadmsg.message.task.Task;
import com.jeecms.universal.service.HttpRequestPlatformUtilService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 短信任务
 * @author: ztx
 * @date: 2019年1月21日 下午4:45:29
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@Repository("smsTask")
public class SmsTaskImpl implements Task {
	private static final Logger LOGGER = LoggerFactory.getLogger(SmsTaskImpl.class);

	@SuppressWarnings("all")
	@Override
	public boolean exec(Object msg) throws Exception {
 		if (msg != null && msg instanceof MessageInfo) {
            MessageInfo msgInfo = (MessageInfo) msg;
            // 比对发送类型如果其中没有包含短信模式直接退出
            switch (msgInfo.getSendType()) {
                case MqConstants.SEND_EMAIL:
                    return true;
                case MqConstants.SEND_SYSTEM_STATION:
                    return true;
                case MqConstants.SEND_SYSTEM_STATION_EMAIL:
                    return true;
                default:
                    break;
            }
            // 如果手机号为空，直接退出
            if (CollectionUtils.isEmpty(msgInfo.getPhones())) {
                return true;
            }
            String mesCode = msgInfo.getMessageCode();
            if (StringUtils.isBlank(mesCode)) {
                return true;
            }
            // 手机消息模板
            MessageTplDetails smsMesTplDetail = msgTplDetailService.findByCodeAndType(mesCode,
                    MessageTplDetails.MESTYPE_PHONE, msgInfo.getSiteId());
            if (smsMesTplDetail == null || !smsMesTplDetail.getIsOpen()) {
                LOGGER.warn("No Phone template message is configured!");
                return true;
            }
            Object resData = null;
            ResponseInfo resInfo = null;
            JSONObject data = msgInfo.getData();
            // 自定义参数
            Map<String, String> smsExtParam = data == null ? new HashMap<>()
                    : (Map<String, String>) data.get(CommonMqConstants.EXT_DATA_KEY_SMS);

            Map<String,String> sendSmsParam = new HashMap<>();
            for (String key:smsExtParam.keySet()) {
                if (key.startsWith("${")) {
                    sendSmsParam.put(key,smsExtParam.get(key));
                } else {
                    sendSmsParam.put("${"+key+"}", smsExtParam.get(key));
                }
            }
            String appId = null;
            String phone = null;
            try {
                appId = requestPlatformUtilService.getUserParameter(true);
                phone = requestPlatformUtilService.getUserParameter(false);
            }
            catch (ParseException e) {
                // 超时，提示然后退出
                LOGGER.warn("Request third-party platform timeout!");
                return true;
            } catch (IOException e) {
                // IO异常，提示然后退出
                LOGGER.warn("Third-party platform request IO exception!");
                return true;
            }
            // 拼接好dto（发送短信dto）对象
            SmsSendDto dto =  new SmsSendDto(smsMesTplDetail.getTplId(), msgInfo.getSiteId(), sendSmsParam, msgInfo.getPhones(), phone);
            String responseStr = null;
            try {
                // 访问云平台如果5分钟没有响应直接断开，这里不需要判断所谓的返回值
                responseStr = HttpClientUtil.timeLimitPostJson(WebConstants.DoMain.SEND_SMS, dto,this.getHeader(),300000);
                //检测余量是否还有，没有的话就关闭双因子
                if (StringUtils.isNotBlank(responseStr)) {
                    JSONObject object = JSONObject.parseObject(responseStr);
                    if (object != null && object.getInteger("code") != null &&
                            SyncResponseBaseVo.SUCCESS_CODE.equals(object.getInteger("code"))) {
                        if (!elementConfigService.check(null)) {
                            GlobalConfig config = globalConfigService.get();
                            Map<String, String> attrMap = config.getAttrs();
                            attrMap.put(GlobalConfigAttr.ELEMENT_OPEN, GlobalConfigAttr.FALSE_STRING);
                            globalConfigService.updateAll(config);
                        }
                    }

                }
            } catch (ParseException e) {
                LOGGER.warn("Request third-party platform timeout!");
            } catch (IOException e) {
                LOGGER.warn("Third-party platform request IO exception!");
            }
			return true;
		}
		return false;
	}

    /**
     * 获取header头
     *
     * @return Map<String, String>
     * @throws GlobalException 全局异常
     */
    private Map<String, String> getHeader() throws GlobalException,ParseException,IOException {
        HEADER_MAP.put(ContentReviewConstant.SEND_REQUEST_HEADER, requestPlatformUtilService.getUserParameter(true));
        return HEADER_MAP;
    }

    /**
     * 请求header头map
     */
    private static Map<String, String> HEADER_MAP = new HashMap<String, String>();

    @Override
	public int operation() {
		return MqConstants.MESSAGE_QUEUE_SMS;
	}

    @Autowired
    private HttpRequestPlatformUtilService requestPlatformUtilService;
	@Autowired
	private MessageTplDetailsService msgTplDetailService;
    @Autowired
    private ElementConfigService elementConfigService;
    @Autowired
    private GlobalConfigService globalConfigService;
}
