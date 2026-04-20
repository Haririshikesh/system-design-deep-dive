package com.architect.idgen.core;

import lombok.extern.slf4j.Slf4j;
import java.time.Instant;

/**
 * Snowflake ID Generator implementation.
 * Format: [1-bit sign] [41-bit timestamp] [10-bit worker id] [12-bit sequence]
 */
@Slf4j
public class SnowflakeGenerator {

    // Epoch: 2024-04-20T00:00:00Z
    private final long epoch = 1713571200000L;

    private final long workerIdBits = 10L;
    private final long sequenceBits = 12L;

    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private final long workerIdShift = sequenceBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits;

    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeGenerator(long workerId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("Worker ID must be between 0 and %d", maxWorkerId));
        }
        this.workerId = workerId;
        log.info("Initialized Snowflake Generator with Worker ID: {}", workerId);
    }

    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            log.error("Clock moved backwards. Refusing to generate ID for {}ms", offset);
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", offset));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                // Sequence overflow, wait for next millisecond
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - epoch) << timestampLeftShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
