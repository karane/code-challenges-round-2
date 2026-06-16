package com.karane.stresstest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

public class HttpRequestExecutor {

    private final HttpClient httpClient;

    public HttpRequestExecutor(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpRequestExecutor() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    public RequestResult execute(StressTestConfig config) {
        Instant start = Instant.now();
        try {
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(config.url()))
                    .timeout(config.timeout());

            config.headers().forEach(reqBuilder::header);

            HttpRequest.BodyPublisher bodyPublisher = config.body() != null
                    ? HttpRequest.BodyPublishers.ofString(config.body())
                    : HttpRequest.BodyPublishers.noBody();

            reqBuilder.method(config.method(), bodyPublisher);

            HttpResponse<Void> response = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.discarding());
            Duration latency = Duration.between(start, Instant.now());
            return RequestResult.ok(response.statusCode(), latency);
        } catch (Exception e) {
            Duration latency = Duration.between(start, Instant.now());
            return RequestResult.failed(latency, e.getMessage());
        }
    }
}
