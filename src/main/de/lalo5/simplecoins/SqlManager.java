package de.lalo5.simplecoins;

import java.sql.*;

/**
 * Created by Axel on 22.12.2015.
 *
 * Project SimpleCoins
 */
public class SqlManager {

    private String host;
    private int port;
    private String databasename;
    private String tablename;
    private String username;
    private String password;

    private Connection conn;


    protected SqlManager(String host, int port, String databasename, String tablename, String username, String password) {
        this.host = host;
        this.port = port;
        this.databasename = databasename;
        this.tablename = tablename;
        this.username = username;
        this.password = password;
    }

    protected void connect() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + databasename;
        conn = DriverManager.getConnection(url, username, password);

        SimpleCoins.log.info(SimpleCoins.consoleprefix + "Successfully connected to the MySQL database.");
    }

    protected void createTable() throws SQLException {
        executeStatement(
                "CREATE TABLE IF NOT EXISTS `minecraft`.`" + tablename + "` " +
                        "( `UUID` TEXT NOT NULL COMMENT 'Players UUID' , " +
                        "`Name` TEXT NOT NULL COMMENT 'Players Name' , " +
                        "`Coins` INT NOT NULL COMMENT 'Players amount of coins' , " +
                        "UNIQUE `UUID` (`UUID`(36))) " +
                        "ENGINE = InnoDB CHARSET=utf8 COLLATE utf8_general_ci COMMENT = 'SimpleCoins account storage';"
        );
    }

    protected void closeConnection() throws SQLException {
        conn.close();
        SimpleCoins.log.info(SimpleCoins.consoleprefix + "Successfully disconnected from the MySQL database.");
    }

    protected void executeStatement(String command) throws SQLException {
        conn.createStatement().execute(command);
    }

    protected ResultSet returnValue(String command) {
        PreparedStatement ps;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(command);
            rs = ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rs;
    }
}
