package dispositivo.api.mqtt;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import dispositivo.componentes.Dispositivo;
import dispositivo.interfaces.Configuracion;
import dispositivo.interfaces.IDispositivo;
import dispositivo.interfaces.IFuncion;
import dispositivo.utils.MySimpleLogger;

public class Dispositivo_APIMQTT implements MqttCallback {

	protected MqttClient myClient;
	protected MqttConnectOptions connOpt;
	protected String clientId = null;
	
	protected IDispositivo dispositivo;
	protected String mqttBroker = null;
	
	private String loggerId = null;
	private MqttClient publisherClient;
	
	public static Dispositivo_APIMQTT build(IDispositivo dispositivo, String brokerURL) {
		Dispositivo_APIMQTT api = new Dispositivo_APIMQTT(dispositivo);
		api.setBroker(brokerURL);
		return api;
	}
	
	protected Dispositivo_APIMQTT(IDispositivo dev) {
		this.dispositivo = dev;
		this.loggerId = dev.getId() + "-apiMQTT";
	}
	
	protected void setBroker(String mqttBrokerURL) {
		this.mqttBroker = mqttBrokerURL;
	}
	
	
	@Override
	public void connectionLost(Throwable t) {
		MySimpleLogger.debug(this.loggerId, "Connection lost!");
		// code to reconnect to the broker would go here if desired
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		//System.out.println("Pub complete" + new String(token.getMessage().getPayload()));
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
	    String payload = new String(message.getPayload());

	    System.out.println("-------------------------------------------------");
	    System.out.println("| Topic:" + topic);
	    System.out.println("| Message: " + payload);
	    System.out.println("-------------------------------------------------");

	    //7. Verificar si el mensaje es para habilitar/deshabilitar el dispositivo
	    if (topic.equals("dispositivo/" + dispositivo.getId() + "/comandos")) {
	        if (dispositivo instanceof Dispositivo) {
	            Dispositivo dispositivoConcreto = (Dispositivo) dispositivo;
	            if (payload.equalsIgnoreCase("habilitar")) {
	                dispositivoConcreto.habilitar();
	                MySimpleLogger.info(this.loggerId, "Dispositivo habilitado.");
	            } else if (payload.equalsIgnoreCase("deshabilitar")) {
	                dispositivoConcreto.deshabilitar();
	                MySimpleLogger.info(this.loggerId, "Dispositivo deshabilitado.");
	            } else {
	                MySimpleLogger.warn(this.loggerId, "Acción no reconocida en el topic de comandos del dispositivo.");
	            }
	        } else {
	            MySimpleLogger.warn(this.loggerId, "El dispositivo no es una instancia de la clase concreta Dispositivo.");
	        }
	        return;
	    }

	    //7. Procesar mensajes relacionados con funciones
	    if (!((Dispositivo) dispositivo).isHabilitado()) {
	        MySimpleLogger.warn(this.loggerId, "El dispositivo está deshabilitado. No se pueden modificar las funciones.");
	        return;
	    }

	    String[] topicNiveles = topic.split("/");
	    String funcionId = topicNiveles[topicNiveles.length - 2];


		//8. Procesar mensajes JSON para controlar las funciones
	    IFuncion f = this.dispositivo.getFuncion(funcionId);
	    if (f == null) {
	        MySimpleLogger.warn(this.loggerId, "No encontrada funcion " + funcionId);
	        return;
	    }

	    try {
	        // Parsear el payload como JSON
	        MySimpleLogger.debug(this.loggerId, "Intentando parsear el payload: " + payload);
	        JSONObject jsonPayload = new JSONObject(payload);
	        String action = jsonPayload.optString("accion", "").toLowerCase();

	        MySimpleLogger.debug(this.loggerId, "Acción recibida: " + action);

	        // Ejecutar la acción indicada
	        switch (action) {
	            case "encender":
	                f.encender();
	                break;
	            case "apagar":
	                f.apagar();
	                break;
	            case "parpadear":
	                f.parpadear();
	                break;
	            default:
	                MySimpleLogger.warn(this.loggerId, "Acción '" + action + "' no reconocida. Sólo admitidas: encender, apagar o parpadear");
	                return;
	        }

	        //9. Publicar el estado actual de la función
	        MySimpleLogger.debug(this.loggerId, "Publicando el estado de la función...");
	        publishFunctionState(f);

	    } catch (Exception e) {
	        MySimpleLogger.error(this.loggerId, "Error al procesar el mensaje JSON: " + e.getMessage());
	    }

		
	}

	
	//9. Publica el estado actual de una función en el topic correspondiente.
	private void publishFunctionState(IFuncion funcion) {
	    try {
	        if (publisherClient == null || !publisherClient.isConnected()) {
	            MySimpleLogger.error(this.loggerId, "Cliente MQTT (publisher) no está conectado. No se puede publicar el estado.");
	            return;
	        }

	        String topic = calculateInfoTopic(funcion);
	        JSONObject estado = new JSONObject();
	        estado.put("estado", funcion.getStatus()); // Suponiendo que la función tiene un método getEstado()
	        MqttMessage message = new MqttMessage(estado.toString().getBytes());
	        message.setQos(0);
	        publisherClient.publish(topic, message);
	        MySimpleLogger.info(this.loggerId, "Estado publicado en el topic " + topic + ": " + estado.toString());
	    } catch (Exception e) {
	        MySimpleLogger.error(this.loggerId, "Error al publicar el estado de la función: " + e.getMessage());
	    }
	}

	/**
	 * 
	 * runClient
	 * The main functionality of this simple example.
	 * Create a MQTT client, connect to broker, pub/sub, disconnect.
	 * 
	 */
	public void connect() {
	    try {
	        if (myClient == null || !myClient.isConnected()) {
	            String clientID = this.dispositivo.getId() + UUID.randomUUID().toString() + ".subscriber";
	            connOpt = new MqttConnectOptions();
	            connOpt.setCleanSession(true);
	            connOpt.setKeepAliveInterval(30);

	            myClient = new MqttClient(this.mqttBroker, clientID);
	            myClient.setCallback(this);
	            myClient.connect(connOpt);

	            //9. Crear un cliente adicional para publicar
	            String publisherClientID = this.dispositivo.getId() + UUID.randomUUID().toString() + ".publisher";
	            publisherClient = new MqttClient(this.mqttBroker, publisherClientID);
	            publisherClient.connect(connOpt);

	            MySimpleLogger.info(this.loggerId, "Conectado al broker " + this.mqttBroker);
	        }
	    } catch (MqttException e) {
	        MySimpleLogger.error(this.loggerId, "Error al conectar con el broker MQTT: " + e.getMessage());
	    }
	}
	
	
	public void disconnect() {
		
		// disconnect
		try {
			// wait to ensure subscribed messages are delivered
			Thread.sleep(10000);

			myClient.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

	
	protected void subscribe(String myTopic) {
		
		// subscribe to topic
		try {
			int subQoS = 0;
			myClient.subscribe(myTopic, subQoS);
			MySimpleLogger.info(this.loggerId, "Suscrito al topic " + myTopic);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	protected void unsubscribe(String myTopic) {
		
		// unsubscribe to topic
		try {
			myClient.unsubscribe(myTopic);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//10. Replicar el estado de la función 'f1' de otro dispositivo
	public void iniciar() {
	    if (this.myClient == null || !this.myClient.isConnected())
	        this.connect();

	    if (this.dispositivo == null)
	        return;

	    // Suscribirse al topic de comandos del dispositivo
	    this.subscribe("dispositivo/" + dispositivo.getId() + "/comandos");

	    for (IFuncion f : this.dispositivo.getFunciones())
	        this.subscribe(this.calculateCommandTopic(f));

	    //10. Publicar información del dispositivo en el topic gestion/dispositivos
	    try {
	        JSONObject dispositivoInfo = new JSONObject();
	        dispositivoInfo.put("id", dispositivo.getId());
	        dispositivoInfo.put("estado", ((Dispositivo) dispositivo).isHabilitado() ? "habilitado" : "deshabilitado");

	        MqttMessage message = new MqttMessage(dispositivoInfo.toString().getBytes());
	        message.setQos(0);
	        publisherClient.publish("gestion/dispositivos", message);

	        MySimpleLogger.info(this.loggerId, "Dispositivo registrado en gestion/dispositivos: " + dispositivoInfo.toString());
	    } catch (Exception e) {
	        MySimpleLogger.error(this.loggerId, "Error al registrar el dispositivo en gestion/dispositivos: " + e.getMessage());
	    }

	    //10. Suscribirse al topic de comandos de la función f1 del dispositivo ttmi050
	    this.subscribe("dispositivo/ttmi050/funcion/f1/comandos");
	}
	
	
	
	public void detener() {
		try {
			// Desuscribirse de todos los topics
			if (myClient != null && myClient.isConnected()) {
				MySimpleLogger.info(this.loggerId, "Desuscribiéndose de todos los topics...");
				for (IFuncion f : this.dispositivo.getFunciones()) {
					String commandTopic = calculateCommandTopic(f);
					this.unsubscribe(commandTopic);
				}
				this.unsubscribe("dispositivo/" + dispositivo.getId() + "/comandos");
				this.unsubscribe("dispositivo/ttmi050/funcion/f1/comandos"); // Si estás suscrito a este topic
			}
	
			// Desconectar los clientes MQTT
			MySimpleLogger.info(this.loggerId, "Desconectando los clientes MQTT...");
			if (myClient != null && myClient.isConnected()) {
				myClient.disconnect();
			}
			if (publisherClient != null && publisherClient.isConnected()) {
				publisherClient.disconnect();
			}
	
			MySimpleLogger.info(this.loggerId, "Dispositivo detenido correctamente.");
		} catch (Exception e) {
			MySimpleLogger.error(this.loggerId, "Error al detener el dispositivo: " + e.getMessage());
		}
		
	}
	
	
	protected String calculateCommandTopic(IFuncion f) {
		return Configuracion.TOPIC_BASE + "dispositivo/" + dispositivo.getId() + "/funcion/" + f.getId() + "/comandos";
	}
	
	protected String calculateInfoTopic(IFuncion f) {
		return Configuracion.TOPIC_BASE + "dispositivo/" + dispositivo.getId() + "/funcion/" + f.getId() + "/info";
	}
	

}
