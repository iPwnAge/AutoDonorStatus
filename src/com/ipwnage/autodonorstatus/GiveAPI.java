package com.ipwnage.autodonorstatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static java.lang.Math.toIntExact;



public class GiveAPI implements Runnable {
	
	private AutoDonor _plugin;
	
	
	public GiveAPI(AutoDonor autoDonor) {
		_plugin = autoDonor;
	}
	
	@Override
	public void run() {
		JSONObject donorList = getDonorList();
		JSONArray donations = (JSONArray) donorList.get("donations");
        Iterator<JSONObject> i = donations.iterator();
        while (i.hasNext()) {
        	JSONObject donation = i.next();
        	int paymentId = toIntExact((Long) donation.get("ID"));
        	JSONObject paymentMeta = (JSONObject)  donation.get("payment_meta");
        	String paymentUsername = (String) paymentMeta.get("username");
        	JSONObject payment_meta = (JSONObject)  donation.get("payment_meta");
        	
        	
        	if (!_plugin._processedPayments.contains(paymentId)) {
        		_plugin._processedPayments.add(paymentId);
        		if(paymentUsername.equals("")) {
        			//the API should've filtered this, but just in case:
        			_plugin.getLogger().info(paymentUsername + " just donated. Processing their donor status now.");
        		}
        		_plugin.getLogger().info(paymentUsername + " just donated. Processing their donor status now.");
        		OfflinePlayer player = Bukkit.getOfflinePlayer(paymentUsername);
        		if(player == null) { //check to see if the username has been seen on the server before. If not, we gotta do things manually.
        			_plugin.getLogger().severe(paymentUsername + " has not been seen on the server before. Cannot process their donor status!");
        			continue;
        		}
        		DonorData playerDonorData = new DonorData((int) (System.currentTimeMillis() / 1000), 0);
        		_plugin._donorData.put(player.getUniqueId(), playerDonorData);
        		_plugin.setDonorStatus(true, player.getUniqueId());
        		
        	}
        }
		
	}
	
	
	
	public JSONObject getDonorList() {

		String serverResponse;

		try {
			serverResponse = sendGet(_plugin._apiURL);
			if (serverResponse.isEmpty()) {
				_plugin.getLogger().severe("[AutoDonor] Can't reach the iPwnAge Donation API!");
				return null;
			}
		} catch (IOException | URISyntaxException e) {
			_plugin.getLogger().severe("[AutoDonor] Can't reach the iPwnAge Donation API!");
			return null;
		}
		
		JSONParser jsonParser = new JSONParser();
		try {
			return (JSONObject) jsonParser.parse(serverResponse);
		} catch (ParseException e) {
			_plugin.getLogger().severe("[AutoDonor] Got non-valid response from iPwnAge Donation API!");
			return null;
		}

	}
	

	
	private static String sendGet(String url) throws IOException, URISyntaxException {

		URL obj = new URL(url);
		URI uri = obj.toURI();

		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setDefaultUseCaches(false);

		con.setRequestMethod("GET");
		con.setRequestProperty("Accept", "application/json,text/html");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con.setRequestProperty("Connection", "close");
		con.setRequestProperty("User-Agent", "AutoDonor");


		InputStream serverResponseStream;

		try {
			serverResponseStream = con.getInputStream();
		} catch (IOException e) {
			serverResponseStream = con.getErrorStream();
		}

		if (serverResponseStream == null)
			return "";

		BufferedReader in = new BufferedReader(new InputStreamReader(serverResponseStream));

		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null)
			response.append(inputLine);

		serverResponseStream.close();

		con.disconnect();

		return response.toString();

	}


}
