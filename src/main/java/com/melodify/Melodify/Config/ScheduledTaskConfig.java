package com.melodify.Melodify.Config;

import com.melodify.Melodify.Utils.RateLimiter;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableScheduling
public class ScheduledTaskConfig {
    private final RateLimiter rateLimiter;

    @Autowired
    public ScheduledTaskConfig(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Scheduled(fixedRate = 60 * 1000) // Reset every minute
    public void resetRateLimiter() {
        rateLimiter.reset();
    }
}
