package me.faris.xpshare;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	private double xpRadius = 10F;
	private boolean sendMessage = false;

	private Permission shareXp = new Permission("xpshare.share");
	private Permission gainXp = new Permission("xpshare.gain");
	private Permission reloadConfig = new Permission("xpshare.config.reload");
	private Permission editConfig = new Permission("xpshare.config.edit");

	public void onEnable() {
		this.loadConfiguration();

		this.getCommand("xpshare").setExecutor(this);

		this.getServer().getPluginManager().addPermission(this.shareXp);
		this.getServer().getPluginManager().addPermission(this.gainXp);
		this.getServer().getPluginManager().addPermission(this.reloadConfig);
		this.getServer().getPluginManager().addPermission(this.editConfig);
		this.getServer().getPluginManager().registerEvents(this, this);
	}

	public void onDisable() {
		this.getServer().getPluginManager().removePermission(this.shareXp);
		this.getServer().getPluginManager().removePermission(this.gainXp);
		this.getServer().getPluginManager().removePermission(this.reloadConfig);
		this.getServer().getPluginManager().removePermission(this.editConfig);
	}

	private void loadConfiguration() {
		this.getConfig().options().header("XPShare configuration");
		this.getConfig().addDefault("XP share radius", 10F);
		this.getConfig().addDefault("Send message", false);
		this.getConfig().options().copyDefaults(true);
		this.getConfig().options().copyHeader(true);
		this.saveConfig();

		this.xpRadius = this.getConfig().getDouble("XP share radius");
		this.sendMessage = this.getConfig().getBoolean("Send message");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("xpshare")) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.GOLD + "XPShare v" + this.getDescription().getVersion());
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission(this.reloadConfig)) {
						this.reloadConfig();
						this.loadConfiguration();
						sender.sendMessage(ChatColor.GOLD + "Reloaded XPShare's configuration.");
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You do not have access to that command.");
					}
				} else if (args[0].equalsIgnoreCase("radius")) {
					if (sender.hasPermission(this.editConfig)) {
						this.getConfig().set("XP share radius", 10D);
						this.saveConfig();
						this.xpRadius = 10D;

						sender.sendMessage(ChatColor.GOLD + "Reset the XP share radius to 10 blocks.");
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You do not have access to that command.");
					}
				} else {
					sender.getServer().dispatchCommand(sender, "xpshare");
				}
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("radius")) {
					if (sender.hasPermission(this.editConfig)) {
						if (this.isFloat(args[1])) {
							float shareRadius = Float.parseFloat(args[1]);
							this.getConfig().set("XP share radius", shareRadius);
							this.saveConfig();
							this.xpRadius = shareRadius;

							sender.sendMessage(ChatColor.GOLD + "Changed the XP share radius to " + shareRadius + " blocks.");
						} else {
							sender.sendMessage(ChatColor.RED + "You must enter a valid number for the XP share radius.");
						}
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You do not have access to that command.");
					}
				} else {
					sender.getServer().dispatchCommand(sender, "xpshare");
				}
			} else {
				sender.getServer().dispatchCommand(sender, "xpshare");
			}
			return true;
		}
		return false;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onExpGain(PlayerExpChangeEvent event) {
		try {
			if (event.getAmount() > 0) {
				if (event.getPlayer().hasPermission(this.shareXp)) {
					for (int i = 0; i < event.getPlayer().getServer().getOnlinePlayers().length; i++) {
						try {
							Player targetPlayer = event.getPlayer().getServer().getOnlinePlayers()[i];
							if (!targetPlayer.getName().equals(event.getPlayer().getName())) {
								if (event.getPlayer().getLocation().distance(targetPlayer.getLocation()) <= this.xpRadius) {
									if (targetPlayer.hasPermission(this.gainXp)) {
										float currentXp = targetPlayer.getExp();
										float newXp = event.getAmount();
										while (currentXp + newXp > 1F) {
											targetPlayer.setExp(1F);
											newXp -= 1F;
											currentXp = targetPlayer.getExp();
										}
										targetPlayer.setExp(targetPlayer.getExp() + newXp);
										if (this.sendMessage) targetPlayer.sendMessage(ChatColor.GREEN + "+" + event.getAmount() + " XP from " + event.getPlayer().getName() + ".");
									}
								}
							}
						} catch (Exception ex3) {
							continue;
						}
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	private boolean isFloat(String aString) {
		try {
			Float.parseFloat(aString);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

}
