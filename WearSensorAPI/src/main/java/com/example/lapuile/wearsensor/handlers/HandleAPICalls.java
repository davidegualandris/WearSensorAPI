package com.example.lapuile.wearsensor.handlers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.example.lapuile.wearsensor.library.models.KaaApplication;
import com.example.lapuile.wearsensor.library.models.KaaEndpoint;
import com.example.lapuile.wearsensor.library.formatters.KaaEndpointsValuesFormatter;
import com.example.lapuile.wearsensor.repositories.KaaApplicationRepository;
import com.example.lapuile.wearsensor.repositories.KaaEndpointRepository;

/**
 * Classe che consente di rispondere direttamente alle call alla API definita in questo progetto
 * /time-series/config per ottenere i "dataNames"
 * /time-series/last per ottenere tutti i dati delle ultime 24 ore in formato JSON
 * /time-series/data/{params} per ottenere i dati con parametri personalizzati
 */
@Path("/time-series")
public class HandleAPICalls {
	
	/**
	 * Funzione di supporto per "recuperare" i nomi dei valori che la applicazione Kaa sta ricevendo
	 * @return JSON mappabile con il model KaaApplication
	 */
	@GET
	@Path("config")
	@Produces(MediaType.APPLICATION_JSON)
	public String getApplicationDataNames(){
		KaaApplication kaaApplication;		
		try {
			kaaApplication = KaaApplicationRepository.getInstance().getKaaApplicationDataName();
		} catch (Exception e) {
			return "{\"message\": " + e.getMessage() + "}";
		}
		
		return kaaApplication.toJSON();
	}
	
	/**
	 * Funzione che ritorna i dati ricavati da Kaa in base ai parametri di seguito specificati
	 * @param timeSeriesName "nomi dei valori" che si vogliono recuperare (ex. "temperature,humidity")
	 * @param endpointId zero o piu' EndpointID separati da virgola (ex. "endpointid1,endpointid2")
	 * @param fromDate "Date" dal quale recuperare i dati
	 * @param toDate "Date" fino al quale recuperare i dati
	 * @param includeTime definisce se fromDate e toDate sono "inclusive" [from, to, both, none]
	 * @param sort ordinamento dei dati [ASC, DESC]
	 * @param format JSON, CSV o XML
	 * @return L'output nel formato desiderato
	 */
	private String getFormattedDataFromKaa(String timeSeriesName, String endpointId, Date fromDate,
			Date toDate, String includeTime, String sort, String format) {		
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		
		//return sdf.format(fromDate) + " " + sdf.format(toDate);
		
		List<KaaEndpoint> kaaEndpoints;
		try {
			kaaEndpoints = KaaEndpointRepository.getInstance().getKaaEndpointsData(timeSeriesName, endpointId,
					sdf.format(fromDate), sdf.format(toDate), includeTime, sort);
		} catch (Exception e) {
			return "{\"message\": \"" + e.getMessage() + "\"}";
		}
		
		if(format != null) {
			if(format.equals("csv")) {
				return KaaEndpointsValuesFormatter.KaaEndpointsValuesToCSV(kaaEndpoints);
			}else if(format.equals("xml")){
				return KaaEndpointsValuesFormatter.KaaEndpointsValuesToXML(kaaEndpoints);
			}
		}
		
		return KaaEndpointsValuesFormatter.KaaEndpointsValuesToJson(kaaEndpoints);

	}
	
	/**
	 * Funzione per ottenere tutti i nomi dei dati ricevuti da Kaa
	 * @return ex "humidity,temperature"
	 */
	private String getDefaultApplicationDataNames() {
		String timeSeriesName = "";
		KaaApplication kaaApplication;		
		try {
			kaaApplication = KaaApplicationRepository.getInstance().getKaaApplicationDataName();
		} catch (Exception e) {
			return "{\"message\": " + e.getMessage() + "}";
		}
		
		for (String dataName : kaaApplication.getDataNames()) {
			timeSeriesName += dataName + ",";
		}
		// remove the last ","
		if(!timeSeriesName.isEmpty())
			timeSeriesName = timeSeriesName.substring(0, timeSeriesName.length() - 1);
		
		return timeSeriesName;
	}
	
	/***
	 * Funzione per gestire la chiamata all'indirizzo /WearSensor/api/kaa/values
	 * @return Tutti i dati di tutti gli endpoint delle ultime 24 ore in formato JSON
	 */
	@GET
	@Path("last")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllEndpointsData() {
		
		String timeSeriesName = this.getDefaultApplicationDataNames();
				
		// by default i am returning the last 24 hours data
		Date toDate = new Date(System.currentTimeMillis());
		Date fromDate = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000));
				
		return this.getFormattedDataFromKaa(timeSeriesName, "", fromDate, toDate, "both", "ASC", "JSON");
	}
	
	/**
	 * Funzione per gestire la chiamata all'indirizzo /WearSensor/api/kaa/data?{params}
	 * @param endpointIDs EndpointID del qualesi vuole recuperare i dati
	 * @param timeSeriesName "nomi dei valori" che si vogliono recuperare (ex. "temperature,humidity")
	 * @param fromDate "Date" dal quale recuperare i dati
	 * @param toDate "Date" fino al quale recuperare i dati
	 * @param includeTime definisce se fromDate e toDate sono "inclusive" [from, to, both, none] (opzionale, none di default)
	 * @param sort ordinamento dei dati [ASC, DESC] (opzionale, ASC di default)
	 * @param format JSON, CSV o XML (opzionale, JSON di default)
	 * @return L'output nel formato desiderato filtrato dai parametri specificati
	 */
	@GET
	@Path("data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getEndpointData(@QueryParam("endpointId") String endpointId,
			@QueryParam("timeSeriesName") String timeSeriesName, @QueryParam("fromDate") Long fromDate,
			@QueryParam("toDate") Long toDate, @QueryParam("includeTime") String includeTime,
			@QueryParam("sort") String sort, @QueryParam("format") String format) {
		
		//by default i am returning all the possible values		
		if(timeSeriesName == null || timeSeriesName.isEmpty())
			timeSeriesName = this.getDefaultApplicationDataNames();
		
		// by default i am returning the last 24 hours data
		Date fDate;
		try {
			fDate = new Date(fromDate*1000);
		}catch(Exception e){
			fDate = new Date(System.currentTimeMillis());
		}
		
		Date tDate;
		try {
			tDate = new Date(toDate*1000);
		}catch(Exception e){
			tDate = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000));
		}		
		
		return this.getFormattedDataFromKaa(timeSeriesName, endpointId, fDate,
				tDate, includeTime, sort, format);
	}
	
}
