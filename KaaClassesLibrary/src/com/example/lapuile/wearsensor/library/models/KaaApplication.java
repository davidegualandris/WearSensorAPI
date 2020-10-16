package com.example.lapuile.wearsensor.library.models;

import java.util.List;

public class KaaApplication {

	private String applicationName; // example: btngtro547tsntf25rtg
	private List<String> dataNames; // example: {"name": "auto~humidity", "values": ["value"]}
	
	public KaaApplication(String applicationName, List<String> dataNames) {
		super();
		this.applicationName = applicationName;
		this.dataNames = dataNames;
	}
	
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public List<String> getDataNames() {
		return dataNames;
	}
	public void setDataNames(List<String> dataNames) {
		this.dataNames = dataNames;
	}
	
	@Override
	public String toString() {
		return "KaaApplication [applicationName=" + applicationName + ", dataNames=" + dataNames + "]";
	}    
	
	public String toJSON() {
		String json = "{\"applicationName\":\"" + applicationName +"\",\"dataNames\":[";
		for(int i=0;i<dataNames.size();i++)		
			json+="\""+dataNames.get(i)+"\",";
		//remove last comma
		if(json.charAt(json.length()-1) == ',')
			json = json.substring(0, json.length() - 1);
		return json += "]}";
	}
	
	public String toXML(){
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        xml += "<application applicationName=\""+this.applicationName+"\">";
        for(int i=0;i<dataNames.size();i++)		
			xml+="<dataName>"+dataNames.get(i)+"</dataName>";
        return xml + "</application>";
    }

    public String toCSV(){
        String csv = "applicationName,dataName";
        for(int i=0;i<dataNames.size();i++)		
        	csv += "\n" + this.applicationName + "," + dataNames.get(i);
        return csv;
    }

}
