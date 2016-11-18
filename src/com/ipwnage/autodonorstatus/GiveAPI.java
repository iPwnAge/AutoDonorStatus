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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class GiveAPI implements Runnable {
	
	private static final CookieHandler COOKIE_MANAGER = new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
	private AutoDonor _plugin;
	
	
	public GiveAPI(AutoDonor autoDonor) {
		_plugin = autoDonor;
	}
	
	@Override
	public void run() {
		AutoDonor._log.info("[AutoDonor] Running as scheduled.");
		JsonArray donorList = getDonorList();
		AutoDonor._log.info("[AutoDonor] Got this JSON: " + donorList.toString());
		//_plugin.setDonorStatus(true, "Protocol_7");
		
	}
	
	
	
	public JsonArray getDonorList() {

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
		
		return new JsonParser().parse(serverResponse).getAsJsonArray();

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

		for (Map.Entry<String, List<String>> pair : COOKIE_MANAGER.get(uri, con.getRequestProperties()).entrySet()) {
			String key = pair.getKey();

			for (String cookie : pair.getValue())
				con.addRequestProperty(key, cookie);

		}

		InputStream serverResponseStream;

		try {
			serverResponseStream = con.getInputStream();
		} catch (IOException e) {
			serverResponseStream = con.getErrorStream();
		}

		if (serverResponseStream == null)
			return "";

		COOKIE_MANAGER.put(uri, con.getHeaderFields());

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
