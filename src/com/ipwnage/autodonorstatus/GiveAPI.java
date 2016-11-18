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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class GiveAPI implements Runnable {
	
	private AutoDonor _plugin;
	
	
	public GiveAPI(AutoDonor autoDonor) {
		_plugin = autoDonor;
	}
	
	@Override
	public void run() {
		_plugin.getLogger().info("[AutoDonor] Running as scheduled.");
		JSONObject donorList = getDonorList();
		JSONArray donations = (JSONArray) donorList.get("donations");
        Iterator<JSONObject> i = donations.iterator();
        while (i.hasNext()) {
        	JSONObject donation = i.next();
        	_plugin.getLogger().info("Got this donor ID from donor list: " + donation.get("ID"));
        	JSONObject payment_meta = (JSONObject)  donation.get("payment_meta");
        	_plugin.getLogger().info("Got this username from donor list: " + payment_meta.get("username"));
        	
        }
		
		//AutoDonor._log.info("[AutoDonor] Got this JSON: " + donorList.toString());
		//_plugin.setDonorStatus(true, "Protocol_7");
		
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
