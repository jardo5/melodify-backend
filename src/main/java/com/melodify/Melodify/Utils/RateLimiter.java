package com.melodify.Melodify.Utils;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {
    private final long permitsPerSecond;
    private long nextAvailablePermit;

    public RateLimiter(@Value("${rateLimiter.permitsPerSecond:10}") long permitsPerSecond) { // Default value is 10
        this.permitsPerSecond = permitsPerSecond;
        this.nextAvailablePermit = System.nanoTime();
    }

    public synchronized boolean tryAcquire() {
        long now = System.nanoTime();
        if (now >= nextAvailablePermit) {
            nextAvailablePermit = now + TimeUnit.SECONDS.toNanos(1) / permitsPerSecond;
            return true;
        }
        return false;
    }

    public synchronized void reset() {
        nextAvailablePermit = System.nanoTime();
    }
}
