package com.netflix.discovery;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.util.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A task for updating and replicating the local instanceinfo to the remote server. Properties of this task are:
 * - configured with a single update thread to guarantee sequential update to the remote server
 * - update tasks can be scheduled on-demand via onDemandUpdate()
 * - task processing is rate limited by burstSize
 * - a new update task is always scheduled automatically after an earlier update task. However if an on-demand task
 *   is started, the scheduled automatic update task is discarded (and a new one will be scheduled after the new
 *   on-demand update).
 *   应用实例信息复制器，用于更新本地instanceinfo并将其复制到远程服务器的任务。该任务属性：
 *   1.配置单个更新线程以保证对远程服务器的顺序更新
 *   2.可以通过onDemandUpdate（）按需安排更新任务
 *   3.任务处理由burstSize限制
 *   4.在较早的更新任务之后，始终会自动安排新的更新任务。但是如果启动了按需任务，计划的自动更新任务将被丢弃（并且在新的按需更新之后将安排新的）
 *   @author dliu
 */
class InstanceInfoReplicator implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(InstanceInfoReplicator.class);

    private final DiscoveryClient discoveryClient;
    /**
     * 应用实例信息
     */
    private final InstanceInfo instanceInfo;
    /**
     * 定时执行频率，单位：秒
     */
    private final int replicationIntervalSeconds;
    /**
     * 定时执行器
     */
    private final ScheduledExecutorService scheduler;
    /**
     * 定时执行任务的 Future
     */
    private final AtomicReference<Future> scheduledPeriodicRef;
    /**
     * 是否开启调度
     */
    private final AtomicBoolean started;
    private final RateLimiter rateLimiter;
    private final int burstSize;
    private final int allowedRatePerMinute;

    InstanceInfoReplicator(DiscoveryClient discoveryClient, InstanceInfo instanceInfo, int replicationIntervalSeconds, int burstSize) {
        this.discoveryClient = discoveryClient;
        this.instanceInfo = instanceInfo;
        this.scheduler = Executors.newScheduledThreadPool(1,
                new ThreadFactoryBuilder()
                        .setNameFormat("DiscoveryClient-InstanceInfoReplicator-%d")
                        .setDaemon(true)
                        .build());

        this.scheduledPeriodicRef = new AtomicReference<Future>();

        this.started = new AtomicBoolean(false);
        this.rateLimiter = new RateLimiter(TimeUnit.MINUTES);
        this.replicationIntervalSeconds = replicationIntervalSeconds;
        this.burstSize = burstSize;

        this.allowedRatePerMinute = 60 * this.burstSize / this.replicationIntervalSeconds;
        logger.info("InstanceInfoReplicator onDemand update allowed rate per min is {}", allowedRatePerMinute);
    }

    public void start(int initialDelayMs) {
        if (started.compareAndSet(false, true)) {
            // 设置 应用实例信息 数据不一致
            //因为 InstanceInfo 刚被创建时，在 Eureka-Server 不存在，也会被注册
            instanceInfo.setIsDirty();  // for initial register
            // 提交任务，并设置该任务的 Future
            //延迟 initialDelayMs 毫秒执行一次任务。为什么此处设置 scheduledPeriodicRef ？在 InstanceInfoReplicator#onDemandUpdate() 方法会看到具体用途
            Future next = scheduler.schedule(this, initialDelayMs, TimeUnit.SECONDS);
            scheduledPeriodicRef.set(next);
        }
    }

    public void stop() {
        shutdownAndAwaitTermination(scheduler);
        started.set(false);
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("InstanceInfoReplicator stop interrupted");
        }
    }


    public boolean onDemandUpdate() {
        if (rateLimiter.acquire(burstSize, allowedRatePerMinute)) {
            if (!scheduler.isShutdown()) {
                scheduler.submit(new Runnable() {
                    @Override
                    public void run() {
                        logger.debug("Executing on-demand update of local InstanceInfo");
                        // 取消任务
                        //调用 Future#cancel(false) 方法，取消定时任务，避免无用的注册。
                        Future latestPeriodic = scheduledPeriodicRef.get();
                        if (latestPeriodic != null && !latestPeriodic.isDone()) {
                            logger.debug("Canceling the latest scheduled update, it will be rescheduled at the end of on demand update");
                            latestPeriodic.cancel(false);
                        }
                        // 再次调用
                        // 发起注册。
                        InstanceInfoReplicator.this.run();
                    }
                });
                return true;
            } else {
                logger.warn("Ignoring onDemand update due to stopped scheduler");
                return false;
            }
        } else {
            logger.warn("Ignoring onDemand update due to rate limiter");
            return false;
        }
    }

    /**
     * 定时检查 InstanceInfo 的状态( status ) 属性是否发生变化。若是，发起注册
     */
    public void run() {
        try {
            // 刷新 应用实例信息
            //此处可能导致应用实例信息数据不一致
            discoveryClient.refreshInstanceInfo();
            // 判断 应用实例信息 是否数据不一致
            Long dirtyTimestamp = instanceInfo.isDirtyWithTime();
            if (dirtyTimestamp != null) {
                // 发起注册
                discoveryClient.register();
                // 设置 应用实例信息 数据一致
                instanceInfo.unsetIsDirty(dirtyTimestamp);
            }
        } catch (Throwable t) {
            logger.warn("There was a problem with the instance info replicator", t);
        } finally {
            // 提交任务，并设置该任务的 Future
            //再次延迟执行任务，并设置 scheduledPeriodicRef。通过这样的方式，不断循环定时执行任务。
            Future next = scheduler.schedule(this, replicationIntervalSeconds, TimeUnit.SECONDS);
            scheduledPeriodicRef.set(next);
        }
    }

}
