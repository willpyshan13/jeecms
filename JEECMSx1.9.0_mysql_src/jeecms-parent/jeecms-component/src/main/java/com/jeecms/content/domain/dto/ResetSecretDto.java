/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.domain.dto;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 重置密级dto
 * @author: chenming
 * @date: 2020/8/18 15:46
 */
public class ResetSecretDto {

    public static final int DELETE_SECRET = 1;

    public static final int DELETE_SECRET_UPDATE_CONTENT = 2;

    /**
     * 内容、附件密级id集合
     */
    private List<Integer> secretIds;
    /**
     * 密级类型（1内容密级 2附件密级类型）
     */
    private Integer type;
    /**
     * 模型id
     */
    private Integer moduleId;
    /**
     * 请求状态：1-删除密级、2-删除内容密级和修改内容
     */
    private Integer requestStatus;

    @NotNull(groups = {DeleteSecret.class})
    public List<Integer> getSecretIds() {
        return secretIds;
    }

    public void setSecretIds(List<Integer> secretIds) {
        this.secretIds = secretIds;
    }

    @NotNull(groups = {DeleteSecret.class})
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @NotNull(groups = {DeleteModuleSecretFiled.class})
    public Integer getModuleId() {
        return moduleId;
    }

    public void setModuleId(Integer moduleId) {
        this.moduleId = moduleId;
    }

    @NotNull(groups = {DeleteSecret.class})
    public Integer getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(Integer requestStatus) {
        this.requestStatus = requestStatus;
    }

    public interface DeleteSecret {

    }
    public interface DeleteModuleSecretFiled {

    }
}
