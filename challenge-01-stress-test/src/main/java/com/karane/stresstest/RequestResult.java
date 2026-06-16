package com.karane.stresstest;

import java.time.Duration;

public record RequestResult(
        int statusCode,
        Duration latency,
        boolean success,
        String errorMessage
) {
    public static RequestResult ok(int statusCode, Duration latency) {
        return new RequestResult(statusCode, latency, true, null);
    }

    public static RequestResult failed(Duration latency, String errorMessage) {
        return new RequestResult(-1, latency, false, errorMessage);
    }

}
