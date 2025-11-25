package uk.gov.hmcts.reform.hmc.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
class RestTemplateConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(RestTemplateConfiguration.class);

    private PoolingHttpClientConnectionManager cm;

    @Value("${http.client.max.total}")
    private int maxTotalHttpClient;

    @Value("${http.client.seconds.idle.connection}")
    private int maxSecondsIdleConnection;

    @Value("${http.client.max.client_per_route}")
    private int maxClientPerRoute;

    @Value("${http.client.validate.after.inactivity}")
    private int validateAfterInactivity;

    @Value("${http.client.connection.timeout}")
    private int connectionTimeout;

    @Value("${http.client.read.timeout}")
    private int readTimeout;


    @Bean(name = "restTemplate")
    public RestTemplate restTemplate() {
        final RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory =
            new HttpComponentsClientHttpRequestFactory(getHttpClient());
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }

    private HttpClient getHttpClient() {
        return getHttpClient(connectionTimeout);
    }

    private HttpClient getHttpClient(final int timeout) {
        cm = new PoolingHttpClientConnectionManager();

        LOG.info("maxTotalHttpClient: {}", maxTotalHttpClient);
        LOG.info("maxSecondsIdleConnection: {}", maxSecondsIdleConnection);
        LOG.info("maxClientPerRoute: {}", maxClientPerRoute);
        LOG.info("validateAfterInactivity: {}", validateAfterInactivity);
        LOG.info("connectionTimeout: {}", timeout);
        LOG.info("readTimeout: {}", readTimeout);

        cm.setConnectionConfigResolver((route) -> ConnectionConfig.custom()
            .setConnectTimeout(Timeout.of(timeout, TimeUnit.MILLISECONDS))
            .setSocketTimeout(Timeout.of(timeout, TimeUnit.MILLISECONDS))
            .setValidateAfterInactivity(Timeout.of(validateAfterInactivity, TimeUnit.MILLISECONDS))
            .build()
        );

        cm.setSocketConfigResolver((route) -> SocketConfig.custom()
            .setSoTimeout(Timeout.of(readTimeout, TimeUnit.MILLISECONDS))
            
            .build()
        );

        cm.setMaxTotal(maxTotalHttpClient);
        cm.closeIdle(Timeout.of(maxSecondsIdleConnection, TimeUnit.SECONDS));
        cm.setDefaultMaxPerRoute(maxClientPerRoute);
        final RequestConfig
            config =
            RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(timeout, TimeUnit.MILLISECONDS))
                .build();

        return HttpClientBuilder.create()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .setConnectionManager(cm)
            .build();
    }
}
