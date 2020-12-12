package com.example.lapuile.wearsensor.repositories;

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

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.lapuile.wearsensor.library.models.KaaApplication;
import com.example.lapuile.wearsensor.library.models.KaaEndpoint;
import com.example.lapuile.wearsensor.library.models.KaaEndpointConfiguration;
import com.example.lapuile.wearsensor.utils.Constants;

public class KaaApplicationRepository {
	private static KaaApplicationRepository instance;
	
    private KaaApplicationRepository() {}

    private static String baseUrl;
    
    public static synchronized KaaApplicationRepository getInstance(){
        if (instance==null) {
        	baseUrl = Constants.KAA_EPTS_API_BASE_URL + "time-series/config";
        	instance = new KaaApplicationRepository();
        }            
        return instance;
    }
    
    /**
     * Function that query the KAA EPTS API to know about all the dataNames
     * @return List of all available data names
     * @throws Exception
     */
    private List<String> getAllKaaApplicationDataNames() throws Exception
    {    	
    	List<String> dataNames = new ArrayList<>();
    	// Create URL
        URL kaaApiUrl = null;
        try {
            kaaApiUrl = new URL(baseUrl);
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
            	
                JSONObject jsonObject = new JSONObject(jsonString);
                
                // Get the keys -> just one in our case -> the applicationName
                Iterator<String> keys = jsonObject.keys();
                while(keys.hasNext()) {
                	String applicationName = keys.next();
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
                
            } else {
                // Error handling code goes here
            }
        }catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        
    	return dataNames;
    }

    /**
    * Function to return the KaaApplication configurations of the specified endpoints (all possible configurations if equals to "")
    * @param endpointId Endpoints whose configuration I want to retrieve separated from ,
    * @return Instance of KaaApplication containing the requested configuration
    * @throws Exception
    */
    public KaaApplication getKaaApplicationDataNames(String endpointId) throws Exception{
    	
    	List<KaaEndpoint> kaaEndPoints;
    	
    	// I get all possible dataNames
    	List<String> dataNames = getAllKaaApplicationDataNames();
    	
    	// If he's asking for every data of every endpoint
    	if( endpointId == null || endpointId.isEmpty() ){
    		
    		kaaEndPoints = KaaEndpointRepository.getInstance().getKaaEndpointsData(String.join(",", dataNames));
    		
    	}else{
  
    		List<KaaEndpointConfiguration> kaaEndpointConfigurations = new ArrayList<>();
    		String[] endpoints = endpointId.split(",");
    		
    		// Since i don't know which endpoint send which data i am gonna ask the platform for every possible data name for every endpoint
    		for(int i=0;i<endpoints.length;i++)
    			kaaEndpointConfigurations.add(new KaaEndpointConfiguration(endpoints[i], dataNames));
    		
    		// I am going to query the EPTS API to know about the configurations of requested endpoints
    		kaaEndPoints = KaaEndpointRepository.getInstance().getKaaEndpointsData(kaaEndpointConfigurations);
    	} 
    	
    	// Then i am formatting the result
    	return convertKaaEndpointToKaaApplication(kaaEndPoints);
    }
    
    /**
     * Since to get the configuration data we need to query the Kaa EPTS API 
     * @param kaaEndPoints KaaEndpoints from which retrieve the configurations
     * @return KaaApplication with the correct configuration
     */
    private KaaApplication convertKaaEndpointToKaaApplication(List<KaaEndpoint> kaaEndpoints) {
    	List<KaaEndpointConfiguration> config = new ArrayList<>();
    	for (KaaEndpoint endpoint : kaaEndpoints) {
    		config.add(new KaaEndpointConfiguration(endpoint.getEndpointId(), endpoint.getValuesDataNames()));
		}
    	return new KaaApplication(Constants.KAA_APPLICATION_NAME, config);
    }
}
