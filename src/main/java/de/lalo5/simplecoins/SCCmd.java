package de.lalo5.simplecoins;

import de.lalo5.simplecoins.util.Perms;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

import static de.lalo5.simplecoins.SimpleCoins.*;

/**
 * Created by Axel on 27.12.2015.
 *
 * Project MinecraftPlugins
 */
@SuppressWarnings("deprecation")
class SCCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(Objects.equals(label, cmd.getName()) || cmd.getAliases().contains(label)) {
            if(sender.hasPermission(Perms.MAIN.perm())) {
                if(args.length == 0) {

                    sendHelp(sender);

                } else if(args[0].equalsIgnoreCase("add")) {
                    if(sender.hasPermission(Perms.ADD.perm())) {
                        if(args.length == 3) {

                            Player p_ = Bukkit.getPlayer(args[1]);
                            String a = StringUtils.replace(args[2], ",", ".");
                            if(p_ != null) {
                                double amount;
                                try {
                                    amount = Double.parseDouble(a);
                                } catch (NumberFormatException e) {
                                    sender.sendMessage(colorize("&cPlease enter a decimal number as the second argument! (e.g. 3.4 or 9,99)"));
                                    return true;
                                }

                                CoinManager.addCoins(p_, amount);
                                String message = config.getString("Messages.Coins_Received");
                                message = message.replaceAll("%amountrec%", String.valueOf(amount));
                                message = message.replaceAll("%amount%", String.valueOf(CoinManager.getCoins(p_)));
                                message = message.replaceAll("%playername%", p_.getName());
                                message = message.replaceAll("%coinname%", config.getString("CoinsName"));

                                p_.sendMessage(colorize(PREFIX + message));
                            } else {
                                sender.sendMessage(colorize("&cThis player does not exist!"));
                            }
                        }
                    } else {
                        sender.sendMessage(colorize(config.getString("Messages.NoPermission")));
                    }
                } else if(args[0].equalsIgnoreCase("remove")) {
                    if(sender.hasPermission(Perms.REMOVE.perm())) {
                        if(args.length == 3) {

                            Player p_ = Bukkit.getPlayer(args[1]);
                            String a = StringUtils.replace(args[2], ",", ".");
                            if(p_ != null) {
                                double amount;
                                try {
                                    amount = Double.parseDouble(a);
                                } catch (NumberFormatException e) {
                                    sender.sendMessage(colorize("&cPlease enter a decimal number as the second argument! (e.g. 3.4 or 9,99)"));
                                    return true;
                                }

                                double now = CoinManager.getCoins(p_);
                                if(now != 0) {
                                    if(amount <= now) {
                                        CoinManager.removeCoins(p_, amount);
                                        String message = config.getString("Messages.Coins_Taken");
                                        message = message.replaceAll("%amountrec%", String.valueOf(amount));
                                        message = message.replaceAll("%amount%", String.valueOf(CoinManager.getCoins(p_)));
                                        message = message.replaceAll("%playername%", p_.getName());
                                        message = message.replaceAll("%coinname%", config.getString("CoinsName"));

                                        p_.sendMessage(colorize(PREFIX + message));
                                    } else {
                                        sender.sendMessage(colorize("&cAmount muss be less than or equals to &2" + now));
                                    }
                                } else {
                                    sender.sendMessage(colorize("&cPlayer " + p_.getName() +  "has 0 &9" + config.getString("CoinsName") + "&c!"));
                                }
                            } else {
                                sender.sendMessage(colorize("&cThis player does not exist!"));
                            }
                        }
                    } else {
                        sender.sendMessage(colorize(config.getString("Messages.NoPermission")));
                    }
                } else if(args[0].equalsIgnoreCase("set")) {
                    if(sender.hasPermission(Perms.SET.perm())) {
                        if(args.length == 3) {

                            Player p_ = Bukkit.getPlayer(args[1]);
                            String a = StringUtils.replace(args[2], ",", ".");
                            if(p_ != null) {
                                double amount;
                                try {
                                    amount = Double.parseDouble(a);
                                } catch (NumberFormatException e) {
                                    sender.sendMessage(colorize(PREFIX + "&cPlease enter a decimal number as the second argument! (e.g. 3.4 or 9,99)"));
                                    return true;
                                }

                                if(amount >= 0) {
                                    CoinManager.setCoins(p_, amount);

                                    String message = config.getString("Messages.Coins_Set");
                                    message = message.replaceAll("%amount%", String.valueOf(CoinManager.getCoins(p_)));
                                    message = message.replaceAll("%playername%", p_.getName());
                                    message = message.replaceAll("%coinname%", config.getString("CoinsName"));

                                    p_.sendMessage(colorize(PREFIX + message));
                                } else {
                                    sender.sendMessage(colorize(PREFIX + "&cAmount must be greater than or equals to &20&c!"));
                                }
                            } else {
                                sender.sendMessage(colorize(PREFIX + "&cThis player does not exist!"));
                            }
                        }
                    } else {
                        sender.sendMessage(colorize(config.getString("Messages.NoPermission")));
                    }
                } else if(args[0].equalsIgnoreCase("get")) {
                    if(args.length == 2) {
                        if(sender.hasPermission(Perms.GETOTHER.perm())) {

                            Player p_ = Bukkit.getPlayer(args[1]);
                            if(p_ != null) {
                                double amount = CoinManager.getCoins(p_);

                                String message = config.getString("Messages.Coins_Get_Other");
                                message = message.replaceAll("%amount%", String.valueOf(amount));
                                message = message.replaceAll("%playername%", sender.getName());
                                message = message.replaceAll("%otherplayername%", p_.getName());
                                message = message.replaceAll("%coinname%", config.getString("CoinsName"));

                                sender.sendMessage(colorize(PREFIX + message));
                            } else {
                                sender.sendMessage(colorize(PREFIX + "&cThis player does not exist!"));
                            }

                        } else {
                            sender.sendMessage(colorize(config.getString("Messages.NoPermission")));
                        }
                    } else if(args.length == 1) {
                        if(sender instanceof Player) {
                            if(sender.hasPermission(Perms.GETSELF.perm())) {
                                double amount = CoinManager.getCoins((Player)sender);

                                String message = config.getString("Messages.Coins_Get_Self");
                                message = message.replaceAll("%amount%", String.valueOf(amount));
                                message = message.replaceAll("%playername%", sender.getName());
                                message = message.replaceAll("%coinname%", config.getString("CoinsName"));

                                sender.sendMessage(colorize(PREFIX + message));
                            } else {
                                sender.sendMessage(colorize(config.getString("Messages.NoPermission")));
                            }
                        } else {
                            sender.sendMessage("§4The Console does not have a coin account!");
                        }
                    }
                } else if(args[0].equalsIgnoreCase("reload")) {
                    if(sender.hasPermission(Perms.RELOAD.perm())) {
                        if(args.length == 1) {
                            try {
                                if(useSQL) {
                                    sqlManager.disconnect();
                                    sqlManager.connect();
                                } else {
                                    config.save(configFile);
                                    CoinManager.saveFiles();
                                    config = YamlConfiguration.loadConfiguration(configFile);
                                    CoinManager.loadFiles();
                                }

                                sender.sendMessage(colorize(PREFIX + config.getString("Messages.Reload")));
                            } catch (SQLException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        sender.sendMessage(colorize(config.getString("Messages.NoPermission")));
                    }
                } /*else if(args[0].equalsIgnoreCase("sync")) {
                        if(sender.hasPermission(Perms.SYNC.perm())) {
                            if(args.length == 2) {

                                int mode = 2;
                                try {
                                    mode = Integer.parseInt(args[1]);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }

                                if(mode == 0 || mode == 1) {
                                    CoinManager.sync(mode);
                                } else {
                                    sender.sendMessage(SimpleCoins.colorize(PREFIX + "&cMode must be &20 &cor &21&c!"));
                                }
                            }
                        }
                    }*/
            } else {
                sender.sendMessage(colorize(config.getString("Messages.NoPermission")));
            }
        }

        return true;
    }
}
