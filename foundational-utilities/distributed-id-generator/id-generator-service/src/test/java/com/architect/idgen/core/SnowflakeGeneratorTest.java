package com.architect.idgen.core;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeGeneratorTest {

    @Test
    void testNextId_Uniqueness() {
        SnowflakeGenerator generator = new SnowflakeGenerator(1);
        int count = 10000;
        Set<Long> ids = new HashSet<>();
        
        for (int i = 0; i < count; i++) {
            long id = generator.nextId();
            assertFalse(ids.contains(id), "Duplicate ID found: " + id);
            ids.add(id);
        }
    }

    @Test
    void testNextId_Concurrency() throws InterruptedException {
        SnowflakeGenerator generator = new SnowflakeGenerator(1);
        int threadCount = 10;
        int idsPerThread = 1000;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < idsPerThread; j++) {
                        ids.add(generator.nextId());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(threadCount * idsPerThread, ids.size(), "Should have generated unique IDs across all threads");
    }

    @Test
    void testNextId_Monotonicity() {
        SnowflakeGenerator generator = new SnowflakeGenerator(1);
        long id1 = generator.nextId();
        long id2 = generator.nextId();
        assertTrue(id2 > id1, "IDs should be monotonically increasing");
    }
}
