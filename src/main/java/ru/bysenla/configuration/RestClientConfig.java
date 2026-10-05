package ru.bysenla.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@Configuration
@EnableScheduling
public class RestClientConfig {
    @Bean
    public RestClient spellerRestClient(RestClient.Builder builder,
                                        @Value("${speller.base-url}") String baseUrl)
    {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
