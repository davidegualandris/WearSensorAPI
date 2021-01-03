package com.example.lapuile.wearsensor.handlers;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.example.lapuile.wearsensor.library.formatters.KaaEndpointsConfigurationsFormatter;
import com.example.lapuile.wearsensor.library.formatters.KaaEndpointsValuesFormatter;
import com.example.lapuile.wearsensor.library.models.KaaApplication;
import com.example.lapuile.wearsensor.library.models.KaaEndpoint;
import com.example.lapuile.wearsensor.library.models.KaaEndpointConfiguration;
import com.example.lapuile.wearsensor.library.models.interfaces.KaaValue;
import com.example.lapuile.wearsensor.repositories.KaaApplicationRepository;
import com.example.lapuile.wearsensor.repositories.KaaEndpointRepository;
import com.example.lapuile.wearsensor.senders.KaaEndpointSenders;
import com.example.lapuile.wearsensor.utils.Constants;

/**
 * Class that allows you to directly respond to calls to the API defined in this project
 * /kaa/scan to get the "dataNames" for every endpoint in a tree structure
 * /kaa/sensors/{endpointIds} to get the "dataNames" of the specified endpoints
 * /kaa/sensor/{endpointId}{dataName} to check the availability of the sensor "dataName" in the specified endpoint 
 * /kaa/play to get all the data of the last 24 hours in JSON format
 * /kaa/play/{params} to get data with custom parameters
 * /kaa/store/{data} to store the specified data in the platform
 */
@Path("/kaa")
public class HandleAPICalls{
	
	/**
	 * Function to "retrieve" the names of the values that the Kaa application is receiving
	 * @return JSON mappable with the KaaApplication model
	 */
	@GET
	@Path("scan")
	@Produces(MediaType.APPLICATION_JSON)
	public String scan() {
		KaaApplication kaaApplication;
		try {
			kaaApplication = KaaApplicationRepository.getKaaApplicationDataNames(null);
		} catch (Exception e) {
			return "{\"message\": " + e.getMessage() + "}";
		}
		return kaaApplication.toJSON();
	}
	
	/**
	 * Function to "retrieve" the names of the values that the Kaa application is receiving
	 * @param endpointId EndpointIDs whose data you want to retrieve (endpointIds separated with ,)
	 * @return JSON mappable with the KaaApplication model
	 */
	@GET
	@Path("sensors")
	@Produces(MediaType.APPLICATION_JSON)
	public String sensors(@QueryParam("endpointId") String endpointId) {
		KaaApplication kaaApplication;
		try {
			kaaApplication = KaaApplicationRepository.getKaaApplicationDataNames(endpointId);
		} catch (Exception e) {
			return "{\"message\": " + e.getMessage() + "}";
		}
		return kaaApplication.toJSON();
	}
	
	/**
	 * Function to check the availability of the sensor "dataName" in the specified endpoint. If more than one endpoint is specified, then it checks only the first one
	 * @param endpointId EndpointID whose data you want to retrieve
	 * @param dataName sensor name to check for availability within the endpointId
	 * @return HTTP response. OK/200 if the sensor exists within the endpoint. 404 otherwise.
	 */
	@GET
	@Path("sensor")
	public Response sensor(@QueryParam("endpointId") String endpointId,
												@QueryParam("dataName") String dataName) {
		
		if(endpointId == null || endpointId.equals("") || dataName == null || dataName.equals(""))
			return Response.status(Status.NOT_FOUND).build();
					
		try {
			return KaaApplicationRepository.checkAvailability(endpointId,dataName);
		}catch(Exception e) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
	}

	/**
	 * Function that returns the data obtained from Kaa filtered on the parameters specified below
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
			kaaEndpoints = KaaEndpointRepository.getKaaEndpointsData(kaaEndpointConfigurations,
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
			kaaApplication = KaaApplicationRepository.getKaaApplicationDataNames(null); 
		} catch (Exception e) {
			throw new Exception("{\"message\": " + e.getMessage() + "}");
		}
		return kaaApplication.getEndpoints();
	}

	/**
	 * Function to get all the data of the last 24 hours in JSON format
	 * @return All data of all endpoints from the last 24 hours in JSON format
	 * @throws Exception If is not possible to retrieve the data from Kaa
	 */	
	@GET	  
	@Path("play")	  
	@Produces(MediaType.APPLICATION_JSON)
	public String playAll() throws Exception {	  
		List<KaaEndpointConfiguration> config;
		try {
			config = this.getDefaultApplicationConfiguration();
		} catch (Exception e) {
			throw new Exception(e);
		}

		// by default i am returning the last 24 hours data
		Date toDate = new Date(System.currentTimeMillis());
		Date fromDate = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000));

		return this.getFormattedDataFromKaa(config, fromDate, toDate, Constants.DEFAULT_INCLUDE_TIME, Constants.DEFAULT_SORT, Constants.DEFAULT_OUTPUT_FORMAT, Constants.DEFAULT_SAMPLE_PERIOD); 
	}
	
	/**
	 * Function to get data from Kaa with custom parameters
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
	@Path("play")		  
	@Produces(MediaType.TEXT_PLAIN)
	public String play(@QueryParam("kaaEndpointConfigurations") String kaaEndpointConfigurations,
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
		} catch (Exception e1) {
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
	 * Function to store the specified data in the platform 
	 * @param data JSON obtained from the function KaaEndpoint.toJson()
	 * @return HTTP response representing the result of the operation (if operation completed successfully OK / 200)
	 */
	@POST
	@Path("store")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response store(String data){
		if(data == null || data.isEmpty())
			return Response.status(Status.BAD_REQUEST).build();

		Response resp = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		try {
			KaaEndpointSenders k = KaaEndpointSenders.getInstance();
			resp = k.sendValueToKaa(new KaaEndpoint(data));
		} catch (Exception e) {
			Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		return resp;
	}
}
