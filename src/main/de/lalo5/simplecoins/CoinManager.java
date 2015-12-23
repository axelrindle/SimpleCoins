package de.lalo5.simplecoins;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Axel on 21.12.2015.
 */
public class CoinManager {

    protected static File coinfile = new File("plugins/" + SimpleCoins.NAME + "/database.yml");
    protected static FileConfiguration cfg = YamlConfiguration.loadConfiguration(coinfile);


    protected static void loadFiles() {

        cfg.options().header("This is the coin database. Everything will be stored here," +
                "\n" +
                "if you set UseDatabase to false in the main config.");

        cfg.addDefault("00-00-00.Coins", 0);
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
            try {
                ResultSet rs = SimpleCoins.sqlManager.returnValue("SELECT `Coins` FROM `simplecoins` WHERE `UUID` = '" + uuid + "'");
                if(!rs.next()) {
                    SimpleCoins.sqlManager.executeStatement("INSERT INTO `simplecoins` (`UUID`, `Name`, `Coins`) VALUES ('" + uuid + "', '" + p.getName() + "', '0');");
                }
                while (rs.next()) {
                    i = rs.getInt(1);
                }
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
            try {
                SimpleCoins.sqlManager.executeStatement(
                        "UPDATE `simplecoins` SET `Coins` = '" + (old + coins) + "' WHERE `simplecoins`.`UUID` = '" + uuid + "' LIMIT 1 "
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
                try {
                    SimpleCoins.sqlManager.executeStatement(
                            "UPDATE `simplecoins` SET `Coins` = '" + (old - coins) + "' WHERE `simplecoins`.`UUID` = '" + uuid + "' LIMIT 1 "
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void setCoins(Player p, int coins) {

        String uuid = p.getUniqueId().toString();

        if(coins >= 0) {
            if(!SimpleCoins.useSQL) {
                cfg.set(uuid + ".Coins", coins);
                saveFiles();
            } else {
                try {
                    SimpleCoins.sqlManager.executeStatement(
                            "UPDATE `simplecoins` SET `Coins` = '" + coins + "' WHERE `simplecoins`.`UUID` = '" + uuid + "' LIMIT 1 "
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
