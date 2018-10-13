package com.github.zhaohzi1299002788.distributed.redis.aop;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    /**
     * 锁的名称
     * 如果lockName可以确定，直接设置该属性
     *
     * @return
     */
    String lockName() default "";

    /**
     * lockName后缀
     * @return
     */
    String lockNamePre() default "";

    /**
     * lockName后缀
     * @return
     */
    String lockNamePost() default "lock";

    /**
     * 获得锁名时拼接前后缀用到的分隔符
     * @return
     */
    String separator() default ".";

    /**
     * 获取注解的方法参数列表的某个参数对象的某个属性值来作为lockName.因为有时候lockName是不固定的。
     * 当param不为空时，可以通过argNum参数来设置具体是参数列表的第几个参数，不设置则默认取第一个。
     *
     * @return
     */
    String param() default "";

    /**
     * 将方法第argNum个参数作为锁
     *
     * @return
     */
    int argNum() default 0;

    /**
     * 是否使用公平锁
     * 公平锁既先来先得
     *
     * @return
     */
    boolean fairLock() default false;

    /**
     * 是否使用尝试锁
     *
     * @return
     */
    boolean tryLock() default false;

    /**
     * 最长等待时间
     * 该字段只有当tryLock()返回true才有效
     *
     * @return
     */
    long waitTime() default 30L;

    /**
     * 锁超时时间。
     * 超时时间过后，锁自动释放。
     * 建议 ：
     *  尽量缩减需要加锁逻辑。
     *
     * @return
     */
    long leaseTime() default 5L;

    /**
     * 时间单位，默认为秒
     *
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
