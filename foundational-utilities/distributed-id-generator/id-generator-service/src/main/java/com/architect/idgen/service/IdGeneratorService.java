package com.architect.idgen.service;

import com.architect.idgen.core.SnowflakeGenerator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IdGeneratorService {

    @Value("${idgen.worker-id:1}")
    private long workerId;

    private SnowflakeGenerator generator;

    @PostConstruct
    public void init() {
        this.generator = new SnowflakeGenerator(workerId);
    }

    public long generateId() {
        return generator.nextId();
    }

    public List<Long> generateIds(int count) {
        if (count <= 0 || count > 1000) {
            throw new IllegalArgumentException("Count must be between 1 and 1000");
        }
        List<Long> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(generator.nextId());
        }
        return ids;
    }
}
