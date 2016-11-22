package com.ipwnage.autodonorstatus.util;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataCache {
	
	private ConcurrentHashMap<UUID, String> _UUIDtoUsername = new ConcurrentHashMap<UUID, String>();
	private ConcurrentHashMap<String, UUID> _UsernametoUUID = new ConcurrentHashMap<String, UUID>();
	
	public PlayerDataCache() {}
	
	public void addPlayer(UUID playerUUID, String playerName) {
		_UUIDtoUsername.put(playerUUID, playerName.toLowerCase());
		_UsernametoUUID.put(playerName.toLowerCase(), playerUUID);
	}
	
	public UUID getPlayerUUID(String playerName) {
		return _UsernametoUUID.get(playerName);
	}
	
	public String getPlayerName(UUID playerUUID) {
		return _UUIDtoUsername.get(playerUUID);
	}
	
	public void removePlayer(UUID playerUUID) {
		_UsernametoUUID.remove(_UUIDtoUsername.get(playerUUID));
		_UUIDtoUsername.remove(playerUUID);
	}
	
	public void removePlayer(String playerName) {
		_UUIDtoUsername.remove(_UsernametoUUID.get(playerName));
		_UsernametoUUID.remove(playerName);
	}
	
	public boolean isCached(UUID playerUUID) {
		return _UUIDtoUsername.containsKey(playerUUID);
	}
	
	public boolean isCached(String playerName) {
		return _UsernametoUUID.containsKey(playerName.toLowerCase());
	}

}
