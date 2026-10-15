package com.huertos.sensorservice.config;

import com.huertos.sensorservice.client.ParcelServiceClient.ParcelServiceClientProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ParcelServiceClientConfiguration {
    @Bean
    ParcelServiceClientProperties parcelServiceClientProperties(
            @Value("${services.parcel-service.base-url}") String baseUrl,
            @Value("${services.parcel-service.connect-timeout}") Duration connectTimeout,
            @Value("${services.parcel-service.read-timeout}") Duration readTimeout
    ) {
        return new ParcelServiceClientProperties(baseUrl, connectTimeout, readTimeout);
    }
}
