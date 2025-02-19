package com.samuel.sniffers.internal;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class Slf4jLoggerTest {

    private ListAppender<ILoggingEvent> listAppender;
    private Slf4jLogger slf4jLogger;

    @BeforeEach
    void setUp() {
        // Get Logback Logger
        Logger logger = (Logger) LoggerFactory.getLogger(Slf4jLoggerTest.class);

        // Create and start a ListAppender
        listAppender = new ListAppender<>();
        listAppender.start();

        // Add appender to the logger
        logger.addAppender(listAppender);

        // Create our slf4jLogger instance
        slf4jLogger = new Slf4jLogger(Slf4jLoggerTest.class);

        // Set the logger level to DEBUG to ensure debug messages are captured
        logger.setLevel(Level.DEBUG);

        // Create our slf4jLogger instance
        slf4jLogger = new Slf4jLogger(Slf4jLoggerTest.class);
    }

    @Test
    void shouldLogError() {
        String message = "Error message";
        slf4jLogger.error(message);

        assertThat(listAppender.list)
                .hasSize(1)
                .first()
                .satisfies(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                    assertThat(event.getFormattedMessage()).isEqualTo(message);
                });
    }

    @Test
    void shouldLogErrorWithException() {
        String message = "Error with exception";
        Exception exception = new RuntimeException("Test exception");

        slf4jLogger.error(message, exception);

        assertThat(listAppender.list)
                .hasSize(1)
                .first()
                .satisfies(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                    assertThat(event.getFormattedMessage()).isEqualTo(message);
                    assertThat(event.getThrowableProxy().getMessage())
                            .isEqualTo(exception.getMessage());
                });
    }

    @Test
    void shouldLogInfo() {
        String message = "Info message";
        slf4jLogger.info(message);

        assertThat(listAppender.list)
                .hasSize(1)
                .first()
                .satisfies(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.INFO);
                    assertThat(event.getFormattedMessage()).isEqualTo(message);
                });
    }

    @Test
    void shouldLogDebug() {
        String message = "Debug message";
        slf4jLogger.debug(message);

        assertThat(listAppender.list)
                .hasSize(1)
                .first()
                .satisfies(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
                    assertThat(event.getFormattedMessage()).isEqualTo(message);
                });
    }
}