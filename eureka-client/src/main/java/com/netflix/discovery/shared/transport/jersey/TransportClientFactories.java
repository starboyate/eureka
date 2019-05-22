package com.netflix.discovery.shared.transport.jersey;

import java.util.Collection;
import java.util.Optional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.shared.transport.TransportClientFactory;

/**
 * 第一个方法已经废弃，这就是为什么说上面的 eurekaJerseyClient 参数( 不是 EurekaJerseyClient 类)已经废弃，被第二个方法取代。相比来说，第二个方法对 EurekaJerseyClient 创建封装会更好。
 * @param <F>
 */
public interface TransportClientFactories<F> {
    
    @Deprecated
    public TransportClientFactory newTransportClientFactory(final Collection<F> additionalFilters,
                                                                   final EurekaJerseyClient providedJerseyClient);

    public TransportClientFactory newTransportClientFactory(final EurekaClientConfig clientConfig,
                                                                   final Collection<F> additionalFilters,
                                                                   final InstanceInfo myInstanceInfo);
    
    public TransportClientFactory newTransportClientFactory(final EurekaClientConfig clientConfig,
            final Collection<F> additionalFilters,
            final InstanceInfo myInstanceInfo,
            final Optional<SSLContext> sslContext,
            final Optional<HostnameVerifier> hostnameVerifier);
}