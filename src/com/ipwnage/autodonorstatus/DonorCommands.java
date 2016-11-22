package com.ipwnage.autodonorstatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ipwnage.autodonorstatus.util.DonorData;
import com.ipwnage.autodonorstatus.util.PlayerDataCache;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class DonorCommands implements CommandExecutor {

	private ConcurrentHashMap<UUID, DonorData> _donorData;
	private AutoDonor _plugin;
	private PlayerDataCache _playerDataCache;
	private HashSet<UUID> _confirmDB = new HashSet<UUID>();
	private Economy _economy;


	public DonorCommands(AutoDonor plugin) {
		_plugin = plugin;
		_donorData = plugin._donorData;
		_playerDataCache = plugin._playerDataCache;
		_economy = AutoDonor._economy;

	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Whoops, incorrect syntax!");
		}
		//Start: /donor check command
		if(args[0].equals("check")) {
			//Start: player commands
			if(sender instanceof Player) { // Start: player running /donor check and seeing their own donor status
				if(args.length < 2) {
					if(_playerDataCache.isCached(((Player) sender).getUniqueId())) {
						DonorData playerDonorData = _donorData.get(((Player) sender).getUniqueId());
						if(playerDonorData.isDonor()) {
							if(playerDonorData.getDays() == 0) {
								sender.sendMessage(ChatColor.GREEN + "Because you've donated money, you have permanent Donor Status. Thank you for your support!");
								return true;
							} else {
								sender.sendMessage(ChatColor.GREEN + "You currently have " + playerDonorData.getDaysRemaining() + " days of Donor Status remaining!");
								return true;
							} 
						} else {
							sender.sendMessage(new String[]{
									ChatColor.GREEN + "You currently do not have Donor Status. Interested in becoming a donor?",
									ChatColor.GREEN + "You can do so via https://ipwnage.com/donate, or through the '/donor buy' command."
							});
							return true;
						}
					} else {
						sender.sendMessage(new String[]{
								ChatColor.GREEN + "You currently do not have Donor Status. Interested in becoming a donor?",
								ChatColor.GREEN + "You can do so via https://ipwnage.com/donate, or through the '/donor buy' command."
						});
						return true;
					} // End: player running /donor check and seeing their own donor status


				} else { // Start: staff running /donor check <username> and seeing that player's donor status
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
								sender.sendMessage(ChatColor.GREEN + args[1] + " has " + playerDonorData.getDaysRemaining() + " days remaining.");
								return true;
							} 
						} else {
							sender.sendMessage(ChatColor.GREEN + args[1] + " does not have donor status.");
							return true;
						}
					}
					sender.sendMessage(ChatColor.GREEN + args[1] + " does not have donor status.");
					return true;

				}//End: staff running /donor check <username> and seeing that player's donor status
				//End: player commands
			} else { //Start: console /donor check <username>
				if(args.length < 2) {
					sender.sendMessage("Incorrect usage. /donor check <username>");
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

			} //End: Console /donor check

		}//End: /donor check

		if(args[0].equals("buy")) { //Start: /donor buy (this just adds a user to the _confirmDB, /donor buy confirm is actually doing the grunt work
			// should be noted: only players can use this command.
			if(sender instanceof Player) {
				UUID playerUUID = ((Player) sender).getUniqueId();
				String playerName = ((Player) sender).getName();


				if(args.length < 2) { //Start: /donor buy
					sender.sendMessage(ChatColor.GREEN + "You can earn 30 days of Donor Status for " + ChatColor.YELLOW + "2250⛃" + ChatColor.GREEN + ". To confirm, type /donor buy confirm");
					_confirmDB.add(((Player) sender).getUniqueId());
					return true;
					//End: /donor buy


				} else if (args[1].equals("confirm")) { //Start: /donor buy confirm
					if(_confirmDB.contains(playerUUID)) {
						if(_economy.getBalance(sender.getName()) >= 2250) { //Start: player has enough money to buy donor
							if (_playerDataCache.isCached(playerUUID)) { //Player buys more time on an existing donor status
								_donorData.get(playerUUID).setDays(_donorData.get(playerUUID).getDays() + 30);
								sender.sendMessage(new String[] {
										ChatColor.GREEN + "Awesome, you're extending your Donor Status by 30 days.",
										ChatColor.GREEN + "You now have Donor Status for " + _donorData.get(playerUUID).getDaysRemaining() + " days.",
										ChatColor.GREEN + "Thanks for helping out, we really appreciate it!"
								});
								_economy.withdrawPlayer(playerName, 2250);

							} else { //Player is new to donor status
								_donorData.put(playerUUID, new DonorData(playerName, (int) (System.currentTimeMillis() / 1000L), 30));
								_playerDataCache.addPlayer(playerUUID, playerName);
								_plugin.setDonorStatus(true, playerUUID, playerName);
								sender.sendMessage(ChatColor.GREEN + "Awesome, you now have Donor Status for the next 30 days. Thanks for helping out!");
								_economy.withdrawPlayer(playerName, 2250);
							}
							//End: player has enough money to buy donor
						} else { //Start: not enough protos error message
							sender.sendMessage(ChatColor.RED + "You do not have enough Protos to buy 30 days of Donor Status. You need a total of " + ChatColor.YELLOW + "2250⛃" + ChatColor.RED + ".");	
						}
						_confirmDB.remove(playerUUID);
						return true;
					}
					sender.sendMessage(ChatColor.RED + "Please run '/donor buy' first, you do not currently have a pending Donor Status purchase.");
					return true;

				} //End: /donor buy confirm command
			} 
			sender.sendMessage("This command cannot be used in console.");
			return true;
		} //End: /donor buy command

		if(args[0].equals("set")) { //Start: /donor set command
			if(args.length > 2)  { //Start: /donor set <player> true <time>
				String playerName = args[1];
				int playerDonorTime = Integer.valueOf(args[2]);
				if(_playerDataCache.isCached(playerName)) {
					// Existing player, probably going to add 
				} else if(Bukkit.getPlayer(playerName).isOnline()){ //New player being added, for the moment requires the player to be online
					UUID playerUUID = Bukkit.getPlayer(playerName).getUniqueId();
					if(playerDonorTime > 0) {
						sender.sendMessage(ChatColor.GREEN + "Successfully set " + Bukkit.getPlayer(playerName).getDisplayName() + "'s Donor Status for " + playerDonorTime + " days.");
						Bukkit.getPlayer(playerName).sendMessage(ChatColor.GREEN + "Awesome, you now have Donor Status for " + playerDonorTime + " days. Thanks for helping out!");
						_donorData.put(playerUUID, new DonorData(playerName, (int) (System.currentTimeMillis() / 1000L), playerDonorTime));
						_playerDataCache.addPlayer(playerUUID, playerName);
						_plugin.setDonorStatus(true, playerUUID, playerName);
					} else if (playerDonorTime == 0){
						sender.sendMessage(ChatColor.GREEN + "Successfully set " + Bukkit.getPlayer(playerName).getDisplayName() + "'s permanent Donor Status.");
						Bukkit.getPlayer(playerName).sendMessage(ChatColor.GREEN + "Awesome, you now have permanent Donor Status. Thanks for helping out!");
						_donorData.put(playerUUID, new DonorData(playerName, (int) (System.currentTimeMillis() / 1000L), 0));
						_playerDataCache.addPlayer(playerUUID, playerName);
						_plugin.setDonorStatus(true, playerUUID, playerName);
					} else {
						_donorData.remove(playerUUID);
						_playerDataCache.removePlayer(playerUUID);
						_plugin.setDonorStatus(false, playerUUID, playerName);
						sender.sendMessage(ChatColor.GREEN + "Successfully removed " + Bukkit.getPlayer(playerName).getDisplayName() + "'s Donor Status.");
						_plugin._data.set("players." + playerUUID, null);
					}
					
				} //End: Proper /donor set <player> <time> syntax
			} else { // Start: Error for improper syntax 
				sender.sendMessage(new String[]{
						ChatColor.RED + "You've used the wrong syntax. Please try again.",
						ChatColor.RED + "/donor set <player> <time>",
						ChatColor.RED + "For example: /donor set John 30",
						ChatColor.RED + "For example: /donor set John -1",
						ChatColor.RED + "Use 0 for permanent DS, -1 to remove DS."
				});
			} //End: Error for improper "true" syntax.
		}

	return false;
}

}
