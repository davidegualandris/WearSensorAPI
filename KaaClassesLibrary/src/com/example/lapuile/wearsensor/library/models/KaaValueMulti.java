 package com.example.lapuile.wearsensor.library.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.example.lapuile.wearsensor.library.models.interfaces.KaaValue;
import com.example.lapuile.wearsensor.library.utils.Constants;

/**
 * Class to represent a Value that contains more then one value (i.e. coordinates)
 */
public class KaaValueMulti implements KaaValue{

	private Date timestamp;
    private Map<String, Object> values;

    public KaaValueMulti(Date timestamp, Map<String, Object> values) {
		this.timestamp = timestamp;
		this.values = values;
	}
    
    /**
     * Function used from the constructor to parse a JSONObject in the Map of values
     * @param values JSONObject to be converted in Map
     */
    private void assignValuesFromJson(JSONObject values) {
    	// i am sure that inside "values" there is a list of value
    	Map<String, Object> valuesMap = new HashMap<>();
    	Iterator<String> keys = values.keys();
    	while(keys.hasNext()) {
    	    String key = keys.next();
    	    valuesMap.put(key, values.get(key));
    	}
    }
    
    /**
     * Constructor to create an instance from a JSON
     * @param JSON JSON to be converted
     * @throws ParseException
     */
    public KaaValueMulti(String JSON) throws ParseException {
    	JSONObject obj = new JSONObject(JSON);
    	String dateString = obj.get("timestamp").toString();
    	//try to convert from long
    	try {
    		this.timestamp = new Date(Long.parseLong(dateString));
    	}catch(Exception e1) {
    		// if not working, try to convert from 
    		try {
	    		this.timestamp = Constants.KAA_EPTS_API_DATE_FORMAT.parse(dateString);
	        }catch(Exception e) {
	        	// if still not working, might be a not parseable date
	        	this.timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
	        			.parse(dateString);
	        }
    	}
    	this.assignValuesFromJson(obj.getJSONObject("values"));
    }
    
    /**
     * Constructor to create an instance from given timestamp and jsonValue
     * Made up for be called from the WearSensorAPI
     * @param timestamp Timestamp of the value
     * @param jsonValue JSON to represent the actual value
     */
    public KaaValueMulti(Date timestamp, String jsonValues){
    	this.timestamp = timestamp;
    	this.assignValuesFromJson(new JSONObject(jsonValues));
    }
    
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, Object> getValue() {
		return values;
	}
	
	public Set<String> getValueNames(){
		return values.keySet();
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}
	
	@Override
	public String toString() {
		return "KaaValueMulti [timestamp=" + timestamp + ", values=" + values + "]";
	}

	/*
	 * KaaValue formatted in a JSON
	 */
	public String toJson(){
        String jsonString = "{";
        jsonString += "\"timestamp\":"+timestamp.getTime()+",";
        jsonString += "\"values\":{";
        for(String key : values.keySet()) {
        	jsonString += "\"" + key + "\":";
        	jsonString += values.get(key) + ",";
        }
        //remove last comma
        if(jsonString.charAt(jsonString.length() - 1) == ',')
            jsonString = jsonString.substring(0, jsonString.length() - 1);
        return jsonString + "}}";
    }
	
	/**
     * KaaValue formatted in a Kaa-like JSON
     * @param valueName Name of the key that this instance is representing
     */
	public String toKaaJson(String valueName) {
    	/*String jsonString = "{";
        jsonString += "\"timestamp\":\""+Constants.KAA_EPTS_API_DATE_FORMAT.format(timestamp)+"\",";
        jsonString += "\"values\":{";
        for(String key : values.keySet()) {
        	jsonString += "\"" + key + "\":";
        	jsonString += values.get(key) + ",";
        }
        //remove last comma
        if(jsonString.charAt(jsonString.length() - 1) == ',')
            jsonString = jsonString.substring(0, jsonString.length() - 1);
        return jsonString + "}}";*/
		
		String jsonString = "{\"timestamp\": \"";
    	//Json += Constants.KAA_EPTS_API_DATE_FORMAT.format(timestamp);
		jsonString += timestamp.getTime();
    	//Json += "\",\"values\": {\"value\": " + value + "}}";
		jsonString += "\",\""+valueName+"\":[";
    	for(String key : values.keySet()) {
        	jsonString += "\"" + key + "\":";
        	jsonString += values.get(key) + ",";
    	}
    	//remove last comma
        if(jsonString.charAt(jsonString.length() - 1) == ',')
            jsonString = jsonString.substring(0, jsonString.length() - 1);
        jsonString += "]}";
    	return jsonString;
    }

	/*
	 * KaaValue formatted in a XML
	 */
    public String toXML(){
        String xml = "<data>";
        xml += "<timestamp>"+timestamp.getTime()+"</timestamp>";
        xml += "<values>";
        for(String key : values.keySet()) {
        	xml += "<value name=\"" + key + "\">";
        	xml += values.get(key);
        	xml += "</value>";
        }
        xml += "</value>";
        return xml + "</data>";
    }

    /*
	 * KaaValue formatted in a CSV
	 */
    public String toCSV(){
    	String csv = "";
    	csv += timestamp.getTime() + ",\"";
    	for(String key : values.keySet()) {
        	csv += key + ":";
        	csv += values.get(key) + ",";
        }
    	//remove last comma
        if(csv.substring(0, csv.length() - 1) == ",")
        	csv = csv.substring(0, csv.length() - 1);
        return csv+"\"";
    }
    
}
