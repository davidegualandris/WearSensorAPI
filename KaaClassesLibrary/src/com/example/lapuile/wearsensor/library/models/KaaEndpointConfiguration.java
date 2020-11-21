package com.example.lapuile.wearsensor.library.models;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 * Class used to represent a configuration of a given Endpoint
 * Don't get confused with the class KaaEndpoint
 */
public class KaaEndpointConfiguration {

	private String endpointId; // example: a3b3dfde-4b95-4bdd-9746-5b994a65c0ce
	private List<String> dataNames; // example: {"auto~humidity", "auto~temperature"}

	public KaaEndpointConfiguration(){
		super();
		this.endpointId = "";
		this.dataNames = new ArrayList<String>();
	}
	
	public KaaEndpointConfiguration(String JSON){
		super();
		JSONObject json = new JSONObject(JSON);		
		this.endpointId = json.getString("endpointId");		
		JSONArray dataNames = json.getJSONArray("dataNames");
		for(int i=0;i<dataNames.length();i++){
		    this.dataNames.add(dataNames.getString(i));
		}		
	}
	
	public KaaEndpointConfiguration(String endpointId, List<String> dataNames) {
		super();
		this.endpointId = endpointId;
		this.dataNames = dataNames;
	}

	public String getendpointId() {
		return endpointId;
	}
	public void setendpointId(String endpointId) {
		this.endpointId = endpointId;
	}
	public List<String> getDataNames() {
		return dataNames;
	}
	public void setDataNames(List<String> dataNames) {
		this.dataNames = dataNames;
	}

	@Override
	public String toString() {
		return "KaaApplication [endpointId=" + endpointId + ", dataNames=" + dataNames + "]";
	}

	/*
	 * KaaEndpointConfiguration formatted in JSON
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

	/*
	 * KaaEndpointConfiguration formatted in XML
	 */
	public String toXML(){
		String xml = "<endpoint id=\""+this.endpointId+"\">";
		for(int i=0;i<dataNames.size();i++)		
			xml+="<dataName>"+dataNames.get(i)+"</dataName>";
		return xml + "</application>";
	}

	/*
	 * KaaEndpointConfiguration formatted in CSV
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
