package de.lalo5.simplecoins;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static de.lalo5.simplecoins.SimpleCoins.CONSOLEPREFIX;
import static de.lalo5.simplecoins.SimpleCoins.sqlManager;
import static de.lalo5.simplecoins.SimpleCoins.useSQL;

/**
 * The <b>CoinManager</b> is used to modify the account balance of a {@link Player}.
 */
@SuppressWarnings("WeakerAccess")
public final class CoinManager {

    private static File coinFile;
    private static FileConfiguration config;

    public static final int fractionalDigits = 2;

    /**
     * Loads the {@link #config} from the filesystem.
     */
    static void loadConfig() {
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

        saveConfig();
    }

    /**
     * Saves the {@link #config}
     */
    static void saveConfig() {
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
    public static double getCoins(Player player) {
        String uuid = player.getUniqueId().toString();

        double i = 0;
        if(!useSQL) {
            if(!hasPlayer(uuid)) {
                config.addDefault(uuid + ".Coins", 0D);
                saveConfig();
            } else {
                i = config.getDouble(uuid + ".Coins");
            }
        } else {
            String tableName = SimpleCoins.config.getString("Database.TableName");
            try {
                ResultSet rs = sqlManager.returnValue(
                        "SELECT `Coins` " +
                                "FROM `" + tableName + "` " +
                                "WHERE `UUID` = '" + uuid + "'"
                );
                if(!rs.next()) { // if ResultSet is empty (no account for player), create an account
                    sqlManager.executeStatement(
                            "INSERT INTO `" + tableName + "` " +
                                    "(`UUID`, `Name`, `Coins`) " +
                                    "VALUES ('" + uuid + "', '" + player.getName() + "', '0');"
                    );
                    i = 0;
                } else {
                    i = rs.getDouble("Coins");
                }
                rs.getStatement().close();
            } catch (SQLException | NullPointerException e) {
                SimpleCoins.LOGGER.severe("Failed to retrieve coin amount from database!");
                e.printStackTrace();
            }
        }

        return i;
    }

    public static void addCoins(UUID uuid, double amount) {
        addCoins(Bukkit.getPlayer(uuid), amount);
    }

    /**
     * Adds a specific amount of coins to a Player's account.
     *
     * @param player The Player to give coins to.
     * @param amount The amount of coins to give to the player.
     */
    public static void addCoins(Player player, double amount) {
        double old = getCoins(player);
        double n = add(old, round(amount, 2));
        setCoins(player, n);
    }

    public static void removeCoins(UUID uuid, double amount) {
        removeCoins(Bukkit.getPlayer(uuid), amount);
    }

    /**
     * Removes a specific amount of coins from a Player's amount.
     * <br>
     * The amount you specify <b>must</b> be more than zero and <b>should</b> be less or equal to the amount of
     * coins the player owns, otherwise it's set to <b>zero</b>.
     *
     * @param player The Player to remove amount from.
     * @param amount The amount of coins to remove from the player's account.
     */
    public static void removeCoins(Player player, double amount) {
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
            if(!useSQL) {
                config.set(uuid + ".Coins", amount);
                saveConfig();
            } else {
                String dbname = SimpleCoins.config.getString("Database.TableName");
                try {
                    sqlManager.executeStatement(
                            "UPDATE `" + dbname + "` " +
                                    "SET `Coins` = '" + amount + "' " +
                                    "WHERE `" + dbname + "`.`UUID` = '" + uuid + "' " +
                                    "LIMIT 1 "
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static double add(double val1, double val2) {
        return new BigDecimal(val1).add(new BigDecimal(val2)).doubleValue();
    }

    private static double subtract(double val1, double val2) {
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
    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException("Can't round to an amount of decimal places smaller than 0!");

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Syncing Modes.
     */
    enum SyncMode {
        /**
         * <b>UPLOAD</b> overwrites the <b>database entries</b> with the <b>local config</b>.
         */
        UPLOAD,

        /**
         * <b>DOWNLOAD</b> overwrites the <b>local config</b> with the <b>database entries</b>
         */
        DOWNLOAD
    }

    /**
     * Synchronizes the coin database. Overwrites either the <b>local</b> or the <b>database</b> information.
     * <br>
     * This is useful if you want to save your <b>local entries</b> to the <b>database</b> or vice versa.
     *
     * @param mode Either {@link SyncMode#DOWNLOAD} or {@link SyncMode#UPLOAD}.
     */
    static void sync(SyncMode mode) {
        // TODO: 26.12.16 (1) Finish the upload/download process
        // TODO: 26.12.16 (2) Do syncing in an own thread
        if(mode == SyncMode.DOWNLOAD) {
            loadConfig();
        } else if(mode == SyncMode.UPLOAD) {
            try {
                SimpleCoins.initMySQL();
                sqlManager.executeStatement("DROP TABLE IF EXISTS `simplecoins`");
                sqlManager.createTable();

                for(String uuid : config.getKeys(false)) {
                    String name = config.getString(uuid + "Name");
                    double coins = config.getDouble(uuid + "Coins");

                    String dbname = SimpleCoins.config.getString("Database.TableName").toLowerCase();

                    sqlManager.executeStatement(
                            "INSERT INTO `" + dbname + "` " +
                                    "(`UUID`, `Name`, `Coins`) " +
                                    "VALUES ('" + uuid + "', '" + name + "', '" + coins + "');"
                    );
                }

                Bukkit.broadcastMessage(CONSOLEPREFIX + "Finished the data synchronisation!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Vault's {@link Economy} implementation.
     */
    public static final class SimpleEconomy implements Economy {

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public String getName() {
            return "SimpleCoins";
        }

        @Override
        public boolean hasBankSupport() {
            return false;
        }

        @Override
        public int fractionalDigits() {
            return fractionalDigits;
        }

        @Override
        public String format(double v) {
            return String.valueOf(CoinManager.round(v, fractionalDigits()));
        }

        @Override
        public String currencyNamePlural() {
            return SimpleCoins.config.getString("CoinsName");
        }

        @Override
        public String currencyNameSingular() {
            return currencyNamePlural();
        }

        @Override
        @Deprecated
        public boolean hasAccount(String s) {
            return false;
        }

        @Override
        public boolean hasAccount(OfflinePlayer offlinePlayer) {
            return CoinManager.hasPlayer(offlinePlayer.getUniqueId().toString());
        }

        @Override
        @Deprecated
        public boolean hasAccount(String s, String s1) {
            return false;
        }

        @Override
        public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
            return CoinManager.hasPlayer(offlinePlayer.getUniqueId().toString());
        }

        @Override
        @Deprecated
        public double getBalance(String s) {
            return 0;
        }

        @Override
        public double getBalance(OfflinePlayer offlinePlayer) {
            return CoinManager.getCoins(offlinePlayer.getUniqueId());
        }

        @Deprecated
        @Override
        public double getBalance(String s, String s1) {
            return 0;
        }

        @Override
        public double getBalance(OfflinePlayer offlinePlayer, String s) {
            return CoinManager.getCoins(offlinePlayer.getUniqueId());
        }

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
