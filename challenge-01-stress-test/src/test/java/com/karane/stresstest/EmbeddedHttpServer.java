package com.karane.stresstest;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class EmbeddedHttpServer implements AutoCloseable {

    private final HttpServer server;
    private final AtomicInteger requestCount = new AtomicInteger();
    private final int port;

    public EmbeddedHttpServer(int statusCode, String responseBody, long delayMs) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        
        server.createContext("/", exchange -> {
            requestCount.incrementAndGet();
            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            byte[] bytes = responseBody.getBytes();
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        port = server.getAddress().getPort();
    }

    public String url() {
        return "http://localhost:" + port;
    }

    public int requestCount() {
        return requestCount.get();
    }

    @Override
    public void close() {
        server.stop(0);
    }

}
