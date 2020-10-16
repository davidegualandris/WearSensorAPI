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

}
