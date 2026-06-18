package com.karane.stresstest;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class StressTestRunnerTest {

    @Test
    void runsExactTotalRequests() throws Exception {
        try (EmbeddedHttpServer server = new EmbeddedHttpServer(200, "OK", 0)) {
            StressTestConfig config = StressTestConfig.builder(server.url())
                    .concurrentUsers(5)
                    .totalRequests(20)
                    .build();

            StressTestRunner runner = new StressTestRunner();
            StressTestReport report = runner.run(config);

            assertEquals(20, report.totalRequests());
            assertEquals(20, server.requestCount());
            assertEquals(20, report.successCount());
            assertEquals(0, report.failureCount());
        }
    }

    @Test
    void recordsFailuresOnConnectionError() throws Exception {
        StressTestConfig config = StressTestConfig.builder("http://localhost:1")
                .concurrentUsers(2)
                .totalRequests(4)
                .timeout(Duration.ofMillis(500))
                .build();

        StressTestRunner runner = new StressTestRunner();
        StressTestReport report = runner.run(config);

        assertEquals(4, report.totalRequests());
        assertEquals(4, report.failureCount());
        assertEquals(0, report.successCount());
    }

    @Test
    void handles404StatusAsSuccess() throws Exception {
        try (EmbeddedHttpServer server = new EmbeddedHttpServer(404, "Not Found", 0)) {
            StressTestConfig config = StressTestConfig.builder(server.url())
                    .concurrentUsers(1)
                    .totalRequests(3)
                    .build();

            StressTestRunner runner = new StressTestRunner();
            StressTestReport report = runner.run(config);

            assertEquals(3, report.successCount());
            assertEquals(3L, report.statusCodeDistribution().get(404));
        }
    }

    @Test
    void measuresLatencyForSlowEndpoint() throws Exception {
        try (EmbeddedHttpServer server = new EmbeddedHttpServer(200, "slow", 100)) {
            StressTestConfig config = StressTestConfig.builder(server.url())
                    .concurrentUsers(2)
                    .totalRequests(4)
                    .build();

            StressTestRunner runner = new StressTestRunner();
            StressTestReport report = runner.run(config);

            assertTrue(report.avgLatency().toMillis() >= 90,
                    "Expected avg latency >= 90ms, got: " + report.avgLatency().toMillis());
        }
    }

    @Test
    void singleUserSingleRequest() throws Exception {
        try (EmbeddedHttpServer server = new EmbeddedHttpServer(200, "hello", 0)) {
            StressTestConfig config = StressTestConfig.builder(server.url())
                    .concurrentUsers(1)
                    .totalRequests(1)
                    .build();

            StressTestRunner runner = new StressTestRunner();
            StressTestReport report = runner.run(config);

            assertEquals(1, report.totalRequests());
            assertEquals(1, report.successCount());
            assertTrue(report.requestsPerSecond() > 0);
        }
    }
}
