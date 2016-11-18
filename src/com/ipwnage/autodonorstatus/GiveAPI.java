package com.ipwnage.autodonorstatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class GiveAPI implements Runnable {
	
	private AutoDonor _plugin;
	
	
	public GiveAPI(AutoDonor autoDonor) {
		_plugin = autoDonor;
	}
	
	@Override
	public void run() {
		AutoDonor._log.info("[AutoDonor] Running as scheduled.");
		JSONObject donorList = getDonorList();
		
		
		//AutoDonor._log.info("[AutoDonor] Got this JSON: " + donorList.toString());
		//_plugin.setDonorStatus(true, "Protocol_7");
		
	}
	
	
	
	public JSONObject getDonorList() {

		String serverResponse;

		try {
			serverResponse = sendGet(_plugin._apiURL);
			if (serverResponse.isEmpty()) {
				AutoDonor._log.info("[AutoDonor] Can't reach the iPwnAge Donation API!");
				return null;
			}
		} catch (IOException | URISyntaxException e) {
			AutoDonor._log.info("[AutoDonor] Can't reach the iPwnAge Donation API!");
			return null;
		}
		
		JSONParser jsonParser = new JSONParser();
		try {
			return (JSONObject) jsonParser.parse(serverResponse);
		} catch (ParseException e) {
			AutoDonor._log.info("[AutoDonor] Got non-valid response from iPwnAge Donation API!");
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
