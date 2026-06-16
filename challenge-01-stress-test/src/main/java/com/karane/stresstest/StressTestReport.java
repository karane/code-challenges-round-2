package com.karane.stresstest;

import java.time.Duration;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.stream.Collectors;

public record StressTestReport(
        int totalRequests,
        int successCount,
        int failureCount,
        Duration minLatency,
        Duration maxLatency,
        Duration avgLatency,
        Duration p50Latency,
        Duration p90Latency,
        Duration p99Latency,
        double requestsPerSecond,
        Duration totalDuration,
        Map<Integer, Long> statusCodeDistribution
) {
    public static StressTestReport from(List<RequestResult> results, Duration totalDuration) {
        int total = results.size();
        int success = (int) results.stream().filter(RequestResult::success).count();
        int failure = total - success;

        List<Long> latenciesMs = results.stream()
                .map(r -> r.latency().toMillis())
                .sorted()
                .toList();

        LongSummaryStatistics stats = latenciesMs.stream().mapToLong(Long::longValue).summaryStatistics();

        Map<Integer, Long> statusDist = results.stream()
                .filter(RequestResult::success)
                .collect(Collectors.groupingBy(RequestResult::statusCode, Collectors.counting()));

        double rps = totalDuration.toMillis() > 0
                ? (total * 1000.0) / totalDuration.toMillis()
                : 0;

        return new StressTestReport(
                total,
                success,
                failure,
                Duration.ofMillis(stats.getMin()),
                Duration.ofMillis(stats.getMax()),
                Duration.ofMillis((long) stats.getAverage()),
                percentile(latenciesMs, 50),
                percentile(latenciesMs, 90),
                percentile(latenciesMs, 99),
                rps,
                totalDuration,
                statusDist
        );
    }

    private static Duration percentile(List<Long> sorted, int p) {
        if (sorted.isEmpty()) return Duration.ZERO;
        int index = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return Duration.ofMillis(sorted.get(Math.max(0, index)));
    }

    @Override
    public String toString() {
        return """
                ===== Stress Test Report =====
                Total Requests  : %d
                Successes       : %d
                Failures        : %d
                Total Duration  : %dms
                Requests/sec    : %.2f
                Min Latency     : %dms
                Avg Latency     : %dms
                P50 Latency     : %dms
                P90 Latency     : %dms
                P99 Latency     : %dms
                Max Latency     : %dms
                Status Codes    : %s
                ==============================
                """.formatted(
                totalRequests, successCount, failureCount,
                totalDuration.toMillis(), requestsPerSecond,
                minLatency.toMillis(), avgLatency.toMillis(),
                p50Latency.toMillis(), p90Latency.toMillis(), p99Latency.toMillis(),
                maxLatency.toMillis(), statusCodeDistribution
        );
    }
}
