package ru.network.anonymizer_msgs.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(MafiaClientProperties.class)
public class RestClientConfig {

    /**
     * Один VT-executor на всё приложение: и для наших задач,
     * и для HttpClient внутри RestClient.
     * destroyMethod = "close" — корректно закроется при shutdown контекста.
     */
    @Bean(name = "mafiaSendExecutor", destroyMethod = "close")
    public ExecutorService mafiaSendExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory(
            @Qualifier("mafiaSendExecutor") ExecutorService mafiaSendExecutor) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .executor(mafiaSendExecutor)          // переиспользуем тот же VT-executor
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(3)); // read timeout ставится на фабрике, не на HttpClient
        return factory;
    }

    @Bean
    public RestClient restClient(@Qualifier("clientHttpRequestFactory") ClientHttpRequestFactory factory) {
        return RestClient.builder()
                .requestFactory(factory)              // ← используем тот же бин
                .build();
    }
}