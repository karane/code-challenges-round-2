package com.karane.stresstest;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StressTestRunner {

    private final HttpRequestExecutor executor;

    public StressTestRunner(HttpRequestExecutor executor) {
        this.executor = executor;
    }

    public StressTestRunner() {
        this(new HttpRequestExecutor());
    }

    public StressTestReport run(StressTestConfig config) throws InterruptedException {
        AtomicInteger remaining = new AtomicInteger(config.totalRequests());
        List<RequestResult> results = new CopyOnWriteArrayList<>();

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            Instant start = Instant.now();

            for (int i = 0; i < config.concurrentUsers(); i++) {
                futures.add(pool.submit(() -> {
                    while (remaining.decrementAndGet() >= 0) {
                        results.add(executor.execute(config));
                    }
                }));
            }

            for (Future<?> f : futures) {
                try { f.get(); } catch (ExecutionException e) { /* individual errors captured in results */ }
            }

            Duration totalDuration = Duration.between(start, Instant.now());
            return StressTestReport.from(results, totalDuration);
        }
    }
}
