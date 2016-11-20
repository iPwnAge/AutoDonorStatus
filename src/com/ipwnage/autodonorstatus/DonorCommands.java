package com.ipwnage.autodonorstatus;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class DonorCommands implements CommandExecutor {

	private ConcurrentHashMap<UUID, DonorData> _donorData;
	private AutoDonor _plugin;
	private PlayerDataCache _playerDataCache;


	public DonorCommands(AutoDonor plugin) {
		_plugin = plugin;
		_donorData = plugin._donorData;
		_playerDataCache = plugin._playerDataCache;

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		_plugin.getLogger().info("Command executed");
		//Start: /donor check command
		if(args[0].equals("check")) {
			_plugin.getLogger().info("Command is check");
			//Start: player commands
			if(sender instanceof Player) {
				_plugin.getLogger().info("Command is by player");
				_plugin.getLogger().info("they got " + args.length);
				// Start: player running /donor check and seeing their own donor status
				if(args.length < 2) {
					_plugin.getLogger().info("they be checking their ownr");
					if(_playerDataCache.isCached(((Player) sender).getUniqueId())) {
						DonorData playerDonorData = _donorData.get(((Player) sender).getUniqueId());
						if(playerDonorData.isDonor()) {
							if(playerDonorData.getDays() == 0) {
								sender.sendMessage(ChatColor.GREEN + "You have donated money, you have permanent Donor Status! Thank you!");
								return true;
							} else {
								sender.sendMessage(ChatColor.GREEN + "You currently have " + playerDonorData.getDaysRemaining() + " days remaining");
								return true;
							} 
						} else {
							sender.sendMessage(new String[]{ChatColor.GREEN + "You currently do not have Donor Status. Interested in becoming a donor?",
									ChatColor.GREEN + "You can do so via https://ipwnage.com/donate, or through the '/donor protos' command."});
							return true;
						}
					}
				// End: player running /donor check and seeing their own donor status
				} else {
					_plugin.getLogger().info("Command running for someone else");
				// Start: staff running /donor check <username> and seeing that player's donor status
					if(!((Player) sender).hasPermission("autodonorstatus.admin")) {
						sender.sendMessage(ChatColor.RED + "You do not have permission!");
						return true;
					}
					if(_playerDataCache.isCached(args[1])) {
						DonorData playerDonorData = _donorData.get(_playerDataCache.getPlayerUUID(args[1]));
						if(playerDonorData.isDonor()) {
							if(playerDonorData.getDays() == 0) {
								sender.sendMessage(ChatColor.GREEN + args[1] + " has donated money. They have permanent Donor Status.");
								return true;
							} else {
								sender.sendMessage(ChatColor.GREEN + args[1] + " has " + playerDonorData.getDaysRemaining() + " days remaining");
								return true;
							} 
						} else {
							sender.sendMessage(ChatColor.GREEN + args[1] + " does not have donor status.");
							return true;
						}
					}
					sender.sendMessage(ChatColor.GREEN + args[1] + " does not have donor status.");
					return true;
					
				}
			//End: staff running /donor check <username> and seeing that player's donor status
			}
			//End: player commands
		}
		//End: /donor check
		return false;
	}

}
