package com.fitcore.api.global.devlog;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

@Profile("!prod")
@Configuration
@RequiredArgsConstructor
public class DevLogConfig {
    private static final String APPENDER_NAME = "FIT_CORE_DEV_LOG_BUFFER";

    private final InMemoryLogBuffer logBuffer;

    @PostConstruct
    public void attachAppender() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

        if (rootLogger.getAppender(APPENDER_NAME) != null) {
            return;
        }

        InMemoryLogbackAppender appender = new InMemoryLogbackAppender(logBuffer);
        appender.setContext(context);
        appender.setName(APPENDER_NAME);
        appender.start();
        rootLogger.addAppender(appender);

        logBuffer.append("dev-log appender attached");
    }
}
