package com.example.lapuile.wearsensor.library.models;

import java.util.List;
import java.util.Map;

public class KaaEndpoint {

	private String endpointId;
    private Map<String, List<KaaValue>> values;

    public KaaEndpoint(String endpointId, Map<String, List<KaaValue>> values) {
        this.endpointId = endpointId;
        this.values = values;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public Map<String, List<KaaValue>> getValues() {
        return values;
    }

    public void setValues(Map<String, List<KaaValue>> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "KaaEndpoint{" +
                "EndpointID='" + endpointId + '\'' +
                ", values=" + values +
                '}';
    }

    public String toJson(){
        String jsonString = "{\"endpointId\":\"" + this.getEndpointId() + "\",\"values\":{";
        for(String key : getValues().keySet()){
            jsonString += "\""+key+"\":[";
            for(int i = 0; i < getValues().get(key).size(); i++){
                jsonString += getValues().get(key).get(i).toJson();
                if(i!=getValues().get(key).size()-1)
                    jsonString += ",";
            }
            jsonString += "],";
        }
        //remove last comma
        jsonString = jsonString.substring(0, jsonString.length() - 1);
        jsonString += "}}";
        return jsonString;
    }
    
    public String toXML(){
        String xml = "<endpoint endpointId=\""+this.getEndpointId()+"\">";
        for(String key : getValues().keySet()){
            xml += "<data name=\""+key+"\">";
            for(int i = 0; i < getValues().get(key).size(); i++)
                xml += getValues().get(key).get(i).toXML();
            xml += "</data>";
        }
        return xml + "</endpoint>";
    }

    public String toCSV(){
        String csv = "";
        for(String key : getValues().keySet())
            for(int i = 0; i < getValues().get(key).size(); i++)
                csv += "\n" + this.getEndpointId() + "," + key + "," + getValues().get(key).get(i).toCSV();
        return csv;
    }
}
