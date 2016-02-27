package de.lalo5.simplecoins;

import com.google.common.io.Files;
import de.lalo5.simplecoins.util.Perms;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Main Class of the SimpleCoins Bukkit Plugin.
 *
 * Created by Axel on 21.12.2015.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class SimpleCoins extends JavaPlugin {

    protected static String NAME = "SimpleCoins";
    protected static String VERSION = "1.0.0";

    protected static String consoleprefix = "[SimpleCoins] ";
    protected static String prefix = "&3[&2SimpleCoins&3] &r";

    protected static final Logger log = Logger.getLogger(NAME);
    protected static SqlManager sqlManager;

    protected static boolean useSQL;
    public static boolean vaultEnabled;

    protected static File configfile;
    protected static FileConfiguration cfg;
    private boolean loaded;

    public static Economy econ = null;
    public static Permission perms = null;
    public static Chat chat = null;


    @Override
    public void onEnable() {

        loaded = true;

        log.info(consoleprefix + "Loading...");
        try {
            loadCConfig();

            useSQL = cfg.getBoolean("Database.UseSQL");
            if(!useSQL) {
                log.info(consoleprefix + "Using local database. (database.yml)");
                CoinManager.loadFiles();
            } else {
                log.info(consoleprefix + "Using MySQL database.");
                initMySQL();
            }
        } catch (IOException e) {
            log.info(consoleprefix + "Something went wrong while loading! DISABLING...");
            Bukkit.getPluginManager().disablePlugin(this);
        } catch (SQLException e) {
            log.severe(consoleprefix + "Couldn't connect to MySQL database! DISABLING...");
            cfg.set("Database.UseSQL", false);
            saveCConfig();
            loaded = false;
            Bukkit.getPluginManager().disablePlugin(this);
        }

        if(cfg.getBoolean("UseVault")) {
            if (checkVault()) {
                setupEconomy();
                setupChat();
                setupPermissions();

                vaultEnabled = true;
            } else {
                log.severe(String.format("[%s] - Vault not found! Disabling Vault support!", getDescription().getName()));
                vaultEnabled = false;
            }

            getCommand("sc").setExecutor(new SCCmd());
        }

        if(loaded) {
            log.info(consoleprefix + "Finished! Version " + VERSION);
        }
    }

    @Override
    public void onDisable() {

        log.info(consoleprefix + "Shutting down...");

        try {
            saveCConfig();
            if(!useSQL) {
                CoinManager.saveFiles();
            } else {
                sqlManager.closeConnection();
            }
        } catch (SQLException e) {
            log.info(consoleprefix + "Couldn't close database connection! Maybe there have never been a connection?");
            e.printStackTrace();
        }

        log.info(consoleprefix + "Finished!");
    }

    private boolean checkVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        return true;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    protected static String colorize(String msg) {
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        return msg;
    }

    protected static void sendHelp(Player p) {
        p.sendMessage(colorize(prefix + "Commands:"));
        p.sendMessage("");
        if(p.hasPermission(Perms.ADD.perm()) || p.isOp()) {
            p.sendMessage(colorize("&2/sc add <player> <amount> &8- &fAdd <amount> coins to <player>'s account."));
        }
        if(p.hasPermission(Perms.REMOVE.perm()) || p.isOp()) {
            p.sendMessage(colorize("&2/sc remove <player> <amount> &8- &fRemove <amount> coins from <player>'s account."));
        }
        if(p.hasPermission(Perms.SET.perm()) || p.isOp()) {
            p.sendMessage(colorize("&2/sc set <player> <amount> &8- &fSet <player>'s coins to <amount>."));
        }
        if(p.hasPermission(Perms.GETSELF.perm()) || p.isOp()) {
            p.sendMessage(colorize("&2/sc get &8- &fView how many coins you have."));
        }
        if(p.hasPermission(Perms.GETOTHER.perm()) || p.isOp()) {
            p.sendMessage(colorize("&2/sc get <player> &8- &fView how many coins <player> have."));
        }
        if(p.hasPermission(Perms.RELOAD.perm()) || p.isOp()) {
            p.sendMessage(colorize("&2/sc reload &8- &fReload the plugin. Reconnects to the database or reloads the database.yml"));
        }
    }

    private void loadCConfig() throws IOException {

        configfile = new File("plugins/" + NAME + "/config.yml");
        if(!configfile.exists()) {
            Files.createParentDirs(configfile);
            configfile.createNewFile();

            InputStream c = this.getClass().getResourceAsStream("/config.yml");
            FileWriter writer = new FileWriter(configfile);
            IOUtils.copy(c, writer);
            writer.close();
            c.close();
        }

        cfg = YamlConfiguration.loadConfiguration(configfile);
    }

    private void saveCConfig() {
        try {
            cfg.save(configfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void initMySQL() throws SQLException {
        String host = cfg.getString("Database.Host");
        int port = cfg.getInt("Database.Port");
        String dbname = cfg.getString("Database.DatabaseName");
        String tablename = cfg.getString("Database.TableName");
        String username = cfg.getString("Database.Username");
        String password = cfg.getString("Database.Password");
        sqlManager = new SqlManager(host, port, dbname, tablename, username, password);
        sqlManager.connect();
        sqlManager.createTable();
    }
}
