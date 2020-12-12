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

    /**
	 * Void constructor
	 */
    public KaaValueMulti() {
		this.timestamp = new Date();
		this.values = new HashMap<String, Object>();
	}
    
    /**
     * Constructor to use whether you know the moment which the sensor has been queried and the the values of the sensors
     * @param timestamp The Date representing the moment which the sensor has been queried
     * @param values the values of the sensors
     */
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
    	this.values = new HashMap<>();
    	Iterator<String> keys = values.keys();
    	while(keys.hasNext()) {
    	    String key = keys.next();
    	    this.values.put(key, values.get(key));
    	}
    }
    
    /**
     * Constructor to create an instance from a JSON (reverse operation than toJson or toKaaJson)
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
    
    /**
     * Function to get the moment which the sensor has been queried 
     * @return the moment which the sensor has been queried
     */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
     * Function to set the moment which the sensor has been queried
     * @param timestamp the moment which the sensor has been queried
     */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
     * Function to get the values of the sensor
     * @return the values of the sensor
     */
	public Map<String, Object> getValue() {
		return values;
	}
	
	/**
	 * Function to get the value names of the sensor
	 * @return the key set of the names of the values represented by the sensor
	 */
	public Set<String> getValueNames(){
		return values.keySet();
	}

	/**
     * Function to set the values of the sensor
     * @param values the values of the sensor
     */
	public void setValues(Map<String, Object> values) {
		this.values = values;
	}
	
	/**
	 * Function to get a short description of the instance
	 * @return a short description of the instance
	 */
	@Override
	public String toString() {
		return "KaaValueMulti [timestamp=" + timestamp + ", values=" + values + "]";
	}

	/**
	 * KaaValue formatted in a JSON
	 * @return the KaaValue instance formatted in a JSON
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
     * KaaValue formatted in a Kaa-accepted JSON
     * @return the KaaValue formatted in a Kaa-accepted JSON
     * @throws ParseException
     */
	public String toKaaJson(String valueName) {		
		String jsonString = "{\"timestamp\": ";
		jsonString += timestamp.getTime();
		jsonString += ",\""+valueName.replace(" ","")+"\":{";
    	for(String key : values.keySet()) {
        	jsonString += "\"" + key + "\":";
        	jsonString += values.get(key) + ",";
    	}
    	//remove last comma
        if(jsonString.charAt(jsonString.length() - 1) == ',')
            jsonString = jsonString.substring(0, jsonString.length() - 1);
        jsonString += "}}";
    	return jsonString;
    }

	/**
	 * KaaValue formatted in a XML
	 * @return the KaaValue instance formatted in a XML
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

    /**
	 * KaaValue formatted in a CSV
	 * @return the KaaValue instance formatted in a CSV
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
