/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.system.domain.vo;

import java.util.List;

/**
 * 同步模板vo
 * @author: chenming
 * @date: 2020/6/18 10:33
 */
public class SynchTplVo {
    /**
     * 云平台同步返回vo集合
     */
    private List<SynchPlatformTplVo> vos;
    /**
     * 同步云平台是否成功：true->同步云平台成功，false->云平台超时
     */
    private Boolean platformSuccess;

    public List<SynchPlatformTplVo> getVos() {
        return vos;
    }

    public void setVos(List<SynchPlatformTplVo> vos) {
        this.vos = vos;
    }

    public Boolean getPlatformSuccess() {
        return platformSuccess;
    }

    public void setPlatformSuccess(Boolean platformSuccess) {
        this.platformSuccess = platformSuccess;
    }
}
