package com.example.lapuile.wearsensor.senders;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.example.lapuile.wearsensor.connettors.KaaConnector;
import com.example.lapuile.wearsensor.library.models.KaaEndpoint;
import com.example.lapuile.wearsensor.utils.Constants;

/**
 * Class used to send to Kaa the values sent to the API
 */
public class KaaEndpointSenders {
	private static KaaEndpointSenders instance;
	private MqttClient mqttClient;
	private MqttConnectOptions connOpts;
	
	/**
	 * Constructor to create the instance of the KaaEndpointSenders. The connection with Kaa is defined here.
	 * @throws Exception 
	 */
    private KaaEndpointSenders() throws Exception{    	
    	try {
    		// connect for the first time
    		Object[] ret = KaaConnector.connect();
    		this.mqttClient = (MqttClient) ret[0];
			this.connOpts = (MqttConnectOptions) ret[1];
    	}catch(Exception e) {    		
    		try {
    			// is it a MqttException?
    			MqttException me = (MqttException)e;
    			System.out.println("reason "+me.getReasonCode());
                System.out.println("msg "+me.getMessage());
                System.out.println("loc "+me.getLocalizedMessage());
                System.out.println("cause "+me.getCause());
                System.out.println("excep "+me);
                me.printStackTrace();
    		}catch(Exception e1) {
    			// generic error
    			e1.printStackTrace();
    			throw new Exception(e1);
    		}
    	}
    }

    /**
     * Function to get the single instance of the class
     * @return the instance of the class
     * @throws Exception 
     */
    public static synchronized KaaEndpointSenders getInstance() throws Exception{
        if (instance==null)
            instance = new KaaEndpointSenders();
        return instance;
    }
    
    /**
     * Actual function to send the values received from the API to Kaa
     * @param data the KaaEndpoint instance to be sent to Kaa
     * @return HTTP response representing the result of the operation
     */
    public Response sendValueToKaa(KaaEndpoint data){
    	try{
    		
    		// try to reconnect
    		KaaConnector.connect(connOpts, mqttClient);
    		
    		String json = data.toKaaJson();
    		byte[] payload = json.getBytes();
            MqttMessage msg = new MqttMessage(payload);
            
            // Based on the endpointId, send to the mobile or to the watch
            String endpointId = data.getEndpointId();	                
    		if(endpointId.equals(Constants.KAA_MOBILE_ENDPOINT_ID)) {	                	
            	mqttClient.publish(Constants.KAA_MOBILE_TOPIC, msg);    
            }else if(endpointId.equals(Constants.KAA_WATCH_ENDPOINT_ID)){
            	mqttClient.publish(Constants.KAA_WATCH_TOPIC, msg);    
            }else {
            	return Response.status(Status.INTERNAL_SERVER_ERROR).build();
            }    
    		
    		System.out.println("Data sent " + json);
    		return Response.status(Status.OK).build();
    		
    	}catch(Exception ex) {
    		ex.printStackTrace();
    		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    	}    	
    }
}
