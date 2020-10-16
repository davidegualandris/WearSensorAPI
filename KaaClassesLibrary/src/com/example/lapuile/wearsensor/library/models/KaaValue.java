package com.example.lapuile.wearsensor.library.models;

import java.util.Date;

public class KaaValue {

	private Date timestamp;
    private Object value;

    public KaaValue(Date timestamp, Object value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "KaaValue{" +
                "timestamp=" + timestamp +
                ", value=" + value +
                '}';
    }

    public String toJson(){
        String jsonString = "{";
        jsonString += "\"timestamp\":"+this.getTimestamp().getTime()+",";
        jsonString += "\"value\":"+this.getValue();
        return jsonString + "}";
    }

    public String toXML(){
        String xml = "<value>";
        xml += "<timestamp>"+this.getTimestamp().getTime()+"</timestamp>";
        xml += "<value>"+this.getValue()+"</value>";
        return xml + "</value>";
    }

    public String toCSV(){
        return this.getTimestamp().getTime() + "," + this.getValue();
    }
    
}
