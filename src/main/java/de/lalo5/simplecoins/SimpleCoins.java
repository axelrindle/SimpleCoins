package de.lalo5.simplecoins;

import com.google.common.io.Files;
import de.lalo5.simplecoins.util.Perms;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Main Class of the SimpleCoins Bukkit Plugin.
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "WeakerAccess"})
public final class SimpleCoins extends JavaPlugin {

    static final String NAME = "SimpleCoins";
    static final String VERSION = "0.3";

    static final String CONSOLEPREFIX = "[SimpleCoins] ";
    static final String PREFIX = "&3[&2SimpleCoins&3] &r";

    static final Logger LOGGER = Logger.getLogger(NAME);
    static SqlManager sqlManager;

    static boolean useSQL;
    static boolean vaultEnabled;

    static File configFile;
    static FileConfiguration fileConfiguration;

    static Economy econ = null;
    static Permission perms = null;
    static Chat chat = null;

    @Override
    public void onEnable() {
        boolean loaded = true;

        LOGGER.info(CONSOLEPREFIX + "Loading...");
        try {
            initConfig();

            useSQL = fileConfiguration.getBoolean("Database.UseSQL");
            if(!useSQL) {
                LOGGER.info(CONSOLEPREFIX + "Using local database. (database.yml)");
                CoinManager.loadFiles();
            } else {
                LOGGER.info(CONSOLEPREFIX + "Using MySQL database.");
                initMySQL();
            }
        } catch (IOException e) {
            LOGGER.severe(CONSOLEPREFIX + "Something went wrong while loading! DISABLING...");
            loaded = false;
            Bukkit.getPluginManager().disablePlugin(this);
        } catch (SQLException e) {
            LOGGER.severe(CONSOLEPREFIX + "Couldn't connect to MySQL database! DISABLING...");
            loaded = false;
            Bukkit.getPluginManager().disablePlugin(this);
        }

        if(fileConfiguration.getBoolean("UseVault")) {
            if (checkVault()) {
                LOGGER.info(CONSOLEPREFIX + "Vault found. Using vault economy instead of internal database.");

                boolean noError = true;
                if(!setupEconomy()) noError = false;
                if(!setupPermissions()) noError = false;

                if(!noError) {
                    LOGGER.severe("Something went wrong while integrating Vault!");
                }

                vaultEnabled = noError;
            } else {
                LOGGER.info(CONSOLEPREFIX + "Vault not found! Disabling Vault support.");
                vaultEnabled = false;
            }
        }

        setupCommand();

        if(loaded) {
            LOGGER.info(CONSOLEPREFIX + "Finished! Version " + VERSION);
        }
    }

    @Override
    public void onDisable() {
        LOGGER.info(CONSOLEPREFIX + "Shutting down...");

        try {
            if(!useSQL) {
                CoinManager.saveFiles();
            } else {
                sqlManager.closeConnection();
            }
        } catch (SQLException e) {
            LOGGER.info(CONSOLEPREFIX + "Couldn't close database connection! Maybe there have never been a connection?");
            e.printStackTrace();
        }

        LOGGER.info(CONSOLEPREFIX + "Finished!");
    }

    /**
     * Initializes the <b>/sc</b> (alias: <b>/simplecoins</b>) command.
     */
    private void setupCommand() {
        PluginCommand sc = getCommand("sc");
        sc.setExecutor(new SCCmd());
        sc.setAliases(Collections.singletonList("simplecoins"));
    }

    /**
     * @return <code>true</code> if <b>Vault</b> is installed on the server, <code>false</code> otherwise.
     */
    private boolean checkVault() {
        return getServer().getPluginManager().getPlugin("Vault") != null;
    }

    /**
     * Initializes Vault's economy system.
     * @return <code>true</code> if it has been found, <code>false</code> otherwise.
     */
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    /**
     * Initializes Vault's permission system.
     * @return <code>true</code> if it has been found, <code>false</code> otherwise.
     */
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return false;
        }
        perms = rsp.getProvider();
        return perms != null;
    }

    /**
     * Initializes Vault's chat system.
     * @return <code>true</code> if it has been found, <code>false</code> otherwise.
     */
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return false;
        }
        chat = rsp.getProvider();
        return chat != null;
    }

    /**
     * Formats a {@link String} to match Bukkit's color format.
     *
     * @param message The message to format.
     *
     * @return The formatted String.
     */
    static String colorize(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    /**
     * Sends the specified Player messages with all commands he may execute.
     *
     * @param player The player to send the messages to.
     */
    static void sendHelp(Player player) {
        player.sendMessage(colorize(PREFIX + "Commands:"));
        player.sendMessage("");
        if(player.hasPermission(Perms.ADD.perm()) || player.isOp()) {
            player.sendMessage(colorize("&2/sc add <player> <amount> &8- &fAdd <amount> coins to <player>'s account."));
        }
        if(player.hasPermission(Perms.REMOVE.perm()) || player.isOp()) {
            player.sendMessage(colorize("&2/sc remove <player> <amount> &8- &fRemove <amount> coins from <player>'s account."));
        }
        if(player.hasPermission(Perms.SET.perm()) || player.isOp()) {
            player.sendMessage(colorize("&2/sc set <player> <amount> &8- &fSet <player>'s coins to <amount>."));
        }
        if(player.hasPermission(Perms.GETSELF.perm()) || player.isOp()) {
            player.sendMessage(colorize("&2/sc get &8- &fView how many coins you have."));
        }
        if(player.hasPermission(Perms.GETOTHER.perm()) || player.isOp()) {
            player.sendMessage(colorize("&2/sc get <player> &8- &fView how many coins <player> have."));
        }
        if(player.hasPermission(Perms.RELOAD.perm()) || player.isOp()) {
            player.sendMessage(colorize("&2/sc reload &8- &fReload the plugin. Reconnects to the database or reloads the database.yml"));
        }
    }

    /**
     * Loads the {@link #fileConfiguration} from the filesystem.
     * <br>
     * If it does not exist, it will be created with default values.
     *
     * @throws IOException If the file could not be read.
     */
    private void initConfig() throws IOException {
        configFile = new File("plugins/" + NAME + "/config.yml");
        if(!configFile.exists()) {
            Files.createParentDirs(configFile);
            configFile.createNewFile();

            InputStream c = getClass().getResourceAsStream("/config.yml");
            FileWriter writer = new FileWriter(configFile);
            IOUtils.copy(c, writer, StandardCharsets.UTF_8);
            writer.close();
            c.close();
        }

        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Initializes the {@link SqlManager} with the values from the {@link #fileConfiguration}.
     *
     * @throws SQLException If the {@link SqlManager} could not be initialized.
     */
    static void initMySQL() throws SQLException {
        String host = fileConfiguration.getString("Database.Host");
        int port = fileConfiguration.getInt("Database.Port");
        String dbName = fileConfiguration.getString("Database.DatabaseName");
        String tableName = fileConfiguration.getString("Database.TableName");
        String username = fileConfiguration.getString("Database.Username");
        String password = fileConfiguration.getString("Database.Password");
        sqlManager = new SqlManager(host, port, dbName, tableName, username, password);
        sqlManager.connect();
        sqlManager.createTable();
    }
}
