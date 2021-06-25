package com.jeecms.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Singleton;
import com.jeecms.common.lang.Sequence;

import java.time.LocalDateTime;

/**
 * id工具类， 提供常用的id生成算法
 *
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/9/30 16:22
 * @copyright 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class IdUtil extends cn.hutool.core.util.IdUtil {

    /**
     * 创建一个Sequence算法的id生成器
     *
     * @param workerId
     * @param datacenterId
     * @return com.jeecms.archlearncommon.utils.lang.Sequence
     * @author Zhu Kaixiao
     * @date 2020/9/30 16:37
     */
    public static Sequence createSequence(long workerId, long datacenterId) {
        return new Sequence(workerId, datacenterId);
    }

    /**
     * 获取一个单例的Sequence算法的id生成器
     *
     * @param workerId
     * @param datacenterId
     * @return com.jeecms.archlearncommon.utils.lang.Sequence
     * @author Zhu Kaixiao
     * @date 2020/9/30 16:37
     */
    public static Sequence getSequence(long workerId, long datacenterId) {
        return Singleton.get(Sequence.class, workerId, datacenterId);
    }

    /**
     * 以主机和进程的机器码获取一个单例的Sequence算法的id生成器
     */
    public static Sequence getSequence() {
        return Singleton.get(Sequence.class);
    }

    /**
     * 获取唯一ID
     *
     * @return id
     */
    public static long getId() {
        return getSequence().nextId();
    }


    /**
     * 获取唯一ID
     *
     * @return id
     */
    public static String getIdStr() {
        return Long.toBinaryString(getId());
    }


    /**
     * 格式化的毫秒时间
     */
    public static String getMillisecond() {
        return DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmssSSS");
    }

    /**
     * 时间 ID = Time + ID
     * <p>例如：可用于商品订单 ID</p>
     */
    public static String getTimeId() {
        return getMillisecond() + getIdStr();
    }


    /**
     * 简化的UUID，去掉了横线，使用性能更好的ThreadLocalRandom生成UUID
     *
     * @return 简化的UUID，去掉了横线
     * @since 4.1.19
     */
    public static String get32UUID() {
        return fastSimpleUUID();
    }




}
