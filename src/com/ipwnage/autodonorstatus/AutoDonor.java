package com.ipwnage.autodonorstatus;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.permission.Permission;

public class AutoDonor extends JavaPlugin {
	static final Logger _log = Logger.getLogger("Minecraft");
	private File _config = new File(getDataFolder(), "config.yml");
	public static Permission _permissions;
	private int _checkInterval = 15 * 20;
	private int _giveAPI;
	String _apiURL = "";
	
	@Override
	public void onEnable() {
		if(!_config.exists()) {
			this.saveDefaultConfig();
			_log.info("[AutoDonor] Didn't find an AutoDonor configuration. Making one now.");
		}
		if(getServer().getPluginManager().getPlugin("Vault") == null){
			_log.severe((String.format("[AutoDonor] Your server doesn't have Vault installed. Disabling plugin.")));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		setupPermissions();
		_checkInterval = getConfig().getInt("checkInterval") * 20;
		_apiURL = getConfig().getString("apiURL");
		_giveAPI = this.getServer().getScheduler().runTaskTimerAsynchronously(this, new GiveAPI(this), _checkInterval, _checkInterval).getTaskId(); 
		_log.info("[AutoDonor] Successfully started AutoDonor.");
		return;
	}
	
	@SuppressWarnings("deprecation")
	public void setDonorStatus(Boolean status, String playerName) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
		String rank = _permissions.getPrimaryGroup(null, player);
		_log.info("[AutoDonor] This player is ranked " + rank);
	}

	
    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        _permissions = rsp.getProvider();
    }
	
	
}
