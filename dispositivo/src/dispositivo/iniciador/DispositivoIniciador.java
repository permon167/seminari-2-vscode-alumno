package dispositivo.iniciador;

import java.io.IOException;

import dispositivo.componentes.Dispositivo;
import dispositivo.componentes.Funcion;
import dispositivo.interfaces.FuncionStatus;
import dispositivo.interfaces.IDispositivo;
import dispositivo.interfaces.IFuncion;

public class DispositivoIniciador {

	public static void main(String[] args) {
		
		if ( args.length < 4 ) {
			System.out.println("Usage: java -jar dispositivo.jar device deviceIP rest-port mqttBroker");
			System.out.println("Example: java -jar dispositivo.jar ttmi050 ttmi050.iot.upv.es 8182 tcp://ttmi008.iot.upv.es:1883");
			return;
		}

		String deviceId = args[0];
		String deviceIP = args[1];
		String port = args[2];
		String mqttBroker = args[3];
		
		IDispositivo d = Dispositivo.build(deviceId, deviceIP, Integer.valueOf(port), mqttBroker);
		
		// Añadimos funciones al dispositivo
		IFuncion f1 = Funcion.build("f1", FuncionStatus.OFF);
		d.addFuncion(f1);
		
		IFuncion f2 = Funcion.build("f2", FuncionStatus.OFF);
		d.addFuncion(f2);

		//1. Añadir la funcion f3 para utilizar en el dispositivo, por defecto debe arrancarse en parapadeo 
		Funcion f3 = Funcion.build("f3", FuncionStatus.BLINK);
		d.addFuncion(f3);
		
		// Arrancamos el dispositivo
		d.iniciar();

		System.out.println("Dispositivo " + d.getId() + " iniciado.");

		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Esperar a que el usuario presione Enter

		// Detener el dispositivo
		d.detener();
		System.out.println("Dispositivo detenido correctamente.");


	}

}
