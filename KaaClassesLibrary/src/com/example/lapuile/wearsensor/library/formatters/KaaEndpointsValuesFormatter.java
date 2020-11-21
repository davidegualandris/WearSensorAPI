package com.example.lapuile.wearsensor.library.formatters;

import java.util.List;

import com.example.lapuile.wearsensor.library.models.KaaEndpoint;

/**
 * Class to format a KaaEndpoint list in the desired format 
 */
public class KaaEndpointsValuesFormatter {

	/**
	 * Formatting in JSON
	 * @param kaaEndpoints KaaEndpoint list
	 * @return List of KaaEndpoints formatted in JSON
	 */
    public static String kaaEndpointsValuesToJson(List<KaaEndpoint> kaaEndpoints){
        String formatted = "{\"endpoints\":[";
        for (int i = 0; i < kaaEndpoints.size(); i++) {
            formatted += kaaEndpoints.get(i).toJson() + ",";
        }
        // remove "," and close the document
        if (formatted.length() > 14){
            formatted = formatted.substring(0, formatted.length() - 1);
        }
        formatted += "]}";
        return formatted;
    }

    /**
	 * Formatting in XML
	 * @param kaaEndpoints KaaEndpoint list
	 * @return List of KaaEndpoints formatted in XML
	 */
    public static String kaaEndpointsValuesToXML(List<KaaEndpoint> kaaEndpoints){
        String formatted = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><endpoints>";
        for (int i = 0; i < kaaEndpoints.size(); i++) {
            formatted +=  kaaEndpoints.get(i).toXML();
        }
        formatted += "</endpoints>";
        return formatted;
    }

    /**
	 * Formatting in CSV
	 * @param kaaEndpoints KaaEndpoint list
	 * @return List of KaaEndpoints formatted in CSV
	 */
    public static String kaaEndpointsValuesToCSV(List<KaaEndpoint> kaaEndpoints){
        String formatted = "endpointID, dataName, timestamp, values";
        for (int i = 0; i < kaaEndpoints.size(); i++) {
            formatted += kaaEndpoints.get(i).toCSV();
        }
        return formatted;
    }
}
