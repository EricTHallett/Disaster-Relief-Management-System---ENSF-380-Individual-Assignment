package edu.ucalgary.oop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton logger that records all user-driven data changes to a plain-text
 * action log file. Each entry is timestamped and labelled with the action type
 * (ADDED, UPDATED, SOFT DELETED, HARD DELETED). Application errors are written
 * to a separate error log and never appear in the action log.
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-25
 */
public class ActionLogger {

    private static ActionLogger instance;

    private static final Path ACTION_LOG_PATH =
        Paths.get("data", "action_log.txt");
    private static final Path ERROR_LOG_PATH =
        Paths.get("data", "error_log.txt");
    private static final DateTimeFormatter DATE_TIME_FORMATTED =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Private constructor. Creates the {@code data/} directory if it does not
     * already exist.
     */
    private ActionLogger() {
        new java.io.File("data").mkdirs();
    }

    /**
     * Returns the single instance of {@code ActionLogger}, creating it on the
     * first call.
     *
     * @return the singleton {@code ActionLogger} instance
     */
    public static ActionLogger getInstance() {
        if (instance == null) {
            instance = new ActionLogger();
        }
        return instance;
    }

    /**
     * Logs the addition of a new entity to the action log.
     *
     * @param entityType  human-readable type of the entity
     *                    (e.g., "disaster victim")
     * @param entityId    the database ID assigned to the new entity
     * @param description a brief description of the added entity
     */
    public void logAdded(String entityType, int entityId, String description) {
        write("[" + now() + "] ADDED " +
            entityType + " " + entityId + " | " + description);
    }

    /**
     * Logs an update to an existing entity in the action log.
     *
     * @param entityType  human-readable type of the entity
     *                    (e.g., "disaster victim")
     * @param entityId    the database ID of the updated entity
     * @param description a brief description of what changed
     */
    public void logUpdated(String entityType, int entityId,
            String description) {
        write("[" + now() + "] UPDATED " +
            entityType + " " + entityId + " | " + description);
    }

    /**
     * Logs a soft deletion of an entity in the action log.
     *
     * @param entityType  human-readable type of the entity
     *                    (e.g., "disaster victim")
     * @param entityId    the database ID of the soft-deleted entity
     * @param description a brief description of the entity that was archived
     */
    public void logSoftDeleted(String entityType, int entityId,
            String description) {
        write("[" + now() + "] SOFT DELETED " +
            entityType + " " + entityId + " | " + description);
    }

    /**
     * Logs a hard (permanent) deletion of an entity in the action log.
     *
     * @param entityType  human-readable type of the entity
     *                    (e.g., "disaster victim")
     * @param entityId    the database ID of the permanently deleted entity
     * @param description a brief description of the entity that was deleted
     */
    public void logDeleted(String entityType, int entityId,
            String description) {
        write("[" + now() + "] HARD DELETED " +
            entityType + " " + entityId + " | " + description);
    }

    /**
     * Logs an unrecoverable application error to the error log file
     * ({@code data/error_log.txt}). This method writes to a separate file from
     * the action log so that application errors never contaminate the audit
     * trail of user-driven changes.
     *
     * @param message   a description of the error context
     * @param throwable the exception that caused the failure, or {@code null}
     *                  if no exception is available
     */
    public void logError(String message, Throwable throwable) {
        String entry = "[" + now() + "] ERROR | " + message
            + (throwable != null
                ? " | " + throwable.getClass().getName()
                  + ": " + throwable.getMessage()
                : "");
        try {
            Files.createDirectories(ERROR_LOG_PATH.getParent());
            Files.writeString(ERROR_LOG_PATH, entry
                + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println(
                "Could not write to " + ERROR_LOG_PATH
                + ": " + e.getMessage());
        }
    }

    /**
     * Returns a formatted timestamp string for the current date and time.
     *
     * @return the current date and time formatted as
     *         {@code yyyy-MM-dd HH:mm:ss}
     */
    private String now() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTED);
    }

    /**
     * Appends a single entry to the action log file. Prints a warning to
     * standard output if the file cannot be written.
     *
     * @param entry the complete log line to append
     */
    private void write(String entry) {
        try {
            Files.createDirectories(ACTION_LOG_PATH.getParent());
            Files.writeString(ACTION_LOG_PATH, entry
                + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println(
                "Could not write to " + ACTION_LOG_PATH
                + ": " + e.getMessage());
        }
    }
}