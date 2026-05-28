package com.fitcore.api.global.devlog;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Profile("!prod")
@RestController
@RequiredArgsConstructor
public class DevLogController {
    private final InMemoryLogBuffer logBuffer;

    @GetMapping("/api/dev/logs")
    public List<String> getLogs(@RequestParam(defaultValue = "120") int limit) {
        if ("production".equalsIgnoreCase(System.getenv("APP_ENV"))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return logBuffer.tail(limit);
    }
}
