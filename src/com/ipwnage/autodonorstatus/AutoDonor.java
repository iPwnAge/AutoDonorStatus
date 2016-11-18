package com.ipwnage.autodonorstatus;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
	private ConcurrentHashMap<UUID, DonorData> _donorData = new ConcurrentHashMap<UUID, DonorData>();
	public static Permission _permissions;
	private int _checkInterval = 15 * 20;
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
			for (String key : _data.getConfigurationSection("players").getKeys(false)) {
				_donorData.put(UUID.fromString(key), new DonorData((String) _data.get("players."+key)));
				}
			getLogger().info("Loaded " + _donorData.size() + " player's donation data.");
		} catch (IOException e) {
			getLogger().info("Didn't find a data file, making one now.");
			_dataFile.getParentFile().mkdirs();
			saveResource("data.yml", false);
		} catch (InvalidConfigurationException|NumberFormatException e) {
			getLogger().log(Level.SEVERE, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			getLogger().log(Level.SEVERE, "!!!!! THE DATA FILE WAS CORRUPT. DONATION TRACKING IS DEAD  !!!!!");
			getLogger().log(Level.SEVERE, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", e);
			return;
		} catch (NullPointerException e) {
			//hurm.
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
	
	@SuppressWarnings("deprecation")
	public void setDonorStatus(Boolean status, String playerName) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
		String rank = _permissions.getPrimaryGroup(null, player);
		getLogger().info("This player is ranked " + rank);
	}

	
    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        _permissions = rsp.getProvider();
    }
	
	
}
