package com.fitcore.api.global.devlog;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("!prod")
@RestController
@RequiredArgsConstructor
public class DevLogController {
    private final InMemoryLogBuffer logBuffer;

    @GetMapping("/api/dev/logs")
    public List<String> getLogs(@RequestParam(defaultValue = "120") int limit) {
        return logBuffer.tail(limit);
    }
}
