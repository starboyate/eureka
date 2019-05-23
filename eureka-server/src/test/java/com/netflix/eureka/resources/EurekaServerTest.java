package com.netflix.eureka.resources;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Applications;
import com.netflix.discovery.shared.resolver.DefaultEndpoint;
import com.netflix.discovery.shared.transport.EurekaHttpClient;
import com.netflix.discovery.shared.transport.EurekaHttpResponse;
import com.netflix.discovery.shared.transport.TransportClientFactory;
import com.netflix.discovery.shared.transport.jersey.JerseyEurekaHttpClientFactory;
import com.netflix.eureka.EurekaServerConfig;
import com.netflix.eureka.transport.JerseyReplicationClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FilenameFilter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class EurekaServerTest {

    private static final String[] EUREKA1_WAR_DIRS = {"build/libs", "eureka-server/build/libs"};

    private static final Pattern WAR_PATTERN = Pattern.compile("eureka-server.*.war");

    private static EurekaServerConfig eurekaServerConfig;

    private static Server server;

    private static String eurekaServiceUrl;

    private static TransportClientFactory httpClientFactory;

    private static EurekaHttpClient jerseyEurekaClient;

    private static JerseyReplicationClient jerseyReplicationClient;

    public static void main(String[] args) throws Exception {

        injectEurekaConfiguration();
        startServer();
        createEurekaServerConfig();
        httpClientFactory = JerseyEurekaHttpClientFactory.newBuilder()
                .withClientName("testEurekaClient")
                .withConnectionTimeout(1000)
                .withReadTimeout(1000)
                .withMaxConnectionsPerHost(1)
                .withMaxTotalConnections(1)
                .withConnectionIdleTimeout(1000)
                .build();

        jerseyEurekaClient = httpClientFactory.newClient(new DefaultEndpoint(eurekaServiceUrl));

        ServerCodecs serverCodecs = new DefaultServerCodecs(eurekaServerConfig);
        jerseyReplicationClient = JerseyReplicationClient.createReplicationClient(
                eurekaServerConfig,
                serverCodecs,
                eurekaServiceUrl
        );
        while (true) {
//            try {
//                TimeUnit.SECONDS.sleep(10);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            EurekaHttpResponse<InstanceInfo> response = jerseyEurekaClient.getInstance("DESKTOP-EQVNGAH");
//            InstanceInfo instanceInfo = response.getEntity();
//            System.out.println(instanceInfo.getHostName());
//            System.out.println(instanceInfo.getAppName());
//            System.out.println(instanceInfo.getIPAddr());
        }

    }


    private static void injectEurekaConfiguration() throws UnknownHostException {
        String myHostName = InetAddress.getLocalHost().getHostName();
        String myServiceUrl = "http://" + myHostName + ":8080/v2/";

        System.setProperty("eureka.region", "default");
        System.setProperty("eureka.name", "eureka");
        System.setProperty("eureka.vipAddress", "eureka.mydomain.net");
        System.setProperty("eureka.port", "8080");
        System.setProperty("eureka.preferSameZone", "false");
        System.setProperty("eureka.shouldUseDns", "false");
        System.setProperty("eureka.shouldFetchRegistry", "false");
        System.setProperty("eureka.serviceUrl.defaultZone", myServiceUrl);
        System.setProperty("eureka.serviceUrl.default.defaultZone", myServiceUrl);
        System.setProperty("eureka.awsAccessId", "fake_aws_access_id");
        System.setProperty("eureka.awsSecretKey", "fake_aws_secret_key");
        System.setProperty("eureka.numberRegistrySyncRetries", "0");
    }

    private static void startServer() throws Exception {

        File warFile = findWar();
        server = new Server(8080);
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar(warFile.getAbsolutePath());
        server.setHandler(webapp);
        server.start();
        eurekaServiceUrl = "http://localhost:8080/v2";
    }

    private static File findWar() {
        File dir = null;
        for (String candidate : EUREKA1_WAR_DIRS) {
            File candidateFile = new File(candidate);
            if (candidateFile.exists()) {
                dir = candidateFile;
                break;
            }
        }
        if (dir == null) {
            throw new IllegalStateException("No directory found at any in any pre-configured location: " + Arrays.toString(EUREKA1_WAR_DIRS));
        }

        File[] warFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return WAR_PATTERN.matcher(name).matches();
            }
        });
        if (warFiles.length == 0) {
            throw new IllegalStateException("War file not found in directory " + dir);
        }
        if (warFiles.length > 1) {
            throw new IllegalStateException("Multiple war files found in directory " + dir + ": " + Arrays.toString(warFiles));
        }
        return warFiles[0];
    }

    private static void createEurekaServerConfig() {
        eurekaServerConfig = mock(EurekaServerConfig.class);

        // Cluster management related
        when(eurekaServerConfig.getPeerEurekaNodesUpdateIntervalMs()).thenReturn(1000);

        // Replication logic related
        when(eurekaServerConfig.shouldSyncWhenTimestampDiffers()).thenReturn(true);
        when(eurekaServerConfig.getMaxTimeForReplication()).thenReturn(1000);
        when(eurekaServerConfig.getMaxElementsInPeerReplicationPool()).thenReturn(10);
        when(eurekaServerConfig.getMinThreadsForPeerReplication()).thenReturn(1);
        when(eurekaServerConfig.getMaxThreadsForPeerReplication()).thenReturn(1);
        when(eurekaServerConfig.shouldBatchReplication()).thenReturn(true);

        // Peer node connectivity (used by JerseyReplicationClient)
        when(eurekaServerConfig.getPeerNodeTotalConnections()).thenReturn(1);
        when(eurekaServerConfig.getPeerNodeTotalConnectionsPerHost()).thenReturn(1);
        when(eurekaServerConfig.getPeerNodeConnectionIdleTimeoutSeconds()).thenReturn(1000);
    }
}
