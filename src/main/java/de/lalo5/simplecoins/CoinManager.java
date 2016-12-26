package de.lalo5.simplecoins;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * The <b>CoinManager</b> is used to modify the account balance of a {@link Player}.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class CoinManager {

    private static File coinFile;
    private static FileConfiguration fileConfiguration;

    /**
     * Loads the {@link #fileConfiguration} from the filesystem.
     */
    static void loadFiles() {
        coinFile = new File("plugins/" + SimpleCoins.NAME + "/database.yml");
        fileConfiguration = YamlConfiguration.loadConfiguration(coinFile);

        fileConfiguration.options().header("This is the coin database. Everything will be stored here," +
                "\n" +
                "if you set UseDatabase to false in the main config." +
                "\n\n\n");

        fileConfiguration.addDefault("00-00-00.Coins", 0);
        fileConfiguration.addDefault("00-00-00.Name", "Player");

        fileConfiguration.options().copyHeader(true);
        fileConfiguration.options().copyDefaults(true);

        saveFiles();
    }

    /**
     * Saves the {@link #fileConfiguration}
     */
    static void saveFiles() {
        try {
            fileConfiguration.save(coinFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a specific {@link Player} has an account.
     *
     * @param uuid The {@link UUID} of the Player to check.
     *
     * @return <code>true</code> if the specified Player has an account, <code>false</code> otherwise.
     */
    public static boolean hasPlayer(String uuid) {
        boolean b;
        if(SimpleCoins.vaultEnabled) {
            b = SimpleCoins.econ.hasAccount(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
        } else {
            b = fileConfiguration.contains(uuid);
        }

        return b;
    }

    /**
     * Check if a specific {@link Player} has an account.
     *
     * @param player The Player to check.
     *
     * @return <code>true</code> if the specified Player has an account, <code>false</code> otherwise.
     */
    public static boolean hasPlayer(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        boolean b;
        if(SimpleCoins.vaultEnabled) {
            b = SimpleCoins.econ.hasAccount(Bukkit.getOfflinePlayer(uuid));
        } else {
            b = fileConfiguration.contains(uuid.toString());
        }

        return b;
    }

    /**
     * Returns the amount of coins a Player owns.
     * <br>
     * If the player has no account, <b>0</b> is returned.
     *
     * @param player The Player to get the amount of coins from.
     * @return The amount of coins the specified player has, as a <code>double</code> value to support <b>Vault's economy system</b>.
     */
    @SuppressWarnings("ConstantConditions")
    public static double getCoins(@NotNull Player player) {
        String uuid = player.getUniqueId().toString();

        double i = 0;
        if(SimpleCoins.vaultEnabled) {
            i = SimpleCoins.econ.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));
        } else {
            if(!SimpleCoins.useSQL) {
                if(!hasPlayer(uuid)) {
                    fileConfiguration.addDefault(uuid + ".Coins", 0D);
                    fileConfiguration.addDefault(uuid + ".Name", player.getName());
                    saveFiles();
                } else {
                    i = fileConfiguration.getDouble(uuid + ".Coins");
                }
            } else {
                String dbname = SimpleCoins.fileConfiguration.getString("Database.TableName").toLowerCase();
                try {
                    ResultSet rs = SimpleCoins.sqlManager.returnValue("SELECT `Coins` FROM `" + dbname + "` WHERE `UUID` = '" + uuid + "'");
                    if(!rs.next()) {
                        SimpleCoins.sqlManager.executeStatement("INSERT INTO `" + dbname + "` (`UUID`, `Name`, `Coins`) VALUES ('" + uuid + "', '" + player.getName() + "', '0');");
                        i = 0;
                    } else {
                        i = rs.getDouble("Coins");
                    }
                    rs.getStatement().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return i;
    }

    /**
     * Adds a specific amount of coins to a Player's account.
     *
     * @param player The Player to give coins to.
     * @param amount The amount of coins to give to the player.
     */
    public static void addCoins(@NotNull Player player, double amount) {
        double old = getCoins(player);
        double n = old + amount;
        setCoins(player, n);
    }

    /**
     * Removes a specific amount of amount from a Player's amount.
     * <br>
     * The amount you specify <b>must</b> be more than zero and <b>should</b> be less or equal to the amount of coins the player owns, otherwise
     * it's set to <b>zero</b>.
     *
     * @param player The Player to remove amount from.
     * @param amount The amount of coins to remove from the player's account.
     */
    public static void removeCoins(@NotNull Player player, double amount) {
        double old = getCoins(player);

        if(amount <= old) {
            double n = old - amount;
            setCoins(player, n);
        }
    }

    /**
     * Set the amount of coins a player owns to a new amount, regardless of the current amount.
     *
     * @param player The player to set the amount of coins to a new amount.
     * @param newAmount The new amount of coins.
     */
    public static void setCoins(Player player, double newAmount) {
        String uuid = player.getUniqueId().toString();

        //create account if none exists to avoid errors
        getCoins(player);

        if(newAmount >= 0) {
            if(!SimpleCoins.useSQL) {
                fileConfiguration.set(uuid + ".Coins", newAmount);
                saveFiles();
            } else {
                String dbname = SimpleCoins.fileConfiguration.getString("Database.TableName").toLowerCase();
                try {
                    SimpleCoins.sqlManager.executeStatement(
                            "UPDATE `" + dbname + "` SET `Coins` = '" + String.valueOf(newAmount) + "' WHERE `" + dbname + "`.`UUID` = '" + uuid + "' LIMIT 1 "
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Syncing Modes.
     * <br>
     * To decide whether we should overwrite the MySQL Database with the local database or the local database with the MySQL Database.
     */
    enum SyncMode {
        UPLOAD,
        DOWNLOAD
    }
    /**
     * Synchronizes the coin database.
     * <br><br>
     * If
     * @param mode Either {@link SyncMode#DOWNLOAD} or {@link SyncMode#UPLOAD}.
     */
    static void sync(SyncMode mode) {
        // TODO: 26.12.16 (1) Finish the upload/download process
        // TODO: 26.12.16 (2) Do syncing in an own thread
        if(mode == SyncMode.DOWNLOAD) {
            loadFiles();


        } else if(mode == SyncMode.UPLOAD) {
            try {
                SimpleCoins.initMySQL();
                SimpleCoins.sqlManager.executeStatement("DROP TABLE IF EXISTS simplecoins");
                SimpleCoins.sqlManager.createTable();

                Bukkit.broadcastMessage(SimpleCoins.CONSOLEPREFIX + "Syncing data to the MySQL database! Lags may occure!");

                for(String uuid : fileConfiguration.getKeys(false)) {
                    String name = fileConfiguration.getString(uuid + "Name");
                    double coins = fileConfiguration.getDouble(uuid + "Coins");

                    String dbname = SimpleCoins.fileConfiguration.getString("Database.TableName").toLowerCase();

                    SimpleCoins.sqlManager.executeStatement("INSERT INTO `" + dbname + "` (`UUID`, `Name`, `Coins`) VALUES ('" + uuid + "', '" + name + "', '" + coins + "');");
                }

                Bukkit.broadcastMessage(SimpleCoins.CONSOLEPREFIX + "Finished the data synchronisation!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
