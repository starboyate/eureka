package com.netflix.discovery;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.inject.Provider;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import com.google.inject.Inject;
import com.netflix.appinfo.HealthCheckCallback;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.discovery.shared.transport.TransportClientFactory;
import com.netflix.discovery.shared.transport.jersey.EurekaJerseyClient;
import com.netflix.discovery.shared.transport.jersey.TransportClientFactories;
import com.netflix.eventbus.spi.EventBus;

/**
 * DiscoveryClient 可选参数抽象基类
 * <T> The type for client supplied filters (supports jersey1 and jersey2)
 */
public abstract class AbstractDiscoveryClientOptionalArgs<T> {
    /**
     * 生成健康检查回调的工厂
     */
    Provider<HealthCheckCallback> healthCheckCallbackProvider;
    /**
     * 生成健康检查处理器的工厂，目前暂未提供合适的默认实现，唯一提供的{@link com.netflix.appinfo.HealthCheckCallbackToHandlerBridge},
     * 用于将 HealthCheckCallback 桥接成 HealthCheckHandler
     */
    Provider<HealthCheckHandler> healthCheckHandlerProvider;
    /**
     * 向eureka-server注册之前的处理器，目前暂未提供默认实现，通过实现该接口，可以在注册之前做一些自定义的处理
     */
    PreRegistrationHandler preRegistrationHandler;
    /**
     * Jersy 过滤器集合
     */
    Collection<T> additionalFilters;
    /**
     * Jersey 客户端，该参数目前废弃，使用下面 TransportClientFactories 参数来进行生成。
     */
    EurekaJerseyClient eurekaJerseyClient;
    /**
     * 生成 Jersey 客户端的工厂的工厂
     *
     */
    TransportClientFactory transportClientFactory;
    /**
     * Eureka 事件监听器集合，生成 Jersey 客户端工厂的工厂接口。目前有 Jersey1TransportClientFactories 、Jersey2TransportClientFactories 两个实现
     */
    TransportClientFactories transportClientFactories;

    /**
     * Eureka 事件监听器集合
     */
    private Set<EurekaEventListener> eventListeners;

    private Optional<SSLContext> sslContext = Optional.empty();

    private Optional<HostnameVerifier> hostnameVerifier = Optional.empty();

    @Inject(optional = true)
    public void setEventListeners(Set<EurekaEventListener> listeners) {
        if (eventListeners == null) {
            eventListeners = new HashSet<>();
        }
        eventListeners.addAll(listeners);
    }
    
    @Inject(optional = true)
    public void setEventBus(final EventBus eventBus) {
        if (eventListeners == null) {
            eventListeners = new HashSet<>();
        }
        
        eventListeners.add(new EurekaEventListener() {
            @Override
            public void onEvent(EurekaEvent event) {
                eventBus.publish(event);
            }
        });
    }

    @Inject(optional = true) 
    public void setHealthCheckCallbackProvider(Provider<HealthCheckCallback> healthCheckCallbackProvider) {
        this.healthCheckCallbackProvider = healthCheckCallbackProvider;
    }

    @Inject(optional = true) 
    public void setHealthCheckHandlerProvider(Provider<HealthCheckHandler> healthCheckHandlerProvider) {
        this.healthCheckHandlerProvider = healthCheckHandlerProvider;
    }

    @Inject(optional = true)
    public void setPreRegistrationHandler(PreRegistrationHandler preRegistrationHandler) {
        this.preRegistrationHandler = preRegistrationHandler;
    }


    @Inject(optional = true) 
    public void setAdditionalFilters(Collection<T> additionalFilters) {
        this.additionalFilters = additionalFilters;
    }

    @Inject(optional = true) 
    public void setEurekaJerseyClient(EurekaJerseyClient eurekaJerseyClient) {
        this.eurekaJerseyClient = eurekaJerseyClient;
    }
    
    Set<EurekaEventListener> getEventListeners() {
        return eventListeners == null ? Collections.<EurekaEventListener>emptySet() : eventListeners;
    }
    
    public TransportClientFactories getTransportClientFactories() {
        return transportClientFactories;
    }

    @Inject(optional = true)
    public void setTransportClientFactories(TransportClientFactories transportClientFactories) {
        this.transportClientFactories = transportClientFactories;
    }
    
    public Optional<SSLContext> getSSLContext() {
        return sslContext;
    }

    @Inject(optional = true)
    public void setSSLContext(SSLContext sslContext) {
        this.sslContext = Optional.of(sslContext);
    }
    
    public Optional<HostnameVerifier> getHostnameVerifier() {
        return hostnameVerifier;
    }

    @Inject(optional = true)
    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = Optional.of(hostnameVerifier);
    }
}