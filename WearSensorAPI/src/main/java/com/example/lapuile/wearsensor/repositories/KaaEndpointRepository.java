package com.example.lapuile.wearsensor.repositories;

import com.example.lapuile.wearsensor.library.models.KaaValue;
import com.example.lapuile.wearsensor.library.models.KaaEndpoint;
import com.example.lapuile.wearsensor.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class KaaEndpointRepository {
	private static KaaEndpointRepository instance;

    private KaaEndpointRepository() {}

    public static synchronized KaaEndpointRepository getInstance(){
        if (instance==null)
            instance = new KaaEndpointRepository();
        return instance;
    }

    public List<KaaEndpoint> getKaaEndpointsData(String timeSeriesName, String endpointId,
    		String fromDate, String toDate, String includeTime, String sort, long samplePeriod)
    				throws Exception{    	
        // List to be returned
        List<KaaEndpoint> kaaEndpointsFinal = new ArrayList<>();
        
        //Value to keep track of the sample period
        long lastSampled = 0 - samplePeriod;
        
        // Try-catch to avoid "nullpointerexception"
        try {
        	if(timeSeriesName.isEmpty() || fromDate.isEmpty() || toDate.isEmpty())
            	throw new Exception("timeSeriesName, fromDate and toDate are required");
        }catch (Exception e) {
        	throw new Exception("timeSeriesName, fromDate and toDate are required");
		}
        
        // default string
        String APIRequest = Constants.KAA_EPTS_API_BASE_URL +"epts/api/v1/applications"+
        					"/btngtro547tsntf25rtg/"+"time-series/data?timeSeriesName="+
        					timeSeriesName+"&fromDate="+fromDate+"&toDate="+toDate;
        
        // let's add optional parameter
        if(endpointId != null && !endpointId.isEmpty())
        	APIRequest+="&endpointId="+endpointId;
        
        if(includeTime != null && !includeTime.isEmpty())
        	APIRequest+="&includeTime="+includeTime;
        
        if(sort != null && !sort.isEmpty())
        	APIRequest+="&sort="+sort;        
        
        // Create URL
        URL kaaApiUrl = null;
        try {
            kaaApiUrl = new URL(APIRequest);
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

                                    Object actualValue = ((JSONObject) kaaValueJson.get("values")).get("value");
                                    Date timestamp;
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    try {
                                    	timestamp = sdf.parse(kaaValueJson.get("timestamp").toString());
                                    }catch(Exception e) {
                                    	String dateString = kaaValueJson.get("timestamp").toString();
                                    	timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                                    			.parse(dateString);
                                    }                          
                                    
                                    // I want to sample the first one
                                    if(x == 0)
                                    	lastSampled = timestamp.getTime() - samplePeriod;

                                    // Check the sampling period
                                    if (timestamp.getTime() - lastSampled >= samplePeriod) {
                                    	lastSampled += samplePeriod;
	                                    KaaValue kaaValue = new KaaValue(timestamp, actualValue);
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
                    kaaEndpointsFinal.add(tmpEndpoints.get(key));

            } else {
                // Error handling code goes here
            }
        }catch (IOException | JSONException  e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        return kaaEndpointsFinal;
    
    }
}
