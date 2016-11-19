package com.ipwnage.autodonorstatus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.ipwnage.autodonorstatus.DonorData;

import net.milkbowl.vault.permission.Permission;

public class AutoDonor extends JavaPlugin {
	static final Logger _log = Logger.getLogger("Minecraft");
	private File _config = new File(getDataFolder(), "config.yml");
	File _dataFile = new File(getDataFolder(), "data.yml");
	FileConfiguration _data = new YamlConfiguration();
	ConcurrentHashMap<UUID, DonorData> _donorData = new ConcurrentHashMap<UUID, DonorData>();
	public static Permission _permissions;
	private int _checkInterval = 15 * 20;
	ArrayList<Integer> _processedPayments;
	private int _giveAPI;
	String _apiURL = "";
	
	@Override
	public void onEnable() {
		//Check for Vault. We need Vault for checking PEX groups.
		if(getServer().getPluginManager().getPlugin("Vault") == null){
			getLogger().severe((String.format("Your server doesn't have Vault installed. Disabling plugin.")));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		//Create the config on new install
		if(!_config.exists()) {
			this.saveDefaultConfig();
			getLogger().info("Didn't find an AutoDonor configuration. Making one now.");
		}
		
		//Load the data file, otherwise create it. Also throw a nasty warning if we can't load the existing data file.
		try {
			_data.load(_dataFile);
		} catch (IOException e) {
			getLogger().info("Didn't find a data file, making one now.");
			_dataFile.getParentFile().mkdirs();
			saveResource("data.yml", false);
		} catch (NumberFormatException|InvalidConfigurationException e) {
			getLogger().log(Level.SEVERE, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			getLogger().log(Level.SEVERE, "!!!!! THE DATA FILE WAS CORRUPT. DONATION TRACKING IS DEAD  !!!!!");
			getLogger().log(Level.SEVERE, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", e);
			return;
		}
		
		try {
			_processedPayments =  (ArrayList<Integer>) _data.getList("processed_ids", new ArrayList<Integer>());
			getLogger().info("Loaded " + _processedPayments.size() + " payment data.");
			for (String key : _data.getConfigurationSection("players").getKeys(false)) {
				_donorData.put(UUID.fromString(key), new DonorData((String) _data.get("players."+key)));
				}
			getLogger().info("Loaded " + _donorData.size() + " player's donation data.");
		} catch (NullPointerException e) {
			getLogger().info("Loaded 0 player's donation data.");
			getLogger().log(Level.SEVERE,"Got an exception", e);
		}
		
		
		//Set config values and connect to Vault's Permission class.
		setupPermissions();
		_checkInterval = getConfig().getInt("checkInterval") * 20;
		_apiURL = getConfig().getString("apiURL");
		
		//Async task for checking the web API
		_giveAPI = this.getServer().getScheduler().runTaskTimerAsynchronously(this, new GiveAPI(this), _checkInterval, _checkInterval).getTaskId(); 
		getLogger().info("Successfully started AutoDonor.");
		return;
	}
	
	@Override
	public void onDisable() {
		saveConfig();
		for (Object key : _donorData.keySet()) {
			_data.set("players."+key, _donorData.get(key).toString());
			}
		_data.set("processed_ids", _processedPayments);
		try {
			_data.save(_dataFile);
		} catch (IOException e) {
			getLogger().severe("Couldn't save the Donor Data file. This isn't good");
			e.printStackTrace();
		}
		getLogger().info("All Donor Data and configuration values saved.");
	}
	
	@SuppressWarnings("deprecation")
	public void setDonorStatus(Boolean setDonor, UUID playerUUID, String playerName) {
		String rank = _permissions.getPrimaryGroup((String) null, playerName);
		if(setDonor) { //set the player to donor status
			if(!rank.startsWith("d")) { //Don't double set donor if the player donated twice.
				if(rank.equals("normal")) {
					rank = "commoner";
				}
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "ee pex user " + playerName + " group set d" + rank);
			getLogger().info("Successfully applied donor status to the player " + playerName + "(" + rank + "->" + "d" + rank + ")");
			} else {
				getLogger().info("The player " + playerName + "already has donor status, ignoring.");
			}
		} else { //remove the player's donor status
			if(rank.startsWith("d")) { //Don't remove the donor status if the player isn't already donor
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "ee pex user " + playerName + " group set " + rank.substring(1, rank.length()));
			getLogger().info("Successfully applied donor status to the player " + playerName + "(" + rank + "->" + "d" + rank + ")");
			}
		}
		
	}

	
    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        _permissions = rsp.getProvider();
    }
	
	
}
