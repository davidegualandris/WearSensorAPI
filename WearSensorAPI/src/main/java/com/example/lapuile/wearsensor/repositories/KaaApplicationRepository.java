package com.example.lapuile.wearsensor.repositories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.lapuile.wearsensor.connettors.KaaConnector;
import com.example.lapuile.wearsensor.library.models.KaaApplication;
import com.example.lapuile.wearsensor.library.models.KaaEndpoint;
import com.example.lapuile.wearsensor.library.models.KaaEndpointConfiguration;
import com.example.lapuile.wearsensor.utils.Constants;

/**
 * Class used to ask Kaa about application configuration
 */
public class KaaApplicationRepository {
    
    /**
     * Function that query the KAA EPTS API to know about all the dataNames
     * @return List of all available data names
     * @throws Exception
     */
    private static List<String> getAllKaaApplicationDataNames() throws Exception
    {    	
    	List<String> dataNames = new ArrayList<>();
    	
        try {
            
        	String jsonString = KaaConnector.connect(Constants.APPLICATION_REPOSITORY_URL);
        	
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
                
        }catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        
    	return dataNames;
    }

    /**
    * Function to return the KaaApplication configurations of the specified endpoints (all possible configurations if equals to "")
    * @param endpointId Endpoints whose configuration I want to retrieve separated with ,
    * @return Instance of KaaApplication containing the requested configuration
    * @throws Exception If is not possible to retrieve the data names from Kaa
    */
    public static KaaApplication getKaaApplicationDataNames(String endpointId) throws Exception{
    	
    	List<KaaEndpoint> kaaEndPoints;
    	
    	// I get all possible dataNames
    	List<String> dataNames = getAllKaaApplicationDataNames();
    	
    	// If he's asking for every data of every endpoint
    	if( endpointId == null || endpointId.isEmpty() ){
    		
    		kaaEndPoints = KaaEndpointRepository.getKaaEndpointsData(String.join(",", dataNames));
    		
    	}else{
  
    		List<KaaEndpointConfiguration> kaaEndpointConfigurations = new ArrayList<>();
    		String[] endpoints = endpointId.split(",");
    		
    		// Since i don't know which endpoint send which data i am gonna ask the platform for every possible data name for every endpoint
    		for(int i=0;i<endpoints.length;i++)
    			kaaEndpointConfigurations.add(new KaaEndpointConfiguration(endpoints[i], dataNames));
    		
    		// I am going to query the EPTS API to know about the configurations of requested endpoints
    		kaaEndPoints = KaaEndpointRepository.getKaaEndpointsData(kaaEndpointConfigurations);
    	} 
    	
    	// Then i am formatting the result
    	return convertKaaEndpointToKaaApplication(kaaEndPoints);
    }
    
    /**
     * Function to check the availability of the sensor "dataName" in the specified endpoint. If more than one endpoint is specified, then it checks only the first one
     * @param endpointId EndpointID whose data you want to retrieve
	 * @param dataName sensor name to check for availability within the endpointId
     * @return HTTP response. OK/200 if the sensor exists within the endpoint. 404 otherwise.
     * @throws Exception If is not possible to retrieve the data names from Kaa
     */
     public static Response checkAvailability(String endpointId, String dataName) throws Exception{
    	
    	// If more than one endpoint is specified, then it checks only the first one
     	KaaApplication kaaApplication = getKaaApplicationDataNames(endpointId.contains(",") ? endpointId.split(",")[0] : endpointId);
     	
     	// since i asked for just one endpoint, i am sure that getEndpoints().size == 1. If not, an error is throwed
      	return kaaApplication.getEndpoints().get(0).getDataNames().contains(dataName) ?
     			 Response.status(Status.OK).build() :
     				Response.status(Status.NOT_FOUND).build();
     }
    
    /**
     * Since to get the configuration data we need to query the Kaa EPTS API 
     * @param kaaEndPoints KaaEndpoints from which retrieve the configurations
     * @return KaaApplication with the correct configuration
     */
    private static KaaApplication convertKaaEndpointToKaaApplication(List<KaaEndpoint> kaaEndpoints) {
    	List<KaaEndpointConfiguration> config = new ArrayList<>();
    	for (KaaEndpoint endpoint : kaaEndpoints) {
    		config.add(new KaaEndpointConfiguration(endpoint.getEndpointId(), endpoint.getValuesDataNames()));
		}
    	return new KaaApplication(Constants.KAA_APPLICATION_NAME, config);
    }
}
