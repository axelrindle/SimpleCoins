package de.lalo5.simplecoins;

import de.lalo5.simplecoins.util.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Main Class of the SimpleCoins Bukkit Plugin.
 *
 * Created by Axel on 21.12.2015.
 */
public class SimpleCoins extends JavaPlugin {

    protected static String NAME = "SimpleCoins";
    protected static String VERSION = "1.0.0";

    protected static String consoleprefix = "[SimpleCoins] ";
    protected static String prefix = "&3[&2SimpleCoins&3] &r";

    protected static final Logger log = Logger.getLogger(NAME);
    protected static SqlManager sqlManager;

    protected static boolean useSQL;

    private File configfile;
    private FileConfiguration cfg;


    @Override
    public void onEnable() {

        log.info(consoleprefix + "Loading...");
        try {
            loadConfig();

            useSQL = cfg.getBoolean("Database.UseSQL");
            if(!useSQL) {
                CoinManager.loadFiles();
            } else {
                String host = cfg.getString("Database.Host");
                int port = cfg.getInt("Database.Port");
                String dbname = cfg.getString("Database.DatabaseName");
                String tablename = cfg.getString("Database.TableName");
                String username = cfg.getString("Database.Username");
                String password = cfg.getString("Database.Password");
                sqlManager = new SqlManager(host, port, dbname, tablename, username, password);
                sqlManager.connect();
            }
        } catch (IOException e) {
            log.info(consoleprefix + "Something went wrong while loading! DISABLING...");
            Bukkit.getPluginManager().disablePlugin(this);
        } catch (SQLException e) {
            log.info(consoleprefix + "Couldn't connect to MySQL database! DISABLING...");
            cfg.set("Database.UseSQL", false);
            saveCConfig();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        log.info(consoleprefix + "Finished! Version " + VERSION);
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


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(sender instanceof Player) {
            Player p = (Player) sender;

            if(cmd.getName().equalsIgnoreCase("sc")) {
                if(args.length == 0) {

                    sendHelp(p);

                    return true;
                } else if(args[0].equalsIgnoreCase("add")) {
                    if(p.hasPermission(Permission.ADD.perm())) {
                        if(args.length == 3) {

                            Player p_ = Bukkit.getPlayer(args[1]);
                            int amount;
                            try {
                                amount = Integer.parseInt(args[2]);
                            } catch (NumberFormatException e) {
                                p.sendMessage(colorize(prefix + "&cPlease enter a number as the second argument!"));
                                return false;
                            }

                            CoinManager.addCoins(p_, amount);

                            String message = cfg.getString("Messages.Coins_Received");
                            message = message.replaceAll("%amountrec%", String.valueOf(amount));
                            message = message.replaceAll("%amount%", String.valueOf(CoinManager.getCoins(p_)));
                            message = message.replaceAll("%playername%", p_.getName());
                            message = message.replaceAll("%coinname%", cfg.getString("CoinsName"));

                            p_.sendMessage(colorize(prefix + message));

                            return true;
                        }
                    }
                } else if(args[0].equalsIgnoreCase("remove")) {
                    if(p.hasPermission(Permission.REMOVE.perm())) {
                        if(args.length == 3) {

                            Player p_ = Bukkit.getPlayer(args[1]);
                            int amount;
                            try {
                                amount = Integer.parseInt(args[2]);
                            } catch (NumberFormatException e) {
                                p.sendMessage(colorize(prefix + "&cPlease enter a number as the second argument!"));
                                return false;
                            }

                            int now = CoinManager.getCoins(p_);
                            if(now != 0) {
                                if(amount <= now) {
                                    CoinManager.removeCoins(p_, amount);

                                    String message = cfg.getString("Messages.Coins_Taken");
                                    message = message.replaceAll("%amountrec%", String.valueOf(amount));
                                    message = message.replaceAll("%amount%", String.valueOf(CoinManager.getCoins(p_)));
                                    message = message.replaceAll("%playername%", p_.getName());
                                    message = message.replaceAll("%coinname%", cfg.getString("CoinsName"));

                                    p_.sendMessage(colorize(prefix + message));
                                } else {
                                    p.sendMessage(colorize(prefix + "&cAmount muss be less than or equals to &2" + now));
                                }
                            } else {
                                p.sendMessage(colorize(prefix + "&cPlayer " + p_.getName() +  "has 0 &9" + cfg.getString("CoinsName") + "&c!"));
                            }

                            return true;
                        }
                    }
                } else if(args[0].equalsIgnoreCase("set")) {
                    if(p.hasPermission(Permission.SET.perm())) {
                        if(args.length == 3) {

                            Player p_ = Bukkit.getPlayer(args[1]);
                            int amount;
                            try {
                                amount = Integer.parseInt(args[2]);
                            } catch (NumberFormatException e) {
                                p.sendMessage(colorize(prefix + "&cPlease enter a number as the second argument!"));
                                return false;
                            }

                            if(amount > 0) {
                                CoinManager.setCoins(p_, amount);

                                String message = cfg.getString("Messages.Coins_Set");
                                message = message.replaceAll("%amount%", String.valueOf(CoinManager.getCoins(p_)));
                                message = message.replaceAll("%playername%", p_.getName());
                                message = message.replaceAll("%coinname%", cfg.getString("CoinsName"));

                                p_.sendMessage(colorize(prefix + message));
                            } else {
                                p.sendMessage(colorize(prefix + "&cAmount must be greater than or equals to &20&c!"));
                            }

                            return true;
                        }
                    }
                } else if(args[0].equalsIgnoreCase("get")) {
                    if(p.hasPermission(Permission.ADD.perm())) {
                        if(args.length == 2) {

                            Player p_ = Bukkit.getPlayer(args[1]);

                            int amount = CoinManager.getCoins(p_);

                            String message = cfg.getString("Messages.Coins_Get_Other");
                            message = message.replaceAll("%amount%", String.valueOf(amount));
                            message = message.replaceAll("%playername%", p.getName());
                            message = message.replaceAll("%otherplayername%", p_.getName());
                            message = message.replaceAll("%coinname%", cfg.getString("CoinsName"));

                            p.sendMessage(colorize(prefix + message));

                            return true;
                        } else if(args.length == 1) {
                            int amount = CoinManager.getCoins(p);

                            String message = cfg.getString("Messages.Coins_Get_Self");
                            message = message.replaceAll("%amount%", String.valueOf(amount));
                            message = message.replaceAll("%playername%", p.getName());
                            message = message.replaceAll("%coinname%", cfg.getString("CoinsName"));

                            p.sendMessage(colorize(prefix + message));
                        }
                    }
                }
            }
        }

        return super.onCommand(sender, cmd, label, args);
    }

    private String colorize(String msg) {
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        return msg;
    }

    private void sendHelp(Player p) {
        p.sendMessage(colorize(prefix + "Commands:"));
        p.sendMessage("");
        if(p.hasPermission(Permission.ADD.perm()) || p.isOp()) {
            p.sendMessage(colorize("&2/sc add <player> <amount> &8- &fAdd <amount> coins to <player>'s account."));
        }
        if(p.hasPermission(Permission.REMOVE.perm()) || p.isOp()) {
            p.sendMessage(colorize("&2/sc remove <player> <amount> &8- &fRemove <amount> coins from <player>'s account."));
        }
        if(p.hasPermission(Permission.SET.perm()) || p.isOp()) {
            p.sendMessage(colorize("&2/sc set <player> <amount> &8- &fSet <player>'s coins to <amount>."));
        }
        if(p.hasPermission(Permission.GET.perm()) || p.isOp()) {
            p.sendMessage(colorize("&2/sc get <player> &8- &fView how many coins <player> have."));
        }
    }

    private void loadConfig() throws IOException {

        configfile = new File("plugins/" + NAME + "/config.yml");
        cfg = YamlConfiguration.loadConfiguration(configfile);

        cfg.options().header("This is the SimpleCoins configuration file." +
                "\n\n" +
                "Here you can configure EVERY aspect of the plugin." +
                "\n" +
                "This includes the name of the coins (default is Coins), every message" +
                "\n\n\n" +
                "You may use the following placeholders:" +
                "\n\n" +
                "%amountrec% : Amount of received coins (Only in add/remove) \n" +
                "%amount% : Amount of coins in account (Everywhere) \n" +
                "%playername% : Name of the player (Player who received or who executed /sc get) \n" +
                "%otherplayername% : Name of the (Specified in /sc get <player>) \n" +
                "%coinname% : Name of the coins \n\n" +
                "Use & + 1-9/a-f for colors. \n\n\n");

        cfg.addDefault("CoinsName", "Coins");

        cfg.addDefault("Database.UseSQL", false);
        cfg.addDefault("Database.Host", "localhost");
        cfg.addDefault("Database.Port", 3306);
        cfg.addDefault("Database.DatabaseName", "minecraft");
        cfg.addDefault("Database.TableName", "SimpleCoins");
        cfg.addDefault("Database.Username", "username");
        cfg.addDefault("Database.Password", "password");

        cfg.addDefault("Messages.Coins_Received", "&aHey, &6%playername%&a! &aYou just received &3%amountrec% &9%coinname% &aand now you have &3%amount%&a!");
        cfg.addDefault("Messages.Coins_Taken", "&aHey, &6%playername%&a! &cYou were take &3%amountrec% &cand have now &3%amount% &9%coinname%&c!");
        cfg.addDefault("Messages.Coins_Set", "&aHey, &6%playername%&a! &cSomeone set your &9%coinname% &cto &3%amount%&c!");
        cfg.addDefault("Messages.Coins_Get_Self", "&aHey, &6%playername%&a! &aYou have &3%amount% &9%coinname%&a!");
        cfg.addDefault("Messages.Coins_Get_Other", "&aHey, &6%playername%&a! &aThe player &6%otherplayername% &ahas &3%amount% &9%coinname%&a!");

        cfg.options().copyHeader(true);
        cfg.options().copyDefaults(true);
        cfg.save(configfile);
    }

    private void saveCConfig() {
        try {
            cfg.save(configfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
