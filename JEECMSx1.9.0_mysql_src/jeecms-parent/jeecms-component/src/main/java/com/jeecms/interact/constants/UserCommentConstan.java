package com.jeecms.interact.constants;/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

/**
 * @author: chenming
 * @date: 2020/8/13 14:37
 */
public class UserCommentConstan {
    /**
     * 待审核
     */
    public static final Short CHECK_WAIT = 0;
    /**
     * 审核通过
     */
    public static final Short CHECK_BY = 1;
    /**
     * 审核不通过
     */
    public static final Short CHECK_FAIL = 2;

    /**
     * 排序-最热
     */
    public static final Short SORT_HOTTEST = 1;
    /**
     * 排序-最新
     */
    public static final Short SORT_LATEST = 2;

    public static final String USER_COMMENT_CACHE_KEY = "USERCOMMENT";

    public static final String USER_COMMENT_TIME_INTERVAL = "timeInterval";

    /**
     * 查询数量：所有
     */
    public static final int COUNT_TYPE_ALL = 1;
    /**
     * 查询数量：待审核
     */
    public static final int COUNT_TYPE_CHECK_WAIT = 2;
    /**
     * 查询数量：审核通过
     */
    public static final int COUNT_TYPE_CHECK_BY = 3;
    /**
     * 查询数量：审核不通过
     */
    public static final int COUNT_TYPE_CHECK_FAIL = 4;
    /**
     * 查询数量：是否被举报
     */
    public static final int COUNT_TYPE_REPORT = 5;


}
