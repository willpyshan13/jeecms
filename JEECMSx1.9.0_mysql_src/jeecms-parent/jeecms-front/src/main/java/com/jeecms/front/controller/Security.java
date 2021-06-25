/*
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.front.controller;

import java.util.List;

/**
 * @author xiaohui
 * @version 1.0
 * @Date 2020-08-20 11:21
 */
public class Security {
    /**
     * 用户的内容密级id
     */
    private List<Integer> contentSecretIds;
    /**
     * 用户的附件密级id
     */
    private List<Integer> annexSecretIds;
    /**
     * 默认没有开启内容密级
     */
    private boolean openContentSecurity;
    /**
     * 默认没有开启附件密级
     */
    private boolean openAttachmentSecurity;

    public List<Integer> getContentSecretIds() {
        return contentSecretIds;
    }

    public void setContentSecretIds(List<Integer> contentSecretIds) {
        this.contentSecretIds = contentSecretIds;
    }

    public List<Integer> getAnnexSecretIds() {
        return annexSecretIds;
    }

    public void setAnnexSecretIds(List<Integer> annexSecretIds) {
        this.annexSecretIds = annexSecretIds;
    }

    public boolean isOpenContentSecurity() {
        return openContentSecurity;
    }

    public void setOpenContentSecurity(boolean openContentSecurity) {
        this.openContentSecurity = openContentSecurity;
    }

    public boolean isOpenAttachmentSecurity() {
        return openAttachmentSecurity;
    }

    public void setOpenAttachmentSecurity(boolean openAttachmentSecurity) {
        this.openAttachmentSecurity = openAttachmentSecurity;
    }
}
