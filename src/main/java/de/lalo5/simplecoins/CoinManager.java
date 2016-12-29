package de.lalo5.simplecoins;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * The <b>CoinManager</b> is used to modify the account balance of a {@link Player}.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class CoinManager {

    private static File coinFile;
    private static FileConfiguration config;

    /**
     * Loads the {@link #config} from the filesystem.
     */
    static void loadFiles() {
        coinFile = new File("plugins/SimpleCoins/database.yml");
        config = YamlConfiguration.loadConfiguration(coinFile);

        config.options().header("This is the coin database. Everything will be stored here," +
                "\n" +
                "if you set UseDatabase to false in the main config." +
                "\n\n\n");

        config.addDefault("00-00-00.Coins", 0);
        config.addDefault("00-00-00.Name", "Player");

        config.options().copyHeader(true);
        config.options().copyDefaults(true);

        saveFiles();
    }

    /**
     * Saves the {@link #config}
     */
    static void saveFiles() {
        try {
            config.save(coinFile);
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
        return config.contains(uuid);
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
        return hasPlayer(uuid.toString());
    }

    public static double getCoins(UUID uuid) {
        return getCoins(Bukkit.getPlayer(uuid));
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
        if(!SimpleCoins.useSQL) {
            if(!hasPlayer(uuid)) {
                config.addDefault(uuid + ".Coins", 0D);
                saveFiles();
            } else {
                i = config.getDouble(uuid + ".Coins");
            }
        } else {
            String tableName = SimpleCoins.fileConfiguration.getString("Database.TableName");
            try {
                ResultSet rs = SimpleCoins.sqlManager.returnValue("SELECT `Coins` FROM `" + tableName + "` WHERE `UUID` = '" + uuid + "'");
                if(!rs.next()) {
                    SimpleCoins.sqlManager.executeStatement("INSERT INTO `" + tableName + "` (`UUID`, `Name`, `Coins`) VALUES ('" + uuid + "', '" + player.getName() + "', '0');");
                    i = 0;
                } else {
                    i = rs.getDouble("Coins");
                }
                rs.getStatement().close();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NullPointerException e1) {
                SimpleCoins.LOGGER.severe("Failed to retrieve coin amount from database!");
                e1.printStackTrace();
            }
        }

        return i;
    }

    public static void addCoins(@NotNull UUID uuid, double amount) {
        addCoins(Bukkit.getPlayer(uuid), amount);
    }

    /**
     * Adds a specific amount of coins to a Player's account.
     *
     * @param player The Player to give coins to.
     * @param amount The amount of coins to give to the player.
     */
    public static void addCoins(@NotNull Player player, double amount) {
        double old = getCoins(player);
        double n = add(old, round(amount, 2));
        setCoins(player, n);
    }

    public static void removeCoins(@NotNull UUID uuid, double amount) {
        removeCoins(Bukkit.getPlayer(uuid), amount);
    }

    /**
     * Removes a specific amount of coins from a Player's amount.
     * <br>
     * The amount you specify <b>must</b> be more than zero and <b>should</b> be less or equal to the amount of coins the player owns, otherwise
     * it's set to <b>zero</b>.
     *
     * @param player The Player to remove amount from.
     * @param amount The amount of coins to remove from the player's account.
     */
    public static void removeCoins(@NotNull Player player, double amount) {
        double old = getCoins(player);
        double rAmount = round(amount, 2);

        if(rAmount <= old) {
            double n = subtract(old, rAmount);
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
        double amount = round(newAmount, 2);

        //create account if none exists to avoid errors
        getCoins(player);

        if(amount >= 0) {
            if(!SimpleCoins.useSQL) {
                config.set(uuid + ".Coins", amount);
                saveFiles();
            } else {
                String dbname = SimpleCoins.fileConfiguration.getString("Database.TableName");
                try {
                    SimpleCoins.sqlManager.executeStatement(
                            "UPDATE `" + dbname + "` SET `Coins` = '" + amount + "' WHERE `" + dbname + "`.`UUID` = '" + uuid + "' LIMIT 1 "
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static double add(double val1, double val2) {
        return new BigDecimal(val1).add(new BigDecimal(val2)).doubleValue();
    }

    public static double subtract(double val1, double val2) {
        return new BigDecimal(val1).subtract(new BigDecimal(val2)).doubleValue();
    }

    /**
     * Rounds a <code>value</code> value to a specified number of decimal places.
     *
     * @param value The <code>value</code> value to round.
     * @param places The maximum amount of decimal places.
     *
     * @return The rounded <code>value</code> value.
     */
    @SuppressWarnings("SameParameterValue")
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("Can't round to an amount of decimal places smaller than 0!");

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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

                for(String uuid : config.getKeys(false)) {
                    String name = config.getString(uuid + "Name");
                    double coins = config.getDouble(uuid + "Coins");

                    String dbname = SimpleCoins.fileConfiguration.getString("Database.TableName").toLowerCase();

                    SimpleCoins.sqlManager.executeStatement("INSERT INTO `" + dbname + "` (`UUID`, `Name`, `Coins`) VALUES ('" + uuid + "', '" + name + "', '" + coins + "');");
                }

                Bukkit.broadcastMessage(SimpleCoins.CONSOLEPREFIX + "Finished the data synchronisation!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Vault's {@link Economy} implementation.
     */
    public static final class SimpleEconomy implements Economy {

        @Contract(value = " -> true", pure = true)
        @Override
        public boolean isEnabled() {
            return true;
        }

        @NotNull
        @Contract(pure = true)
        @Override
        public String getName() {
            return "SimpleCoins";
        }

        @Contract(value = " -> false", pure = true)
        @Override
        public boolean hasBankSupport() {
            return false;
        }

        @Contract(pure = true)
        @Override
        public int fractionalDigits() {
            return 2;
        }

        @NotNull
        @Override
        public String format(double v) {
            return String.valueOf(CoinManager.round(v, fractionalDigits()));
        }

        @Override
        public String currencyNamePlural() {
            return SimpleCoins.fileConfiguration.getString("CoinsName");
        }

        @Override
        public String currencyNameSingular() {
            return SimpleCoins.fileConfiguration.getString("CoinsName");
        }

        @Contract(value = "_ -> false", pure = true)
        @Override
        @Deprecated
        public boolean hasAccount(String s) {
            return false;
        }

        @Override
        public boolean hasAccount(OfflinePlayer offlinePlayer) {
            return CoinManager.hasPlayer(offlinePlayer.getUniqueId().toString());
        }

        @Contract(value = "_, _ -> false", pure = true)
        @Override
        @Deprecated
        public boolean hasAccount(String s, String s1) {
            return false;
        }

        @Override
        public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
            return CoinManager.hasPlayer(offlinePlayer.getUniqueId().toString());
        }

        @Contract(pure = true)
        @Override
        @Deprecated
        public double getBalance(String s) {
            return 0;
        }

        @Override
        public double getBalance(OfflinePlayer offlinePlayer) {
            return CoinManager.getCoins(offlinePlayer.getUniqueId());
        }

        @Contract(pure = true)
        @Deprecated
        @Override
        public double getBalance(String s, String s1) {
            return 0;
        }

        @Override
        public double getBalance(OfflinePlayer offlinePlayer, String s) {
            return CoinManager.getCoins(offlinePlayer.getUniqueId());
        }

        @Contract(value = "_, _ -> false", pure = true)
        @Deprecated
        @Override
        public boolean has(String s, double v) {
            return false;
        }

        @Override
        public boolean has(OfflinePlayer offlinePlayer, double v) {
            if (v < 0) v = 0;
            return CoinManager.getCoins(offlinePlayer.getUniqueId()) >= v;
        }

        @Contract(value = "_, _, _ -> false", pure = true)
        @Deprecated
        @Override
        public boolean has(String s, String s1, double v) {
            return false;
        }

        @Override
        public boolean has(OfflinePlayer offlinePlayer, String s, double v) {
            if (v < 0) v = 0;
            return CoinManager.getCoins(offlinePlayer.getUniqueId()) >= v;
        }

        @Contract(value = "_, _ -> null", pure = true)
        @Deprecated
        @Override
        public EconomyResponse withdrawPlayer(String s, double v) {
            return null;
        }

        @Override
        public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double v) {
            CoinManager.removeCoins(offlinePlayer.getUniqueId(), v);
            return new EconomyResponse(
                    v,
                    CoinManager.getCoins(offlinePlayer.getUniqueId()),
                    EconomyResponse.ResponseType.SUCCESS,
                    ""
            );
        }

        @Contract(value = "_, _, _ -> null", pure = true)
        @Deprecated
        @Override
        public EconomyResponse withdrawPlayer(String s, String s1, double v) {
            return null;
        }

        @Override
        public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
            CoinManager.removeCoins(offlinePlayer.getUniqueId(), v);
            return new EconomyResponse(
                    v,
                    CoinManager.getCoins(offlinePlayer.getUniqueId()),
                    EconomyResponse.ResponseType.SUCCESS,
                    ""
            );
        }

        @Contract(value = "_, _ -> null", pure = true)
        @Deprecated
        @Override
        public EconomyResponse depositPlayer(String s, double v) {
            return null;
        }

        @Override
        public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double v) {
            CoinManager.addCoins(offlinePlayer.getUniqueId(), v);
            return new EconomyResponse(
                    v,
                    CoinManager.getCoins(offlinePlayer.getUniqueId()),
                    EconomyResponse.ResponseType.SUCCESS,
                    ""
            );
        }

        @Contract(value = "_, _, _ -> null", pure = true)
        @Deprecated
        @Override
        public EconomyResponse depositPlayer(String s, String s1, double v) {
            return null;
        }

        @Override
        public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double v) {
            CoinManager.addCoins(offlinePlayer.getUniqueId(), v);
            return new EconomyResponse(
                    v,
                    CoinManager.getCoins(offlinePlayer.getUniqueId()),
                    EconomyResponse.ResponseType.SUCCESS,
                    ""
            );
        }

        @Override
        public EconomyResponse createBank(String s, String s1) {
            return null;
        }

        @Override
        public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
            return null;
        }

        @Override
        public EconomyResponse deleteBank(String s) {
            return null;
        }

        @Override
        public EconomyResponse bankBalance(String s) {
            return null;
        }

        @Override
        public EconomyResponse bankHas(String s, double v) {
            return null;
        }

        @Override
        public EconomyResponse bankWithdraw(String s, double v) {
            return null;
        }

        @Override
        public EconomyResponse bankDeposit(String s, double v) {
            return null;
        }

        @Override
        public EconomyResponse isBankOwner(String s, String s1) {
            return null;
        }

        @Override
        public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
            return null;
        }

        @Override
        public EconomyResponse isBankMember(String s, String s1) {
            return null;
        }

        @Override
        public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
            return null;
        }

        @Override
        public List<String> getBanks() {
            return null;
        }

        @Contract(value = "_ -> false", pure = true)
        @Deprecated
        @Override
        public boolean createPlayerAccount(String s) {
            return false;
        }

        @Override
        public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
            getCoins(offlinePlayer.getUniqueId());
            return true;
        }

        @Contract(value = "_, _ -> false", pure = true)
        @Deprecated
        @Override
        public boolean createPlayerAccount(String s, String s1) {
            return false;
        }

        @Override
        public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
            getCoins(offlinePlayer.getUniqueId());
            return true;
        }
    }
}
