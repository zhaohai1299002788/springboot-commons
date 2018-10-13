package com.github.zhaohzi1299002788.distributed.zookeeper;

import com.github.zhaohzi1299002788.distributed.redis.aop.DistributedLock;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * zookeeper实现分布式共享锁
 */
@Component
public class DistributedLockZkp implements Lock, Watcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedLockZkp.class);

    private ZooKeeper zooKeeper;
    /**
     * 根
     */
    private String root = "/locks";

    /**
     * 竞争资源的标志
     */
    private String lockName;

    /**
     * 等待前一个锁
     */
    private String waitNode;

    /**
     * 当前锁
     */
    private String myZnode;

    /**
     * 计数器
     */
    private CountDownLatch latch;

    /***
     * 30秒
     */
    private int sessionTimeout = 30000;

    /**
     * 等待节点失效最大实际 30秒
     */
    private int waitTimeout = 30000;
    private List<Exception> exceptions = new ArrayList<>();

    /**
     * 创建分布式锁，使用前者确认zkConnString配置的zookeeper服务可用
     *
     * @param zkConnString ip地址 + 端口号
     * @param lockName     竞争资源标志，LockName中不能包含lock
     */
    public DistributedLockZkp(String zkConnString, String lockName) {
        this.lockName = lockName;
        // 创建一个与服务器的连接
        try {
            zooKeeper = new ZooKeeper(zkConnString, sessionTimeout, this);
            Stat stat = zooKeeper.exists(root, Boolean.FALSE);
            if (stat == null) {
                // 创建根节点
                zooKeeper.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            exceptions.add(e);
        } catch (KeeperException e) {
            exceptions.add(e);
        } catch (InterruptedException e) {
            exceptions.add(e);
        }
    }

    @Override
    public void lock() {
        if (exceptions.size() > 0) {
            LOGGER.error("lock error | e : {}", exceptions.get(0));
            return;
        }
        try {
            if (this.tryLock()) {
                LOGGER.info("Thread : {}", Thread.currentThread().getId() + " " + myZnode + " get lock true");
                return;
            } else {
                // 等待获取锁
                waitForLock(waitNode, waitTimeout);
            }
        } catch (KeeperException e) {
            LOGGER.error("lock error | e : {}", e);
        } catch (InterruptedException e) {
            LOGGER.error("lock error | e : {}", e);
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        this.lock();
    }

    @Override
    public boolean tryLock() {
        try {
            String splitStr = "_lock_";
            if (lockName.contains(splitStr)) {
                throw new RuntimeException();
            }
            // 创建临时有序子节点
            myZnode = zooKeeper.create(root + "/" + lockName + splitStr, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.info("myZnode : {}", myZnode);
            // 取出所有子节点
            List<String> subNodes = zooKeeper.getChildren(root, false);
            // 取出所有lockName的锁
            List<String> lockObjNodes = new ArrayList<>();
            for (String node : subNodes) {
                String nodeString = node.split(splitStr)[0];
                if (nodeString.equals(lockName)) {
                    lockObjNodes.add(node);
                }
            }
            // 对所有节点进行默认排序，从小到大
            Collections.sort(lockObjNodes);
            LOGGER.info("myZnode==lockObjeNode : {}", myZnode + "==" + lockObjNodes.get(0));
            if (myZnode.equals(root + "/" + lockObjNodes.get(0))) {
                // 如果是最小的节点，则表示取得锁
                return true;
            }
            // 如果不是最小的节点，找到比自己小1的节点
            String subMyZnode = myZnode.substring(myZnode.lastIndexOf("/") + 1);
            // 获取比当前节点小一级的节点(Collections.binarySearch(lockObjNodes, subMyZnode):获取当前节点的角标)
            waitNode = lockObjNodes.get(Collections.binarySearch(lockObjNodes, subMyZnode) - 1);
        } catch (KeeperException e) {
            LOGGER.error("tryLock error | e : {}", e);
        } catch (InterruptedException e) {
            LOGGER.error("tryLock error | e : {}", e);
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        try {
            if (this.tryLock()) {
                return true;
            }
            return waitForLock(waitNode, time);
        } catch (Exception e) {
            LOGGER.error("tryLock error | e : {}", e);
        }
        return false;
    }

    /**
     * 等待获取锁
     *
     * @param lower 等待的锁
     * @param waitTime 最大等待时间
     * @return
     */
    private boolean waitForLock(String lower, long waitTime) throws InterruptedException, KeeperException{
        Stat stat = zooKeeper.exists(root + "/" + lower, true);
        // 判断比自己小一个数的节点是否存在，如果不存在则无需等待锁，同时注册监听
        if (stat != null) {
            LOGGER.info("Thread : {}", Thread.currentThread().getId() + "waiting for" + root + "/" + lower);
            this.latch = new CountDownLatch(1);
            this.latch.await(waitTime, TimeUnit.MILLISECONDS);
            this.latch = null;
        }
        return true;
    }

    /**
     * 取消锁监控
     */
    @Override
    public void unlock() {
        try {
            LOGGER.info("Thread : {}", Thread.currentThread().getId() + ", unlock" + myZnode);
            zooKeeper.delete(myZnode, -1);
            myZnode = null;
            // zookeeper.close();
        } catch (InterruptedException e) {
            LOGGER.error("unlock error | e : {}", e);
        } catch (KeeperException e) {
            LOGGER.error("unlock error | e : {}", e);
        }
    }

    /**
     * 关闭zk链接
     */
    public void closeZookeeper() {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            LOGGER.error("closeZookeeper error | e : {}", e);
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    /**
     * zookeeper节点的监视器
     * @param watchedEvent
     */
    @Override
    public void process(WatchedEvent watchedEvent) {
        if (this.latch != null) {
            this.latch.countDown();
        }
    }

    /**
     * 自定义异常信息
     * @author lenovo
     *
     */
    public class LockException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public LockException(String e){
            super(e);
        }
        public LockException(Exception e){
            super(e);
        }
    }
}
