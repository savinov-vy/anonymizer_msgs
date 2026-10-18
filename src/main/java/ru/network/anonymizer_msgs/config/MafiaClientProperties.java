package ru.network.anonymizer_msgs.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mafia.client")
public class MafiaClientProperties {
    private String baseUrl;
    private long retryDelayMs = 1000;
}
