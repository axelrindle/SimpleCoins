package de.lalo5.simplecoins;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Axel on 21.12.2015.
 *
 * Project SimpleCoins
 */
public class CoinManager {

    protected static File coinfile;
    protected static FileConfiguration cfg;

    protected static int DOWNLOAD = 0;
    protected static int UPLOAD = 1;


    protected static void loadFiles() {

        coinfile = new File("plugins/" + SimpleCoins.NAME + "/database.yml");
        cfg = YamlConfiguration.loadConfiguration(coinfile);

        cfg.options().header("This is the coin database. Everything will be stored here," +
                "\n" +
                "if you set UseDatabase to false in the main config." +
                "\n\n\n");

        cfg.addDefault("Database.00-00-00.Coins", 0);
        cfg.addDefault("00-00-00.Name", "Player");

        cfg.options().copyHeader(true);
        cfg.options().copyDefaults(true);

        try {
            cfg.save(coinfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void saveFiles() {
        try {
            cfg.save(coinfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean fileCointains(String uuid) {
        return cfg.contains(uuid);
    }

    public static int getCoins(Player p) {

        String uuid = p.getUniqueId().toString();

        int i = 0;
        if(!SimpleCoins.useSQL) {
            if(!fileCointains(uuid)) {
                cfg.addDefault(uuid + ".Coins", 0);
                cfg.addDefault(uuid + ".Name", p.getName());
                saveFiles();
            } else {
                i = cfg.getInt(uuid + ".Coins");
            }
        } else {
            String dbname = SimpleCoins.cfg.getString("Database.TableName").toLowerCase();
            try {
                ResultSet rs = SimpleCoins.sqlManager.returnValue("SELECT `Coins` FROM `" + dbname + "` WHERE `UUID` = '" + uuid + "'");
                if(!rs.next()) {
                    SimpleCoins.sqlManager.executeStatement("INSERT INTO `" + dbname + "` (`UUID`, `Name`, `Coins`) VALUES ('" + uuid + "', '" + p.getName() + "', '0');");
                    i = 0;
                } else {
                    i = rs.getInt("Coins");
                }
                rs.getStatement().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return i;
    }

    public static void addCoins(Player p, int coins) {

        String uuid = p.getUniqueId().toString();

        int old = getCoins(p);

        if(!SimpleCoins.useSQL) {
            cfg.set(uuid + ".Coins", old + coins);
            saveFiles();
        } else {
            String dbname = SimpleCoins.cfg.getString("Database.TableName").toLowerCase();
            try {
                int n = old + coins;
                SimpleCoins.sqlManager.executeStatement(
                        "UPDATE `" + dbname + "` SET `Coins` = '" + String.valueOf(n) + "' WHERE `" + dbname + "`.`UUID` = '" + uuid + "' LIMIT 1 "
                );
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void removeCoins(Player p, int coins) {

        String uuid = p.getUniqueId().toString();

        int old = getCoins(p);

        if(!(coins > old)) {
            if(!SimpleCoins.useSQL) {
                cfg.set(uuid + ".Coins", old - coins);
                saveFiles();
            } else {
                String dbname = SimpleCoins.cfg.getString("Database.TableName").toLowerCase();
                try {
                    int n = old - coins;
                    SimpleCoins.sqlManager.executeStatement(
                            "UPDATE `" + dbname + "` SET `Coins` = '" + String.valueOf(n) + "' WHERE `" + dbname + "`.`UUID` = '" + uuid + "' LIMIT 1 "
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void setCoins(Player p, int coins) {

        String uuid = p.getUniqueId().toString();

        getCoins(p);

        if(coins >= 0) {
            if(!SimpleCoins.useSQL) {
                cfg.set(uuid + ".Coins", coins);
                saveFiles();
            } else {
                String dbname = SimpleCoins.cfg.getString("Database.TableName").toLowerCase();
                try {
                    SimpleCoins.sqlManager.executeStatement(
                            "UPDATE `" + dbname + "` SET `Coins` = '" + String.valueOf(coins) + "' WHERE `" + dbname + "`.`UUID` = '" + uuid + "' LIMIT 1 "
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void sync(int mode) {
        if(mode == DOWNLOAD) {
            loadFiles();


        } else if(mode == UPLOAD) {
            try {
                SimpleCoins.initMySQL();
                SimpleCoins.sqlManager.executeStatement("DROP TABLE IF EXISTS simplecoins");
                SimpleCoins.sqlManager.createTable();

                Bukkit.broadcastMessage(SimpleCoins.consoleprefix + "Syncing data to the MySQL database! Lags may occure!");

                for(String uuid : cfg.getKeys(false)) {
                    String name = cfg.getString(uuid + "Name");
                    int coins = cfg.getInt(uuid + "Coins");

                    String dbname = SimpleCoins.cfg.getString("Database.TableName").toLowerCase();

                    SimpleCoins.sqlManager.executeStatement("INSERT INTO `" + dbname + "` (`UUID`, `Name`, `Coins`) VALUES ('" + uuid + "', '" + name + "', '" + coins + "');");
                }

                Bukkit.broadcastMessage(SimpleCoins.consoleprefix + "Finished the data synchronisation!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
