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
 * !!! Don't get confused with the class KaaEndpointConfiguration !!!
 * The values variable represents the actual values collected from the endpoint
 */
public class KaaEndpoint {

	private String endpointId;
    private Map<String, List<KaaValue>> values;
    
    /**
	 * Void constructor
	 */
    public KaaEndpoint() {
        this.endpointId = null;
        this.values = new HashMap<String, List<KaaValue>>();
    }
    
    /**
     * Construct to use whether you know the endpointId and the values collected from the endpoint
     * @param endpointId the endpointId
     * @param values the values collected from the endpoint
     */
    public KaaEndpoint(String endpointId, Map<String, List<KaaValue>> values) {
        this.endpointId = endpointId;
        this.values = values;
    }
    
    /**
     * Constructor to create an instance from a JSON (reverse operation than toJson)
     * @param JSON to be converted in an intance of this class
     * @throws Exception
     */
    public KaaEndpoint(String JSON) throws Exception {
    	JSONObject obj = new JSONObject(JSON);
    	Map<String, List<KaaValue>> valuesMap = new HashMap<>();
    	
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
    	}
    	/* TODO parse if it comes from toKaaJson
    	 * else{}
    	 */
    	this.values = valuesMap;
    }

    /**
     * Function to get the endpointId
     * @return the endpointId
     */
    public String getEndpointId() {
        return endpointId;
    }

    /**
     * Function to set the endpointId
     * @param endpointId the endpointId
     */
    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    /**
     * Function to get the values collected from the endpoint
     * @return the values collected from the endpoint
     */
    public Map<String, List<KaaValue>> getValues(){
        return values;
    }

    /**
     * Function to set the values collected from the endpoint
     * @param values the values collected from the endpoint
     */
    public void setValues(Map<String, List<KaaValue>> values) {
        this.values = values;
    }
    
    /**
	 * Function to get the value names of the endpoint sensors
	 * @return the key set of the value names of the endpoint sensors
	 */
    public List<String> getValuesDataNames(){
    	return new ArrayList<String>(values.keySet());
    }

    /**
	 * Function to get a short description of the instance
	 * @return a short description of the instance
	 */
    @Override
    public String toString() {
        return "KaaEndpoint{" +
                "EndpointID='" + endpointId + '\'' +
                ", values=" + values +
                '}';
    }

    /**
	 * KaaEndpoint formatted in JSON
	 * @return the KaaEndpoint instance formatted in a JSON
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
    
    /**
     * KaaEndpoint formatted in a Kaa-accepted JSON
     * @return the KaaEndpoint formatted in a Kaa-accepted JSON
     * @throws ParseException
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
    
    /**
     * Function to get a list of JSON representing the various values collected from the endpoint
     * @return A list of JSON representing the various values collected from the endpoint
     * @throws ParseException
     */
    public List<String> getValuesJsonList() throws ParseException{
    	List<String> values = new ArrayList<String>();
		String json = toKaaJson();
		//remove first [ and last ]
    	json = json.substring(1,json.length()-1);
    	//return new ArrayList<>(Arrays.asList(json.split(",")));
    	String pattern = "},{";
    	int index = json.indexOf(pattern);
    	int oldIndex = -2;
    	String value = null;
    	while (index >= 0) {
    		value = json.substring(oldIndex+2, index+1);
    		values.add(value);
    		oldIndex = index;
    	    index = json.indexOf(pattern, index + 1);
    	}
    	value = json.substring(oldIndex+2, json.length());    
    	values.add(value);
    	return values;
    }
    
    /**
	 * KaaEndpoint formatted in XML
	 * @return the KaaEndpoint instance formatted in a XML
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

    /**
	 * KaaEndpoint formatted in CSV
	 * @return the KaaEndpoint instance formatted in a CSV
	 */
    public String toCSV(){
        String csv = "endpointId,dataName,timestamp,value";
        for(String key : values.keySet())
            for(int i = 0; i < values.get(key).size(); i++)
                csv += "\n" + endpointId + "," + key + "," + values.get(key).get(i).toCSV();
        return csv;
    }
}
