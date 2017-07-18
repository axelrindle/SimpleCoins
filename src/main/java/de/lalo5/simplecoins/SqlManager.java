package de.lalo5.simplecoins;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static de.lalo5.simplecoins.SimpleCoins.CONSOLEPREFIX;
import static de.lalo5.simplecoins.SimpleCoins.LOGGER;

/**
 * Helper-class to interact with the MySQL Database.
 */
class SqlManager {

    /** The database host. */
    private String host;

    /** The database port. */
    private int port;

    /** The database name to create tables in. Usually "<b>minecraft</b>". */
    private String databaseName;

    /** The table name to store the data in. */
    private String tableName;

    /** A user having modify access to the database. */
    private String username;

    /** The password for the user. */
    private String password;

    /** Connection instance. */
    private Connection conn;

    /**
     * Default constructor with all connection relevant fields.
     *
     * @param host The database host.
     * @param port The database port.
     * @param databaseName The database name to create tables in.
     * @param tableName  The table name to store the data in.
     * @param username  A user having modify access to the database.
     * @param password  The password for the user.
     */
    SqlManager(String host, int port, String databaseName, String tableName, String username, String password) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.username = username;
        this.password = password;
    }

    /**
     * Tries to open a connection to the database.
     *
     * @throws SQLException If an exception occurs while trying to connect.
     */
    void connect() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName;
        conn = DriverManager.getConnection(url, username, password);

        LOGGER.info(CONSOLEPREFIX + "Successfully connected to the MySQL database.");
    }

    /**
     * Creates a new table with the specified name (see {@link #tableName} and structure required for storing the account data.
     *
     * @throws SQLException If an exception occurs while trying to create the table.
     */
    void createTable() throws SQLException {
        executeStatement(
                "CREATE TABLE IF NOT EXISTS `" + databaseName +"`.`" + tableName + "` " +
                        "( `UUID` TEXT NOT NULL COMMENT 'Players UUID' , " +
                        "`Name` TEXT NOT NULL COMMENT 'Players Name' , " +
                        "`Coins` DOUBLE NOT NULL COMMENT 'Players amount of coins' , " +
                        "UNIQUE `UUID` (`UUID`(36))) " +
                        "ENGINE = InnoDB CHARSET=utf8 COLLATE utf8_general_ci COMMENT = 'SimpleCoins account storage';"
        );
        LOGGER.info("Created table " + tableName + ".");
    }

    /**
     * Closes the connection.
     *
     * @throws SQLException If an exception occurs while trying to close the connection.
     */
    void disconnect() throws SQLException {
        conn.close();
        LOGGER.info(CONSOLEPREFIX + "Successfully disconnected from the MySQL database.");
    }

    /**
     * Executes an SQL statement on the database.
     * <br>
     * Returns nothing. Useful for <b>INSERT</b> or <b>UPDATE</b> statements.
     *
     * @param command The command (or query) to execute.
     *
     * @throws SQLException If an exception occurs while trying to execute the command (or query).
     */
    void executeStatement(String command) throws SQLException {
        conn.createStatement().execute(command);
    }

    /**
     * Executes an SQL statement on the database.
     * <br>
     * Returns a {@link ResultSet}. Useful for <b>SELECT</b> statements.
     *
     * @param command The command (or query) to execute.
     *
     * @return A {@link ResultSet} containing the returned data.
     */
    @Nullable
    ResultSet returnValue(String command) {
        try {
            return conn.prepareStatement(command).executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
