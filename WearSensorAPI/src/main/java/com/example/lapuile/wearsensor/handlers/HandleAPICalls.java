package com.example.lapuile.wearsensor.handlers;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.example.lapuile.wearsensor.library.formatters.KaaEndpointsConfigurationsFormatter;
import com.example.lapuile.wearsensor.library.utils.Constants;
import com.example.lapuile.wearsensor.library.formatters.KaaEndpointsValuesFormatter;
import com.example.lapuile.wearsensor.library.models.KaaApplication;
import com.example.lapuile.wearsensor.library.models.KaaEndpoint;
import com.example.lapuile.wearsensor.library.models.KaaEndpointConfiguration;
import com.example.lapuile.wearsensor.repositories.KaaApplicationRepository;
import com.example.lapuile.wearsensor.repositories.KaaEndpointRepository;
import com.example.lapuile.wearsensor.senders.KaaEndpointSenders;

/**
 * Class that allows you to directly respond to calls to the API defined in this project
 * /time-series/config/{endpointId} to get the "dataNames" of the specified endpoints
 * /time-series/last to get all the data of the last 24 hours in JSON format
 * /time-series/data/{params} to get data with custom parameters
 */
@Path("/time-series")
public class HandleAPICalls {

	/**
	 * Support function to "retrieve" the names of the values that the Kaa application is receiving
	 * @param endpointID EndpointIDs whose data you want to retrieve
	 * @return JSON mappable with the KaaApplication model
	 */
	@GET
	@Path("config")
	@Produces(MediaType.APPLICATION_JSON)
	public String getApplicationConfiguration(@QueryParam("endpointId") String endpointId) {
		KaaApplication kaaApplication;
		try {
			kaaApplication = KaaApplicationRepository.getInstance().getKaaApplicationDataNames(endpointId);
		} catch (Exception e) {
			return "{\"message\": " + e.getMessage() + "}";
		}
		return kaaApplication.toJSON();
	}

	/**
	 * Function that returns the data obtained from Kaa based on the parameters specified below
	 * @param kaaEndpointConfigurations List of KaaEndpointConfiguration that tells the API which data retrieve for which endpoint
	 * @param fromDate       "Date" from which to retrieve the data
	 * @param toDate         "Date" up to which to recover the data
	 * @param includeTime    defines whether fromDate and toDate are inclusive [from, to, both, none]
	 * @param sort           data sorting [ASC, DESC]
	 * @param format         JSON, CSV, XML
	 * @param periodSample   The data sampling period
	 * @return output in the desired format
	 */
	private String getFormattedDataFromKaa(List<KaaEndpointConfiguration> kaaEndpointConfigurations, Date fromDate,
			Date toDate, String includeTime, String sort, String format, long periodSample) {

		List<KaaEndpoint> kaaEndpoints;
		try {
			kaaEndpoints = KaaEndpointRepository.getInstance().getKaaEndpointsData(kaaEndpointConfigurations,
							Constants.KAA_EPTS_API_DATE_FORMAT.format(fromDate), Constants.KAA_EPTS_API_DATE_FORMAT.format(toDate),
							includeTime, sort, periodSample);
		} catch (Exception e) {
			return "{\"message\": \"" + e.getMessage() + "\"}";
		}

		if(format != null) {
			if(format.equals("csv")) {
				return KaaEndpointsValuesFormatter.kaaEndpointsValuesToCSV(kaaEndpoints);
			}else if(format.equals("xml")){
				return KaaEndpointsValuesFormatter.kaaEndpointsValuesToXML(kaaEndpoints);
			}
		}

		return KaaEndpointsValuesFormatter.kaaEndpointsValuesToJson(kaaEndpoints);

	}

	/**
	 * Function to get all the data names that Kaa received
	 * @return ex "humidity,temperature"
	 * @throws Exception 
	 */	
	private List<KaaEndpointConfiguration> getDefaultApplicationConfiguration() throws Exception {
		KaaApplication kaaApplication;
		try {
			kaaApplication = KaaApplicationRepository.getInstance().getKaaApplicationDataNames(null); 
		} catch (Exception e) {
			throw new Exception("{\"message\": " + e.getMessage() + "}");
		}
		return kaaApplication.getEndpoints();
	}

	/**
	 * Function to handle the call to the address /WearSensorAPI/kaa/values
	 * @return All data of all endpoints from the last 24 hours in JSON format
	 * @throws Exception 
	 */	
	@GET	  
	@Path("last")	  
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllEndpointsData() throws Exception {	  
		List<KaaEndpointConfiguration> config;
		try {
			config = this.getDefaultApplicationConfiguration();
		} catch (Exception e) {
			throw new Exception(e);
		}

		// by default i am returning the last 24 hours data
		Date toDate = new Date(System.currentTimeMillis());
		Date fromDate = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000));

		return this.getFormattedDataFromKaa(config, fromDate, toDate, "both", "ASC", "JSON", 1000); 
	}
	
	/**
	 * Function to handle the call to the address /WearSensorAPI/kaa/data?{params}
	 * @param kaaEndpointConfigurations encoded JSON (KaaEndpointsConfigurationsFormatter.KaaEndpointConfigurationsToJSON) that tells the API which data retrieve for which endpoint
	 * @param fromDate       "Date" from which to retrieve the data
	 * @param toDate         "Date" up to which to recover the data
	 * @param includeTime    defines whether fromDate and toDate are inclusive [from, to, both, none]
	 * @param sort           data sorting [ASC, DESC]
	 * @param format         JSON, CSV, XML
	 * @param periodSample   The data sampling period
	 * @return output in the desired format
	 */
	@GET		  
	@Path("data")		  
	@Produces(MediaType.TEXT_PLAIN)
	public String getEndpointData(@QueryParam("kaaEndpointConfigurations") String kaaEndpointConfigurations,
			@QueryParam("fromDate") long fromDate, @QueryParam("toDate") long toDate, @QueryParam("includeTime") String includeTime,
			@QueryParam("sort") String sort, @QueryParam("format") String format, @QueryParam("periodSample") long periodSample) {

		List<KaaEndpointConfiguration> config;
		
		// by default i am returning all the possible values
		if(kaaEndpointConfigurations == null || kaaEndpointConfigurations.isEmpty()) {
			try {
				config = this.getDefaultApplicationConfiguration();
			} catch (Exception e1) {
				return e1.toString();
			}
		}
		
		// encode the kaaEndpointConfigurations
		try {
			config = KaaEndpointsConfigurationsFormatter.JSONtoKaaEndpointConfigurations(kaaEndpointConfigurations);
		} catch (UnsupportedEncodingException e1) {
			return "{\"message\": " + e1.getMessage() + "}";
		}
		
		// by default i am returning the last 24 hours data
		Date fDate;
		try { fDate = new Date(fromDate); }catch(Exception e){ fDate = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000)); }

		Date tDate;
		try { tDate = new Date(toDate); }catch(Exception e){ tDate = new Date(System.currentTimeMillis()); }

		if(periodSample < 1000) periodSample = 1000;

		return this.getFormattedDataFromKaa(config, fDate, tDate, includeTime, sort, format, periodSample);
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 * @throws MqttException 
	 * @throws ParseException 
	 * @throws URISyntaxException
	 */
	@POST
	@Path("data")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveKaaEndpointData(String data) throws MqttException, ParseException{
		if(data == null || data.isEmpty())
			return Response.status(Status.BAD_REQUEST).build();
		
		KaaEndpointSenders k = KaaEndpointSenders.getInstance();
		Response resp = null;
		try {
			resp = k.sendValueToKaa(new KaaEndpoint(data));
		}catch(Exception e) {
			Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return resp;
	}
}
