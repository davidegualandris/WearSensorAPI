package com.example.lapuile.wearsensor.library.models;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.lapuile.wearsensor.library.models.interfaces.KaaValue;

/*
 * Class used to represent the values of a given Endpoint
 * Don't get confused with the class KaaEndpointConfiguration
 */
public class KaaEndpoint {

	private String endpointId;
    private Map<String, List<KaaValue>> values;
    
    public KaaEndpoint(String endpointId, Map<String, List<KaaValue>> values) {
        this.endpointId = endpointId;
        this.values = values;
    }
    
    /**
     * Constructor to create an instance from a JSON
     * @param JSON to be converted in an intance of this class
     * @throws Exception
     */
    public KaaEndpoint(String JSON) throws Exception {
    	JSONObject obj = new JSONObject(JSON);
    	Map<String, List<KaaValue>> valuesMap = new HashMap<>();
    	
    	// it has to consider both toKaaJson and toJson
    	if(obj.has("endpointId")) {
    		// it comes from toJson
    		this.endpointId = obj.getString("endpointId");
    		JSONObject valueJson = obj.getJSONObject("values");
        	Iterator<String> keys = valueJson.keys();
        	while(keys.hasNext()) {
        		String key = keys.next();
        		JSONArray jsonValues = valueJson.getJSONArray(key);
        		List<KaaValue> list = new ArrayList<>();
        		for (int i = 0; i < jsonValues.length(); i++) {
        			KaaValue kaaValue = null;
        			JSONObject val = jsonValues.getJSONObject(i);
        			if(val.has("values")) {
        				kaaValue = new KaaValueMulti(val.toString());
                    }else{
                    	kaaValue = new KaaValueSingle(val.toString());
                    }
        			list.add(kaaValue);
    			}
        		valuesMap.put(key, list);
        	}
    	}else {
    		// it comes from toKaaJson	
            /*// Get the keys -> just one in our case -> the endpointId
            Iterator<String> keys = obj.keys();
    		while(keys.hasNext()) {
                String endpointID = keys.next();
                this.endpointId = endpointID;
                if (obj.get(endpointID) instanceof JSONObject) {
                    JSONObject valueJson = (JSONObject) obj.get(endpointID);
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
                        	int jsonValuesSize = kaaValueJson.getJSONObject("values").keySet().size();
                        	KaaValue kaaValue = null;                  
                            if(jsonValuesSize == 0) {
                            	throw new Exception("Error while parsing the data");
                            }else if(jsonValuesSize == 1) {
                            	kaaValue = new KaaValueSingle(kaaValueJson.toString());
                            }else {
                            	kaaValue = new KaaValueMulti(kaaValueJson.toString());
                            }
                            kaaValues.add(kaaValue);
                        }
                        valuesMap.put(dataName, kaaValues);
                    }
                }
            }*/
    	}

    	this.values = valuesMap;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public Map<String, List<KaaValue>> getValues(){
        return values;
    }

    public void setValues(Map<String, List<KaaValue>> values) {
        this.values = values;
    }
    
    public List<String> getValuesDataNames(){
    	return new ArrayList<String>(values.keySet());
    }

    @Override
    public String toString() {
        return "KaaEndpoint{" +
                "EndpointID='" + endpointId + '\'' +
                ", values=" + values +
                '}';
    }

    /*
	 * KaaEndpoint formatted in JSON
	 */
    public String toJson(){
        String jsonString = "{\"endpointId\":\"" + endpointId + "\",\"values\":{";
        for(String key : values.keySet()){
            jsonString += "\""+key+"\":[";
            for(int i = 0; i < values.get(key).size(); i++){
                jsonString += values.get(key).get(i).toJson();
                if(i!=values.get(key).size()-1)
                    jsonString += ",";
            }
            jsonString += "],";
        }
        //remove last comma
        if(jsonString.charAt(jsonString.length() - 1) == ',')
        	jsonString = jsonString.substring(0, jsonString.length() - 1);
        jsonString += "}}";
        return jsonString;
    }
    
    /*
	 * KaaEndpoint formatted in a Kaa-like JSON
	 */
    public String toKaaJson() throws ParseException{
        //String jsonString = "{\"" + endpointId + "\":{";
    	String jsonString = "[";
        for(String key : values.keySet()){
            //jsonString += "\""+key+"\":[";
            for(int i = 0; i < values.get(key).size(); i++){
                jsonString += values.get(key).get(i).toKaaJson(key);
                if(i!=values.get(key).size()-1)
                    jsonString += ",";
            }
            //jsonString += "],";
        }
        /*//remove last comma
        if(jsonString.charAt(jsonString.length() - 1) == ',')
        	jsonString = jsonString.substring(0, jsonString.length() - 1);*/
        jsonString += "]";
        return jsonString;
    }
    
    /*
	 * KaaEndpoint formatted in XML
	 */
    public String toXML(){
        String xml = "<endpoint endpointId=\""+endpointId+"\">";
        for(String key : values.keySet()){
            xml += "<data name=\""+key+"\">";
            for(int i = 0; i < values.get(key).size(); i++)
                xml += values.get(key).get(i).toXML();
            xml += "</data>";
        }
        return xml + "</endpoint>";
    }

    /*
	 * KaaEndpoint formatted in CSV
	 */
    public String toCSV(){
        String csv = "endpointId,dataName,timestamp,value";
        for(String key : values.keySet())
            for(int i = 0; i < values.get(key).size(); i++)
                csv += "\n" + endpointId + "," + key + "," + values.get(key).get(i).toCSV();
        return csv;
    }
}
