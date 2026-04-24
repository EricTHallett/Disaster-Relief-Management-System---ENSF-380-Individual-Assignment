package edu.ucalgary.oop;

import java.sql.SQLException;

/**
 * Entry point for the Disaster Relief Management System. Establishes the
 * database connection, initializes the application controller, and launches
 * the command-line interface. Unrecoverable startup errors are reported to the
 * user and appended to {@code data/error_log.txt} before the program exits.
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-22
 */
public final class Main {

    /**
     * Prevents instantiation of this utility class.
     */
    private Main() {
    }

    /**
     * Application entry point.
     *
     * <p>Attempts to connect to the PostgreSQL database and initialize the
     * {@link DisasterReliefController}. If either step fails, an error message
     * is printed to standard error, the failure is logged to
     * {@code data/error_log.txt}, and the program exits cleanly.</p>
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        ActionLogger logger = ActionLogger.getInstance();

        DatabaseQueryInterface db;

        try {
            DatabaseConnection dbConn = DatabaseConnection.getInstance();
            db = new DatabaseQuery(dbConn.getConnection());
        } catch (SQLException e) {
            System.err.println(
                "Could not connect to the database. Ending application.");
            logger.logError("Database connection failed at startup", e);
            return;
        }

        DisasterReliefController controller;

        try {
            controller = new DisasterReliefController(db);
        } catch (Exception e) {
            System.err.println(
                "Could not initialize the application. Ending application.");
            System.err.println(e.getMessage());
            logger.logError("Application initialization failed", e);
            return;
        }

        CLI cli = new CLI(controller);
        cli.start();
    }
}