package com.fitcore.api.global.devlog;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class InMemoryLogBuffer {
    private static final int MAX_LINES = 300;
    private final ArrayDeque<String> lines = new ArrayDeque<>();

    public synchronized void append(String line) {
        if (line == null || line.isBlank()) {
            return;
        }

        lines.addLast(line);
        while (lines.size() > MAX_LINES) {
            lines.removeFirst();
        }
    }

    public synchronized List<String> tail(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_LINES));
        int skip = Math.max(0, lines.size() - safeLimit);
        return new ArrayList<>(lines).subList(skip, lines.size());
    }
}
