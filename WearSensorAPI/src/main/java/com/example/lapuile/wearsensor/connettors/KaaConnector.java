package com.example.lapuile.wearsensor.connettors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.example.lapuile.wearsensor.utils.Constants;

/**
 * Class to connect with the platform
 */
public class KaaConnector {
	
	/**
	 * Class that try to get connected with the Kaa platform through the Kaa API
	 * @param url url to be added to the KAA_EPTS_API_BASE_URL
	 * @return the json given by kaa if it could connect. Error otherwise.
	 * @throws Exception 
	 */
	public static String connect(String url) throws Exception {
		// Create URL
        URL kaaApiUrl = null;
        try {
            kaaApiUrl = new URL(Constants.KAA_EPTS_API_BASE_URL + url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new Exception("Malformed URL");
        }
    	
        HttpsURLConnection kaaConnection = null;

        // Create connection
        try {
            kaaConnection =
                    (HttpsURLConnection) kaaApiUrl.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Couldn't estabilish a connection with Kaa");
        }

        kaaConnection.setRequestProperty("Authorization", Constants.KAA_EPTS_API_BEARER_TOKEN);
        
        if (kaaConnection.getResponseCode() == 200){
        	InputStream responseBody = kaaConnection.getInputStream();

            InputStreamReader responseBodyReader =
                    new InputStreamReader(responseBody, "UTF-8");

            BufferedReader r = new BufferedReader(responseBodyReader);

            StringBuilder response = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                response.append(line).append('\n');
            }

            // Close the connection
            kaaConnection.disconnect();
            
            return response.toString();
        }else {
        	throw new Exception("Couldn't connect with the platform");
        }
	}
	
	/**
	 * Function to connect the API to the Kaa MQTT Server
	 * @return Array of 2 objects: in the first place an instance of MqttClient, in the second one an instance of MqttConnectOptions
	 * @throws MqttException
	 */
	public static Object[] connect() throws MqttException {
        MqttClient mqttClient = new MqttClient(Constants.KAA_MQTT_SERVER, "WearSensorAPI", new MemoryPersistence());
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setConnectionTimeout(10);
        System.out.println("Connecting to server: "+Constants.KAA_MQTT_SERVER);
        mqttClient.connect(connOpts);
        System.out.println("Connected");
        Object[] ret = {mqttClient, connOpts};
		return ret;
	}
	
	/**
	 * Function to reconnect to the Kaa MQTT Server to be sure that the connection hasn't been lost
	 * @param connOpts instance of MqttConnectOptions
	 * @param mqttClient instance of MqttClient
	 * @return true if the API can reconnect to the Kaa MQTT Server, error otherwise
	 * @throws Exception
	 */
	public static boolean connect(MqttConnectOptions connOpts, MqttClient mqttClient) throws Exception {
		// Try to get connected 3 times
		int c = 0;
		do {
			if (!mqttClient.isConnected()) {
                mqttClient.connect(connOpts);
                System.out.println("Connected");
                c++;
            }else {
            	return true;
            }
		}while(c < 3);
		throw new Exception("Client not connected");
	}
}
