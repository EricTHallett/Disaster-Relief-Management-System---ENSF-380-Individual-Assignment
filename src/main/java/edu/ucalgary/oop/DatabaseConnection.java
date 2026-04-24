package edu.ucalgary.oop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton that manages the single JDBC connection to the PostgreSQL database.
 * <p>
 * Connection parameters are read from {@code /db.properties} on the classpath
 * (located in {@code src/main/resources/}). If the file is absent or a key is
 * missing, sensible defaults are used. The connection is lazily created and
 * automatically re-established if it has been closed.
 * </p>
 *
 * @author Eric Hallett (30117108)
 * @version 1.0
 * @since 2026-03-25
 */
public class DatabaseConnection {

    /** The single shared instance. */
    private static DatabaseConnection instance;

    /** The underlying JDBC connection. */
    private final Connection connection;

    /** Classpath location of the database configuration file. */
    private static final String CONFIG_FILE = "/db.properties";

    /**
     * Private constructor. Reads connection properties from the config file
     * and opens the JDBC connection.
     *
     * @throws SQLException if the JDBC connection cannot be established
     */
    private DatabaseConnection() throws SQLException {
        Map<String, String> props = loadProperties();
        String host     = props.getOrDefault("host", "localhost");
        String port     = props.getOrDefault("port", "5432");
        String database = props.getOrDefault("database", "ensf380project");
        String username = props.getOrDefault("username", "oop");
        String password = props.getOrDefault("password", "ucalgary");

        String url = "jdbc:postgresql://" + host + ":" + port
            + "/" + database;
        this.connection = DriverManager.getConnection(url, username, password);
    }

    /**
     * Returns the singleton {@code DatabaseConnection}, creating or
     * re-establishing it if necessary.
     *
     * @return the singleton {@code DatabaseConnection} instance
     * @throws SQLException if a new connection cannot be opened
     */
    public static synchronized DatabaseConnection getInstance()
            throws SQLException {
        if (instance == null || instance.connection == null
                || instance.connection.isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns the underlying {@link Connection} object for use in SQL
     * operations.
     *
     * @return the active JDBC {@code Connection}
     */
    public Connection getConnection() { 
        return connection;
    }

    /**
     * Closes the underlying JDBC connection and clears the singleton instance
     * so that the next call to {@link #getInstance()} will open a fresh
     * connection.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
        } finally {
            instance = null;
        }
    }

    /**
     * Reads {@code key=value} pairs from the classpath config file into a map.
     * Lines that are blank or begin with {@code #} are ignored.
     *
     * @return a {@code Map} of property keys to their string values;
     *         empty if the file cannot be found or read
     */
    private Map<String, String> loadProperties() {
        Map<String, String> properties = new HashMap<>();
        try (InputStream is =
                DatabaseConnection.class.getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                return properties;
            }
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int index = line.indexOf('=');
                if (index > 0) {
                    properties.put(line.substring(0, index).trim(),
                        line.substring(index + 1).trim());
                }
            }
        } catch (IOException e) {
        }
        return properties;
    }
}