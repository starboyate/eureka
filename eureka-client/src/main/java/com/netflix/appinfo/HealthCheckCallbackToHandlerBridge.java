package com.netflix.appinfo;

/**
 * @author Nitesh Kant
 * HealthCheckHandler实现类，用于将 HealthCheckCallback 桥接成 HealthCheckHandler，
 * 在 spring cloud 体系中，提供了默认实现EurekaHealthCheckHandler，需要结合 spirng-boot-actuate 使用
 */
@SuppressWarnings("deprecation")
public class HealthCheckCallbackToHandlerBridge implements HealthCheckHandler {

    private final HealthCheckCallback callback;

    public HealthCheckCallbackToHandlerBridge() {
        callback = null;
    }

    public HealthCheckCallbackToHandlerBridge(HealthCheckCallback callback) {
        this.callback = callback;
    }

    @Override
    public InstanceInfo.InstanceStatus getStatus(InstanceInfo.InstanceStatus currentStatus) {
        if (null == callback || InstanceInfo.InstanceStatus.STARTING == currentStatus
                || InstanceInfo.InstanceStatus.OUT_OF_SERVICE == currentStatus) { // Do not go to healthcheck handler if the status is starting or OOS.
            return currentStatus;
        }

        return callback.isHealthy() ? InstanceInfo.InstanceStatus.UP : InstanceInfo.InstanceStatus.DOWN;
    }
}
