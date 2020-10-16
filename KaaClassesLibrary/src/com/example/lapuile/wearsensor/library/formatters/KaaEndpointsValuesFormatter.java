package com.example.lapuile.wearsensor.library.formatters;

import java.util.List;

import com.example.lapuile.wearsensor.library.models.KaaEndpoint;

/**
 * Classe per formattare nel formato desiderato una lista di KaaEndpoint 
 */
public class KaaEndpointsValuesFormatter {

	/**
	 * Formattazione in JSON
	 * @param kaaEndpoints List dei KaaEndpoint
	 * @return Lista dei KaaEndpoint formattata in JSON
	 */
    public static String KaaEndpointsValuesToJson(List<KaaEndpoint> kaaEndpoints){
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
	 * Formattazione in XML
	 * @param kaaEndpoints List dei KaaEndpoint
	 * @return Lista dei KaaEndpoint formattata in XML
	 */
    public static String KaaEndpointsValuesToXML(List<KaaEndpoint> kaaEndpoints){
        String formatted = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><endpoints>";
        for (int i = 0; i < kaaEndpoints.size(); i++) {
            formatted +=  kaaEndpoints.get(i).toXML();
        }
        formatted += "</endpoints>";
        return formatted;
    }

    /**
	 * Formattazione in CSV
	 * @param kaaEndpoints List dei KaaEndpoint
	 * @return Lista dei KaaEndpoint formattata in CSV
	 */
    public static String KaaEndpointsValuesToCSV(List<KaaEndpoint> kaaEndpoints){
        String formatted = "endpointID, dataName, timestamp, value";
        for (int i = 0; i < kaaEndpoints.size(); i++) {
            formatted += kaaEndpoints.get(i).toCSV();
        }
        return formatted;
    }
}
