package com.netflix.discovery;

import com.netflix.appinfo.ApplicationInfoManager;

/**
 * A handler that can be registered with an {@link EurekaClient} at creation time to execute
 * pre registration logic. The pre registration logic need to be synchronous to be guaranteed
 * to execute before registration.
 * 向eureka-server注册之前的处理器，目前暂未提供默认实现，通过实现该接口，可以在注册之前做一些自定义的处理
 */
public interface PreRegistrationHandler {
    void beforeRegistration();
}
