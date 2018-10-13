package com.github.zhaohzi1299002788.distributed.redis;

/**
 * @author
 * 分布式锁回调接口
 */
public interface DistributedLockCallback<T> {

    /**
     * 调用者必须在此方法中实现需要加分布式锁的场景
     */
    public T process();

    /**
     * 得到分布式锁名称
     */
    public String getLockName();
}
