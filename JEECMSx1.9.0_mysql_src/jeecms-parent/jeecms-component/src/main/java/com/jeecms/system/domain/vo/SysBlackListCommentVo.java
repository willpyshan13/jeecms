/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.domain.vo;

import java.util.List;

/**
 * 黑名单实体类查询取值VO
 * @author: chenming
 * @date: 2020/8/14 9:28
 */
public class SysBlackListCommentVo {
    /**
     * 违规的用户id集合
     */
    private List<Integer> userIds;
    /**
     * 违规的用户IP集合
     */
    private List<String> ips;

    public SysBlackListCommentVo() {

    }

    public SysBlackListCommentVo(List<Integer> userIds, List<String> ips) {
        this.userIds = userIds;
        this.ips = ips;
    }

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }
}
