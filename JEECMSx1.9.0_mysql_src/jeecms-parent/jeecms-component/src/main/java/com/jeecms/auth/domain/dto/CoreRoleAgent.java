package com.jeecms.auth.domain.dto;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.auth.domain.CoreRole;
import com.jeecms.common.web.ApplicationContextProvider;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.service.CmsSiteService;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 角色增强类
 * @author: tom
 * @date: 2020/4/3 15:54   
 */
public class CoreRoleAgent implements Serializable {
    private CoreRole role;

    public static  void agentOwnerSites(CoreRole role) {
        CmsSiteService siteService = ApplicationContextProvider.getBean(CmsSiteService.class);
        siteService.findByIds(CmsSite.fetchIds(role.getOwnerSites()));
    }

    private CoreRoleAgent(Builder builder) {
        setRole(builder.role);
    }

    public CoreRole getRole() {
        return role;
    }

    public void setRole(CoreRole role) {
        this.role = role;
    }

    public static final class Builder {
        private CoreRole role;

        public Builder() {
        }

        public Builder role(CoreRole val) {
            role = val;
            return this;
        }

        public CoreRoleAgent build() {
            return new CoreRoleAgent(this);
        }
    }
}
