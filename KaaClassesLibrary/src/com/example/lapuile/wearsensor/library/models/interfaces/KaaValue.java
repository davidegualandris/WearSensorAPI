package com.example.lapuile.wearsensor.library.models.interfaces;

import java.text.ParseException;
import java.util.Date;

/**
 * Interface written to let the application handle the 2 different types of KaaValue: Single and Multi.
 */
public interface KaaValue {
	/**
     * Function to get the moment which the sensor has been queried 
     * @return the moment which the sensor has been queried
     */
	public Date getTimestamp();
	/**
     * Function to get the values of the sensor
     * @return the value(s) of the sensor
     */
	public Object getValue();
	/**
	 * KaaValue formatted in a JSON
	 * @return the KaaValue instance formatted in a JSON
	 */
	public String toJson();
	/**
     * KaaValue formatted in a Kaa-accepted JSON
     * @param valueName the name of the value to be embedded
     * @return the KaaValue formatted in a Kaa-accepted JSON
     */
	public String toKaaJson(String valueName) throws ParseException;
	/**
	 * KaaValue formatted in a XML
	 * @return the KaaValue instance formatted in a XML
	 */
	public String toXML();
	/**
	 * KaaValue formatted in a CSV
	 * @return the KaaValue instance formatted in a CSV
	 */
	public String toCSV();
}
