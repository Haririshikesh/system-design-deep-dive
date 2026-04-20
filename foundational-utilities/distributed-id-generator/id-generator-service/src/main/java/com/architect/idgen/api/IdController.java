package com.architect.idgen.api;

import com.architect.idgen.service.IdGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ids")
@RequiredArgsConstructor
public class IdController {

    private final IdGeneratorService idGeneratorService;

    @GetMapping("/next")
    public long getNextId() {
        return idGeneratorService.generateId();
    }

    @GetMapping("/bulk")
    public List<Long> getBulkIds(@RequestParam(defaultValue = "10") int count) {
        return idGeneratorService.generateIds(count);
    }
}
