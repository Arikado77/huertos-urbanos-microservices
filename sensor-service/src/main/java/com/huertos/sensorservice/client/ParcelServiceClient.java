package com.huertos.sensorservice.client;

import com.huertos.sensorservice.exception.ResourceNotFoundException;
import com.huertos.sensorservice.exception.UpstreamServiceException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;

@Component
public class ParcelServiceClient {
    private final RestClient restClient;

    public ParcelServiceClient(
            RestClient.Builder builder,
            ParcelServiceClientProperties properties
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public void assertParcelExists(Long parcelId) {
        try {
            restClient.get()
                    .uri("/api/v1/parcels/{id}", parcelId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Parcela", parcelId);
            }
            throw new UpstreamServiceException(
                    "parcel-service respondió con HTTP " + exception.getStatusCode().value(),
                    exception
            );
        } catch (RestClientException exception) {
            throw new UpstreamServiceException("No fue posible comunicarse con parcel-service", exception);
        }
    }

    public record ParcelServiceClientProperties(
            String baseUrl,
            Duration connectTimeout,
            Duration readTimeout
    ) {
    }
}
