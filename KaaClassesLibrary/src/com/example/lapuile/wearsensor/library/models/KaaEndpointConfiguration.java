package com.example.lapuile.wearsensor.library.models;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

//!!! Don't get confused with the class KaaEndpoint !!!
//The dataNames variable represents the configuration of the endpoint. It means the name of values collected from the endpoint {"auto~humidity", "auto~temperature"}

/**
 * Class used to represent a configuration of a given Endpoint
 */
public class KaaEndpointConfiguration {

	private String endpointId; // example: a3b3dfde-4b95-4bdd-9746-5b994a65c0ce
	private List<String> dataNames; // example: {"auto~humidity", "auto~temperature"}

	/**
	 * Void constructor
	 */
	public KaaEndpointConfiguration(){
		this.endpointId = "";
		this.dataNames = new ArrayList<String>();
	}
	
	/**
	 * Constructor to create an instance from a JSON (reverse operation than toJson)
	 * @param JSON to be converted in an intance of this class
	 */
	public KaaEndpointConfiguration(String JSON){
		this.dataNames = new ArrayList<String>();
		JSONObject json = new JSONObject(JSON);		
		this.endpointId = json.getString("endpointId");		
		JSONArray dataNames = json.getJSONArray("dataNames");
		for(int i=0;i<dataNames.length();i++){
		    this.dataNames.add(dataNames.getString(i));
		}		
	}
	
	/**
	 * Construct to use whether you know the endpointId and the configuration of the endpoint
	 * @param endpointId the endpointId
	 * @param dataNames the configuration of the endpoint
	 */
	public KaaEndpointConfiguration(String endpointId, List<String> dataNames) {
		this.endpointId = endpointId;
		this.dataNames = dataNames;
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
     * Function to get the configuration of the endpoint
     * @return the configuration of the endpoint
     */
	public List<String> getDataNames() {
		return dataNames;
	}
	
	/**
	 * Function to set the configuration of the endpoint
	 * @param dataNames the configuration of the endpoint
	 */
	public void setDataNames(List<String> dataNames) {
		this.dataNames = dataNames;
	}

	/**
	 * Function to get a short description of the instance
	 * @return a short description of the instance
	 */
	@Override
	public String toString() {
		return "KaaApplication [endpointId=" + endpointId + ", dataNames=" + dataNames + "]";
	}

    /**
	 * KaaEndpointConfiguration formatted in JSON
	 * @return the KaaEndpointConfiguration instance formatted in a JSON
	 */
	public String toJSON() {
		String json = "{\"endpointId\":\"" + endpointId +"\",\"dataNames\":[";
		for(int i=0;i<dataNames.size();i++)		
			json+="\""+dataNames.get(i)+"\",";
		//remove last comma
		if(json.charAt(json.length()-1) == ',')
			json = json.substring(0, json.length() - 1);
		return json += "]}";
	}

	/**
	 * KaaEndpointConfiguration formatted in XML
	 * @return the KaaEndpointConfiguration instance formatted in a XML
	 */
	public String toXML(){
		String xml = "<endpoint id=\""+this.endpointId+"\">";
		for(int i=0;i<dataNames.size();i++)		
			xml+="<dataName>"+dataNames.get(i)+"</dataName>";
		return xml + "</application>";
	}

	/**
	 * KaaEndpointConfiguration formatted in CSV
	 * @return the KaaEndpointConfiguration instance formatted in a CSV
	 */
	public String toCSV(){
		String csv = "";
		for(int i=0;i<dataNames.size();i++)		
			csv += "\n" + this.endpointId + "," + dataNames.get(i);
		//remove first \n
		if(csv.length()>0)
			csv = csv.substring(2);
		return csv;
	}

}
