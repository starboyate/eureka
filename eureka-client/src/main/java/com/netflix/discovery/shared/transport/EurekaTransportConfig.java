package com.netflix.discovery.shared.transport;

/**
 * Config class that governs configurations relevant to the transport layer
 * Eureka 传输配置接口
 * @author David Liu
 */
public interface EurekaTransportConfig {

    /**
     * EurekaHttpClient 会话周期性重连时间，单位：秒
     * @return the reconnect inverval to use for sessioned clients
     */
    int getSessionedClientReconnectIntervalSeconds();

    /**
     * 重试 EurekaHttpClient ，请求失败的 Eureka-Server 隔离集合占比 Eureka-Server 全量集合占比，超过该比例，进行清空。
     * @return the percentage of the full endpoints set above which the quarantine set is cleared in the range [0, 1.0]
     */
    double getRetryableClientQuarantineRefreshPercentage();

    /**
     *
     * @return the max staleness threshold tolerated by the applications resolver
     */
    int getApplicationsResolverDataStalenessThresholdSeconds();

    /**
     * By default, the applications resolver extracts the public hostname from internal InstanceInfos for resolutions.
     * Set this to true to change this behaviour to use ip addresses instead (private ip if ip type can be determined).
     *
     * @return false by default
     */
    boolean applicationsResolverUseIp();

    /**
     * 异步解析 EndPoint 集群频率，单位：毫秒。
     * @return the interval to poll for the async resolver.
     */
    int getAsyncResolverRefreshIntervalMs();

    /**
     * 异步解析器预热解析 EndPoint 集群超时时间，单位：毫秒。
     * @return the async refresh timeout threshold in ms.
     */
    int getAsyncResolverWarmUpTimeoutMs();

    /**
     * 异步解析器线程池大小。
     * @return the max threadpool size for the async resolver's executor
     */
    int getAsyncExecutorThreadPoolSize();

    /**
     * The remote vipAddress of the primary eureka cluster to register with.
     *
     * @return the vipAddress for the write cluster to register with
     */
    String getWriteClusterVip();

    /**
     * The remote vipAddress of the eureka cluster (either the primaries or a readonly replica) to fetch registry
     * data from.
     *
     * @return the vipAddress for the readonly cluster to redirect to, if applicable (can be the same as the bootstrap)
     */
    String getReadClusterVip();

    /**
     * Can be used to specify different bootstrap resolve strategies. Current supported strategies are:
     *  - default (if no match): bootstrap from dns txt records or static config hostnames
     *  - composite: bootstrap from local registry if data is available
     *    and warm (see {@link #getApplicationsResolverDataStalenessThresholdSeconds()}, otherwise
     *    fall back to a backing default
     *
     * @return null for the default strategy, by default
     */
    String getBootstrapResolverStrategy();

    /**
     * By default, the transport uses the same (bootstrap) resolver for queries.
     *
     * Set this property to false to use an indirect resolver to resolve query targets
     * via {@link #getReadClusterVip()}. This indirect resolver may or may not return the same
     * targets as the bootstrap servers depending on how servers are setup.
     *
     * @return true by default.
     */
    boolean useBootstrapResolverForQuery();
}
