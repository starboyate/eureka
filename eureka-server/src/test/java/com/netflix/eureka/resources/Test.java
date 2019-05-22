package com.netflix.eureka.resources;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.providers.DefaultEurekaClientConfigProvider;


public class Test {
    public static void main(String[] args) {
        System.setProperty("NETFLIX_APP_GROUP", "EUREKA_GROUP");
        TEurekaInstanceConfig instanceConfig = new TEurekaInstanceConfig("eureka.");
        InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
        ApplicationInfoManager manager = new ApplicationInfoManager(instanceConfig, instanceInfo);
        DefaultEurekaClientConfigProvider provider = new DefaultEurekaClientConfigProvider();
        EurekaClientConfig clientConfig = provider.get();
        EurekaClient eurekaClient = new DiscoveryClient(manager, clientConfig);
        System.out.println(eurekaClient);
    }
}
