package com.example.lapuile.wearsensor.library.models.interfaces;

import java.text.ParseException;
import java.util.Date;

/**
 * Interface written to let the application handle the 2 different types of KaaValue: Single and Multi.
 */
public interface KaaValue {
	public Date getTimestamp();
	public Object getValue();
	public String toJson();
	public String toKaaJson(String valueName) throws ParseException;
	public String toXML();
	public String toCSV();
}
