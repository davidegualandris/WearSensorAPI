package com.example.lapuile.wearsensor.library.formatters;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.lapuile.wearsensor.library.models.KaaEndpointConfiguration;

/*
 * Formatters to let the application to get a List of KaaEndpointConfigurations from a JSON and viceversa
 */
public class KaaEndpointsConfigurationsFormatter {

	/**
	 * Function to convert a JSON in a List of KaaEndpointConfigurations
	 * @param JSON JSON to be converted in the List of KaaEndpointConfigurations
	 * @return List of KaaEndpointConfigurations
	 * @throws UnsupportedEncodingException 
	 */
	public static List<KaaEndpointConfiguration> JSONtoKaaEndpointConfigurations(String encodedJSON) throws UnsupportedEncodingException{
		List<KaaEndpointConfiguration> configs = new ArrayList<>();
		if(encodedJSON == null || encodedJSON.equals(""))
			return configs;
		String JSON = URLDecoder.decode(encodedJSON, StandardCharsets.UTF_8.name());
		JSONObject jsonObj = new JSONObject(JSON);
		JSONArray jsonConfigurations = jsonObj.getJSONArray("config");
		for(int i=0;i<jsonConfigurations.length();i++) {
			configs.add(new KaaEndpointConfiguration(jsonConfigurations.get(i).toString()));
		}
		return configs;
	}
	
	/**
	 * Function to convert a List of KaaEndpointConfigurations in a JSON
	 * @param JSON List of KaaEndpointConfigurations to be converted in a JSON
	 * @return JSON of serialized KaaEndpointConfigurations
	 * @throws UnsupportedEncodingException 
	 */
	public static String KaaEndpointConfigurationsToJSON(List<KaaEndpointConfiguration> kaaEndpointConfigurations) throws UnsupportedEncodingException{
		String decodedJSON = "{\"config\":[";
		for(int i=0;i<kaaEndpointConfigurations.size();i++)
			decodedJSON+=kaaEndpointConfigurations.get(i).toJSON()+",";
		//remove last comma
		if(decodedJSON.charAt(decodedJSON.length()-1) == ',')
			decodedJSON = decodedJSON.substring(0, decodedJSON.length() - 1);
		return URLEncoder.encode(decodedJSON + "]}", StandardCharsets.UTF_8.name());
	}
}
