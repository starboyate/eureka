package com.netflix.eureka.resources;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.providers.DefaultEurekaClientConfigProvider;
import com.netflix.discovery.shared.resolver.DefaultEndpoint;
import com.netflix.discovery.shared.transport.EurekaHttpClient;
import com.netflix.discovery.shared.transport.EurekaHttpResponse;
import com.netflix.discovery.shared.transport.jersey.Jersey1DiscoveryClientOptionalArgs;
import com.netflix.discovery.shared.transport.jersey.JerseyEurekaHttpClientFactory;


public class Test {


    public static void main(String[] args) {

        System.setProperty("NETFLIX_APP_GROUP", "EUREKA_GROUP");
        TEurekaInstanceConfig instanceConfig = new TEurekaInstanceConfig("eureka.");
        InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
        ApplicationInfoManager manager = new ApplicationInfoManager(instanceConfig, instanceInfo);
        DefaultEurekaClientConfigProvider provider = new DefaultEurekaClientConfigProvider();
        EurekaClientConfig clientConfig = provider.get();
//        EurekaClient eurekaClient = new DiscoveryClient(manager, clientConfig);
        JerseyEurekaHttpClientFactory httpClientFactory = JerseyEurekaHttpClientFactory.newBuilder()
                .withClientName("testEurekaClient")
                .withConnectionTimeout(1000)
                .withReadTimeout(1000)
                .withMaxConnectionsPerHost(1)
                .withMaxTotalConnections(1)
                .withConnectionIdleTimeout(1000)
                .build();
        EurekaHttpClient httpClient = httpClientFactory.newClient(new DefaultEndpoint("http://localhost:8080/v2"));
        EurekaHttpResponse<Void> httpResponse = httpClient.register(instanceInfo);
        System.out.println(httpResponse.getStatusCode());
    }



}
