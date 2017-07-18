package de.lalo5.simplecoins;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import de.lalo5.simplecoins.util.Perms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Main Class of the SimpleCoins Bukkit Plugin.
 */
public final class SimpleCoins extends JavaPlugin {

    static final String CONSOLEPREFIX = "[SimpleCoins] ";
    static final String PREFIX = "&6SimpleCoins &r> ";

    static final Logger LOGGER = Logger.getLogger("SimpleCoins");
    static SqlManager sqlManager;

    static boolean useSQL;

    static File configFile;
    static FileConfiguration config;

    @Override
    public void onEnable() {
        boolean loaded = true;

        LOGGER.info(CONSOLEPREFIX + "Loading...");

        try {
            initConfig();
        } catch (IOException e) {
            LOGGER.severe(CONSOLEPREFIX + "Something went wrong while loading the config!");
            e.printStackTrace();
            loaded = false;
            Bukkit.getPluginManager().disablePlugin(this);
        }

        if (checkVault()) {
            LOGGER.info(CONSOLEPREFIX + "Vault found. Integrating Vault's economy system...");
            setupEconomy();
            LOGGER.info(CONSOLEPREFIX + "Successfully integrated Vault.");
        }

        try {
            useSQL = config.getBoolean("Database.UseSQL");
            if(!useSQL) {
                LOGGER.info(CONSOLEPREFIX + "Using local database. (database.yml)");
                CoinManager.loadFiles();
            } else {
                LOGGER.info(CONSOLEPREFIX + "Using MySQL database.");
                initMySQL();
            }
        } catch (SQLException e) {
            LOGGER.severe(CONSOLEPREFIX + "Couldn't connect to MySQL database!");
            e.printStackTrace();
            loaded = false;
            Bukkit.getPluginManager().disablePlugin(this);
        }

        LOGGER.info("Registering events...");
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onLogin(PlayerLoginEvent event) {
                CoinManager.getCoins(event.getPlayer()); // creates an account if the joined player does not have one.
            }
        }, this);

        if(loaded) {
            setupCommand();
            LOGGER.info(CONSOLEPREFIX + "Finished! Version " + getDescription().getVersion());
        }
    }

    @Override
    public void onDisable() {
        LOGGER.info(CONSOLEPREFIX + "Shutting down...");

        try {
            if(!useSQL) {
                CoinManager.saveFiles();
            } else {
                sqlManager.disconnect();
            }
        } catch (SQLException e) {
            LOGGER.info(CONSOLEPREFIX + "Couldn't close database connection! Maybe there have never been a connection?");
            e.printStackTrace();
        }

        LOGGER.info(CONSOLEPREFIX + "Done!");
    }

    /**
     * Initializes the <b>/sc</b> (alias: <b>/simplecoins</b>) command.
     */
    private void setupCommand() {
        getCommand("sc").setExecutor(new SCCmd());
    }

    /**
     * @return <code>true</code> if <b>Vault</b> is installed on the server, <code>false</code> otherwise.
     */
    private boolean checkVault() {
        return getServer().getPluginManager().getPlugin("Vault") != null;
    }

    /**
     * Initializes Vault's economy system.
     */
    private void setupEconomy() {
        getServer().getServicesManager().register(Economy.class, new CoinManager.SimpleEconomy(), this, ServicePriority.Normal);
    }

    /**
     * Formats a {@link String} to match Bukkit's color format.
     *
     * @param message The message to format.
     *
     * @return The formatted String.
     */
    static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Sends the specified Player chat with all commands he may execute.
     *
     * @param sender The player to send the chat to.
     */
    static void sendHelp(CommandSender sender) {
        sender.sendMessage(colorize(PREFIX + "Commands:"));
        sender.sendMessage("");
        if(sender.hasPermission(Perms.ADD.perm()) || sender.isOp()) {
            sender.sendMessage(colorize("&2/sc add <player> <amount> &8- &fAdd <amount> coins to <player>'s account."));
        }
        if(sender.hasPermission(Perms.REMOVE.perm()) || sender.isOp()) {
            sender.sendMessage(colorize("&2/sc remove <player> <amount> &8- &fRemove <amount> coins from <player>'s account."));
        }
        if(sender.hasPermission(Perms.SET.perm()) || sender.isOp()) {
            sender.sendMessage(colorize("&2/sc set <player> <amount> &8- &fSet <player>'s coins to <amount>."));
        }
        if(sender instanceof Player && (sender.hasPermission(Perms.GETSELF.perm()) || sender.isOp())) {
            sender.sendMessage(colorize("&2/sc get &8- &fView how many coins you have."));
        }
        if(sender.hasPermission(Perms.GETOTHER.perm()) || sender.isOp()) {
            sender.sendMessage(colorize("&2/sc get <player> &8- &fView how many coins <player> have."));
        }
        if(sender.hasPermission(Perms.RELOAD.perm()) || sender.isOp()) {
            sender.sendMessage(colorize("&2/sc reload &8- &fReload the plugin. Reconnects to the database or reloads the database.yml"));
        }
    }

    /**
     * Loads the {@link #config} from the filesystem.
     * <br>
     * If it does not exist, it will be created with default values.
     *
     * @throws IOException If the file could not be read.
     */
    private void initConfig() throws IOException {
        configFile = new File("plugins/SimpleCoins/config.yml");
        if(!configFile.exists()) {
            Files.createParentDirs(configFile);
            //noinspection ResultOfMethodCallIgnored
            configFile.createNewFile();

            InputStream is = getClass().getResourceAsStream("/config.yml");
            OutputStream os = new FileOutputStream(configFile);
            ByteStreams.copy(is, os);
            os.close();
            is.close();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Initializes the {@link SqlManager} with the values from the {@link #config}.
     *
     * @throws SQLException If the {@link SqlManager} could not be initialized.
     */
    static void initMySQL() throws SQLException {
        String host = config.getString("Database.Host");
        int port = config.getInt("Database.Port");
        String dbName = config.getString("Database.DatabaseName");
        String tableName = config.getString("Database.TableName");
        String username = config.getString("Database.Username");
        String password = config.getString("Database.Password");
        sqlManager = new SqlManager(host, port, dbName, tableName, username, password);
        sqlManager.connect();
        sqlManager.createTable();
    }
}
