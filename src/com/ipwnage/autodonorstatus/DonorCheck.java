package com.ipwnage.autodonorstatus;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DonorCheck implements Runnable {
	
	private AutoDonor _plugin;
	private ConcurrentHashMap<UUID, DonorData> _donorData;
	private PlayerDataCache _playerDataCache;
	
	DonorCheck(AutoDonor plugin) {
		_plugin = plugin;
		_donorData = plugin._donorData;
		_playerDataCache = plugin._playerDataCache;
		
	}

	@Override
	public void run() {
		//check if players in the DB are still allowed to remain donor
		for(Entry<UUID, DonorData> playerProfile : _donorData.entrySet()) {
			UUID playerUUID = playerProfile.getKey();
			DonorData playerDonorData = playerProfile.getValue();
			if (!playerDonorData.isDonor()) {
				_plugin.getLogger().info("Player " + _playerDataCache.getPlayerName(playerUUID) + " has exceeded their remaining Donor Status time. Removing them from the Donor List.");
				_plugin.setDonorStatus(false, playerUUID, _playerDataCache.getPlayerName(playerUUID));
				_donorData.remove(playerUUID);
			}
		}
		
	}

}
