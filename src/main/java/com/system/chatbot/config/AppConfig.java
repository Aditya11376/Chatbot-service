package com.system.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {
    @Value("${gemini.service.url}")
    private String geminiServiceUrl;

    @Bean
    public RestClient geminiRestClient(){
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(60000);
        requestFactory.setReadTimeout(60000);

        return RestClient.builder()
                .baseUrl(geminiServiceUrl)
                .requestFactory(requestFactory).build();
    }

}
