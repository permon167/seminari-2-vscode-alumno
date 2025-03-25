package dispositivo.iniciador;

import dispositivo.componentes.Dispositivo;
import dispositivo.componentes.Funcion;
import dispositivo.interfaces.FuncionStatus;
import dispositivo.interfaces.IDispositivo;
import dispositivo.interfaces.IFuncion;

public class DispositivoIniciador2 {

    public static void main(String[] args) {

        if (args.length < 4) {
            System.out.println("Usage: java -jar dispositivo.jar device deviceIP rest-port mqttBroker");
            System.out.println("Example: java -jar dispositivo.jar ttmi051 ttmi051.iot.upv.es 8183 tcp://ttmi008.iot.upv.es:1883");
            return;
        }

        String deviceId = args[0];
        String deviceIP = args[1];
        String port = args[2];
        String mqttBroker = args[3];

        IDispositivo d = Dispositivo.build(deviceId, deviceIP, Integer.valueOf(port), mqttBroker);

        // Añadimos funciones al dispositivo
        IFuncion f1 = Funcion.build("f1", FuncionStatus.ON);
        d.addFuncion(f1);

        IFuncion f2 = Funcion.build("f2", FuncionStatus.ON);
        d.addFuncion(f2);

        Funcion f3 = Funcion.build("f3", FuncionStatus.BLINK);
        d.addFuncion(f3);

        // Arrancamos el dispositivo
        d.iniciar();

        System.out.println("Dispositivo " + d.getId() + " iniciado.");
    }
}
