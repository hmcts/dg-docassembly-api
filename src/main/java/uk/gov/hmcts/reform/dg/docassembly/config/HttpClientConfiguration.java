package uk.gov.hmcts.reform.dg.docassembly.config;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfiguration {

    @Value("${httpclient.call-timeout}")
    private int timeout;

    @Value("${httpclient.connection-timeout}")
    private int connectionTimeout;

    @Bean
    public OkHttpClient okHttpClient() {

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(100);
        dispatcher.setMaxRequestsPerHost(100);
        return new OkHttpClient
                .Builder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .dispatcher(dispatcher)
                .build();
    }

}
