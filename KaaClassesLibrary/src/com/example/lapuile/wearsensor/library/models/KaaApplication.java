package com.example.lapuile.wearsensor.library.models;

import java.util.List;

/**
 * Class to represent a KaaApplication
 */
public class KaaApplication {

	private String applicationName; // example: btngtro547tsntf25rtg
	private List<KaaEndpointConfiguration> endpoints;
	
	public KaaApplication(String applicationName, List<KaaEndpointConfiguration> endpoints) {
		super();
		this.applicationName = applicationName;
		this.endpoints = endpoints;
	}
	
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public List<KaaEndpointConfiguration> getEndpoints() {
		return endpoints;
	}
	public void setEndpoints(List<KaaEndpointConfiguration> endpoints) {
		this.endpoints = endpoints;
	}
	
	@Override
	public String toString() {
		return "KaaApplication [applicationName=" + applicationName + ", endpoints=" + endpoints + "]";
	}
	
	/*
	 * KaaApplication formatted in JSON
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
	
	/*
	 * KaaApplication formatted in XML
	 */
	public String toXML(){
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        xml += "<application applicationName=\""+applicationName+"\">";
        for(int i=0;i<endpoints.size();i++)		
			xml+=endpoints.get(i).toXML();
        return xml + "</application>";
    }

	/*
	 * KaaApplication formatted in CSV
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
