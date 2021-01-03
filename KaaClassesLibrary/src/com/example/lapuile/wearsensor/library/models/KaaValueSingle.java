package com.example.lapuile.wearsensor.library.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.example.lapuile.wearsensor.library.models.interfaces.KaaValue;
import com.example.lapuile.wearsensor.library.utils.Constants;

/**
 * Class to represent a Value that contains only one value (i.e. temperature, humidity, light intensity)
 */
public class KaaValueSingle implements KaaValue{

	private Date timestamp;
    private Object value;
    
    /**
	 * Void constructor
	 */
    public KaaValueSingle() {
        this.timestamp = new Date();
        this.value = null;
    }
    
    /**
     * Constructor to use whether you know the moment which the sensor has been queried and the value of the sensor
     * @param timestamp The Date representing the moment which the sensor has been queried
     * @param value the value of the sensor
     */
    public KaaValueSingle(Date timestamp, Object value) {
        this.timestamp = timestamp;
        this.value = value;
    }
    
    /**
     * Function used from the constructor to parse a JSONObject in the object representing the value
     * @param value JSONObject to be converted
     */
    private void assignValuesFromJson(JSONObject value) {
    	// it has to be different between toJson and toKaaJson
    	
    	// try to get the values, if it throws an exception it means that comes from toJson    	
    	try{
    		JSONObject values; values = value.getJSONObject("values");
    		this.value = values.get("value");
    	}catch (Exception e) {
    		this.value = value.get("value");
		}
    }
    
    /**
     * Constructor to create an instance from a JSON (reverse operation than toJson or toKaaJson)
     * @param JSON JSON to be converted
     * @throws ParseException If is not possible to convert the JSON in an instance of this class
     */
    public KaaValueSingle(String JSON) throws ParseException {
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
    	this.assignValuesFromJson(obj);    	
    }
    
    /**
     * Constructor to create an instance from given timestamp and jsonValue
     * Made up for be called from the WearSensorAPI
     * @param timestamp Timestamp of the value
     * @param jsonValue JSON to represent the actual value
     */
    public KaaValueSingle(Date timestamp, String jsonValue){
    	this.timestamp = timestamp;
    	this.assignValuesFromJson(new JSONObject(jsonValue));
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
     * Function to get the value of the sensor
     * @return the value of the sensor
     */
    public Object getValue() {
        return value;
    }

    /**
     * Function to set the value of the sensor
     * @param value the values of the sensor
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
	 * Function to get a short description of the instance
	 * @return a short description of the instance
	 */
    @Override
    public String toString() {
        return "KaaValue{" +
                "timestamp=" + timestamp +
                ", value=" + value +
                '}';
    }

    /**
	 * KaaValue formatted in a JSON
	 * @return the KaaValue instance formatted in a JSON
	 */
    public String toJson(){
        String jsonString = "{";
        jsonString += "\"timestamp\":"+timestamp.getTime()+",";
        jsonString += "\"value\":"+value;
        return jsonString + "}";
    }
        
    /**
     * KaaValue formatted in a Kaa-accepted JSON
     * @param valueName the name of the value to be embedded
     * @return the KaaValue formatted in a Kaa-accepted JSON
     */
    public String toKaaJson(String valueName){
    	String Json = "{\"timestamp\": ";
    	Json += timestamp.getTime();
    	Json += ",\""+valueName.replace(" ","")+"\":" + value + "}";
    	return Json;
    }

    /**
	 * KaaValue formatted in a XML
	 * @return the KaaValue instance formatted in a XML
	 */
    public String toXML(){
        String xml = "<data>";
        xml += "<timestamp>"+timestamp.getTime()+"</timestamp>";
        xml += "<value>"+value+"</value>";
        return xml + "</data>";
    }

    /**
	 * KaaValue formatted in a CSV
	 * @return the KaaValue instance formatted in a CSV
	 */
    public String toCSV(){
        return timestamp.getTime() + "," + value;
    }    
    
}
