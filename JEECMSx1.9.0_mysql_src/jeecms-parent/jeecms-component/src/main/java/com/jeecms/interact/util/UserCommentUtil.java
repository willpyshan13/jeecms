/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.interact.util;

import com.jeecms.interact.domain.UserComment;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 用户评论util
 * @author: chenming
 * @date: 2020/8/14 9:48
 */
public class UserCommentUtil {

    /**
     * util类里面都是静态方法，不会去初始化这个类，所以不应该暴露一个public构造函数
     */
    private UserCommentUtil() {
    }

    /**
     * 检验设置参数
     *
     * @param userComment 评论对象
     * @param replytText  回复内容
     * @param prohibitUserIdList    禁止评论的用户id集合
     * @param prohibitIpList        禁止评论的用户IP集合
     */
    public static void checkParameter(UserComment userComment, String replytText, List<Integer> prohibitUserIdList, List<String> prohibitIpList) {

        if (StringUtils.isNotBlank(replytText)) {
            // 为了比对回复内容查询条件
            if (userComment.getReplyAdminComment() != null) {
                String replyCommentText = userComment.getReplyAdminComment().getCommentText();
                // 回复内容不为空，并且回复内容不为空那么就不显示回复内容对象
                if (StringUtils.isNotBlank(replytText) && !replyCommentText.contains(replytText)) {
                    userComment.setReplyAdminComment(null);
                }
            }
        }
        // 会员是否被禁止
        if (prohibitUserIdList.contains(userComment.getUserId())) {
            userComment.setIsUserDisable(true);
        }
        // ip是否被禁止
        if (prohibitIpList.contains(userComment.getIp())) {
            userComment.setIsIpDisable(true);
        }
    }

}
