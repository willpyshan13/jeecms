package com.jeecms.common.util;

import cn.hutool.core.collection.ConcurrentHashSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/22 14:42
 */
public class LockUtil extends cn.hutool.core.thread.lock.LockUtil {

    private static final Map<Object, ReentrantLock> lockMap = new ConcurrentHashMap<>();
    private static final Set<Object> getLockFlagMap = new ConcurrentHashSet<>();

    private static final ReentrantLock lock = new ReentrantLock();

    /**
     * 尝试获取锁并锁定，如果获取锁成功，返回true
     */
    public static boolean tryLock(Object obj) {
        try {
            lock.lock();
            ReentrantLock lockForObj = lockMap.computeIfAbsent(obj, o -> new ReentrantLock());
            return lockForObj.tryLock();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取锁，如果锁被占用则一直等待
     */
    public static void lock(Object obj) {
        ReentrantLock lockForObj;
        try {
            lock.lock();
            lockForObj = lockMap.computeIfAbsent(obj, o -> new ReentrantLock());
            getLockFlagMap.add(obj);
        } finally {
            lock.unlock();
        }
        try {
            lockForObj.lock();
        } finally {
            getLockFlagMap.remove(obj);
        }
    }

    /**
     * 释放锁
     */
    public static void unlock(Object obj) {
        try {
            lock.lock();
            ReentrantLock lockForObj = lockMap.computeIfAbsent(obj, o -> new ReentrantLock());
            lockForObj.unlock();
            if (lockForObj.getQueueLength() <= 0 && !getLockFlagMap.contains(obj)) {
                lockMap.remove(obj);
            }
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {

        
    }
}
