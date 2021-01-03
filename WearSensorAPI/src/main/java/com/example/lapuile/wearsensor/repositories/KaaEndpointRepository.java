package com.example.lapuile.wearsensor.repositories;

import com.example.lapuile.wearsensor.connettors.KaaConnector;
import com.example.lapuile.wearsensor.library.models.KaaEndpoint;
import com.example.lapuile.wearsensor.library.models.KaaEndpointConfiguration;
import com.example.lapuile.wearsensor.library.models.interfaces.KaaValue;
import com.example.lapuile.wearsensor.library.models.KaaValueMulti;
import com.example.lapuile.wearsensor.library.models.KaaValueSingle;
import com.example.lapuile.wearsensor.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class used to ask Kaa about endpoint values
 */
public class KaaEndpointRepository {

    /**
     * Function to parse the JSON returned from the KAA EPTS API to a List of KaaEndpoints
     * @param jsonString JSON returned from the KAA EPTS API
     * @param samplePeriod The data sampling period
     * @return List of KaaEndpoints parsed from a JSON
     * @throws Exception 
     */
    private static List<KaaEndpoint> getKaaEndpointsFromJSON(String jsonString, Long samplePeriod) throws Exception{
    	
    	// List to be returned
        List<KaaEndpoint> kaaEndpoints = new ArrayList<>();
        
        // To keep track of the "lastSampled timestamp"
        long lastSampled = 0L;
        
        // Temporary map to save the data retrived scanning the JSON
        Map<String, KaaEndpoint> tmpEndpoints = new HashMap<>();

        JSONArray j = new JSONArray(jsonString);

        // Let's iterate over the objects
        for (int i = 0 ; i < j.length(); i++) {

            // Get the object
            JSONObject jsonObject = j.getJSONObject(i);

            // Get the keys -> just one in our case -> the endpointId
            Iterator<String> keys = jsonObject.keys();
            while(keys.hasNext()) {
                String endpointID = keys.next();
                if (jsonObject.get(endpointID) instanceof JSONObject) {

                    JSONObject valueJson = (JSONObject) jsonObject.get(endpointID);

                    // Get the keys -> just one in our case -> hum,temp or co2
                    Iterator<String> dataNames = valueJson.keys();
                    while (dataNames.hasNext()) {
                        // Get the list of values associated with this JsonObject
                        String dataName = dataNames.next(); // Might be temp, hum, co2 etc
                        List<KaaValue> kaaValues = new ArrayList<>();

                        // Convert the values in a List<KaaValue>
                        JSONArray values = (JSONArray) valueJson.get(dataName);
                        for (int x = 0 ; x < values.length(); x++) {
                            // Get the object
                            JSONObject kaaValueJson = values.getJSONObject(x);
                            
                            Date timestamp;
                            String dateString = kaaValueJson.get("timestamp").toString();
                        	try {
                        		timestamp = Constants.KAA_EPTS_API_DATE_FORMAT.parse(dateString);
                            }catch(Exception e) {
                            	timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                            			.parse(dateString);
                            }
                            // I want to sample the first one
                            if(x == 0)
                            	lastSampled = timestamp.getTime() - samplePeriod;

                            // Check the sampling period
                            if (timestamp.getTime() - lastSampled >= samplePeriod) {
                            	lastSampled += samplePeriod;
                            	int jsonValuesSize = kaaValueJson.getJSONObject("values").keySet().size();
                            	KaaValue kaaValue = null;                  
                                if(jsonValuesSize == 0) {
                                	throw new Exception("Error while parsing the data");
                                }else if(jsonValuesSize == 1) {
                                	kaaValue = new KaaValueSingle(timestamp, kaaValueJson.getJSONObject("values").toString());
                                }else {
                                	kaaValue = new KaaValueMulti(timestamp, kaaValueJson.getJSONObject("values").toString());
                                }
                                kaaValues.add(kaaValue);
                            }
                        }

                        // Update the KaaValue we already got
                        KaaEndpoint tmpEndpoint = null;
                        Map<String, List<KaaValue>> tmpMapValues = new HashMap<>();
                        if (tmpEndpoints.containsKey(endpointID)) {
                            // If we already read some data from that KaaEndpoint
                            tmpEndpoint = tmpEndpoints.get(endpointID);
                            tmpMapValues = tmpEndpoint.getValues();
                        } else {
                            tmpEndpoint = new KaaEndpoint(endpointID, tmpMapValues);
                        }
                        tmpMapValues.put(dataName, kaaValues);
                        tmpEndpoint.setValues(tmpMapValues);
                        tmpEndpoints.put(endpointID, tmpEndpoint);
                    }
                }
            }
        }

        // "Exporting" data
        for(String key : tmpEndpoints.keySet())
        	kaaEndpoints.add(tmpEndpoints.get(key));
        
        return kaaEndpoints;
    }
    
    /**
     * Function to parse the JSON returned from the KAA EPTS API to a List of KaaEndpoints without values
     * @param jsonString JSON returned from the KAA EPTS API
     * @return List of KaaEndpoints (without values) parsed from a JSON
     * @throws ParseException 
     */
    private static List<KaaEndpoint> getKaaEndpointsConfigurationFromJSON(String jsonString) throws ParseException{
    	
    	// List to be returned
        List<KaaEndpoint> kaaEndpoints = new ArrayList<>();
        
        // Temporary map to save the data retrived scanning the JSON
        Map<String, KaaEndpoint> tmpEndpoints = new HashMap<>();

        JSONArray j = new JSONArray(jsonString);

        // Let's iterate over the objects
        for (int i = 0 ; i < j.length(); i++) {

            // Get the object
            JSONObject jsonObject = j.getJSONObject(i);

            // Get the keys -> just one in our case -> the endpointId
            Iterator<String> keys = jsonObject.keys();
            while(keys.hasNext()) {
                String endpointID = keys.next();
                if (jsonObject.get(endpointID) instanceof JSONObject) {

                    JSONObject valueJson = (JSONObject) jsonObject.get(endpointID);

                    // Get the keys -> just one in our case -> hum,temp or co2
                    Iterator<String> dataNames = valueJson.keys();
                    while (dataNames.hasNext()) {
                        // Get the list of values associated with this JsonObject
                        String dataName = dataNames.next(); // Might be temp, hum, co2 etc
                        // Update the KaaValue we already got
                        KaaEndpoint tmpEndpoint = null;
                        Map<String, List<KaaValue>> tmpMapValues = new HashMap<>();
                        if (tmpEndpoints.containsKey(endpointID)) {
                            // If we already read some data from that KaaEndpoint
                            tmpEndpoint = tmpEndpoints.get(endpointID);
                            tmpMapValues = tmpEndpoint.getValues();
                        } else {
                            tmpEndpoint = new KaaEndpoint(endpointID, tmpMapValues);
                        }
                        tmpMapValues.put(dataName, new ArrayList<>());
                        tmpEndpoint.setValues(tmpMapValues);
                        tmpEndpoints.put(endpointID, tmpEndpoint);
                    }
                }
            }
        }

        // "Exporting" data
        for(String key : tmpEndpoints.keySet())
        	kaaEndpoints.add(tmpEndpoints.get(key));
        
        return kaaEndpoints;
    }
    
    /**
	 * Function that returns the data obtained from Kaa EPTS API based on the parameters specified below (FOR EVERY ENDPOINT)
	 * @param timeSeriesName dataNames that you want to retrieve for every end(ex. "temperature,humidity")
	 * @param fromDate       "Date" from which to retrieve the data
	 * @param toDate         "Date" up to which to recover the data
	 * @param includeTime    defines whether fromDate and toDate are inclusive [from, to, both, none]
	 * @param sort           data sorting [ASC, DESC]
	 * @param samplePeriod   The data sampling period
	 * @return List of KaaEndpoint
	 * @throws Exception If something goes wrong while retrieving the data
	 */
    public static List<KaaEndpoint> getKaaEndpointsData(String timeSeriesName, String fromDate, String toDate,
    												String includeTime, String sort, long samplePeriod)
    											throws Exception{    	
    	
        // List to be returned
        List<KaaEndpoint> kaaEndpointsFinal = new ArrayList<>();
        
        // Try-catch to avoid "nullpointerexception"
        try {
        	if(timeSeriesName.isEmpty() || fromDate.isEmpty() || toDate.isEmpty())
            	throw new Exception("timeSeriesName, fromDate and toDate are required");
        }catch (Exception e) {
        	throw new Exception("timeSeriesName, fromDate and toDate are required");
		}
        
        // default string
        String APIRequest = Constants.ENDPOINT_REPOSITORY_URL + "?timeSeriesName="+
        					timeSeriesName+"&fromDate="+fromDate+"&toDate="+toDate;
        
        if(includeTime != null && !includeTime.isEmpty())
        	APIRequest+="&includeTime="+includeTime;
        
        if(sort != null && !sort.isEmpty())
        	APIRequest+="&sort="+sort;        
        
        
        try {
        	String jsonString = KaaConnector.connect(APIRequest);

            List<KaaEndpoint> res = getKaaEndpointsFromJSON(jsonString, samplePeriod);
            
            // Append results here
            kaaEndpointsFinal.addAll(res);
                
        }catch (IOException | JSONException  e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        return kaaEndpointsFinal;
    
    }
    
    /**
	 * Function that returns, for every endpoint, the data obtained from Kaa EPTS API without values. Used to get the dataNamesof every endpoint
	 * @param timeSeriesName dataNames that you want to retrieve for every end(ex. "temperature,humidity")
	 * @return List of KaaEndpoint without any values inside
	 * @throws Exception If something goes wrong
	 */
    public static List<KaaEndpoint> getKaaEndpointsData(String timeSeriesName) throws Exception{
		
    	// List to be returned
        List<KaaEndpoint> kaaEndpointsFinal = new ArrayList<>();
        
        // Try-catch to avoid "nullpointerexception"
        try {
        	if(timeSeriesName.isEmpty())
            	throw new Exception("timeSeriesName is required");
        }catch (Exception e) {
        	throw new Exception("timeSeriesName is required");
		}
        
        // From 1970
    	Long fDate = 0L;
    	String fromDate = Constants.KAA_EPTS_API_DATE_FORMAT.format(new Date(fDate));
    	// To now
    	Long tDate = new Date(System.currentTimeMillis()).getTime();
        String toDate = Constants.KAA_EPTS_API_DATE_FORMAT.format(new Date(tDate));
        
        // default string
        String APIRequest = Constants.ENDPOINT_REPOSITORY_URL + "?timeSeriesName="+
        					timeSeriesName+"&fromDate="+fromDate+"&toDate="+toDate;
        
        try {

        	String jsonString = KaaConnector.connect(APIRequest);

            List<KaaEndpoint> res = getKaaEndpointsConfigurationFromJSON(jsonString);
            
            // Append results here
            kaaEndpointsFinal.addAll(res);
                
        }catch (IOException | JSONException  e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        return kaaEndpointsFinal;	
    }    
    
    /**
	 * Function that returns the data obtained from Kaa EPTS API based on the parameters specified below
	 * @param kaaEndpointConfigurations List of KaaEndpointConfiguration that tells the API which data retrieve for which endpoint
	 * @param fromDate       "Date" from which to retrieve the data
	 * @param toDate         "Date" up to which to recover the data
	 * @param includeTime    defines whether fromDate and toDate are inclusive [from, to, both, none]
	 * @param sort           data sorting [ASC, DESC]
	 * @param samplePeriod   The data sampling period
	 * @return List of KaaEndpoint
	 * @throws Exception If something goes wrong while retrieving the data
	 */
    public static List<KaaEndpoint> getKaaEndpointsData(List<KaaEndpointConfiguration> kaaEndpointConfigurations,
    		String fromDate, String toDate, String includeTime, String sort, long samplePeriod)
    				throws Exception{    	
        // List to be returned
        List<KaaEndpoint> kaaEndpointsFinal = new ArrayList<>();
        
        // Now, for every endpoint, i am going to retrieve the data the user is interested in
        for(int i=0;i<kaaEndpointConfigurations.size();i++) {
        	
        	KaaEndpointConfiguration endpointConfig = kaaEndpointConfigurations.get(i);
        	
        	// default string
            String APIRequest = Constants.ENDPOINT_REPOSITORY_URL+"?timeSeriesName="+String.join(",", endpointConfig.getDataNames())
            					+"&endpointId="+endpointConfig.getEndpointId()+"&fromDate="
            					+fromDate+"&toDate="+toDate;
            
            if(includeTime != null && !includeTime.isEmpty())
            	APIRequest+="&includeTime="+includeTime;
            
            if(sort != null && !sort.isEmpty())
            	APIRequest+="&sort="+sort;        
            
            
            try {
                
            	String jsonString = KaaConnector.connect(APIRequest);
            	
                List<KaaEndpoint> res = getKaaEndpointsFromJSON(jsonString, samplePeriod);
                
                // Append results here
                kaaEndpointsFinal.addAll(res);

            }catch (IOException | JSONException  e) {
                e.printStackTrace();
                throw new Exception(e.getMessage());
            }
        	
        }

        return kaaEndpointsFinal;
    
    }
    
    /**
	 * Function that returns, for the specified endpoints, the data obtained from Kaa EPTS API without values. Used to get the dataNames of selected endpoint
	 * @param kaaEndpointConfigurations List of KaaEndpointConfiguration that tells the API which data retrieve for which endpoint
	 * @return List of KaaEndpoint without any values inside
	 * @throws Exception If something goes wrong while retrieving the data
	 */
    public static List<KaaEndpoint> getKaaEndpointsData(List<KaaEndpointConfiguration> kaaEndpointConfigurations) throws Exception{
		
    	// From 1970
    	Long fDate = 0L;
    	String fromDate = Constants.KAA_EPTS_API_DATE_FORMAT.format(new Date(fDate));
    	// To now
    	Long tDate = new Date(System.currentTimeMillis()).getTime();
        String toDate = Constants.KAA_EPTS_API_DATE_FORMAT.format(new Date(tDate));
    	
    	// List to be returned
        List<KaaEndpoint> kaaEndpointsFinal = new ArrayList<>();
        
        // Now, for every endpoint, i am going to retrieve the data the user is interested in
        for(int i=0;i<kaaEndpointConfigurations.size();i++) {
        	
        	KaaEndpointConfiguration endpointConfig = kaaEndpointConfigurations.get(i);
        	
        	// default string
            String APIRequest = Constants.ENDPOINT_REPOSITORY_URL+"?timeSeriesName="+String.join(",", endpointConfig.getDataNames())
            					+"&endpointId="+endpointConfig.getEndpointId()+"&fromDate="
            					+fromDate+"&toDate="+toDate;
            
            try {
        		String jsonString = KaaConnector.connect(APIRequest); 
                
                List<KaaEndpoint> res = getKaaEndpointsConfigurationFromJSON(jsonString);
                
                // Append results here
                kaaEndpointsFinal.addAll(res);
            }catch (IOException | JSONException  e) {
                e.printStackTrace();
                throw new Exception(e.getMessage());
            }
        	
        }
        
    	return kaaEndpointsFinal;    	
    	
    }
}
