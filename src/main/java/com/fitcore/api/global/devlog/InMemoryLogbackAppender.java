package com.fitcore.api.global.devlog;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class InMemoryLogbackAppender extends AppenderBase<ILoggingEvent> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
        .ofPattern("HH:mm:ss.SSS")
        .withZone(ZoneId.systemDefault());

    private final InMemoryLogBuffer buffer;

    public InMemoryLogbackAppender(InMemoryLogBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        String line = "%s %-5s [%s] %s".formatted(
            FORMATTER.format(Instant.ofEpochMilli(eventObject.getTimeStamp())),
            eventObject.getLevel(),
            eventObject.getLoggerName(),
            eventObject.getFormattedMessage()
        );
        buffer.append(line);
    }
}
