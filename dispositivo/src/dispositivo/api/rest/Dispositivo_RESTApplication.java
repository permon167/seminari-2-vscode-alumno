package dispositivo.api.rest;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import dispositivo.interfaces.IDispositivo;
import dispositivo.utils.MySimpleLogger;

public class Dispositivo_RESTApplication extends Application {

    private IDispositivo dispositivo;
    private String loggerId;

    public Dispositivo_RESTApplication(IDispositivo dispositivo) {
        this.dispositivo = dispositivo;
        this.loggerId = dispositivo.getId() + "-apiREST";

    }

    @Override
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());

        // Define las rutas
        router.attach(Funcion_Recurso.RUTA, Funcion_Recurso.class);
        MySimpleLogger.trace(this.loggerId, "Registrada ruta " + Funcion_Recurso.RUTA + " en api REST");
        router.attach(Dispositivo_Recurso.RUTA, Dispositivo_Recurso.class);
        MySimpleLogger.trace(this.loggerId, "Registrada ruta " + Dispositivo_Recurso.RUTA + " en api REST");

        return router;
    }
    public IDispositivo getDispositivo() {
        return this.dispositivo;
    }

}
