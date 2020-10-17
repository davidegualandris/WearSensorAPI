package com.example.lapuile.wearsensor.repositories;

import com.example.lapuile.wearsensor.library.models.KaaApplication;
import com.example.lapuile.wearsensor.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class KaaApplicationRepository {
	private static KaaApplicationRepository instance;

    private KaaApplicationRepository() {}

    public static synchronized KaaApplicationRepository getInstance(){
        if (instance==null)
            instance = new KaaApplicationRepository();
        return instance;
    }

    public KaaApplication getKaaApplicationDataName() throws Exception
    {    	
    	KaaApplication kaaApplication = null;
    	
    	// Create URL
        URL kaaApiUrl = null;
        try {
            kaaApiUrl = new URL(Constants.KAA_EPTS_API_BASE_URL + "epts/api/v1/time-series/config");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new Exception("Malformed URL");
        }
    	
        HttpsURLConnection kaaConnection = null;

        // Create connection
        try {
            kaaConnection =
                    (HttpsURLConnection) kaaApiUrl.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Couldn't estabilish a connection with Kaa");
        }

        kaaConnection.setRequestProperty("Authorization", Constants.KAA_EPTS_API_BEARER_TOKEN);
        try {
            if (kaaConnection.getResponseCode() == 200) {
            	
            	InputStream responseBody = kaaConnection.getInputStream();

                InputStreamReader responseBodyReader =
                        new InputStreamReader(responseBody, "UTF-8");

                BufferedReader r = new BufferedReader(responseBodyReader);

                StringBuilder response = new StringBuilder();
                for (String line; (line = r.readLine()) != null; ) {
                    response.append(line).append('\n');
                }
                String jsonString = response.toString();

                // Close the connection
                kaaConnection.disconnect();
                
                List<String> dataNames = new ArrayList<>();
                String applicationName = "";
            	
                JSONObject jsonObject = new JSONObject(jsonString);
                
                // Get the keys -> just one in our case -> the applicationName
                Iterator<String> keys = jsonObject.keys();
                while(keys.hasNext()) {
                	applicationName = keys.next();
                    if (jsonObject.getJSONArray(applicationName) instanceof JSONArray) {
                    	JSONArray jsonValues = jsonObject.getJSONArray(applicationName);
                    	// Let's iterate over the values
                        for (int i = 0 ; i < jsonValues.length(); i++) {
                        	// Get the object
                            JSONObject jsonValue = jsonValues.getJSONObject(i);
                            dataNames.add(jsonValue.getString("name"));
                        }
                    }
                }

                
                /*JSONArray j = new JSONArray(jsonString);

                // Let's iterate over the objects -> just one in our case
                for (int i = 0 ; i < j.length(); i++) {
                	
                	// Get the object
                    JSONObject jsonObject = j.getJSONObject(i);
                    
                    // Get the keys -> just one in our case -> the applicationName
                    Iterator<String> keys = jsonObject.keys();
                    while(keys.hasNext()) {
                    	applicationName = keys.next();
                        if (jsonObject.get(endpointID) instanceof JSONObject) {
                        	
                        }
                    }
                }*/
                
                kaaApplication = new KaaApplication(applicationName, dataNames);
                
            } else {
                // Error handling code goes here
            }
        }catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        
    	return kaaApplication;
    }
}
