package com.example.lapuile.wearsensor.library.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent a KaaApplication
 */
public class KaaApplication {

	private String applicationName; // example: btngtro547tsntf25rtg
	private List<KaaEndpointConfiguration> endpoints;
	
	/**
	 * Void constructor
	 */
	public KaaApplication() {
		this.applicationName = null;
		this.endpoints = new ArrayList<KaaEndpointConfiguration>();
	}
	
	/**
	 * Constructor to use whether you already got the applicationName and the List of KaaEndpointConfiguration
	 * @param applicationName The application name
	 * @param endpoints the KaaEndpointConfiguration list
	 */
	public KaaApplication(String applicationName, List<KaaEndpointConfiguration> endpoints) {
		this.applicationName = applicationName;
		this.endpoints = endpoints;
	}
	
	/**
	 * Function to get the applicationName
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
	}
	
	/**
	 * Function to set the applicationName
	 * @param applicationName The application name
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	/**
	 * Function to get the KaaEndpointConfiguration list
	 * @return the KaaEndpointConfiguration list
	 */
	public List<KaaEndpointConfiguration> getEndpoints() {
		return endpoints;
	}
	
	/**
	 * Function to set the KaaEndpointConfiguration list
	 * @param endpoints the KaaEndpointConfiguration list
	 */
	public void setEndpoints(List<KaaEndpointConfiguration> endpoints) {
		this.endpoints = endpoints;
	}
	
	/**
	 * Function to get a short description of the instance
	 * @return a short description of the instance
	 */
	@Override
	public String toString() {
		return "KaaApplication [applicationName=" + applicationName + ", endpoints=" + endpoints + "]";
	}
	
	/**
	 * Function to get the KaaApplication formatted in JSON
	 * @return KaaApplication formatted in JSON
	 */
	public String toJSON() {
		String json = "{\"applicationName\":\""+applicationName+"\",\"endpoints\":[";
		for(int i=0;i<endpoints.size();i++)		
			json+=endpoints.get(i).toJSON()+",";
		//remove last comma
		if(json.charAt(json.length()-1) == ',')
			json = json.substring(0, json.length() - 1);
		return json += "]}";
	}
	
	/**
	 * Function to get the KaaApplication formatted in XML
	 * @return KaaApplication formatted in XML
	 */
	public String toXML(){
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        xml += "<application applicationName=\""+applicationName+"\">";
        for(int i=0;i<endpoints.size();i++)		
			xml+=endpoints.get(i).toXML();
        return xml + "</application>";
    }

	/**
	 * Function to get the KaaApplication formatted in CSV
	 * @return KaaApplication formatted in CSV
	 */
    public String toCSV(){
        String csv = "applicationName,endpointId,dataName";
        for(int i=0;i<endpoints.size();i++) {
        	String[] endpointCsvSplitted = endpoints.get(i).toCSV().split("\n");
        	for(int x=0;x<endpointCsvSplitted.length;x++)
        		csv += "\n" + applicationName + "," + endpointCsvSplitted[x];
        }
        return csv;
    }

}
